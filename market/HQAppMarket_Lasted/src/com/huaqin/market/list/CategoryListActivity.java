package com.huaqin.market.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.android.market.sdk.bean.Sort;
import com.huaqin.market.MarketBrowser;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.model.Image2;
import com.huaqin.market.ui.MyGridView;
import com.huaqin.market.utils.CachedThumbnails;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.FileManager;
import com.huaqin.market.utils.OptionsMenu;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class CategoryListActivity extends Activity
	implements OnItemClickListener {
	
	private static final int ACTION_CATEGORY_LIST = 0;
	private static final int ACTION_CATEGORY_ICON = 1;
	private static final int ACTION_NETWORK_ERROR = 2;
	
	private static final int DIALOG_NETWORK_ERROR = 100;
	
	private int nCategoryId;
	private boolean bBusy;
	private Hashtable<Integer, Boolean> mIconStatusMap;
	private Hashtable<Integer, Drawable> mThumbMap;
	
	private View mLoadingIndicator;
	private ImageView mLoadingAnimation;
	private AnimationDrawable loadingAnimation;
	private CategoryAdapter mAdapter;
	
	private IMarketService mMarketService;
	private Request mCurrentRequest;
	private Handler mHandler;
	private MyGridView  gridview;
	private ArrayList<HashMap<String, Object>> memuList;
	private Context mContext;
	public CategoryListActivity() {
//		mAdapter = new CategoryAdapter[2];
		nCategoryId = 0;
		bBusy = false;
		mIconStatusMap = new Hashtable<Integer, Boolean>();
		mThumbMap = new Hashtable<Integer, Drawable>();
		gridview = null;
		memuList = new ArrayList<HashMap<String, Object>>();
		mContext = this;
/*		mScrollListener = new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case SCROLL_STATE_IDLE:
					bBusy = false;
					int start = view.getFirstVisiblePosition();
					int counts = view.getChildCount();
					int position = 0;
					
					for (int i = 0; i < counts; i++) {
						position = start + i;
						
						if (!mIconStatusMap.containsKey(Integer.valueOf(position))) {
							ViewHolder viewHolder = 
								(ViewHolder) view.getChildAt(i).getTag();
							if (viewHolder != null) {
								int id = (int) mAdapter.getItemId(position);
								Drawable drawable = getThumbnail(position, id);
								viewHolder.mCategotyThumb.setImageDrawable(drawable);
							}
						}
					}
					break;
					
				case SCROLL_STATE_TOUCH_SCROLL:
				case SCROLL_STATE_FLING:
					if (!bBusy) {
						clearPendingThumbRequest();
						bBusy = true;
					}
					break;
					
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				return;
			}
		};*/
	}
	
	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case ACTION_CATEGORY_LIST:
					@SuppressWarnings("unchecked")
					ArrayList<Sort> cateList = (ArrayList<Sort>) msg.obj;
					
					Log.v("asd", "Sort ArrayList = "+cateList.size());
					for(int i=0;i<cateList.size();i++){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("ItemImage", (Drawable)getThumbnail(i,cateList.get(i)));
						map.put("ItemText", cateList.get(i).getName());
						map.put("ItemInfo", cateList.get(i));
						memuList.add(map);
					}
//					mAdapter = new CategoryAdapter(
//							CategoryListActivity.this,
//							cateList);
//					mListView.setAdapter(mAdapter);
					Log.v("asd", "memuList = "+memuList.size());
					mAdapter = new CategoryAdapter(mContext, memuList); // 对应R的Id

					//添加Item到网格中
					gridview.setAdapter(mAdapter);
					gridview.setVisibility(View.VISIBLE);
					mLoadingIndicator.setVisibility(View.GONE);
					if(nCategoryId == 0){
						gridview.setVisibility(View.GONE);
					}
//					mListView.setVisibility(View.VISIBLE);
					break;
					
				case ACTION_CATEGORY_ICON:
					Image2 icInfo = (Image2) msg.obj;
					
					if (icInfo.mAppIcon != null) {
						mThumbMap.put(icInfo._id, icInfo.mAppIcon);
						if (CachedThumbnails.bAllowBufferIcon) {
							FileManager.writeCateIconToFile(CategoryListActivity.this,
									icInfo._id, icInfo.mAppIcon);
						}
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
						
					}
					break;
					
				case ACTION_NETWORK_ERROR:
					mLoadingIndicator.setVisibility(View.GONE);
		//			mListView.setVisibility(View.VISIBLE);
					gridview.setVisibility(View.VISIBLE);
//					showDialog(DIALOG_NETWORK_ERROR);
					Toast.makeText(mContext, mContext.getString(R.string.error_network_low_speed), Toast.LENGTH_LONG).show();
					break;
					
				default:
					break;
				}
			}
		};
	}
	
	private void initListView() {
		// TODO Auto-generated method stub
		mLoadingIndicator = findViewById(R.id.fullscreen_loading_indicator);
		mLoadingAnimation = 
			(ImageView) mLoadingIndicator.findViewById(R.id.fullscreen_loading);
		mLoadingAnimation.setBackgroundResource(R.anim.loading_anim);
		loadingAnimation = (AnimationDrawable) mLoadingAnimation.getBackground();
		mLoadingAnimation.post(new Runnable(){
			@Override     
			public void run() {
				loadingAnimation.start();     
			}                     
		});  
		gridview = (MyGridView ) findViewById(R.id.gridview);
//		mAdapter = new CategoryAdapter(this, memuList,R.layout.cate_list_menuitem, new String[] { "ItemImage", "ItemText" }, // 对应map的Key
//				new int[] { R.id.ItemImage, R.id.ItemText }); // 对应R的Id
//		
//		//添加Item到网格中
//		gridview.setAdapter(mAdapter);
		
//		mListView = getListView();
//		mListView.setScrollbarFadingEnabled(true);
		
		View emptyView = findViewById(R.id.low_speed);
		TextView tvRefresh = (TextView) emptyView.findViewById(R.id.lowspeed_refresh);
		tvRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Log.v("asd", "mCurrentRequest ="+mCurrentRequest);
//				Log.v("asd", "mListView ="+mListView);
//				inflateCategoryList();
				if (mCurrentRequest != null) {
					mLoadingIndicator.setVisibility(View.VISIBLE);
//					mListView.setVisibility(View.GONE);
					mMarketService.getSortsList(mCurrentRequest);
				}
			}
		});
