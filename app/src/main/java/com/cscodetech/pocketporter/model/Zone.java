package com.grader.user.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Zone{
	@SerializedName("Main_data")
	@Expose
	private MainData mainData;

	@SerializedName("Zone")
	private List<ZoneItem> zones;

	public List<ZoneItem> getZones(){
		return zones;
	}

	public void setZones(List<ZoneItem> zones) {
		this.zones = zones;
	}

	public MainData getMainData() {
		return mainData;
	}

	public void setMainData(MainData mainData) {
		this.mainData = mainData;
	}
}