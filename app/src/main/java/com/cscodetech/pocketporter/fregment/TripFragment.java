package com.grader.user.fregment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grader.user.R;
import com.grader.user.activity.DriverSearchActivity;
import com.grader.user.activity.TripDetailsActivity;
import com.grader.user.adepter.TripAdapter;
import com.grader.user.model.HistoryinfoItem;
import com.grader.user.model.Orders;
import com.grader.user.model.User;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

public class TripFragment extends Fragment implements TripAdapter.RecyclerTouchListener, GetResult.MyListener {


    @BindView(R.id.recycleview_trip)
    RecyclerView recycleviewTrip;
    @BindView(R.id.lvl_notfound)
    LinearLayout lvlNotfound;

    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity());
        mLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(getActivity());
        user = sessionManager.getUserDetails();
        recycleviewTrip.setLayoutManager(mLayoutManager2);
        recycleviewTrip.setItemAnimator(new DefaultItemAnimator());
        recycleviewTrip.setAdapter(new TripAdapter(getActivity(), new ArrayList<>(), this));
        getOrders();
        return view;

    }

    private void getOrders() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().orderHistory(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }

    @Override
    public void onClickPackageItem(HistoryinfoItem item, int position) {
        if(item.getOrderStatus().equalsIgnoreCase("Completed")||item.getOrderStatus().equalsIgnoreCase("Cancelled")){
            startActivity(new Intent(getActivity(), TripDetailsActivity.class).putExtra("myclass", item).putExtra("list", (Serializable) item.getParceldata()));
        }else {

            startActivity(new Intent(getActivity(), DriverSearchActivity.class).putExtra("oid", item.getOrderid()));
        }

    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Orders orders = gson.fromJson(result.toString(), Orders.class);
                if (orders.getResult().equalsIgnoreCase("true")) {
                    if(orders.getHistoryinfo().size()!=0){
                        lvlNotfound.setVisibility(View.GONE);
                        recycleviewTrip.setVisibility(View.VISIBLE);
                        recycleviewTrip.setAdapter(new TripAdapter(getActivity(), orders.getHistoryinfo(), this));
                    }else {
                        lvlNotfound.setVisibility(View.VISIBLE);
                        recycleviewTrip.setVisibility(View.GONE);
                    }


                }
            }
        } catch (Exception e) {
            Log.e("Error","-"+e.getMessage());

        }
    }
}