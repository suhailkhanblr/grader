package com.grader.user.activity;

import static com.grader.user.utility.SessionManager.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.grader.user.R;
import com.grader.user.utility.SessionManager;

public class FirstActivity extends AppCompatActivity {

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        sessionManager = new SessionManager(FirstActivity.this);
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                if (sessionManager.getBooleanData(SessionManager.intro)) {
                    if (sessionManager.getBooleanData(login)) {
                        startActivity(new Intent(FirstActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(FirstActivity.this, LoginActivity.class));
                        finish();
                    }
                } else {
                    Intent i = new Intent(FirstActivity.this, IntroActivity.class);
                    startActivity(i);
                }
                finish();

            }
        }, 3000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}