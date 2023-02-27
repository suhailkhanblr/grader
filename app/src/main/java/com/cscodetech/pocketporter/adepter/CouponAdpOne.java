package com.grader.user.adepter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grader.user.R;
import com.grader.user.model.CouponItem;
import com.grader.user.retrofit.APIClient;
import com.grader.user.utility.SessionManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CouponAdpOne extends RecyclerView.Adapter<CouponAdpOne.MyViewHolder> {
    private Context mContext;
    private List<CouponItem> couponsList;
    private RecyclerTouchListener listener;
        private int amount;
    SessionManager sessionManager;
    public interface RecyclerTouchListener {
        public void onClickItem(View v, CouponItem coupon);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_coupon)
        TextView txtCoupon;
        @BindView(R.id.txt_apply)
        TextView txtApply;
        @BindView(R.id.txt_titel)
        TextView txtTitel;
        @BindView(R.id.txt_amount)
        TextView txtAmount;
        @BindView(R.id.txt_desc)
        TextView txtDesc;
        @BindView(R.id.imageView)
        ImageView imgCode;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }

    public CouponAdpOne(Context mContext, List<CouponItem> categoryList, final RecyclerTouchListener listener, int amount) {
        this.mContext = mContext;
        this.couponsList = categoryList;
        this.listener = listener;
        this.amount = amount;
        sessionManager=new SessionManager(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coupon1, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        CouponItem coupon = couponsList.get(position);
        Glide.with(mContext).load(APIClient.baseUrl + "/" + coupon.getCImg()).thumbnail(Glide.with(mContext).load(R.drawable.emty)).into(holder.imgCode);
        Log.e("amount",amount+" "+coupon.getMinAmt());
        if (amount < coupon.getMinAmt()) {
            holder.txtApply.setTextColor(mContext.getResources().getColor(R.color.colorgrey));
            holder.txtApply.setEnabled(false);
        } else {
            holder.txtApply.setEnabled(true);
            holder.txtApply.setTextColor(mContext.getResources().getColor(R.color.black));

        }

        holder.txtCoupon.setText("" + coupon.getCouponCode());
        holder.txtTitel.setText("" + coupon.getCouponTitle());
        holder.txtAmount.setText(sessionManager.getStringData(SessionManager.currency) + coupon.getCValue());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.txtDesc.setText(Html.fromHtml(coupon.getCDesc(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.txtDesc.setText(Html.fromHtml(coupon.getCDesc()));
        }
        makeTextViewResizable(holder.txtDesc, 3, "See More", true);
        holder.txtApply.setOnClickListener(v -> listener.onClickItem(v, coupon));
    }

    @Override
    public int getItemCount() {
        return couponsList.size();
    }


    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv,  expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv,  expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv,  expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {


            ssb.setSpan(new MySpannable(false){
                @Override
                public void onClick(View widget) {
                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "See Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, ".. See More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }
    public static class MySpannable extends ClickableSpan {

        private boolean isUnderline = true;

        /**
         * Constructor
         */
        public MySpannable(boolean isUnderline) {
            this.isUnderline = isUnderline;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(isUnderline);
            ds.setColor(Color.parseColor("#F28021"));
        }

        @Override
        public void onClick(View widget) {
        }
    }
}