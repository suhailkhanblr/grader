package com.grader.user.fregment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static com.grader.user.utility.SessionManager.currency;
import static com.grader.user.utility.SessionManager.dropList;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.grader.user.R;
import com.grader.user.activity.DropMapActivity;
import com.grader.user.model.Zone;
import com.grader.user.polygon.Point;
import com.grader.user.polygon.Polygon;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.Pickup;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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


public class HomeFragment extends Fragment implements OnMapReadyCallback, GetResult.MyListener {
    MapView mMapView;
    GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;

    LinearLayout locationMarkertext;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.imageMarker)
    ImageView imageMarker;
    @BindView(R.id.locationMarker)
    LinearLayout locationMarker;
    @BindView(R.id.lvl_sorry)
    LinearLayout lvlSorry;
    @BindView(R.id.lvl_drop)
    LinearLayout lvlDrop;

    CustPrograssbar custPrograssbar;
    Polygon polygon;
    SessionManager sessionManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        sessionManager = new SessionManager(getActivity());

        ButterKnife.bind(this, view);
        custPrograssbar = new CustPrograssbar();
        fusedLocationProviderClient = getFusedLocationProviderClient(getActivity());
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.API_KEY), Locale.US);
        }
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        getZone();
        return view;
    }

    private void getZone() {
        custPrograssbar.prograssCreate(getActivity());
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
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            mMapView.getMapAsync(this);
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Zone zone = gson.fromJson(result.toString(), Zone.class);
                sessionManager.setStringData(currency, zone.getMainData().getCurrency());
                Polygon.Builder poly2 = new Polygon.Builder();
                for (int i = 0; i < zone.getZones().size(); i++) {
                    poly2.addVertex(new Point(zone.getZones().get(i).getLat(), zone.getZones().get(i).getLng()));
                }
                polygon = poly2.build();
            }
        } catch (Exception e) {
            Log.e("Error", "--->" + e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setPadding(0, 50, 50, 0);

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
                        lvlDrop.setVisibility(View.VISIBLE);
                        GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng.latitude, latLng.longitude);
                    } else {
                        lvlSorry.setVisibility(View.VISIBLE);
                        lvlDrop.setVisibility(View.GONE);

                    }

                } else {
                    if (SDK_INT == Build.VERSION_CODES.R) {
                        LocationManager systemService = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                        systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getActivity().getMainExecutor(), (Consumer<Location>) (locationCallback -> {
                            Log.e("cc", "currLong: " + locationCallback.getLongitude());
                            LatLng latLng1 = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                            Point point = new Point(latLng.latitude, latLng.longitude);
                            boolean contains = polygon.contains(point);
                            if (contains) {
                                lvlSorry.setVisibility(View.GONE);
                                lvlDrop.setVisibility(View.VISIBLE);
                                GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng1.latitude, latLng1.longitude);
                            } else {
                                lvlSorry.setVisibility(View.VISIBLE);
                                lvlDrop.setVisibility(View.GONE);

                            }


                        }));
                    }
                }
            }
        });
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {

            LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
            mMap.animateCamera(yourLocation);

        } else {
            LocationManager systemService = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (SDK_INT == Build.VERSION_CODES.R) {
                systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getActivity().getMainExecutor(), (Consumer<Location>) (locationCallback -> {
                    Log.e("cc", "currLong: " + locationCallback.getLongitude());
                    LatLng coordinate = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                    mMap.animateCamera(yourLocation);
                }));
            } else {
                Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
                lastLocation.addOnSuccessListener(getActivity(), location1 -> {
                    if (location1 != null) {
                        mMap.clear();
                        LatLng coordinate = new LatLng(location1.getLatitude(), location1.getLongitude());
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                        mMap.animateCamera(yourLocation);
                    } else {
                        //Gps not enabled if loc is null
                        Utility.enableLoc(getActivity());
                        Toast.makeText(getActivity(), "Location not Available", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }
    }

    @OnClick({R.id.lvl_drop, R.id.txt_address, R.id.img_currunt})
    public void onBindClick(View view) {
        switch (view.getId()) {

            case R.id.lvl_drop:
                dropList.clear();
                dropList = new ArrayList<>();
                Pickup pickup = new Pickup();
                pickup.setLat(latitude);
                pickup.setLog(longitude);
                pickup.setAddress(addressBundle.getString("fulladdress"));
                startActivity(new Intent(getActivity(), DropMapActivity.class).putExtra("pickup", pickup));
                break;
            case R.id.txt_address:
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                        .build(getActivity());
                getActivity().startActivityForResult(intent, 1020);
                break;
            case R.id.img_currunt:
                if(mMap!=null){
                    onMapReady(mMap);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1020) {
            if (resultCode == RESULT_OK) {
                try {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.e("TAG", "Place: " + place.getName() + ", " + place.getId());
                    mMap.clear();
                    CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17);
                    mMap.animateCamera(yourLocation);

                    Point point = new Point(place.getLatLng().latitude, place.getLatLng().longitude);
                    boolean contains = polygon.contains(point);
                    Log.e("resulr", "---> " + contains);
                    if (contains) {

                        lvlSorry.setVisibility(View.GONE);
                        lvlDrop.setVisibility(View.VISIBLE);
                    } else {
                        lvlSorry.setVisibility(View.VISIBLE);
                        lvlDrop.setVisibility(View.GONE);

                    }

                } catch (Exception e) {
                    e.toString();
                    Log.e("Error", "-->" + e.getMessage());
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

    Double latitude;
    Double longitude;
    Bundle addressBundle;

    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(getActivity());
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
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

