package com.grader.user.activity;

import static android.os.Build.VERSION.SDK_INT;
import static com.grader.user.utility.SessionManager.dropList;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grader.user.R;
import com.grader.user.model.User;
import com.grader.user.model.Zone;
import com.grader.user.polygon.Point;
import com.grader.user.polygon.Polygon;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.Drop;
import com.grader.user.utility.Pickup;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class DropMapActivity extends AppCompatActivity implements OnMapReadyCallback, GetResult.MyListener {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1020;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.ed_search)
    EditText edSearch;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.btn_send)
    TextView btnSend;
    @BindView(R.id.locationMarkertext)
    LinearLayout locationMarkertext;
    @BindView(R.id.imageMarker)
    ImageView imageMarker;
    @BindView(R.id.locationMarker)
    LinearLayout locationMarker;
    private GoogleMap mMap;
    @BindView(R.id.lvl_sorry)
    LinearLayout lvlSorry;
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    User user;
    Polygon polygon;
    Pickup pickup;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);
        ButterKnife.bind(this);
        pickup = getIntent().getParcelableExtra("pickup");
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.API_KEY), Locale.US);
        }



        getzone();
    }

    public void bottonConfirm() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(DropMapActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        mBottomSheetDialog.setContentView(sheetView);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        EditText edName = sheetView.findViewById(R.id.ed_name);
        EditText edmobile = sheetView.findViewById(R.id.ed_mobile);
        TextView txtChoose = sheetView.findViewById(R.id.btn_send);
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
                Drop drop = new Drop();
                drop.setLat(latitude);
                drop.setLog(longitude);
                drop.setAddress(addressBundle.getString("fulladdress"));
                drop.setRname("" + edName.getText().toString());
                drop.setRmobile("" + edmobile.getText().toString());
                dropList.add(drop);
                startActivity(new Intent(this, ReviewMapActivity.class)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));
            }
        });
        mBottomSheetDialog.show();
    }

    private void getzone() {

        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", "01");

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getzone(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCameraIdle() {
                LatLng latLng = mMap.getCameraPosition().target;
                if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                    mMap.clear();
                    Point point = new Point(latLng.latitude, latLng.longitude);
                    boolean contains = polygon.contains(point);
                    if (contains) {
                        lvlSorry.setVisibility(View.GONE);
                        btnSend.setVisibility(View.VISIBLE);
                        GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng.latitude, latLng.longitude);
                    } else {
                        lvlSorry.setVisibility(View.VISIBLE);
                        btnSend.setVisibility(View.GONE);
                    }

                } else {
                    if (SDK_INT == Build.VERSION_CODES.R) {
                        try {
                            LocationManager systemService = (LocationManager) getSystemService(LOCATION_SERVICE);
                            systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(), (Consumer<Location>) (locationCallback -> {
                                Log.e("cc", "currLong: " + locationCallback.getLongitude());
                                LatLng latLng1 = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                                Point point = new Point(latLng1.latitude, latLng1.longitude);
                                boolean contains = polygon.contains(point);
                                if (contains) {
                                    lvlSorry.setVisibility(View.GONE);
                                    btnSend.setVisibility(View.VISIBLE);
                                    GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng1.latitude, latLng1.longitude);
                                } else {
                                    lvlSorry.setVisibility(View.VISIBLE);
                                    btnSend.setVisibility(View.GONE);
                                }
                            }));



                        } catch (Exception e) {
                            Log.e("error","->"+e.toString());
                        }
                    }
                }
            }
        });

        if (curruntLocation) {
            curruntLocation = false;
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                mMap.animateCamera(yourLocation);

            } else {
                LocationManager systemService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (SDK_INT == Build.VERSION_CODES.R) {
                    systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(), (Consumer<Location>) (locationCallback -> {
                        Log.e("cc", "currLong: " + locationCallback.getLongitude());
                        LatLng coordinate = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                        mMap.animateCamera(yourLocation);
                    }));
                } else {
                    Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
                    lastLocation.addOnSuccessListener(this, location1 -> {
                        if (location1 != null) {
                            mMap.clear();
                            LatLng coordinate = new LatLng(location1.getLatitude(), location1.getLongitude());
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                            mMap.animateCamera(yourLocation);
                        } else {
                            //Gps not enabled if loc is null
                            Utility.enableLoc(this);
                            Toast.makeText(this, "Location not Available", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.e("TAG", "Place: " + place.getName() + ", " + place.getId());
                    edSearch.setText(place.getName());
                    mMap.clear();
                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17);
                    mMap.animateCamera(yourLocation);

                    Point point = new Point(place.getLatLng().latitude, place.getLatLng().longitude);
                    if (polygon != null) {
                        boolean contains = polygon.contains(point);
                        Log.e("resulr", "---> " + contains);
                        if (contains) {

                            lvlSorry.setVisibility(View.GONE);
                            btnSend.setVisibility(View.VISIBLE);
                        } else {
                            lvlSorry.setVisibility(View.VISIBLE);
                            btnSend.setVisibility(View.GONE);

                        }
                    } else {
                        btnSend.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    e.toString();

                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean curruntLocation = false;

    @OnClick({R.id.img_back, R.id.ed_search, R.id.btn_send, R.id.img_currunt})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.ed_search:
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.btn_send:
                bottonConfirm();
                break;
            case R.id.img_currunt:
                curruntLocation = true;
                onMapReady(mMap);

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();

            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Zone zone = gson.fromJson(result.toString(), Zone.class);
                Polygon.Builder poly2 = new Polygon.Builder();
                for (int i = 0; i < zone.getZones().size(); i++) {
                    poly2.addVertex(new Point(zone.getZones().get(i).getLat(), zone.getZones().get(i).getLng()));
                }
                polygon = poly2.build();


                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                        .build(this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e("Error", "--->" + e.getMessage());
        }
    }

    Double latitude;
    Double longitude;
    Bundle addressBundle;

    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(DropMapActivity.this);
            addressBundle = new Bundle();
        }
        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                Utility.hideProgress();
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(DropMapActivity.this, Locale.getDefault());
                StringBuilder sb = new StringBuilder();
                //get location from lat long if address string is null
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && addresses.size() > 0) {

                    String address = addresses.get(0).getSubLocality();
                    if (address != null) {
                        addressBundle.putString("addressline2", address);

                    } else {
                        String address1 = addresses.get(0).getFeatureName();
                        addressBundle.putString("addressline2", address1);

                    }
                    String addline1 = addresses.get(0).getAddressLine(0);
                    if (addline1 != null)
                        sb.append(addline1).append(" ");

                    String city = addresses.get(0).getLocality();
                    if (city != null)
                        addressBundle.putString("city", city);
                    sb.append(city).append(" ");

                    String state = addresses.get(0).getAdminArea();
                    if (state != null)
                        addressBundle.putString("state", state);
                    sb.append(state).append(" ");

                    String country = addresses.get(0).getCountryName();
                    if (country != null)
                        addressBundle.putString("country", country);
                    sb.append(country).append(" ");

                    String postalCode = addresses.get(0).getPostalCode();
                    if (postalCode != null)
                        addressBundle.putString("postalcode", postalCode);
                    sb.append(postalCode).append(" ");

                    addressBundle.putString("fulladdress", sb.toString());

                    return addressBundle;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                addressBundle.putBoolean("error", true);
                return addressBundle;

            }

        }

        @Override
        // setting address into different components
        protected void onPostExecute(Bundle userAddress) {
            super.onPostExecute(userAddress);
            try {
                String address = userAddress.getString("fulladdress"); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String knownName = userAddress.getString("addressline2");
                Log.e("address", "----" + address);
                Log.e("knownName", "----" + knownName);
                Utility.hideProgress();

                if (address != null) {
                    txtAddress.setText(address);
                    locationMarkertext.setVisibility(View.VISIBLE);

                }

            } catch (Exception e) {
                e.toString();
            }

        }
    }
}