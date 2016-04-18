package com.stxnext.volontulo.ui.offers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.stxnext.volontulo.R;
import com.stxnext.volontulo.VolontuloBaseFragment;
import com.stxnext.volontulo.model.Ofer;
import com.stxnext.volontulo.ui.utils.BaseTextWatcher;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.Realm;

public class AddOfferFragment extends VolontuloBaseFragment {
    private static final int REQUEST_IMAGE = 0x1011;
    private static final String KEY_OFFER_FORM = "offer-form";
    public static final LatLngBounds POLAND_BOUNDING_BOX = new LatLngBounds(
            new LatLng(48.9089926, 24.2709887),
            new LatLng(54.7344539, 14.0381971)
    );

    @Bind(R.id.offer_name_layout) TextInputLayout offerNameLayout;
    @Bind(R.id.offer_name) EditText offerName;
    @Bind(R.id.offer_description_layout) TextInputLayout offerDescriptionLayout;
    @Bind(R.id.offer_description) EditText offerDescription;
    @Bind(R.id.offer_time_requirement_layout) TextInputLayout offerTimeRequirementLayout;
    @Bind(R.id.offer_time_requirement) EditText offerTimeRequirement;
    @Bind(R.id.offer_benefits_layout) TextInputLayout offerBenefitsLayout;
    @Bind(R.id.offer_benefits) EditText offerBenefits;
    @Bind(R.id.offer_thumbnail_card) View offerThumbnailCard;
    @Bind(R.id.offer_thumbnail) ImageView offerThumbnail;
    @Bind(R.id.offer_thumbnail_name) TextView offerThumbnailName;
    @Bind(R.id.scroller) ScrollView scrollView;
    @Bind(R.id.place_autocomplete_result_switcher) ViewSwitcher placeAutocompleteResultSwitcher;
    private SupportPlaceAutocompleteFragment placeFragment;

