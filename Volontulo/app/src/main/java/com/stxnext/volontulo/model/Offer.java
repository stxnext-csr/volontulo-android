package com.stxnext.volontulo.model;

import android.net.Uri;
import android.support.annotation.DrawableRes;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.parceler.Parcel;

@Parcel
public class Offer {
    private String name;
    private String place;
    private String description;
    private String timeRequirement;
    private String benefits;

    private double placeLongitude;
    private double placeLatitude;
    private String placeName;

    private long startTime;
    private long endTime;
    private boolean isUserJoined;

    private int imageResource;
    private String imagePath;

    private static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormat.forPattern("dd-MM-yyyy, HH:mm");
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormat.forPattern("dd/MM/yy");

    public static Offer mock(String name, String place, DateTime startTime, DateTime endTime, @DrawableRes int imageResource, boolean isJoined) {
        final Offer result = new Offer();
        result.name = name;
        result.place = place;
        result.startTime = startTime.getMillis();
        result.endTime = endTime.getMillis();
        result.imageResource = imageResource;
        result.isUserJoined = isJoined;
        return result;
    }

    public void setPlaceNameAndPosition(Place place) {
        LatLng position = place.getLatLng();
        placeName = String.valueOf(place.getName());
        placeLongitude = position.longitude;
        placeLatitude = position.latitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public double getPlaceLatitude() {
        return placeLatitude;
    }

    public double getPlaceLongitude() {
        return placeLongitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimeRequirement(String timeRequirement) {
        this.timeRequirement = timeRequirement;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public String getFormattedStartTime() {
        if (startTime > 0) {
            return new DateTime(startTime).toString(DATE_FORMAT_LONG);
        }
        return "";
    }

    public String getFormattedEndTime() {
        if (endTime > 0) {
            return new DateTime(endTime).toString(DATE_FORMAT_LONG);
        }
        return "";
    }

    public String getFormattedStartDay() {
        if (startTime > 0) {
            return new DateTime(startTime).toString(DATE_FORMAT_SHORT);
        }
        return "";
    }

    public String getFormattedEndDay() {
        if (endTime > 0) {
            return new DateTime(endTime).toString(DATE_FORMAT_SHORT);
        }
        return "";
    }

    @DrawableRes
    public int getImageResource() {
        return imageResource;
    }

    public boolean isUserJoined() {
        return isUserJoined;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeRequirement() {
        return timeRequirement;
    }

    public String getBenefits() {
        return benefits;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(Uri path) {
        this.imagePath = path.toString();
    }
}
