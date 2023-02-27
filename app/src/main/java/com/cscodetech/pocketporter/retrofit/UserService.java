package com.grader.user.retrofit;


import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {



    @POST(APIClient.APPEND_URL + "reg_user.php")
    Call<JsonObject> createUser(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "getzone.php")
    Call<JsonObject> getzone(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "vehiclelist.php")
    Call<JsonObject> vehiclelist(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "countrycode.php")
    Call<JsonObject> getCodelist(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "mobile_check.php")
    Call<JsonObject> mobileCheck(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "user_login.php")
    Call<JsonObject> userLogin(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "forget_password.php")
    Call<JsonObject> getForgot(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "couponlist.php")
    Call<JsonObject> getCouponList(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "check_coupon.php")
    Call<JsonObject> checkCoupon(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "order_now.php")
    Call<JsonObject> orderNow(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "map_info.php")
    Call<JsonObject> tripinfo(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "order_history.php")
    Call<JsonObject> orderHistory(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "cancle.php")
    Call<JsonObject> tripCancle(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "wallet_report.php")
    Call<JsonObject> walletReport(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "getdata.php")
    Call<JsonObject> getdata(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "paymentgateway.php")
    Call<JsonObject> getPaymentList(@Body RequestBody requestBody);

    @POST(APIClient.APPEND_URL + "wallet_up.php")
    Call<JsonObject> walletUp(@Body RequestBody requestBody);




}