    private Ofer formState = new Ofer();

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_offer_add;
    }

    @Override
    protected void onPostCreateView(View root) {
        setHasOptionsMenu(true);
        offerName.addTextChangedListener(new OfferObjectUpdater(offerName.getId(), formState));
        offerDescription.addTextChangedListener(new OfferObjectUpdater(offerDescription.getId(), formState));
        offerBenefits.addTextChangedListener(new OfferObjectUpdater(offerBenefits.getId(), formState));
        offerTimeRequirement.addTextChangedListener(new OfferObjectUpdater(offerTimeRequirement.getId(), formState));
        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        placeFragment = (SupportPlaceAutocompleteFragment) getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        placeFragment.setHint(getString(R.string.offer_place));
        placeFragment.setBoundsBias(POLAND_BOUNDING_BOX);
        placeFragment.setFilter(new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build());
        placeFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                formState.setPlaceNameAndPosition(place);
                mapFragment.getMapAsync(new ThumbnailMap(place.getLatLng(), place.getName()));
                placeAutocompleteResultSwitcher.showNext();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getActivity(), String.format("Error@place selector: %s", status), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static class OfferObjectUpdater extends BaseTextWatcher {
        @IdRes
        private final int editTextId;
        private final Ofer updated;

        OfferObjectUpdater(@IdRes int id, Ofer model) {
            editTextId = id;
            updated = model;
        }

        @Override
        public void afterTextChanged(Editable s) {
            final String result = s.toString();
            switch (editTextId) {
                case R.id.offer_name:
                    updated.setName(result);
                    break;

                case R.id.offer_description:
                    updated.setDescription(result);
                    break;

                case R.id.offer_benefits:
                    updated.setBenefits(result);
                    break;

                case R.id.offer_time_requirement:
                    updated.setTimeRequirement(result);
                    break;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.attach_image_menu, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            final Uri selectedImage = data.getData();
            loadImageFromUri(selectedImage);
            formState.setImagePath(selectedImage);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_attach_file:
                startActivityForResult(
                        new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                        REQUEST_IMAGE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_OFFER_FORM, Parcels.wrap(formState));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            formState = Parcels.unwrap(savedInstanceState.getParcelable(KEY_OFFER_FORM));
            fillFormFrom(formState);
        }
    }

    private void fillFormFrom(final Ofer formState) {
        offerName.setText(formState.getName());
        placeFragment.setText(formState.getPlaceName());
        placeFragment.setText(formState.getPlaceName());
        offerDescription.setText(formState.getDescription());
        offerTimeRequirement.setText(formState.getTimeRequirement());
        offerBenefits.setText(formState.getBenefits());
        if (!TextUtils.isEmpty(formState.getImagePath())) {
            loadImageFromUri(Uri.parse(formState.getImagePath()));
        } else {
            unloadImage();
        }
        if (!TextUtils.isEmpty(formState.getPlaceName())) {
            final LatLng position = new LatLng(formState.getPlaceLatitude(), formState.getPlaceLongitude());
            final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new ThumbnailMap(position, formState.getPlaceName()));
            placeAutocompleteResultSwitcher.showNext();
        }
    }

    @OnClick(R.id.close_map)
    void onMapClose() {
        placeAutocompleteResultSwitcher.showNext();
    }

    @OnClick(R.id.offer_thumbnail_delete)
    void onThumbnailDelete() {
        unloadImage();
    }

    private void unloadImage() {
        offerThumbnail.setImageDrawable(null);
        offerThumbnailName.setText("");
        offerThumbnailCard.setVisibility(View.GONE);
    }

    @OnClick(R.id.add_offer)
    void onOfferAdd() {
        if (!validateFields()) {
            return;
        }
        saveOffer(formState);
        getActivity().finish();
    }

    private void saveOffer(Ofer formState) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(formState);
        realm.commitTransaction();
        realm.close();
    }

    private boolean validateFields() {
        boolean result = true;
        if (TextUtils.isEmpty(offerName.getText())) {
            offerNameLayout.setError(getString(R.string.offer_name_validate));
            result = false;
        }
        if (TextUtils.isEmpty(formState.getPlaceName())) {
            Toast.makeText(getActivity(), getString(R.string.offer_place_validate), Toast.LENGTH_LONG).show();
            result = false;
        }
        if (TextUtils.isEmpty(offerDescription.getText())) {
            offerDescriptionLayout.setError(getString(R.string.offer_description_validate));
            result = false;
        }
        if (TextUtils.isEmpty(offerTimeRequirement.getText())) {
            offerTimeRequirementLayout.setError(getString(R.string.offer_time_requirement_validate));
            result = false;
        }
        if (TextUtils.isEmpty(offerBenefits.getText())) {
            offerBenefitsLayout.setError(getString(R.string.offer_benefits_validate));
            result = false;
        }
        return result;
    }

    private void loadImageFromUri(Uri selectedImage) {
        Picasso.with(getActivity())
                .load(selectedImage)
                .fit()
                .into(offerThumbnail);
        offerThumbnailName.setText(extractNameFromUri(selectedImage));
        offerThumbnailCard.setVisibility(View.VISIBLE);
    }

    private CharSequence extractNameFromUri(final Uri selectedImage) {
        if ("file".equals(selectedImage.getScheme())) {
            return selectedImage.getLastPathSegment();
        } else if ("content".equals(selectedImage.getScheme())) {
            final String[] projection = {MediaStore.Images.Media.TITLE};
            final Cursor cursor = getActivity().getContentResolver().query(selectedImage, projection, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return "";
    }
}

class ThumbnailMap implements OnMapReadyCallback {
    private LatLng position;
    private CharSequence positionTitle;

    ThumbnailMap(LatLng marker, CharSequence markerTitle) {
        position = marker;
        positionTitle = markerTitle;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        googleMap.addMarker(new MarkerOptions()
            .position(position)
            .title(String.valueOf(positionTitle)));
    }
}