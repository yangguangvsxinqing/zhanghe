package com.huaqin.market.ui;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.huaqin.market.R;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class AddCommentDetailActivity extends Activity 
	implements View.OnClickListener, RatingBar.OnRatingBarChangeListener  {
	
	private static final int INSERT_COMMENT_CONTENT = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	private static final int COMMENT_CONTENT_NULL = 3;
	private static final int COMMENT_CONTENT_OVERFLOW = 4;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	private static final int DIALOG_COMMENT_CONTENT_NULL = 101;
	private static final int DIALOG_COMMENT_CONTENT_OVERFLOW = 102;
//	private boolean addFlag;
	private String clientId;
	
//	private Button btn_back; // back button
	private Button btn_confirm_ok; // add comment ok
	private Button btn_confirm_cancel; // add comment cancel 
	private RatingBar mRatingBar;
	private EditText comment_content;
	
	private Handler mHandler;
	private Request mCurrentRequest;
	
	private IMarketService mMarketService;

	private Context mContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_comment_detail);
		mContext = this;
		String ranClientId = DeviceUtil.getClientId();
		SharedPreferences sharedPreferences = this.getPreferences(MODE_WORLD_WRITEABLE);
		clientId = sharedPreferences.getString("clientId", ranClientId);
		if(clientId.equals(ranClientId)) {
			sharedPreferences.edit().putString("clientId", clientId).commit();
		}
		mMarketService = MarketService.getServiceInstance(this);
		comment_content = (EditText)findViewById(R.id.comment_content);
//		btn_back = (Button)findViewById(R.id.btn_back_add_comment);
		btn_confirm_ok = (Button)findViewById(R.id.btn_confirm_comment);
		btn_confirm_cancel = (Button)findViewById(R.id.btn_cancel_comment);
		mRatingBar = (RatingBar)findViewById(R.id.comment_ratingbar);
		initListener();
		initHandler();
	}

	private void initListener() {
//		btn_back.setOnClickListener(this);
		btn_confirm_ok.setOnClickListener(this);
		btn_confirm_cancel.setOnClickListener(this);
		mRatingBar.setOnRatingBarChangeListener(this);
	}
	
	private void initHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case INSERT_COMMENT_CONTENT:
					Intent intent = new Intent();
					intent.setAction(AppInfoCommentsActivity.ADD_COMMENT);
					sendBroadcast(intent);
					finish();
					Toast.makeText(getApplicationContext(), R.string.add_app_comment_success, Toast.LENGTH_LONG).show();
					
					break;
				case COMMENT_CONTENT_OVERFLOW:
					showDialog(DIALOG_COMMENT_CONTENT_OVERFLOW);
					break;
				case ACTION_NETWORK_ERROR:
					if(!isFinishing())
//						showDialog(DIALOG_NETWORK_ERROR);
						Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
				case COMMENT_CONTENT_NULL:
					showDialog(DIALOG_COMMENT_CONTENT_NULL);
					break;
				
				default:
					break;
				}
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm_comment:
			String content = comment_content.getText().toString();
			if(content != null && !"".equals(content) && content.length() <= 250) {
//					btn_back.setClickable(false);
				btn_confirm_ok.setClickable(false);
//					btn_confirm_cancel.setClickable(false);
				Intent addCommentIntent = getIntent();
				Bundle bundle = addCommentIntent.getExtras();
				final int appId = bundle.getInt("appId");
				Request request = new Request(0, Constant.TYPE_ADD_APP_COMMENT);
				Object[] params = new Object[4];
				
				params[0] = Integer.valueOf(appId);
				params[1] = content;
				params[2] = Float.valueOf(mRatingBar.getRating());
				params[3] = clientId;
				request.setData(params);
				request.addObserver(new Observer() {
	
					@Override
					public void update(Observable observable, Object data) {
						// TODO Auto-generated method stub
						if (data != null) {
							Message msg = Message.obtain(mHandler, INSERT_COMMENT_CONTENT, data);
							msg.obj = Integer.valueOf(appId);
							mHandler.sendMessage(msg);
						} else {
							Request request = (Request) observable;
							if (request.getStatus() == Constant.STATUS_ERROR) {
								mHandler.sendEmptyMessage(ACTION_NETWORK_ERROR);
							}
						}
					}
				});
				mCurrentRequest = request;
				mMarketService.addAppComment(request);
				if(mCurrentRequest.getStatus() != Constant.STATUS_SUCCESS)
					btn_confirm_ok.setClickable(true);
			} else if(content != null && content.length() > 250) {
				mHandler.sendEmptyMessage(COMMENT_CONTENT_OVERFLOW);
			} else {
				mHandler.sendEmptyMessage(COMMENT_CONTENT_NULL);
			}
			finish();
			break;
//		case R.id.btn_back_add_comment:
		case R.id.btn_cancel_comment:
			finish();
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		final int numStars = ratingBar.getNumStars();
		if(mRatingBar.getNumStars() != numStars) {
			mRatingBar.setNumStars(numStars);
		}
		
		if(mRatingBar.getRating() != rating) {
			mRatingBar.setRating(rating);
        }
		
		final float ratingBarStepSize = ratingBar.getStepSize();
		if (mRatingBar.getStepSize() != ratingBarStepSize) {
			mRatingBar.setStepSize(ratingBarStepSize);
        }
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(AddCommentDetailActivity.this);
		switch (id) {
		case DIALOG_NETWORK_ERROR:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (mCurrentRequest != null) {
							mMarketService.addAppComment(mCurrentRequest);
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		case DIALOG_COMMENT_CONTENT_NULL:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_add_comment_content_null_title)
				.setMessage(R.string.dlg_add_comment_content_null_msg)
				.setPositiveButton(R.string.btn_ok, null);
				
			return builder.create();
		case DIALOG_COMMENT_CONTENT_OVERFLOW:
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_add_comment_content_overflow_title)
				.setMessage(R.string.dlg_add_comment_content_overflow_msg)
				.setPositiveButton(R.string.btn_ok, null);
			return builder.create();
		}
		return null;
	}
}
