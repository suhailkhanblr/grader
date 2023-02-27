package com.grader.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.grader.user.R;
import com.grader.user.fregment.Info1Fragment;
import com.grader.user.fregment.Info2Fragment;
import com.grader.user.fregment.Info3Fragment;
import com.grader.user.model.User;
import com.grader.user.utility.SessionManager;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class IntroActivity extends AppCompatActivity {

    SessionManager sessionManager;
    public static ViewPager vpPager;
    MyPagerAdapter adapterViewPager;
    @BindView(R.id.dots_indicator)
    DotsIndicator flexibleIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);
        vpPager = findViewById(R.id.vpPager);
        sessionManager = new SessionManager(IntroActivity.this);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        if (sessionManager.getBooleanData(SessionManager.login) && sessionManager.getBooleanData(SessionManager.intro)) {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            finish();
        }
        vpPager.setAdapter(adapterViewPager);
        flexibleIndicator.setViewPager(vpPager);
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e("data", "jsadlj");
            }
            @Override
            public void onPageSelected(int position) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                Log.e("sjlkj", "sjahdal");
            }
        });
    }

    @OnClick({R.id.lvl_phone})
    public void onClick(View view) {
        if (view.getId() == R.id.lvl_phone) {
            User user = new User();
            user.setId("0");
            user.setFname("Test");
            user.setEmail("test@gmail.com");
            user.setMobile("1020304050");

            sessionManager.setUserDetails(user);
            sessionManager.setBooleanData(SessionManager.intro, true);
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int numItems = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return numItems;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return Info1Fragment.newInstance();
                case 1:
                    return Info2Fragment.newInstance();
                case 2:
                    return Info3Fragment.newInstance();
                default:
                    return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.e("page", "" + position);
            return "Page " + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            return fragment;
        }

    }
}
