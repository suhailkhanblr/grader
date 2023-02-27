package com.grader.user.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.grader.user.R;
import com.grader.user.utility.SessionManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendPackageActivity extends AppCompatActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_pickaddress)
    TextView txtPickaddress;
    @BindView(R.id.txt_dropaddress)
    TextView txtDropaddress;
    @BindView(R.id.txt_item)
    TextView txtItem;
    @BindView(R.id.ed_instrucation)
    EditText edInstrucation;
    @BindView(R.id.swich)
    SwitchCompat swich;
    @BindView(R.id.txt_dcharge)
    TextView txtDcharge;
    @BindView(R.id.txt_expresscharge)
    TextView txtExpresscharge;
    @BindView(R.id.txt_tax)
    TextView txtTax;
    @BindView(R.id.txt_continue)
    TextView txtContinue;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_package);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);


    }

    @OnClick({R.id.img_back, R.id.txt_pickaddress, R.id.txt_dropaddress, R.id.txt_continue})

    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.txt_pickaddress:
                break;
            case R.id.txt_dropaddress:
                break;
            case R.id.txt_continue:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }
}