package com.grader.user.adepter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.model.Wheeleritem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.utility.SessionManager;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.DebouncingOnClickListener;

public class WheelerAdapter extends RecyclerView.Adapter<WheelerAdapter.MyViewHolder> {


    double dis;
    private Context mContext;
    private List<Wheeleritem> wheeleritemList;
    private RecyclerTouchListener listener;
    SessionManager sessionManager;
    int prSelct;

    public interface RecyclerTouchListener {
        public void onClickWheelerItem(Wheeleritem item, int position);
        public void onClickWheelerInfo(Wheeleritem item, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_name)
        TextView txtName;
        @BindView(R.id.txt_time)
        TextView txtTime;
        @BindView(R.id.txt_price)
        TextView txtPrice;
        @BindView(R.id.lvl_click)
        LinearLayout lvlClick;
        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.img_imfo)
        ImageView imgImfo;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public WheelerAdapter(Context mContext, List<Wheeleritem> wheeleritemList, final RecyclerTouchListener listener, double dst) {
        this.mContext = mContext;
        this.wheeleritemList = wheeleritemList;
        this.listener = listener;
        this.dis = dst;
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wheeler, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Wheeleritem item = wheeleritemList.get(position);
        holder.txtName.setText(item.getTitle());
        Glide.with(mContext).load(APIClient.baseUrl + "/" + item.getImg()).thumbnail(Glide.with(mContext).load(R.drawable.emty)).into(holder.imgIcon);
        if(item.getIsAvailable().equalsIgnoreCase("0")){
            holder.txtTime.setText("Not Available");
        }else {
            double tempp = 0;
            if (dis <= item.getStartDistance()) {
                tempp = item.getStartPrice();
            } else {
                double km = dis - item.getStartDistance();
                tempp = item.getStartPrice() + (km * item.getAfterPrice());
            }
                Double time = dis * item.getTimeTaken();
                holder.txtPrice.setText("$" + new DecimalFormat("##.##").format(tempp));
                if (time < 60) {
                    holder.txtTime.setText(new DecimalFormat("##").format(time)+" mins");

                } else {
                    double tamp = time / 60;
                    holder.txtTime.setText(new DecimalFormat("##.##").format(tamp)+" Hours");
                }
        }


        if (item.isSelct()) {
            holder.txtName.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.txtTime.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.black));
            holder.imgIcon.setBackgroundResource(R.drawable.circlebg);
            holder.imgImfo.setVisibility(View.VISIBLE);
        } else {
            holder.imgIcon.setBackgroundResource(0);
            holder.txtName.setTextColor(mContext.getResources().getColor(R.color.colorgrey2));
            holder.txtTime.setTextColor(mContext.getResources().getColor(R.color.colorgrey2));
            holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.colorgrey2));
            holder.imgImfo.setVisibility(View.GONE);

        }

        holder.lvlClick.setOnClickListener(v -> {
            if(item.getIsAvailable().equalsIgnoreCase("0")){
                Log.e("tete","jflja");
            }else {
                if (item.isSelct()) {
                    item.setSelct(false);
                } else {
                    Wheeleritem itema = wheeleritemList.get(prSelct);
                    itema.setSelct(false);
                    wheeleritemList.set(prSelct, itema);
                    item.setSelct(true);
                    prSelct = position;
                    notifyDataSetChanged();
                }

                listener.onClickWheelerInfo(item, position);
            }

        });
        holder.imgImfo.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {

                listener.onClickWheelerItem(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wheeleritemList.size();
    }
}