//		mListView.setEmptyView(emptyView);
		
//		mListView.setOnItemClickListener(this);
//		mListView.setOnScrollListener(mScrollListener);
		gridview.setEmptyView(emptyView);
		gridview.setOnItemClickListener(this);  

		inflateCategoryList();

	}

	private void inflateCategoryList() {
		// TODO Auto-generated method stub
		Request request = new Request(0, Constant.TYPE_CATEGORY_LIST);
		Object[] params = new Object[1];
		
		params[0] = Integer.valueOf(nCategoryId);

		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Log.v("asd", "ACTION_CATEGORY_LIST data= "+data);
					Message msg = Message.obtain(mHandler, ACTION_CATEGORY_LIST, data);
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
		mMarketService.getSortsList(request);
	}
	
	private void addThumbnailRequest(int id, String thumbUrl) {
		// TODO Auto-generated method stub
		Request request = new Request(0L, Constant.TYPE_CATE_ICON);
		Object[] params = new Object[2];
		
		params[0] = Integer.valueOf(id);
		params[1] = thumbUrl;
		request.setData(params);
		request.addObserver(new Observer() {

			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				if (data != null) {
					Message msg = Message.obtain(mHandler, ACTION_CATEGORY_ICON, data);
					mHandler.sendMessage(msg);
				}
			}
		});
		mCurrentRequest = request;
		mMarketService.getCategoryIcon(request);
	}

	private void clearPendingThumbRequest() {
		// TODO Auto-generated method stub
		Iterator<Integer> iterator = mIconStatusMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (!mIconStatusMap.get(value).booleanValue()) {
				iterator.remove();
			}
		}
		mMarketService.clearThumbRequest(MarketService.THREAD_THUMB);
	}
	
	public Drawable getThumbnail(int position, Sort sort) {
		// TODO Auto-generated method stub
		boolean bThumbExists = mIconStatusMap.containsKey(Integer.valueOf(position));
		boolean bThumbCached = false;
		
		if (bBusy && !bThumbExists) {
			return CachedThumbnails.getDefaultIcon(this);
		}
		
		if (bThumbExists) {
			bThumbCached = mIconStatusMap.get(Integer.valueOf(position)).booleanValue();
		}
		
		if (mThumbMap.containsKey(sort.getSortId())) {
			if (!bThumbExists || (bThumbExists && !bThumbCached)) {
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			}
			return mThumbMap.get(sort.getSortId());
		} else {
			Drawable drawable = null;
			
			if (CachedThumbnails.bAllowBufferIcon) {
				drawable = FileManager.readCateIconFromFile(this, sort.getSortId());
			}
			if (drawable == null) {
				if (bThumbExists && !bThumbCached) {
					// cause thumb record existed but not cached
					// do not sent thumb request again, just return default icon
					return CachedThumbnails.getDefaultIcon(this);
				} else {
					// cause thumb record not existed
					// or thumb not cached yet,
					// set cached flag as false, and send thumb request
					mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(false));
					
					// check image URL validity
					String url = sort.getIcon();
					if (url.startsWith("http://")) {
						addThumbnailRequest(sort.getSortId(), sort.getIcon());
					} else {
						// push default icon to buffer
						mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
						mThumbMap.put(sort.getSortId(), CachedThumbnails.getDefaultIcon(this));
					}
					return CachedThumbnails.getDefaultIcon(this);
				}
			} else {
				mThumbMap.put(sort.getSortId(), drawable);
				mIconStatusMap.put(Integer.valueOf(position), Boolean.valueOf(true));
			}
			
			return drawable;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mMarketService = MarketService.getServiceInstance(this);
		if (savedInstanceState == null) {
			nCategoryId = getIntent().getIntExtra("cateId", 0);
		} else {
			nCategoryId = savedInstanceState.getInt("cateId", 0);
		}
		
		setContentView(R.layout.cate_list);
		
		initHandler();
		initListView();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG_NETWORK_ERROR) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this.getParent());
			builder.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dlg_network_error_title)
				.setMessage(R.string.dlg_network_error_msg)
				.setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (mCurrentRequest.getType()) {
						case Constant.TYPE_CATEGORY_LIST:
							mMarketService.getSortsList(mCurrentRequest);
							break;
						case Constant.TYPE_CATE_ICON:
							mMarketService.getCategoryIcon(mCurrentRequest);
							break;
						default:
							break;
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, null);
			return builder.create();
		}
		return null;
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		boolean bRet = super.onCreateOptionsMenu(menu);
//		if (bRet) {
//			bRet = OptionsMenu.onCreateOptionsMenu(menu);
//		}
//		return bRet;
//	}
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		startActivity(new Intent(CategoryListActivity.this, OptionsMenu.class));
    		overridePendingTransition(R.anim.fade, R.anim.hold);
    	}
    	return super.onKeyUp(keyCode, event);
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mThumbMap != null) {
			mThumbMap.clear();
		}
		if (mIconStatusMap != null) {
			mIconStatusMap.clear();
		}
		if (mAdapter != null) {
			mAdapter.clear();
		}
