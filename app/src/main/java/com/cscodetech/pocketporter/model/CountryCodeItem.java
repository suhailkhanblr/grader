package com.grader.user.model;

import com.google.gson.annotations.SerializedName;

public class CountryCodeItem{

	@SerializedName("ccode")
	private String ccode;

	@SerializedName("id")
	private String id;

	@SerializedName("status")
	private String status;

	public String getCcode(){
		return ccode;
	}

	public String getId(){
		return id;
	}

	public String getStatus(){
		return status;
	}
}