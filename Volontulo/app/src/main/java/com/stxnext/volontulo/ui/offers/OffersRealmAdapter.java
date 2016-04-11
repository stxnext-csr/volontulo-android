package com.stxnext.volontulo.ui.offers;

import android.content.Context;
import android.view.View;

import com.stxnext.volontulo.R;
import com.stxnext.volontulo.model.Offer;
import com.stxnext.volontulo.ui.utils.BaseMockAdapter;

import java.util.List;

public class OffersRealmAdapter extends BaseMockAdapter<Offer, OfferViewRealmHolder> {

    public OffersRealmAdapter(Context context, final List<Offer> offers) {
        super(context, R.layout.item_offer, offers);
    }

    @Override
    protected OfferViewRealmHolder createViewHolder(View item) {
        return new OfferViewRealmHolder(item);
    }
}
