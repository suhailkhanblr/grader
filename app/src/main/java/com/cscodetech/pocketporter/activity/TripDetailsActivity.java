package com.grader.user.activity;

import static com.grader.user.utility.SessionManager.currency;
import static com.grader.user.utility.SessionManager.dropList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.model.Dropitem;
import com.grader.user.model.HistoryinfoItem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.utility.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    SessionManager sessionManager;
    HistoryinfoItem item;

    @BindView(R.id.ima_back)
    ImageView imaBack;
    @BindView(R.id.txt_date)
    TextView txtDate;
    @BindView(R.id.txt_packid)
    TextView txtPackid;
    @BindView(R.id.txtttotle)
    TextView txtttotle;
    @BindView(R.id.img_icon)
    ImageView imgIcon;
    @BindView(R.id.txt_ridername)
    TextView txtRidername;
    @BindView(R.id.txt_vtype)
    TextView txtVtype;
    @BindView(R.id.txt_status)
    TextView txtStatus;
    @BindView(R.id.txt_pickaddress)
    TextView txtPickaddress;
    @BindView(R.id.lvl_drop)
    LinearLayout lvlDrop;
    @BindView(R.id.txt_porterfare)
    TextView txtPorterfare;
    @BindView(R.id.txt_coupondis)
    TextView txtCoupondis;
    @BindView(R.id.lvl_discount)
    LinearLayout lvlDiscount;
    @BindView(R.id.txt_wallet)
    TextView txtWallet;
    @BindView(R.id.lvl_wallet)
    LinearLayout lvlWallet;
    @BindView(R.id.lvl_rider)
    LinearLayout lvlRider;
    @BindView(R.id.txt_total)
    TextView txtTotal;
    @BindView(R.id.img_category)
    ImageView imgCategory;
    @BindView(R.id.txt_ctitle)
    TextView txtCTitle;
    @BindView(R.id.img_payment)
    ImageView imgPayment;
    @BindView(R.id.txt_paymenttitle)
    TextView txtPaymenttitle;
    List<Dropitem> dropitems=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);
        item = getIntent().getParcelableExtra("myclass");
        dropitems = (List<Dropitem>) getIntent().getSerializableExtra("list");

        Glide.with(this).load(APIClient.baseUrl + "/" + item.getRiderImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgIcon);
        Glide.with(this).load(APIClient.baseUrl + "/" + item.getCatImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgCategory);
        Glide.with(this).load(APIClient.baseUrl + "/" + item.getPMethodImg()).thumbnail(Glide.with(this).load(R.drawable.emty)).into(imgPayment);


        if (item.getOrderStatus().equalsIgnoreCase("cancelled")) {
            lvlRider.setVisibility(View.GONE);
        }
        txtttotle.setText(sessionManager.getStringData(currency) + item.getPTotal());
        txtDate.setText("" + item.getDateTime());
        txtPackid.setText("" + item.getPackageNumber());
        txtRidername.setText("" + item.getRiderName());
        txtVtype.setText("" + item.getvType());
        txtStatus.setText("" + item.getOrderStatus());
        txtPickaddress.setText("" + item.getPickAddress());

        for (int i = 0; i < dropitems.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.custome_droplocation, null);
            TextView txtDropaddress = view.findViewById(R.id.txt_dropaddress);
            txtDropaddress.setText("" + dropitems.get(i).getDropPointAddress());

            lvlDrop.addView(view);
        }

        txtPorterfare.setText("" + item.getSubtotal());
        txtTotal.setText(sessionManager.getStringData(currency) + item.getPTotal());
        txtPorterfare.setText(sessionManager.getStringData(currency) + item.getSubtotal());
        txtCTitle.setText("" + item.getCatName());
        txtPaymenttitle.setText("" + item.getPMethodName());

        if (Double.parseDouble(item.getCouAmt()) != 0) {
            lvlDiscount.setVisibility(View.VISIBLE);
            txtCoupondis.setText(sessionManager.getStringData(currency) + item.getCouAmt());
        }

        if (Double.parseDouble(item.getWallAmt()) != 0) {
            lvlWallet.setVisibility(View.VISIBLE);
            txtWallet.setText(sessionManager.getStringData(currency) + item.getWallAmt());
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @OnClick({R.id.ima_back})
    public void onBindClick(View view) {
        if (view.getId() == R.id.ima_back) {
            finish();
        }
    }
    Polyline polyline;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getPickLat(), item.getPickLng()), 10));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(item.getPickLat(), item.getPickLng()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));



        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(item.getPickLat(), item.getPickLng()));

        for (int i = 0; i < dropitems.size(); i++) {
            Bitmap bitmap = drawTextToBitmap(TripDetailsActivity.this, R.drawable.ic_destination_long, String.valueOf(i + 1));

            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(dropitems.get(i).getDropPointLat(), dropitems.get(i).getDropPointLng()))
                    .title("Drop")
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            latLngs.add(new LatLng(dropitems.get(i).getDropPointLat(), dropitems.get(i).getDropPointLng()));
        }
        polyline = googleMap
                .addPolyline((new PolylineOptions())
                        .addAll(latLngs).width(5).color(Color.BLUE)
                        .geodesic(true));
    }
    public Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (16 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 3;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}