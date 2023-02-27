package com.grader.user.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.grader.user.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HelpDetailsActivity extends AppCompatActivity {

    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_desc)
    TextView txtDesc;

    @OnClick(R.id.img_back)
    public void onClick() {
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_details);
        ButterKnife.bind(this);

        txtTitle.setText(""+getIntent().getExtras().getString("title"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDesc.setText(Html.fromHtml(getIntent().getExtras().getString("desc"), Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtDesc.setText(Html.fromHtml(getIntent().getExtras().getString("desc")));
        }
    }
}