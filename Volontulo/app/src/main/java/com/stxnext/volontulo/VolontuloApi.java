package com.stxnext.volontulo;

import com.stxnext.volontulo.api.CreateResponse;
import com.stxnext.volontulo.api.JoinResponse;
import com.stxnext.volontulo.api.LoginResponse;
import com.stxnext.volontulo.api.Offer;
import com.stxnext.volontulo.api.UserProfile;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface VolontuloApi {

    @GET("api/users_profiles/{id}.json")
    Call<UserProfile> getVolunteer(@Path("id") int id);

    @GET("api/users_profiles.json")
    Call<List<UserProfile>> listVolunteers();

    @GET("/api/offers/{id}.json")
    Call<Offer> getOffer(@Path("id") int id);

    @GET("/api/offers.json")
    Call<List<Offer>> listOffers();

    @GET("/api/users/a{id}/attend?format=json")
    Call<List<Offer>> listUserAttends(@Path("id") int id);

    @FormUrlEncoded
    @POST("/rest-auth/login/")
    Call<LoginResponse> login(@Field("username") String username, @Field("password") String password);


    @POST("/rest-auth/logout/")
    Call<Void> logout(@Header("Authorization") String authorization);

    @FormUrlEncoded
    @POST("/api/offers/{id}/join")
    Call<JoinResponse> joinOffer(@Path("id") int id, @Header("Authorization") String authorization, @Field("email") String email, @Field("phone_no") String phoneNo, @Field("fullname") String fullname);

    @FormUrlEncoded
    @POST("/api/offers/create/")
    Call<CreateResponse> createOffer(@Header("Authorization") String authorization, @FieldMap(encoded = true) Map<String, String> params);

    @FormUrlEncoded
    @PUT("/api/offers/{id}/update/")
    Call<CreateResponse> updateOffer(@Header("Authorization") String authorization, @Path("id") int id, @FieldMap(encoded = true) Map<String, String> params);
}