//		if (mListView != null) {
//			mListView.setAdapter(null);
//		}
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		Log.v("asd", "CategoryListActivity onResume");
		super.onResume();
	}
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view,
//			int position, long id) {
//		if(position < mAdapter.getCount()) {
//			Category category = mAdapter.getItem(position);
//			Intent intent = new Intent(this, CategoryInfoActivity.class);
//			intent.putExtra("cateId", category.getCateId());
//			/*************Added-s by JimmyJin for Pudding Project**************/
//			intent.putExtra("type",0);
//			/*************Added-s by JimmyJin for Pudding Project**************/
//			startActivity(intent);
//		}
//	}
	@Override
	 public void onItemClick(AdapterView<?> adapter,//The AdapterView where the click happened    
	                                   View view,//The view within the AdapterView that was clicked   
	                                   int position,//The position of the view in the adapter   
	                                   long arg3//The row id of the item that was clicked   
	                                   ) {  
	     //在本例中arg2=arg3   
	    @SuppressWarnings("unchecked")
		HashMap<String, Object> item=(HashMap<String, Object>) adapter.getItemAtPosition(position);  

	     if(position < mAdapter.getCount()) {
//				Category category = mAdapter.getItem(position);
	    	 	Sort category = (Sort) memuList.get(position).get("ItemInfo");
//				Intent intent = new Intent(this, CategoryInfoActivity.class);
	    		Intent intent = new Intent(this, SlideViewPager.class);
	    		intent.putExtra("intentpage", MarketBrowser.TAB_CATEAPP);
				intent.putExtra("cateId", category.getSortId());
				/*************Added-s by JimmyJin for Pudding Project**************/
				intent.putExtra("type",1);
				/*************Added-s by JimmyJin for Pudding Project**************/
				startActivity(intent);
			}
	     
	 }  

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		return OptionsMenu.onOptionsItemSelected(this, item);
//	}
	
