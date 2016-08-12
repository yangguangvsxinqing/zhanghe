package com.huaqin.market.ui;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.huaqin.market.R;
import com.huaqin.market.model.Comment2;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class UpdateCommentDetailActivity extends Activity 
	implements View.OnClickListener, RatingBar.OnRatingBarChangeListener  {
	
	private static final int UPDATE_COMMENT_CONTENT = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	private static final int COMMENT_CONTENT_NULL = 3;
	private static final int COMMENT_CONTENT_OVERFLOW = 4;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	private static final int DIALOG_COMMENT_CONTENT_NULL = 101;
	private static final int DIALOG_COMMENT_CONTENT_OVERFLOW = 102;
	
	private Button mBackButton; // back button
	private Button mConfirmButton; // add comment ok
	private Button mCancelButton; // add comment cancel 
	private RatingBar mRatingBar;
	private EditText commentContent;
	
	private Handler mHandler;
	private Request mCurrentRequest;
	
	private Comment2 comment2;
	private Context mContext;
	private IMarketService mMarketService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_comment_detail);
		mContext = this;
		mMarketService = MarketService.getServiceInstance(this);
		commentContent = (EditText)findViewById(R.id.update_comment_content);
		mBackButton = (Button)findViewById(R.id.btn_back_update_comment);
		mConfirmButton = (Button)findViewById(R.id.btn_confirm_update_comment);
		mCancelButton = (Button)findViewById(R.id.btn_cancel_update_comment);
		mRatingBar = (RatingBar)findViewById(R.id.update_comment_ratingbar);
		initView();
		initListener();
		initHandler();
	}
	
	private void initView() {
		Intent updateIntent = getIntent();
		comment2 = (Comment2)updateIntent.getExtras().get("comment2");
		commentContent.setText(comment2.getContent());
		mRatingBar.setRating(comment2.getStars() * 1.0f);
	}

	private void initListener() {
		mBackButton.setOnClickListener(this);
		mConfirmButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		mRatingBar.setOnRatingBarChangeListener(this);
	}
	
	private void initHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_COMMENT_CONTENT:
					finish();
					Toast.makeText(getApplicationContext(), R.string.update_app_comment_success, Toast.LENGTH_LONG).show();
					break;
				case COMMENT_CONTENT_OVERFLOW:
					showDialog(DIALOG_COMMENT_CONTENT_OVERFLOW);
					break;
				case ACTION_NETWORK_ERROR:
					if(!isFinishing())
						Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
//						showDialog(DIALOG_NETWORK_ERROR);
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
		case R.id.btn_confirm_update_comment:
			String content = commentContent.getText().toString();
			if(content != null && !"".equals(content) && content.length() <= 250) {
				Request request = new Request(0, Constant.TYPE_UPDATE_APP_COMMENT);
				Object[] params = new Object[3];
				params[0] = comment2;
				params[1] = content;
				params[2] = Float.valueOf(mRatingBar.getRating());
				request.setData(params);
				request.addObserver(new Observer() {
	
					@Override
					public void update(Observable observable, Object data) {
						// TODO Auto-generated method stub
						if (data != null) {
							Message msg = Message.obtain(mHandler, UPDATE_COMMENT_CONTENT, data);
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
				mMarketService.updateAppComment(request);
			} else if(content != null && content.length() > 250) {
				mHandler.sendEmptyMessage(COMMENT_CONTENT_OVERFLOW);
			} else {
				mHandler.sendEmptyMessage(COMMENT_CONTENT_NULL);
			}
			break;
		case R.id.btn_back_update_comment:
		case R.id.btn_cancel_update_comment:
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
							mMarketService.updateAppComment(mCurrentRequest);
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
