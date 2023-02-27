package com.grader.user.fregment;

import static com.grader.user.utility.Utility.paymentsucsses;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.activity.FlutterwaveActivity;
import com.grader.user.activity.PaypalActivity;
import com.grader.user.activity.PaystackActivity;
import com.grader.user.activity.PaytmActivity;
import com.grader.user.activity.RazerpayActivity;
import com.grader.user.activity.SenangpayActivity;
import com.grader.user.activity.StripPaymentActivity;
import com.grader.user.model.Payment;
import com.grader.user.model.PaymentItem;
import com.grader.user.model.RestResponse;
import com.grader.user.model.User;
import com.grader.user.model.Wallet;
import com.grader.user.model.WalletitemItem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.retrofit.GetResult;
import com.grader.user.utility.CustPrograssbar;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;


public class WalletFragment extends Fragment implements GetResult.MyListener {


    @BindView(R.id.txt_wallet)
    TextView txtWallet;
    @BindView(R.id.addpayment)
    TextView addpayment;
    @BindView(R.id.recycleview_history)
    RecyclerView recycleviewHistory;
    @BindView(R.id.lvl_notfound)
    LinearLayout lvlNotfound;

    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    User user;
    List<PaymentItem> paymentList = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        ButterKnife.bind(this, view);
        sessionManager = new SessionManager(getActivity());
        custPrograssbar = new CustPrograssbar();
        user = sessionManager.getUserDetails();
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity());
        mLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recycleviewHistory.setLayoutManager(mLayoutManager2);
        recycleviewHistory.setItemAnimator(new DefaultItemAnimator());

        getHistory();
        return view;
    }


    private void getHistory() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().walletReport(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }

    private void getPayment() {
        custPrograssbar.prograssCreate(getActivity());

        JSONObject jsonObject = new JSONObject();
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().getPaymentList(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

    private void getAddWallet() {
        custPrograssbar.prograssCreate(getActivity());

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("wallet", walletAmount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody bodyRequest = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Call<JsonObject> call = APIClient.getInterface().walletUp(bodyRequest);
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "3");

    }

    @Override
    public void callback(JsonObject result, String callNo) {

        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                Wallet wallet = gson.fromJson(result.toString(), Wallet.class);
                if (wallet.getResult().equalsIgnoreCase("true")) {
                    if (wallet.getWalletitem().size() != 0) {
                        recycleviewHistory.setVisibility(View.VISIBLE);
                        lvlNotfound.setVisibility(View.GONE);
                        recycleviewHistory.setAdapter(new HstoryAdepter(wallet.getWalletitem()));
                    } else {
                        recycleviewHistory.setVisibility(View.GONE);
                        lvlNotfound.setVisibility(View.VISIBLE);
                    }

                    txtWallet.setText("Balance " + sessionManager.getStringData(SessionManager.currency) + wallet.getWallets());
                    user.setWallet(wallet.getWallets());
                    sessionManager.setUserDetails(user);
                }

            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                Payment payment = gson.fromJson(result.toString(), Payment.class);
                paymentList = new ArrayList<>();

                for (int i = 0; i < payment.getData().size(); i++) {
                    if (payment.getData().get(i).getpShow().equalsIgnoreCase("1")) {
                        paymentList.add(payment.getData().get(i));
                    }
                }

                bottonPaymentList();

            } else if (callNo.equalsIgnoreCase("3")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                if (response.getResult().equalsIgnoreCase("true")) {
                    getHistory();
                }
            }
        } catch (Exception e) {
            Log.e("Error", "-->" + e.getMessage());
        }
    }

    double walletAmount = 0;

    public void bottonPaymentList() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getLayoutInflater().inflate(R.layout.custome_payment_wallet, null);
        LinearLayout listView = sheetView.findViewById(R.id.lvl_list);
        EditText edAmount = sheetView.findViewById(R.id.ed_amount);
        for (int i = 0; i < paymentList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            PaymentItem paymentItem = paymentList.get(i);
            View view = inflater.inflate(R.layout.custome_paymentitem, null);
            ImageView imageView = view.findViewById(R.id.img_icon);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            TextView txtSubtitel = view.findViewById(R.id.txt_subtitel);
            txtTitle.setText("" + paymentList.get(i).getmTitle());
            txtSubtitel.setText("" + paymentList.get(i).getSubtitle());
            Glide.with(getActivity()).load(APIClient.baseUrl + "/" + paymentList.get(i).getmImg()).thumbnail(Glide.with(getActivity()).load(R.drawable.emty)).into(imageView);
            int finalI = i;
            view.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(edAmount.getText().toString())) {
                    Utility.paymentId = paymentList.get(finalI).getmId();
                    walletAmount = Double.parseDouble(edAmount.getText().toString());
                    try {
                        switch (paymentList.get(finalI).getmTitle()) {
                            case "Razorpay":
                                int temtoal = (int) Math.round(walletAmount);
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), RazerpayActivity.class).putExtra("amount", temtoal).putExtra("detail", paymentItem));
                                break;

                            case "Paypal":
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), PaypalActivity.class).putExtra("amount", walletAmount).putExtra("detail", paymentItem));
                                break;
                            case "Stripe":
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), StripPaymentActivity.class).putExtra("amount", walletAmount).putExtra("detail", paymentItem));
                                break;
                            case "FlutterWave":
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), FlutterwaveActivity.class).putExtra("amount", walletAmount));
                                break;
                            case "Paytm":
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), PaytmActivity.class).putExtra("amount", walletAmount));
                                break;
                            case "SenangPay":
                                mBottomSheetDialog.cancel();
                                startActivity(new Intent(getActivity(), SenangpayActivity.class).putExtra("amount", walletAmount).putExtra("detail", paymentItem));
                                break;
                            case "PayStack":
                            int temtoal1 = (int) Math.round(walletAmount);
                                mBottomSheetDialog.cancel();
                            startActivity(new Intent(getActivity(), PaystackActivity.class).putExtra("amount", temtoal1).putExtra("detail", paymentItem));
                                break;
                            default:
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    edAmount.setError("ENTER AMOUNT");
                }

            });
            listView.addView(view);
        }
        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    @OnClick({R.id.addpayment})
    public void onBindClick(View view) {
        if (view.getId() == R.id.addpayment) {
            getPayment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paymentsucsses == 1) {
            paymentsucsses = 0;
            getAddWallet();

        }
    }

    public class HstoryAdepter extends RecyclerView.Adapter<HstoryAdepter.ViewHolder> {
        private List<WalletitemItem> itmeList;

        public HstoryAdepter(List<WalletitemItem> itmeList) {
            this.itmeList = itmeList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            Log.e("position", "" + position);
            WalletitemItem item = itmeList.get(position);
            holder.txtMsg.setText("" + item.getMessage());
            holder.txtStatus.setText("" + item.getStatus());
            holder.txtTotle.setText(sessionManager.getStringData(SessionManager.currency) + item.getAmt());

        }

        @Override
        public int getItemCount() {
            return itmeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txt_msg)
            TextView txtMsg;
            @BindView(R.id.txt_status)
            TextView txtStatus;
            @BindView(R.id.txt_totle)
            TextView txtTotle;


            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}