//	class CategoryAdapter extends BaseAdapter {
//    private  ArrayList<HashMap<String, Object>> objlist;
//    private  Category cate;
//	@Override
//	public int getCount() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//	@Override
//	public Object getItem(int arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public long getItemId(int arg0) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//	@Override
//	public View getView(int arg0, View arg1, ViewGroup arg2) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	}
	
	class CategoryAdapter extends ArrayAdapter<HashMap<String, Object>> {
		
		private LayoutInflater mInflater;
		
		public CategoryAdapter(Context context, ArrayList<HashMap<String, Object>> objects) {
			
			super(context, 0, objects);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return ((Sort)getItem(position).get("ItemInfo")).getSortId();
		}
		
		public String getThumbURL(int position) {
			// TODO Auto-generated method stub
			return ((Sort)getItem(position).get("ItemInfo")).getIcon();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Sort category = (Sort) getItem(position).get("ItemInfo");
			int cateId = category.getSortId();
			ViewHolder viewHolder = null;
			
			if (convertView == null ||
					!(convertView.getTag() instanceof ViewHolder)) {
				convertView = 
					mInflater.inflate(R.layout.cate_list_menuitem, parent, false);
				viewHolder = new ViewHolder();
				
				viewHolder.mListItem = (RelativeLayout) convertView.findViewById(R.id.cate_list_menu);
				viewHolder.mCategotyThumb = (ImageView) convertView.findViewById(R.id.ItemImage);
				viewHolder.mCategotyTitle = (TextView) convertView.findViewById(R.id.ItemText);
				viewHolder.mCategotyThumbTrey = (ImageView) convertView.findViewById(R.id.ItemImage_trey);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}		
//			if ((position % 2) == 0) {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_view);
//			} else {
//				viewHolder.mListItem.setBackgroundResource(R.drawable.bg_list_item_grey);
//			}
			Drawable drawable = mContext.getResources().getDrawable(R.drawable.icon_bg);
			viewHolder.mCategotyThumbTrey.setImageDrawable(drawable);
			viewHolder.mCategotyThumb.setTag(Integer.valueOf(cateId));
			viewHolder.mCategotyThumb.setImageDrawable(getThumbnail(position, category));

			viewHolder.mCategotyTitle.setText(category.getName());
			
			int width = RecommandAppListActivity.mWindowManager.getDefaultDisplay().getWidth();
			int height = RecommandAppListActivity.mWindowManager.getDefaultDisplay().getHeight();
//			Log.v("qwe", "width = "+width);
//			Log.v("qwe", "height = "+height);
//			int mHeight = (height - 180*height/800)/6;
			Resources r=getResources(); 
			float mHeight =r.getDimension(R.dimen.cate_listitem_size); 

//			Log.v("qwe", "mHeight = "+mHeight);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(width/2, (int)mHeight);
			convertView.setLayoutParams(lp);
			
			return convertView;
		}
		
		class ViewHolder {
			
			RelativeLayout mListItem;
			ImageView mCategotyThumb;
			ImageView mCategotyThumbTrey;
			TextView mCategotyTitle;
		}
	}/**/
}