package com.alert.redcolor.adapters;

public class TopAlert {
	private int count;
	private String area;
	
	public TopAlert(String area , int count) {
		this.area = area;
		this. count = count;
	}
	
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

}