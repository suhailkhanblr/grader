package com.grader.user.activity;

import static androidx.recyclerview.widget.ItemTouchHelper.Callback.makeFlag;
import static com.grader.user.utility.SessionManager.dropList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grader.user.R;
import com.grader.user.model.Wheeleritem;
import com.grader.user.utility.Drop;
import com.grader.user.utility.Pickup;
import com.grader.user.utility.SessionManager;
import com.google.android.gms.common.util.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewMapActivity extends AppCompatActivity implements OnMapReadyCallback {


    @BindView(R.id.txt_pickaddress)
    TextView txtPickaddress;

    @BindView(R.id.btn_proce)
    TextView btnProce;
    @BindView(R.id.lvl_view)
    LinearLayout lvlView;
    @BindView(R.id.recycler_drop)
    RecyclerView recyclerDrop;
    Pickup pickup;
    Drop drop;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_map);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);

        pickup = getIntent().getParcelableExtra("pickup");
        drop = getIntent().getParcelableExtra("drop");
        txtPickaddress.setText("" + pickup.getAddress());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        mLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerDrop.setLayoutManager(mLayoutManager2);
        recyclerDrop.setItemAnimator(new DefaultItemAnimator());


        final int[] oldPos = new int[1];
        final int[] newPos = new int[1];
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(

                ItemTouchHelper.UP |
                        ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT,
                0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                oldPos[0] = viewHolder.getAdapterPosition();
                newPos[0] = target.getAdapterPosition();
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.e("position", "-->" + direction);

            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                moveItem(oldPos[0], newPos[0]);
            }
        });


        itemTouchHelper.attachToRecyclerView(recyclerDrop);


    }

    @OnClick({R.id.btn_proce, R.id.txt_addnewstop})
    public void onBindClick(View view) {
        if (view.getId() == R.id.btn_proce) {
            startActivity(new Intent(this, BookWheelerActivity.class)
                    .putExtra("pickup", pickup)
                    .putExtra("drop", drop));
        } else if (view.getId() == R.id.txt_addnewstop) {
            finish();
        }
    }

    GoogleMap gMap;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // move camera to zoom on map
        gMap = googleMap;

        updatMap(googleMap);


    }

    Polyline polyline;

    public void updatMap(GoogleMap googleMap) {

        if (polyline != null) {
            polyline.remove();
        }
        if (googleMap != null) {
            googleMap.clear();
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickup.getLat(), pickup.getLog()), 13));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(pickup.getLat(), pickup.getLog()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));

        if (dropList != null) {
            dropAdapter = new DropAdapter(dropList);
            recyclerDrop.setAdapter(dropAdapter);

            List<LatLng> latLngs = new ArrayList<>();
            latLngs.add(new LatLng(pickup.getLat(), pickup.getLog()));


            for (int i = 0; i < dropList.size(); i++) {
                Bitmap bitmap = drawTextToBitmap(ReviewMapActivity.this, R.drawable.ic_destination_long, String.valueOf(i + 1));

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(dropList.get(i).getLat(), dropList.get(i).getLog()))
                        .title("Drop")
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                latLngs.add(new LatLng(dropList.get(i).getLat(), dropList.get(i).getLog()));
            }
            polyline = googleMap
                    .addPolyline((new PolylineOptions())
                            .addAll(latLngs).width(5).color(Color.BLUE)
                            .geodesic(true));

        }
    }

    DropAdapter dropAdapter;

    public class DropAdapter extends RecyclerView.Adapter<DropAdapter.MyViewHolder> {

        private List<Drop> dropList1;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txt_dropaddress)
            TextView txtDropaddress;
            @BindView(R.id.img_delete)
            ImageView imgDelete;
            @BindView(R.id.lvl_click)
            LinearLayout lvlClick;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        public DropAdapter(List<Drop> dropList1) {

            this.dropList1 = dropList1;

        }

        @Override
        public DropAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_drop, parent, false);
            return new DropAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DropAdapter.MyViewHolder holder, int position) {
            Drop item = dropList1.get(position);
            holder.txtDropaddress.setText(item.getAddress());
            if (position == 0) {
                holder.imgDelete.setVisibility(View.GONE);

            } else {
                holder.imgDelete.setVisibility(View.VISIBLE);
            }
            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == 0) {
                        Log.e("some","logo");
                    } else {
                        dropList1.remove(item);
                        dropAdapter.notifyDataSetChanged();
                        updatMap(gMap);
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return dropList1.size();
        }
    }


    private void moveItem(int oldPos, int newPos) {
        Drop temp = dropList.get(oldPos);
        dropList.set(oldPos, dropList.get(newPos));
        dropList.set(newPos, temp);
        dropAdapter.notifyItemChanged(oldPos);
        dropAdapter.notifyItemChanged(newPos);
        updatMap(gMap);
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