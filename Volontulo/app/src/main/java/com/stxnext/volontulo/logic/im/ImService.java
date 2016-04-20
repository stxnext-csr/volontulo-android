package com.stxnext.volontulo.logic.im;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.stxnext.volontulo.BuildConfig;
import com.stxnext.volontulo.logic.im.config.ImConfigFactory;
import com.stxnext.volontulo.logic.im.config.ImConfiguration;

import java.util.List;

import io.realm.Realm;

public class ImService extends Service implements SinchClientListener {
    private static final String TAG = "Volontulo-Im";
    private ImConfiguration configuration = ImConfigFactory.create();
    private final InstantMessaging serviceInterface = new InstantMessaging();
    private SinchClient client = null;
    private MessageClient messageClient = null;

    private Realm realm;

    private Intent broadcastIntent = new Intent(ACTION_VOLONTULO_IM);
    private LocalBroadcastManager localBroadcastManager;

    public static final String ACTION_VOLONTULO_IM = "com.stxnext.volontulo.ImClient";
    public static final String EXTRA_KEY_HAS_CONNECTED = "x-has-conn";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String currentUserId = retrieveCurrentUser();
        Log.d(TAG, String.format("IM service initializing with user [%s]", currentUserId));
        if (!TextUtils.isEmpty(currentUserId) && !isIMClientStarted()) {
            Log.i(TAG, "IM service not started, so init it.");
            startIMClient(currentUserId);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }

    private String retrieveCurrentUser() {
        final SharedPreferences preferences = getSharedPreferences(configuration.getPreferencesFileName(), MODE_PRIVATE);
        return preferences.getString("user", "");
    }

    private boolean isIMClientStarted() {
        return client != null && client.isStarted();
    }

    public void startIMClient(String userId) {
        client = Sinch.getSinchClientBuilder()
            .context(this)
            .userId(userId)
            .applicationKey(configuration.getApiKey())
            .applicationSecret(configuration.getSecret())
            .environmentHost(configuration.getEnvironmentHost())
            .build();
        client.addSinchClientListener(this);
        client.setSupportMessaging(true);
        client.setSupportActiveConnectionInBackground(true);
        if (BuildConfig.DEBUG) {
            client.checkManifest();
        }
        realm = Realm.getDefaultInstance();
        client.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    @Override
    public void onDestroy() {
        if (realm != null) {
            realm.close();
        }
        if (client != null) {
            client.stopListeningOnActiveConnection();
            client.terminate();
        }
        super.onDestroy();
    }

    @Override
    public void onClientStarted(SinchClient sinchClient) {
        sinchClient.startListeningOnActiveConnection();
        messageClient = sinchClient.getMessageClient();
        messageClient.addMessageClientListener(new MessageClientListener() {
            @Override
            public void onIncomingMessage(MessageClient messageClient, Message message) {
                Log.i(TAG, String.format("Incoming message %s [from %s, to %s]", message.getMessageId(), message.getSenderId(), message.getRecipientIds()));
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(LocalMessage.createFrom(client, message));
                realm.commitTransaction();
            }

            @Override
            public void onMessageSent(MessageClient messageClient, Message message, String s) {
                Log.i(TAG, String.format("Outcoming message %s [to %s, from %s]", message.getMessageId(), message.getRecipientIds(), message.getSenderId()));
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(LocalMessage.createFrom(client, message));
                realm.commitTransaction();
            }

            @Override
            public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
                Log.e(TAG, String.format("Sending message %s failed [%s]", message.getMessageId(), messageFailureInfo.getSinchError()));
            }

            @Override
            public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
                Log.d(TAG, String.format("LocalMessage %s delivered [to %s]", messageDeliveryInfo.getMessageId(), messageDeliveryInfo.getRecipientId()));
            }

            @Override
            public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {
                Log.i(TAG, String.format("Should send push data %s [%s]", message.getMessageId(), list));
            }
        });
        broadcastIntent.putExtra(EXTRA_KEY_HAS_CONNECTED, true);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onClientStopped(SinchClient sinchClient) {
        client = null;
    }

    @Override
    public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
        Log.e(TAG, String.format("Client: %s, Error: %d [%s@%s]", sinchClient, sinchError.getCode(), sinchError.getErrorType(), sinchError.getMessage()));
        client = null;
        broadcastIntent.putExtra(EXTRA_KEY_HAS_CONNECTED, false);
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
        Log.i(TAG, String.format("Registration credentials required %s", clientRegistration));
    }

    @Override
    public void onLogMessage(int i, String s, String s1) {
        Log.i(TAG, String.format("Log message: %d | %s | %s", i, s, s1));
    }

    public void sendMessage(String recipientUser, String messageBody) {
        if (messageClient != null) {
            final WritableMessage message = new WritableMessage(recipientUser, messageBody);
            messageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener messageClientListener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(messageClientListener);
        }
    }

    public void removeMessageClientListener(MessageClientListener messageClientListener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(messageClientListener);
        }
    }

    public class InstantMessaging extends Binder {
        public void sendMessage(String recipientUser, String messageBody) {
            ImService.this.sendMessage(recipientUser, messageBody);
        }

        public void addMessageClientListener(MessageClientListener messageClientListener) {
            ImService.this.addMessageClientListener(messageClientListener);
        }

        public void removeMessageClientListener(MessageClientListener messageClientListener) {
            ImService.this.removeMessageClientListener(messageClientListener);
        }

        public SinchClient getSinchClient() {
            return ImService.this.client;
        }

        public boolean isClientStarted() {
            return ImService.this.isIMClientStarted();
        }
    }
}