package com.grader.user.activity;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.map.FetchURL;
import com.grader.user.map.TaskLoadedCallback;
import com.grader.user.model.Map;
import com.grader.user.model.RestResponse;
import com.grader.user.model.User;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class DriverSearchActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, GetResult.MyListener {

    FusedLocationProviderClient fusedLocationProviderClient;


    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.img_icon)
    ImageView imgIcon;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_subtitel)
    TextView txtSubtitel;
    @BindView(R.id.btn_cancel)
    TextView btnCancel;
    @BindView(R.id.lvl_view)
    ScrollView lvlView;
    @BindView(R.id.txt_picaddress)
    TextView txtPicaddress;
    @BindView(R.id.txt_dropaddress)
    TextView txtDropaddress;
    @BindView(R.id.txt_packagenumber)
    TextView txtPackagenumber;
    @BindView(R.id.txt_timer)
    TextView txtTimer;
    @BindView(R.id.txt_msg)
    TextView txtMsg;
    @BindView(R.id.txt_ridername)
    TextView txtRidername;
    @BindView(R.id.txt_vtype)
    TextView txtVtype;
    @BindView(R.id.img_wheeler)
    ImageView imgWheeler;
    @BindView(R.id.img_riderapp)
    ImageView imgRiderapp;
    @BindView(R.id.locationMarkertext)
    LinearLayout locationMarkertext;
    @BindView(R.id.lvl_riderinfo)
    LinearLayout lvlRiderinfo;
    @BindView(R.id.txt_msghead)
    TextView txtMsghead;
    @BindView(R.id.txt_msgsub)
    TextView txtMsgsub;

    private Polyline currentPolyline;
    String oID;
    SessionManager sessionManager;
    User user;
    GoogleMap mMap;
    CustPrograssbar custPrograssbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_search);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 201);
        }
        oID = getIntent().getStringExtra("oid");
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(DriverSearchActivity.this);
        user = sessionManager.getUserDetails();

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tripInfo();
    }

    protected Boolean isActivityRunning(Class activityClass) {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (isActivityRunning(HomeActivity.class)) {
            finish();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @OnClick({R.id.img_back, R.id.btn_cancel, R.id.img_call})

    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                if (isActivityRunning(HomeActivity.class)) {
                    finish();

                } else {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                }
                break;
            case R.id.btn_cancel:
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Cancel Trip")
                        .setMessage("Are you sure you want to cancel trip?.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                tripCancel();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();


                break;
            case R.id.img_call:
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + mapData.getMapinfo().getRiderMobile()));
                startActivity(intent);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;


    }

    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }

    private void tripInfo() {
        custPrograssbar.prograssCreate(DriverSearchActivity.this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("order_id", oID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call;
        call = APIClient.getInterface().tripinfo(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    private void tripCancel() {
        custPrograssbar.prograssCreate(DriverSearchActivity.this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("order_id", oID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call;
        call = APIClient.getInterface().tripCancle(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

    Map mapData;

    @Override
    public void callback(JsonObject result, String callNo) {

        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                mapData = gson.fromJson(result.toString(), Map.class);
                txtMsghead.setText("" + mapData.getMapinfo().getHeadMsg());
                txtMsgsub.setText("" + mapData.getMapinfo().getSubMsg());
                txtPicaddress.setText("" + mapData.getMapinfo().getPickAddress());
                txtDropaddress.setText("" + mapData.getMapinfo().getDropAddress());
                txtPackagenumber.setText("" + mapData.getMapinfo().getPackageNumber());
                txtMsg.setText("" + mapData.getMapinfo().getRestMsg());
                txtTitle.setText("" + mapData.getMapinfo().getpMethodName());
                txtSubtitel.setText("" + sessionManager.getStringData(SessionManager.currency) + mapData.getMapinfo().getpTotal());
                Glide.with(this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getpMethodImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgIcon);


                if (600 > mapData.getMapinfo().getOrderArriveSeconds()) {
                    long ttime = 600 - mapData.getMapinfo().getOrderArriveSeconds();
                    ttime = ttime * 1000;
                    new CountDownTimer(ttime, 1000) {

                        public void onTick(long millisUntilFinished) {
                            //here you can have your logic to set text to edittext
                            String hms = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                            txtTimer.setText("Booking will get cancelled if not allocated in: " + hms);
                        }

                        public void onFinish() {
                            txtTimer.setVisibility(View.GONE);
                            btnCancel.setVisibility(View.GONE);
                        }

                    }.start();

                } else {
                    btnCancel.setVisibility(View.GONE);
                    txtTimer.setVisibility(View.GONE);
                }

                final MarkerOptions[] place2 = new MarkerOptions[1];
                final MarkerOptions[] place1 = new MarkerOptions[1];
                final Bitmap[] bmp = new Bitmap[1];
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URL url;
                        try {
                            url = new URL(APIClient.baseUrl + mapData.getMapinfo().getvImg());
                            bmp[0] = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mapData.getMapinfo().getoStatus().equalsIgnoreCase("Processing")) {

                                    lvlRiderinfo.setVisibility(View.VISIBLE);
                                    txtRidername.setText("" + mapData.getMapinfo().getRiderName());
                                    txtVtype.setText("" + mapData.getMapinfo().getvType() + "(" + mapData.getMapinfo().getVehicleNumber() + ")");

                                    place1[0] = new MarkerOptions().position(new LatLng(mapData.getMapinfo().getRiderLats(), mapData.getMapinfo().getRiderLongs())).title(mapData.getMapinfo().getRiderName()).icon(BitmapDescriptorFactory.fromBitmap(bmp[0]));
                                    place2[0] = new MarkerOptions().position(new LatLng(mapData.getMapinfo().getPickLat(), mapData.getMapinfo().getPickLng())).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long));
                                    new FetchURL(DriverSearchActivity.this).execute(getUrl(place1[0].getPosition(), place2[0].getPosition(), "driving"), "driving");

                                    mMap.addMarker(place1[0]);
                                    mMap.addMarker(place2[0]);

                                    LatLng coordinate = new LatLng(mapData.getMapinfo().getPickLat(), mapData.getMapinfo().getPickLng());
                                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 13);
                                    mMap.animateCamera(yourLocation);

                                    Glide.with(DriverSearchActivity.this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getvImg()).thumbnail(Glide.with(DriverSearchActivity.this).load(R.drawable.emty)).into(imgWheeler);
                                    Glide.with(DriverSearchActivity.this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getRiderImg()).thumbnail(Glide.with(DriverSearchActivity.this).load(R.drawable.emty)).into(imgRiderapp);

                                } else if (mapData.getMapinfo().getoStatus().equalsIgnoreCase("On Route")) {

                                    lvlRiderinfo.setVisibility(View.VISIBLE);
                                    txtRidername.setText("" + mapData.getMapinfo().getRiderName());
                                    txtVtype.setText("" + mapData.getMapinfo().getvType() + "(" + mapData.getMapinfo().getVehicleNumber() + ")");

                                    MarkerOptions place2;
                                    MarkerOptions place1;
                                    place1 = new MarkerOptions().position(new LatLng(mapData.getMapinfo().getRiderLats(), mapData.getMapinfo().getRiderLongs())).title(mapData.getMapinfo().getRiderName()).icon(BitmapDescriptorFactory.fromBitmap(bmp[0]));
                                    place2 = new MarkerOptions().position(new LatLng(mapData.getMapinfo().getDropLat(), mapData.getMapinfo().getDropLng())).title("Drop").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_long));
                                    new FetchURL(DriverSearchActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

                                    mMap.addMarker(place1);
                                    mMap.addMarker(place2);

                                    LatLng coordinate = new LatLng(mapData.getMapinfo().getPickLat(), mapData.getMapinfo().getPickLng());
                                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 13);
                                    mMap.animateCamera(yourLocation);

                                    Glide.with(DriverSearchActivity.this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getvImg()).thumbnail(Glide.with(DriverSearchActivity.this).load(R.drawable.emty)).into(imgWheeler);
                                    Glide.with(DriverSearchActivity.this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getRiderImg()).thumbnail(Glide.with(DriverSearchActivity.this).load(R.drawable.emty)).into(imgRiderapp);

                                } else {
                                    Glide.with(DriverSearchActivity.this).load(APIClient.baseUrl + "/" + mapData.getMapinfo().getvImg()).thumbnail(Glide.with(DriverSearchActivity.this).load(R.drawable.emty)).into(imgWheeler);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapData.getMapinfo().getPickLat(), mapData.getMapinfo().getPickLng()), 13));
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(mapData.getMapinfo().getPickLat(), mapData.getMapinfo().getPickLng()))
                                            .title("Pickup")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));
                                }

                            }
                        });
                    }
                });
                thread.start();


            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                Toast.makeText(this, response.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (response.getResult().equalsIgnoreCase("true")) {
                    finish();
                }
            }

        } catch (Exception e) {
            Log.e("Error", "-->" + e.getMessage());
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String strDest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = strOrigin + "&" + strDest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.API_KEY);
        return url;
    }


}