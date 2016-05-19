package com.stxnext.volontulo.ui.volunteers;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stxnext.volontulo.R;
import com.stxnext.volontulo.VolontuloApp;
import com.stxnext.volontulo.VolontuloBaseFragment;
import com.stxnext.volontulo.api.Offer;
import com.stxnext.volontulo.api.User;
import com.stxnext.volontulo.api.UserProfile;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class VolunteerDetailsFragment extends VolontuloBaseFragment {

    private UserProfile userProfile;

    @BindView(R.id.text_name)
    TextView name;

    @BindView(R.id.text_email)
    TextView email;

    @BindView(R.id.text_description)
    TextView description;

    @BindView(R.id.text_phone)
    TextView phone;

    @BindView(R.id.image)
    ImageView image;

    @BindView(R.id.offers)
    protected RecyclerView offers;
    private Realm realm;
    private AttendsAdapter adapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_volunteer_details;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        realm = Realm.getDefaultInstance();
    }

    protected void onPostCreateView(View root) {
        final Bundle arguments = getArguments();
        userProfile = Parcels.unwrap(arguments.getParcelable(UserProfile.USER_PROFILE_OBJECT));
        Context context = getContext();
        offers.setLayoutManager(new LinearLayoutManager(context));
        offers.setHasFixedSize(true);

        adapter = new AttendsAdapter(getContext());
        adapter.setUserProfile(userProfile);
        offers.setAdapter(adapter);
        retrieveData();
    }

    private void retrieveData() {
        final int userId = userProfile.getUser().getId();
        final String fieldVolunteersId = Offer.FIELD_VOLUNTEERS + "." + User.FIELD_ID;
        final RealmQuery<Offer> queryFindAttends = realm.where(Offer.class)
                .equalTo(fieldVolunteersId, userId);
        final RealmResults<Offer> offerResults = queryFindAttends.findAll();
        Timber.d("[REALM] Attends count: %d", offerResults.size());
        adapter.swap(offerResults);
        Timber.d("[REALM] Attends UI PUT");
        final Call<List<Offer>> call = VolontuloApp.api.listUserAttends(userId);
        call.enqueue(new Callback<List<Offer>>() {
            @Override
            public void onResponse(Call<List<Offer>> call, Response<List<Offer>> response) {
                if (response.isSuccessful()) {
                    final List<Offer> offerList = response.body();
                    Timber.d("[RETRO] Attends count: %d", offerList.size());
                    realm.beginTransaction();
                    for (Offer offer : offerList) {
                        final Offer stored = realm.where(Offer.class).equalTo(Offer.FIELD_ID, offer.getId()).findFirst();
                        if (stored != null) {
                            offer.setLocationLatitude(stored.getLocationLatitude());
                            offer.setLocationLongitude(stored.getLocationLongitude());
                        }
                        realm.copyToRealmOrUpdate(offer);
                    }
                    Timber.d("[REALM] Attends COPY/UPDATE");
                    realm.commitTransaction();
                    final RealmResults<Offer> updatedList = queryFindAttends.findAll();
                    adapter.swap(updatedList);
                    Timber.d("[RETRO] Attends UI SWAP");
                }
            }

            @Override
            public void onFailure(Call<List<Offer>> call, Throwable t) {
                Timber.d("[FAILURE] message - %s", t.getMessage());
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        realm.close();
    }
}
