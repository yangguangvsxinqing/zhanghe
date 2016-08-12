package com.huaqin.market.webservice;

import java.util.Observable;

public class Request extends Observable {

	private long _id;
	private boolean bAsync;
	private Object mData;
	private Object extra;
	private String errorMsg;
	private int status;
	private int type;

	public Request(long _id, int type) {

		this._id = _id;
		this.type = type;
		this.bAsync = true;
		this.errorMsg = null;
	}

	public Request(long _id, int type, boolean bAsync) {
		
		this(_id, type);
		this.bAsync = bAsync;
	}

	public long getId() {
		return _id;
	}

	public Object getData() {
		return mData;
	}

	public Object getExtra() {
		return extra;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public int getStatus() {
		return status;
	}

	public int getType() {
		return type;
	}

	public boolean isAsync() {
		return bAsync;
	}

	public void setData(Object data) {
		this.mData = data;
	}

	public void setExtra(Object extraObj) {
		this.extra = extraObj;
	}

	public void setErrorMsg(String error) {
		this.errorMsg = error;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void notifyObservers(Object data) {
		// TODO Auto-generated method stub
		setChanged();
		super.notifyObservers(data);
	}
}
