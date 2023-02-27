package com.grader.user.activity;

import static com.grader.user.utility.SessionManager.intro;
import static com.grader.user.utility.SessionManager.login;
import static com.grader.user.utility.SessionManager.wallet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.grader.user.R;
import com.grader.user.model.Contry;
import com.grader.user.model.CountryCodeItem;
import com.grader.user.model.Login;
import com.grader.user.model.RestResponse;
import com.grader.user.model.User;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity implements GetResult.MyListener {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.ed_mobile)
    EditText edMobile;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.txt_continue)
    TextView txtContinue;
    @BindView(R.id.txt_titlepassword)
    TextView txtTitlepassword;

    @BindView(R.id.lvl_password)
    LinearLayout lvlPassword;


    List<CountryCodeItem> cCodes = new ArrayList<>();
    String codeSelect;
    SessionManager sessionManager;
    CustPrograssbar custPrograssbar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(LoginActivity.this);
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && Utility.hasGPSDevice(this)) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            Utility.enableLoc(this);
        }
        getCode();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                codeSelect = cCodes.get(position).getCcode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("tem", parent.toString());
            }
        });
    }

    private void getCode() {
        JSONObject jsonObject = new JSONObject();
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getCodelist(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }

    private void checkMobile() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobile", edMobile.getText().toString());
            jsonObject.put("ccode", codeSelect);

            RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<JsonObject> call = APIClient.getInterface().mobileCheck(bodyRequest);
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "2");
            custPrograssbar.prograssCreate(LoginActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void checkMobileForgot() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobile", edMobile.getText().toString());
            jsonObject.put("ccode", codeSelect);

            RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<JsonObject> call = APIClient.getInterface().mobileCheck(bodyRequest);
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "4");
            custPrograssbar.prograssCreate(LoginActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void login() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mobile", edMobile.getText().toString());
            jsonObject.put("ccode", codeSelect);
            jsonObject.put("password", edPassword.getText().toString());

            RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<JsonObject> call = APIClient.getInterface().userLogin(bodyRequest);
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "3");
            custPrograssbar.prograssCreate(LoginActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @OnClick({R.id.img_back, R.id.txt_forgot, R.id.txt_continue})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();

                break;
            case R.id.txt_forgot:
                if (isValidPhoneNumber(edMobile.getText().toString())) {
                    checkMobileForgot();
                }

                break;
            case R.id.txt_continue:
                if (isValidPhoneNumber(edMobile.getText().toString())) {
                    if (edPassword.getVisibility() == View.VISIBLE) {
                        login();
                    } else {
                        checkMobile();
                    }
                } else {
                    edMobile.setError(getResources().getString(R.string.enter_mobile_number));
                }


                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private boolean isValidPhoneNumber(String phone) {

        if (!phone.trim().equals("") && phone.length() !=0) {
            return true;
        }
        edMobile.setError("Enter valid mobile ");
        return false;
    }

    @Override
    public void callback(JsonObject result, String callNo) {

        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Contry contry = gson.fromJson(result.toString(), Contry.class);
                cCodes = contry.getCountryCode();
                List<String> list = new ArrayList<>();
                for (int i = 0; i < cCodes.size(); i++) {
                    if (cCodes.get(i).getStatus().equalsIgnoreCase("1")) {
                        list.add(cCodes.get(i).getCcode());
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, list);
                dataAdapter.setDropDownViewResource(R.layout.spinner_layout);
                spinner.setAdapter(dataAdapter);
            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                if (response.getResult().equals("true")) {
                    Utility.isvarification = 1;
                    User user = new User();
                    user.setCcode(codeSelect);
                    user.setMobile("" + edMobile.getText().toString());
                    sessionManager.setUserDetails(user);
                    startActivity(new Intent(LoginActivity.this, SendOTPActivity.class));
                } else {
                    edPassword.setVisibility(View.VISIBLE);
                    lvlPassword.setVisibility(View.VISIBLE);

                }
            } else if (callNo.equalsIgnoreCase("3")) {
                Gson gson = new Gson();
                Login loginUser = gson.fromJson(result.toString(), Login.class);
                Toast.makeText(this, loginUser.getResponseMsg(), Toast.LENGTH_LONG).show();
                if (loginUser.getResult().equalsIgnoreCase("true")) {
                    sessionManager.setUserDetails(loginUser.getUserLogin());
                    sessionManager.setIntData(wallet, Integer.parseInt(loginUser.getUserLogin().getWallet()));
                    sessionManager.setBooleanData(login, true);
                    sessionManager.setBooleanData(intro, true);

                    OneSignal.sendTag("userid", loginUser.getUserLogin().getId());

                    startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                } else {
                    edPassword.setVisibility(View.GONE);
                    edPassword.setText("");
                    lvlPassword.setVisibility(View.GONE);
                }
            }else if (callNo.equalsIgnoreCase("4")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                if (response.getResult().equals("true")) {
                    Toast.makeText(this,"Number is not register",Toast.LENGTH_SHORT).show();
                } else {
                    Utility.isvarification = 0;
                    User user = new User();
                    user.setCcode(codeSelect);
                    user.setMobile("" + edMobile.getText().toString());
                    sessionManager.setUserDetails(user);
                    startActivity(new Intent(LoginActivity.this, SendOTPActivity.class));
                }
            }

        } catch (Exception e) {
            Log.e("Error","-"+e.getMessage());

        }
    }
    public void showHidePass(View view) {

        if (view.getId() == R.id.show_pass_btn) {

            if (edPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                ((ImageView) (view)).setImageResource(R.drawable.ic_eye_close);

                //Show Password
                edPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                ((ImageView) (view)).setImageResource(R.drawable.ic_eye);

                //Hide Password
                edPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        }
    }
}