package com.grader.user.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainData {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("webname")
    @Expose
    private String webname;
    @SerializedName("weblogo")
    @Expose
    private String weblogo;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("pdboy")
    @Expose
    private String pdboy;
    @SerializedName("one_key")
    @Expose
    private String oneKey;
    @SerializedName("one_hash")
    @Expose
    private String oneHash;
    @SerializedName("d_key")
    @Expose
    private String dKey;
    @SerializedName("d_hash")
    @Expose
    private String dHash;
    @SerializedName("scredit")
    @Expose
    private String scredit;
    @SerializedName("rcredit")
    @Expose
    private String rcredit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWebname() {
        return webname;
    }

    public void setWebname(String webname) {
        this.webname = webname;
    }

    public String getWeblogo() {
        return weblogo;
    }

    public void setWeblogo(String weblogo) {
        this.weblogo = weblogo;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPdboy() {
        return pdboy;
    }

    public void setPdboy(String pdboy) {
        this.pdboy = pdboy;
    }

    public String getOneKey() {
        return oneKey;
    }

    public void setOneKey(String oneKey) {
        this.oneKey = oneKey;
    }

    public String getOneHash() {
        return oneHash;
    }

    public void setOneHash(String oneHash) {
        this.oneHash = oneHash;
    }

    public String getdKey() {
        return dKey;
    }

    public void setdKey(String dKey) {
        this.dKey = dKey;
    }

    public String getdHash() {
        return dHash;
    }

    public void setdHash(String dHash) {
        this.dHash = dHash;
    }

    public String getScredit() {
        return scredit;
    }

    public void setScredit(String scredit) {
        this.scredit = scredit;
    }

    public String getRcredit() {
        return rcredit;
    }

    public void setRcredit(String rcredit) {
        this.rcredit = rcredit;
    }

}
