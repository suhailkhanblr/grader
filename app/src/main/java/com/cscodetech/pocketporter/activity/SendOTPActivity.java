package com.grader.user.activity;


import static com.grader.user.utility.SessionManager.intro;
import static com.grader.user.utility.SessionManager.login;
import static com.grader.user.utility.SessionManager.wallet;
import static com.grader.user.utility.Utility.isvarification;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grader.user.R;
import com.grader.user.model.Login;
import com.grader.user.model.User;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.SessionManager;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;


public class SendOTPActivity extends AppCompatActivity implements GetResult.MyListener {
    @BindView(R.id.txt_mob)
    TextView txtMob;
    @BindView(R.id.ed_otp1)
    EditText edOtp1;
    @BindView(R.id.ed_otp2)
    EditText edOtp2;
    @BindView(R.id.ed_otp3)
    EditText edOtp3;
    @BindView(R.id.ed_otp4)
    EditText edOtp4;
    @BindView(R.id.ed_otp5)
    EditText edOtp5;
    @BindView(R.id.ed_otp6)
    EditText edOtp6;

    @BindView(R.id.btn_reenter)
    TextView btnReenter;
    @BindView(R.id.btn_timer)
    TextView btnTimer;
    private String verificationId;
    private FirebaseAuth mAuth;
    String phonenumber;
    String phonecode;
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(SendOTPActivity.this);
        custPrograssbar = new CustPrograssbar();
        mAuth = FirebaseAuth.getInstance();


        if (isvarification == 2) {
            user = (User) getIntent().getSerializableExtra("user");
        } else {
            user = sessionManager.getUserDetails();
        }

        sendVerificationCode(user.getCcode() + user.getMobile());
        txtMob.setText("We have sent you an SMS on " + user.getCcode() + " " + user.getMobile() + "\n with 6 digit verification code");
        try {
            new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                    btnTimer.setText(seconds + " Second Wait");
                }

                @Override
                public void onFinish() {
                    btnReenter.setVisibility(View.VISIBLE);
                    btnTimer.setVisibility(View.GONE);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        edOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 1) {
                    edOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("fdlk", "kgjd");

            }
        });
        edOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    edOtp3.requestFocus();
                } else if (count == 0) {
                    edOtp1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    edOtp4.requestFocus();
                } else if (count == 0) {
                    edOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("fdlk", "kgjd");
            }
        });
        edOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 1) {
                    edOtp5.requestFocus();
                } else if (count == 0) {
                    edOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("fdlk", "kgjd");

            }
        });
        edOtp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 1) {
                    edOtp6.requestFocus();
                } else if (count == 0) {
                    edOtp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("fdlk", "kgjd");

            }
        });
        edOtp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("fdlk", "kgjd");

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    edOtp6.requestFocus();
                } else if (count == 0) {
                    edOtp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("fdlk", "kgjd");

            }
        });
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        switch (isvarification) {
                            case 0:
                                Intent intent = new Intent(SendOTPActivity.this, ChanegPasswordActivity.class);
                                intent.putExtra("phone", user.getMobile());
                                startActivity(intent);
                                finish();
                                break;
                            case 1:
                                bottonConfirm();

                                break;
                            case 2:
                                break;
                            case 3:
                                finish();
                                break;
                            default:
                                break;
                        }
                    } else {
                        Toast.makeText(SendOTPActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                edOtp1.setText("" + code.substring(0, 1));
                edOtp2.setText("" + code.substring(1, 2));
                edOtp3.setText("" + code.substring(2, 3));
                edOtp4.setText("" + code.substring(3, 4));
                edOtp5.setText("" + code.substring(4, 5));
                edOtp6.setText("" + code.substring(5, 6));
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            User user1 = new User();
            user1.setId("0");
            user1.setFname("User");
            user1.setCcode("user@gmail.com");
            user1.setMobile("+91 8888888888");
            user1.setRefercode("+91 8888888888");
            sessionManager.setUserDetails(user1);
            Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    };

    @OnClick({R.id.btn_send, R.id.btn_reenter})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (validation()) {
                    verifyCode(edOtp1.getText().toString() + "" + edOtp2.getText().toString() + "" + edOtp3.getText().toString() + "" + edOtp4.getText().toString() + "" + edOtp5.getText().toString() + "" + edOtp6.getText().toString());
                }
                break;
            case R.id.btn_reenter:
                btnReenter.setVisibility(View.GONE);
                btnTimer.setVisibility(View.VISIBLE);
                try {
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                            btnTimer.setText(seconds + " Second Wait");
                        }

                        @Override
                        public void onFinish() {
                            btnReenter.setVisibility(View.VISIBLE);
                            btnTimer.setVisibility(View.GONE);
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendVerificationCode(user.getCcode() + user.getMobile());
                break;
            default:
                break;
        }
    }

    public void bottonConfirm() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(SendOTPActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_singup, null);
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.setCancelable(false);
        EditText edName = sheetView.findViewById(R.id.ed_name);
        EditText edEmail = sheetView.findViewById(R.id.ed_email);
        TextView edmobile = sheetView.findViewById(R.id.ed_mobile);
        EditText edPassword = sheetView.findViewById(R.id.ed_password);
        EditText edRefercode = sheetView.findViewById(R.id.ed_refercode);
        TextView txtChoose = sheetView.findViewById(R.id.btn_send);
        edmobile.setText("" + user.getMobile());

        txtChoose.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(edName.getText().toString())
                    && !TextUtils.isEmpty(edEmail.getText().toString())
                    && !TextUtils.isEmpty(edmobile.getText().toString())
                    && !TextUtils.isEmpty(edPassword.getText().toString())) {
                mBottomSheetDialog.cancel();
                user.setFname("" + edName.getText().toString());
                user.setEmail("" + edEmail.getText().toString());
                user.setPassword("" + edPassword.getText().toString());
                user.setRefercode("" + edRefercode.getText().toString());
                createUser();
            }
        });
        mBottomSheetDialog.show();

    }


    private void createUser() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", user.getFname());
            jsonObject.put("ccode", user.getCcode());
            jsonObject.put("mobile", user.getMobile());
            jsonObject.put("password", user.getPassword());
            jsonObject.put("email", user.getEmail());
            jsonObject.put("rcode", user.getRefercode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().createUser(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }


    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            Log.e("response", "--->" + result);
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("2")) {
                isvarification = -1;
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validation() {
        if (edOtp1.getText().toString().isEmpty()) {
            edOtp1.setError("");
            return false;
        }
        if (edOtp2.getText().toString().isEmpty()) {
            edOtp2.setError("");
            return false;
        }
        if (edOtp3.getText().toString().isEmpty()) {
            edOtp3.setError("");
            return false;
        }
        if (edOtp4.getText().toString().isEmpty()) {
            edOtp4.setError("");
            return false;
        }
        if (edOtp5.getText().toString().isEmpty()) {
            edOtp5.setError("");
            return false;
        }
        if (edOtp6.getText().toString().isEmpty()) {
            edOtp6.setError("");
            return false;
        }
        return true;
    }
}
