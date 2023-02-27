package com.grader.user.adepter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.activity.BookWheelerActivity;
import com.grader.user.activity.FlutterwaveActivity;
import com.grader.user.activity.PaypalActivity;
import com.grader.user.activity.PaystackActivity;
import com.grader.user.activity.PaytmActivity;
import com.grader.user.activity.RazerpayActivity;
import com.grader.user.activity.SenangpayActivity;
import com.grader.user.activity.StripPaymentActivity;
import com.grader.user.model.HistoryinfoItem;
import com.grader.user.model.PaymentItem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.utility.SessionManager;
import com.grader.user.utility.Utility;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.MyViewHolder> {


    private Context mContext;
    private List<HistoryinfoItem> historyinfo;
    private RecyclerTouchListener listener;
    SessionManager sessionManager;

    public interface RecyclerTouchListener {
        public void onClickPackageItem(HistoryinfoItem item, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.txt_date)
        TextView txtDate;
        @BindView(R.id.txt_type)
        TextView txtType;
        @BindView(R.id.txt_totle)
        TextView txtTotle;
        @BindView(R.id.txt_pickaddress)
        TextView txtPickaddress;
        @BindView(R.id.lvl_drop)
        LinearLayout lvlDrop;
        @BindView(R.id.txt_status)
        TextView txtStatus;
        @BindView(R.id.lvl_click)
        LinearLayout lvlClick;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public TripAdapter(Context mContext, List<HistoryinfoItem> historyinfo, final RecyclerTouchListener listener) {
        this.mContext = mContext;
        this.historyinfo = historyinfo;
        this.listener = listener;
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        HistoryinfoItem item = historyinfo.get(position);
        holder.txtDate.setText("" + item.getDateTime());
        holder.txtType.setText("" + item.getvType());
        holder.txtTotle.setText(sessionManager.getStringData(SessionManager.currency) + item.getPTotal());
        holder.txtPickaddress.setText(""+item.getPickAddress());
        holder.lvlDrop.removeAllViews();

        for (int i = 0; i < item.getParceldata().size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            View view = inflater.inflate(R.layout.custome_droplocation, null);
            TextView txtDropaddress = view.findViewById(R.id.txt_dropaddress);
            txtDropaddress.setText("" + item.getParceldata().get(i).getDropPointAddress());

            holder.lvlDrop.addView(view);
        }

        holder.txtStatus.setText("" + item.getOrderStatus());
        Glide.with(mContext).load(APIClient.baseUrl + "/" + item.getVImg()).thumbnail(Glide.with(mContext).load(R.drawable.emty)).into(holder.imgIcon);
        holder.lvlClick.setOnClickListener(v -> {
            listener.onClickPackageItem(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return historyinfo.size();
    }
}