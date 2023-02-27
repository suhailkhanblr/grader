package com.grader.user.activity;

import static com.grader.user.utility.SessionManager.coupon;
import static com.grader.user.utility.SessionManager.couponid;
import static com.grader.user.utility.SessionManager.currency;
import static com.grader.user.utility.SessionManager.dropList;
import static com.grader.user.utility.SessionManager.login;
import static com.grader.user.utility.SessionManager.wallet;
import static com.grader.user.utility.Utility.paymentsucsses;
import static com.grader.user.utility.Utility.tragectionID;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.adepter.WheelerAdapter;
import com.grader.user.map.FetchURL;
import com.grader.user.map.TaskLoadedCallback;
import com.grader.user.model.DUser;
import com.grader.user.model.PaymentItem;
import com.grader.user.model.RestResponse;
import com.grader.user.model.User;
import com.grader.user.model.WCategory;
import com.grader.user.model.Wheele;
import com.grader.user.model.Wheeleritem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.Drop;
import com.grader.user.utility.Pickup;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class BookWheelerActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, WheelerAdapter.RecyclerTouchListener, GetResult.MyListener {

    @BindView(R.id.recycleview_wheele)
    RecyclerView recycleviewWheele;
    @BindView(R.id.txt_pickupcontect)
    TextView txtPickupcontect;
    @BindView(R.id.btn_bookwheele)
    TextView btnBookwheele;
    @BindView(R.id.txt_goodtype)
    TextView txtGoodtype;
    @BindView(R.id.txt_applycode)
    TextView txtApplycode;
    @BindView(R.id.lvl_view)
    ScrollView lvlView;
    Pickup pickup;
    Drop drop;
    GoogleMap mMap;
    private Polyline currentPolyline;
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    double dist = 0;
    User user;


    List<PaymentItem> paymentList = new ArrayList<>();
    List<WCategory> categoryList = new ArrayList<>();
    WCategory wCategor;
    Wheeleritem wheelitem;
    double tWallet = 0;
    double itmeprice = 0;
    double totalprice = 0;
    double time = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_wheeler);
        ButterKnife.bind(this);

        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        sessionManager.setIntData(coupon, 0);
        user = sessionManager.getUserDetails();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pickup = getIntent().getParcelableExtra("pickup");
        drop = getIntent().getParcelableExtra("drop");
        txtPickupcontect.setText("" + drop.getRname() + "-" + drop.getRmobile());

        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        mLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycleviewWheele.setLayoutManager(mLayoutManager2);
        recycleviewWheele.setItemAnimator(new DefaultItemAnimator());

        for (int i = 0; i < dropList.size(); i++) {
            dist = dist + distance(pickup.getLat(), pickup.getLog(), dropList.get(i).getLat(), dropList.get(i).getLog());
            Log.e("dis", "-->" + dist);

        }


        vehiclelist();

    }

    private void vehiclelist() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("lat", pickup.getLat());
            jsonObject.put("long", pickup.getLog());

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().vehiclelist(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    private void placeOrder() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("p_method_id", Utility.paymentId);
            jsonObject.put("pick_address", pickup.getAddress());
            jsonObject.put("pick_lat", pickup.getLat());
            jsonObject.put("pick_lng", pickup.getLog());
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < dropList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("drop_address", dropList.get(i).getAddress());
                object.put("drop_lat", dropList.get(i).getLat());
                object.put("drop_lng", dropList.get(i).getLog());
                object.put("drop_name", dropList.get(i).getRname());
                object.put("drop_mobile", dropList.get(i).getRmobile());
                jsonArray.put(object);
            }

            jsonObject.put("vehicleid", wheelitem.getId());
            jsonObject.put("cou_id", sessionManager.getIntData(couponid));
            jsonObject.put("cou_amt", sessionManager.getIntData(coupon));
            jsonObject.put("transaction_id", tragectionID);
            jsonObject.put("wall_amt", tWallet);
            jsonObject.put("o_total", totalprice);
            jsonObject.put("cat_id", wCategor.getId());
            jsonObject.put("subtotal", itmeprice);
            jsonObject.put("ParcelData", jsonArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().orderNow(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Wheele wheele = gson.fromJson(result.toString(), Wheele.class);
                Toast.makeText(BookWheelerActivity.this, wheele.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (wheele.getResult().equalsIgnoreCase("true")) {

                    List<Wheeleritem> list = wheele.getVehiclelist();
                    recycleviewWheele.setAdapter(new WheelerAdapter(this, list, this, dist));
                    paymentList = wheele.getPaymentItems();

                    if (wheele.getCategories().size() != 0) {
                        categoryList = wheele.getCategories();
                        wCategor = wheele.getCategories().get(0);
                    }
                    txtGoodtype.setText("" + wCategor.getCatName());


                }
            } else if (callNo.equalsIgnoreCase("2")) {

                Gson gson = new Gson();
                RestResponse restResponse = gson.fromJson(result.toString(), RestResponse.class);
                user.setWallet(restResponse.getWallet());
                sessionManager.setUserDetails(user);
                if (restResponse.getResult().equalsIgnoreCase("true")) {
                    dropList.clear();
                    dropList = new ArrayList<>();
                    sessionManager.setIntData(wallet, Integer.parseInt(restResponse.getWallet()));
                    Intent intent = new Intent(this, DriverSearchActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("oid", restResponse.getOrderId());
                    startActivity(intent);
                    finish();

                } else {
                    finish();
                    Toast.makeText(this, restResponse.getResponseMsg(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("Error", "-" + e.getMessage());

        }
    }


    public void bottonPaymentList() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_payment, null);
        LinearLayout listView = sheetView.findViewById(R.id.lvl_list);
        TextView txtBooktrip = sheetView.findViewById(R.id.btn_booktrip);
        LinearLayout lvlWallat = sheetView.findViewById(R.id.lvl_wallat);
        if (Double.parseDouble(user.getWallet()) == 0) {
            lvlWallat.setVisibility(View.GONE);
        }
        Switch swich = sheetView.findViewById(R.id.swich);
        TextView txtTotal = sheetView.findViewById(R.id.txt_total);
        TextView txtWallet = sheetView.findViewById(R.id.txt_wallet);
        totalprice = itmeprice - sessionManager.getIntData(coupon);
        txtTotal.setText("Total " + sessionManager.getStringData(currency) + totalprice);
        txtWallet.setText("user credit (" + sessionManager.getStringData(currency) + user.getWallet() + ")");
        txtBooktrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.paymentId = "5";
                placeOrder();
            }
        });
        swich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (Double.parseDouble(user.getWallet()) < totalprice) {
                        double t = totalprice - Double.parseDouble(user.getWallet());
                        txtTotal.setText("item total " + sessionManager.getStringData(currency) + t);
                        tWallet = Double.parseDouble(user.getWallet());
                        txtBooktrip.setVisibility(View.GONE);

                    } else {
                        double t = Double.parseDouble(user.getWallet()) - totalprice;
                        txtWallet.setText("user credit (" + sessionManager.getStringData(currency) + t + ")");
                        txtTotal.setText("item total " + sessionManager.getStringData(currency) + "0");
                        tWallet = totalprice;
                        listView.setVisibility(View.GONE);
                        txtBooktrip.setVisibility(View.VISIBLE);
                    }
                } else {
                    txtTotal.setText("item total " + sessionManager.getStringData(currency) + totalprice);
                    txtWallet.setText("user credit (" + sessionManager.getStringData(currency) + user.getWallet() + ")");
                    listView.setVisibility(View.VISIBLE);
                    txtBooktrip.setVisibility(View.GONE);
                    tWallet = 0;
                }
            }
        });
        for (int i = 0; i < paymentList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(BookWheelerActivity.this);
            PaymentItem paymentItem = paymentList.get(i);
            View view = inflater.inflate(R.layout.custome_paymentitem, null);
            ImageView imageView = view.findViewById(R.id.img_icon);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            TextView txtSubtitel = view.findViewById(R.id.txt_subtitel);
            txtTitle.setText("" + paymentList.get(i).getmTitle());
            txtSubtitel.setText("" + paymentList.get(i).getSubtitle());
            Glide.with(BookWheelerActivity.this).load(APIClient.baseUrl + "/" + paymentList.get(i).getmImg()).thumbnail(Glide.with(BookWheelerActivity.this).load(R.drawable.emty)).into(imageView);
            int finalI = i;
            view.setOnClickListener(v -> {
                Utility.paymentId = paymentList.get(finalI).getmId();
                try {
                    switch (paymentList.get(finalI).getmTitle()) {
                        case "Razorpay":
                            int temtoal = (int) Math.round(totalprice);
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, RazerpayActivity.class).putExtra("amount", temtoal).putExtra("detail", paymentItem));
                            break;
                        case "Cash On Delivery":
                            placeOrder();
                            mBottomSheetDialog.cancel();
                            break;
                        case "Paypal":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, PaypalActivity.class).putExtra("amount", totalprice).putExtra("detail", paymentItem));
                            break;
                        case "Stripe":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, StripPaymentActivity.class).putExtra("amount", totalprice).putExtra("detail", paymentItem));
                            break;
                        case "FlutterWave":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, FlutterwaveActivity.class).putExtra("amount", totalprice));
                            break;
                        case "Paytm":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, PaytmActivity.class).putExtra("amount", totalprice));
                            break;
                        case "SenangPay":
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, SenangpayActivity.class).putExtra("amount", totalprice).putExtra("detail", paymentItem));
                            break;
                        case "PayStack":
                            int temtoal1 = (int) Math.round(totalprice);
                            mBottomSheetDialog.cancel();
                            startActivity(new Intent(BookWheelerActivity.this, PaystackActivity.class).putExtra("amount", temtoal1).putExtra("detail", paymentItem));
                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            listView.addView(view);
        }
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    public void bottonGoodsList() {

        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_goods, null);
        LinearLayout listView = sheetView.findViewById(R.id.lvl_list);
        for (int i = 0; i < categoryList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(BookWheelerActivity.this);
            WCategory wCategory = categoryList.get(i);
            View view = inflater.inflate(R.layout.custome_goodsitem, null);
            ImageView imageView = view.findViewById(R.id.img_select);
            ImageView imgIcon = view.findViewById(R.id.img_icon);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            Glide.with(this).load(APIClient.baseUrl + "/" + wCategory.getCatImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgIcon);
            txtTitle.setText("" + wCategory.getCatName());
            if (wCategory.isSelected()) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
            int finalI = i;
            view.setOnClickListener(v -> {
                for (int iq = 0; iq < categoryList.size(); iq++) {
                    if (finalI == iq) {

                        categoryList.get(iq).setSelected(true);
                        txtGoodtype.setText("" + categoryList.get(iq).getCatName());
                        wCategor = categoryList.get(iq);
                    } else {
                        categoryList.get(iq).setSelected(false);
                    }
                }
                imageView.setVisibility(View.VISIBLE);
                mBottomSheetDialog.cancel();
            });
            listView.addView(view);
        }
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();


    }

    public void bottonInfo(Wheeleritem item) {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.custome_info, null);
        ImageView imgIcon = sheetView.findViewById(R.id.img_icon);
        TextView txtTitle = sheetView.findViewById(R.id.txt_title);
        TextView txtCapcity = sheetView.findViewById(R.id.txt_capcity);
        TextView txtSize = sheetView.findViewById(R.id.txt_size);
        TextView txtDis = sheetView.findViewById(R.id.txt_dis);
        Glide.with(this).load(APIClient.baseUrl + "/" + item.getImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgIcon);
        txtTitle.setText("" + item.getTitle());
        txtCapcity.setText("" + item.getCapcity());
        txtSize.setText("" + item.getSize());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDis.setText(Html.fromHtml(item.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtDis.setText(Html.fromHtml(item.getDescription()));
        }
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    public void bottonConfirm() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(BookWheelerActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        mBottomSheetDialog.setContentView(sheetView);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        EditText edName = sheetView.findViewById(R.id.ed_name);
        EditText edmobile = sheetView.findViewById(R.id.ed_mobile);
        TextView txtChoose = sheetView.findViewById(R.id.btn_send);
        edName.setText(drop.getRname());
        edmobile.setText(drop.getRmobile());
        chUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    edName.setText(user.getFname());
                    edmobile.setText(user.getMobile());
                } else {
                    edmobile.setText("");
                    edName.setText("");
                }
            }
        });
        txtChoose.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(edmobile.getText().toString()) && !TextUtils.isEmpty(edName.getText().toString())) {
                mBottomSheetDialog.cancel();
                drop.setRname(edName.getText().toString());
                drop.setRmobile("" + edmobile.getText().toString());
                txtPickupcontect.setText("" + drop.getRname() + "-" + drop.getRmobile());

            }
        });
        mBottomSheetDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btn_bookwheele, R.id.lvl_payment, R.id.txt_goodtype, R.id.txt_applycode, R.id.txt_pickupcontect})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.btn_bookwheele:
                if (wheelitem != null) {
                    bottonPaymentList();
                } else {
                    Toast.makeText(this, "Select Vehicle first", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.txt_goodtype:
                bottonGoodsList();
                break;
            case R.id.lvl_payment:

                break;
            case R.id.txt_pickupcontect:
                bottonConfirm();
                break;
            case R.id.txt_applycode:
                if (sessionManager.getIntData(coupon) != 0) {
                    txtApplycode.setText("Apply Coupon");
                    sessionManager.setIntData(coupon, 0);
                } else {
                    int temp = (int) Math.round(itmeprice);
                    startActivity(new Intent(BookWheelerActivity.this, CoupunActivity.class).putExtra("amount", temp));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    public void onClickWheelerItem(Wheeleritem item, int position) {
        bottonInfo(item);
    }


    @Override
    public void onClickWheelerInfo(Wheeleritem item, int position) {
        sessionManager.setIntData(coupon, 0);
        txtApplycode.setText("Apply Coupon");
        wheelitem = item;
        if (dist <= item.getStartDistance()) {
            itmeprice = item.getStartPrice();
        } else {
            double km = dist - item.getStartDistance();
            itmeprice = item.getStartPrice() + (km * item.getAfterPrice());
        }
        time = dist * item.getTimeTaken();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.getIntData(coupon) != 0) {
            txtApplycode.setText("Save " + sessionManager.getStringData(currency) + sessionManager.getIntData(coupon));
        } else {
            txtApplycode.setText("Apply Coupon");
            sessionManager.setIntData(coupon, 0);
        }

        if (paymentsucsses == 1) {
            paymentsucsses = 0;
            placeOrder();
        }
    }


    @Override
    public void onTaskDone(Object... values) {

        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        final MarkerOptions[] place2 = {null};
        final MarkerOptions[] place1 = new MarkerOptions[1];
        place1[0] = new MarkerOptions().position(new LatLng(pickup.getLat(), pickup.getLog())).title("Path").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long));
        mMap.addMarker(place1[0]);
        Handler handler1 = new Handler();
        for (int i = 0; i < dropList.size(); i++) {
            Log.e("SSSSSSS", "POPO" + i);
            int finalI = i;
            handler1.postDelayed(new Runnable() {

                @Override
                public void run() {

                    if (finalI != 0) {
                        place1[0] = new MarkerOptions().position(new LatLng(dropList.get(finalI - 1).getLat(), dropList.get(finalI - 1).getLog())).title("f").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_long));
                    }
                    place2[0] = new MarkerOptions().position(new LatLng(dropList.get(finalI).getLat(), dropList.get(finalI).getLog())).title("f").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_long));
                    mMap.addMarker(place2[0]);
                    new FetchURL(BookWheelerActivity.this).execute(getUrl(place1[0].getPosition(), place2[0].getPosition(), "driving"), "driving");
                }
            }, 1000);
        }

        LatLng coordinate = new LatLng(pickup.getLat(), pickup.getLog());
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 12);
        mMap.animateCamera(yourLocation);

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

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            dist = (int) Math.round(dist);

            return (dist);
        }
    }
}