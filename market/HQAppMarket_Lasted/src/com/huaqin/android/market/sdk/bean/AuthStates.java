package com.huaqin.android.market.sdk.bean;

public class AuthStates {
	
	private int authResult;
	
	private String accessToken;

	public AuthStates() {
		// TODO Auto-generated constructor stub
	}
	
	public AuthStates(int authResult, String accessToken) {
		super();
		this.authResult = authResult;
		this.accessToken = accessToken;
	}

	public int getAuthResult() {
		return authResult;
	}

	public void setAuthResult(int authResult) {
		this.authResult = authResult;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

}
