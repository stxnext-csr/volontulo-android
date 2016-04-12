package com.stxnext.volontulo.ui.volunteers;

import android.content.Context;
import android.view.View;

import com.stxnext.volontulo.R;
import com.stxnext.volontulo.api.UserProfile;
import com.stxnext.volontulo.ui.utils.BaseMockAdapter;

import java.util.List;

public class UserProfileAdapter extends BaseMockAdapter<UserProfile, UserProfileViewHolder> {

    public UserProfileAdapter(Context context, List<UserProfile> results) {
        super(context, R.layout.item_volunteer, results);
    }

    @Override
    protected UserProfileViewHolder createViewHolder(View item) {
        return new UserProfileViewHolder(item);
    }
}