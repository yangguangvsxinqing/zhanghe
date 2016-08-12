package com.huaqin.market.webservice;

import java.net.SocketException;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.Comment;
import com.huaqin.android.market.sdk.bean.HotWordList;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.android.market.sdk.bean.NewSubject;
import com.huaqin.android.market.sdk.bean.NewTopic;
import com.huaqin.android.market.sdk.bean.Partner;
import com.huaqin.android.market.sdk.bean.Sort;
import com.huaqin.android.market.sdk.bean.UpdateStates;
import com.huaqin.android.market.sdk.bean.appNameList;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.model.Comment2;
import com.huaqin.market.model.Image2;
import com.huaqin.market.model.Image3;
import com.huaqin.market.utils.Constant;

public class RequestHandler extends Thread {

	private static final String THREAD_NAME = "RequestHandler";
	
	public boolean bIsRunning;
	private int nThreadId;
	private MarketService mService;
	private MarketServiceAgent mAgent;

	public RequestHandler(MarketService service, int threadId) {
		// set thread name according to thread id
		super((THREAD_NAME + threadId));
		
		this.nThreadId = threadId;
		this.mService = service;
		this.bIsRunning = true;
		this.mAgent = mService.getAgentInstance();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (!bIsRunning) {
				continue;
			}
			Request request = null;
			Object[] params = null;
			try {
				request = (Request) mService.popRequest(nThreadId);
				if (request == null) {
					continue;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			ArrayList<Application> appList = null;
			int startIndex;
			int categoryId;
			int appId;
			int download;
			String userID;
			String content;
			String rversion;
			String phoneModel;
			String pageName;
			float stars;
			String clientId;
			String subjId;
			String up;
			String down;
			String newFlag;
			
			switch (request.getType()) {
			case Constant.TYPE_APP_RECOMMAND_LIST:
			case Constant.TYPE_APP_NEW_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getAppList(request.getType(), startIndex);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_COMMENT_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					appId = ((Integer)params[0]).intValue();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						ArrayList<Comment> commentList = 
							mAgent.getCommentList(appId, startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			// add comment
			case Constant.TYPE_ADD_APP_COMMENT:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					appId = ((Integer)params[0]).intValue();
					content = (String)params[1];
					stars = ((Float)params[2]).floatValue();
					clientId = (String)params[3];
					
					try {
						Comment comment= 
							mAgent.addComment(appId, content, stars, clientId);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(comment);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			// update comment
			case Constant.TYPE_UPDATE_APP_COMMENT:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					content = (String)params[1];
					stars = ((Float)params[2]).floatValue();
					Comment2 comment2 = (Comment2)params[0];
					Comment comment = new Comment();
					comment.setAppId(comment2.getAppId());
					comment.setClientId(comment2.getClientId());
					comment.setCommentId(comment2.getCommentId());
					comment.setContent(content);
					comment.setCreateTime(comment2.getCreateTime());
					comment.setDeviceId(comment2.getDeviceId());
					comment.setMemberId(comment2.getMemberId());
					comment.setNickname(comment2.getNickName());
					comment.setStars((int)stars);
					comment.setSubscriberId(comment2.getSubscribeId());
					try {
						Comment commentObj= 
							mAgent.updateComment(comment);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_POST_MARKET_COMMENT:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					userID = (String)params[0];
					content = (String)params[1];
					try {
						String commentObj= 
							mAgent.postMarketComment(userID,content);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_POST_VERSION_REGISTER:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					userID = (String)params[0];
					rversion = (String)params[1];
					try {
						String commentObj= 
							mAgent.postVersion(userID,rversion);
						Log.v("TYPE_POST_VERSION_REGISTER","rversion ="+commentObj);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_POST_PARTNER:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					phoneModel = (String)params[0];
					rversion = (String)params[1];
					try {
						Partner commentObj= 
							mAgent.postPartner(phoneModel,rversion);
						Log.v("TYPE_POST_PARTNER", "commentObj ="+commentObj);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
				
			case Constant.TYPE_POST_PV:
				Log.v("TYPE_POST_PV", "request.getData() ="+request.getData());
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					phoneModel = (String)params[0];
					rversion = (String)params[1];
					pageName = (String)params[2];
					try {
						String commentObj= 
								mAgent.postPV(phoneModel,rversion,pageName);
							Log.v("TYPE_POST_PV", "commentObj ="+commentObj);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_ADD_FAVORITE:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					Log.v("asd", "params = "+params[0]);
					Log.v("asd", "params = "+params[1]);
					Log.v("asd", "params = "+params[2]);
					
					subjId = (String)params[0];
					up = (String)params[1];
					down = (String)params[2];
					try {
						String commentObj= 
							mAgent.postSubjComment(subjId,up,down);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(commentObj);
					} catch (SocketException e) {
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_RELATED_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					appId = ((Integer)params[0]).intValue();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getRelatedAppList(appId, startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SUBJECT_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						ArrayList<NewSubject> subList =
							mAgent.getSubjectsList(startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(subList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SUBJECT_TOPIC_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					int subjectId = ((Integer)params[0]).intValue();
					startIndex = ((Integer)params[1]).intValue();

					try {
						ArrayList<NewTopic> topicList =
							mAgent.getTopicListOfSubject(subjectId, startIndex);
						Log.v("asd", "TYPE_SUBJECT_TOPIC_LIST topicList = "+topicList.size());
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(topicList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_RANKING_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					int rankType = ((Integer)params[0]).intValue();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getRankingAppList(rankType, startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SORT_APP_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getSortAppList(startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SORT_GAME_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getSortGameList(startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_CATEGORY_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					categoryId = ((Integer)params[0]).intValue();
					
					try {
						ArrayList<Sort> cateList =
							mAgent.getSortsList(categoryId);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(cateList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_CATE_APP_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					categoryId = ((Integer)params[0]).intValue();
					int sortType = ((Integer)params[1]).intValue();
					startIndex = ((Integer)params[2]).intValue();
					int type = ((Integer)params[3]).intValue();
					Log.v("asd", "TYPE_CATE_APP_LIST type ="+type);
					Log.v("asd", "TYPE_CATE_APP_LIST categoryId ="+categoryId);
					try {
//						appList = mAgent.getCategoryAppList(categoryId, sortType, startIndex);
						appList = mAgent.getTypeList(type, categoryId, sortType, startIndex);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.LIST_VIEWPAGER_FLAG:
					try {
						newFlag = mAgent.getNewSubFlag();
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(newFlag);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				break;
			case Constant.TYPE_SEARCH_HOTWORDS:
				try {
					HotWordList hotList = mAgent.getSearchHotWords();
					
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(hotList);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				break;
			case Constant.TYPE_SEARCH_DATABASE:
				try {
					appNameList appNameList = mAgent.getDatabase();
					
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(appNameList);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				break;
			case Constant.TYPE_SEARCH_APP_LIST:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					String keyWord = ((String) params[0]);
					startIndex = ((Integer)params[1]).intValue();
					
					try {
						appList = mAgent.getSearchResultList(keyWord, startIndex);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_TOP_APP_ICON:
				try {
					Drawable[] drawable = mAgent.getTopAppIcon();
					
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(drawable);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				break;
			case Constant.TYPE_APP_ICON:
			case Constant.TYPE_SUBS_ICON:
			case Constant.TYPE_SUBS_TOPIC_IMAGE:
			case Constant.TYPE_CATE_ICON:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					String imgUrl = (String) params[1];
					Image2 img = new Image2();
					
					try {
						img._id = ((Integer) params[0]).intValue();
						Log.v("TYPE_CATE_ICON", "imgUrl ="+imgUrl);
						img.mAppIcon = mAgent.getAppIcon(imgUrl);
						Log.v("TYPE_CATE_ICON", "img.mAppIcon ="+img.mAppIcon);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(img);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_RELATE_APP_ICON:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					String imgUrl = (String) params[1];
					Image3 img = new Image3();
					
					try {
						img._id = ((Integer) params[0]).intValue();
						img.mAppIcon = mAgent.getAppIcon(imgUrl);
						img.postion = ((Integer) params[2]).intValue();
						Log.v("asd", "img._id = "+img._id);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(img);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SUBS_TOPIC_THUMB:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					String imgUrl = (String) params[1];
					Image2 img = new Image2();
					appId = ((Integer) params[2]).intValue();
					
					try {
						img._id = ((Integer) params[0]).intValue();
						Log.v("Constant.TYPE_SUBS_TOPIC_THUMB", "img._id ="+img._id);
						Log.v("Constant.TYPE_SUBS_TOPIC_THUMB", "appId ="+appId);
						Log.v("Constant.TYPE_SUBS_TOPIC_THUMB", "imgUrl ="+imgUrl);
						img.mAppIcon = mAgent.getTopicAppIcon(appId, imgUrl);
						Log.v("Constant.TYPE_SUBS_TOPIC_THUMB", "img.mAppIcon ="+img.mAppIcon);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(img);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_DETAIL:
				if (request.getData() != null) {
					appId = ((Integer) request.getData()).intValue();
					
					try {
						Application app =
							 mAgent.getAppDetail(appId);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(app);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_RELATE:
				if (request.getData() != null) {
					download = ((Integer) request.getData()).intValue();
					try {
						ArrayList<Application> app =
							 mAgent.getAroundAppList(download);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(app);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_SUBS_TOPIC_APP:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
					appId = ((Integer) params[1]).intValue();
					Object[] data = new Object[2];
					
					try {
						data[0] = ((Integer) params[0]).intValue();
						data[1] = mAgent.getTopicAppDetail(appId);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(data);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_APP_PREVIEW:
				if (request.getData() != null) {
					String[] urls = (String[]) request.getData();
					
					try {
						ArrayList<Drawable> previews = mAgent.getAppPreviews(urls);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(previews);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_CHECK_SELF_UPDATE:
				if (request.getData() != null) {
					int version = ((Integer) request.getData()).intValue();
					
					try {
						UpdateStates update = mAgent.checkSelfUpdate(version);
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(update);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.TYPE_CHECK_USER_NOTIFY:
					try {
						String notice = mAgent.getNotice();
						
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(notice);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				break;	
			case Constant.TYPE_CHECK_APP_UPDATE:
				if (request.getData() != null) {
					InstalledApp[] apps = (InstalledApp[]) request.getData();
//					Log.v("asd", "TYPE_CHECK_APP_UPDATE");
//					Log.v("asd", "apps = "+apps.length);
//					Log.v("asd", "apps = "+apps);
					try {
						appList = mAgent.checkAppUpdate(apps);
//						Log.v("asd", "appList = "+appList.size());
						request.setStatus(Constant.STATUS_SUCCESS);
						
						request.notifyObservers(appList);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
				/*************Added-s by JimmyJin**************/
			case Constant.TYPE_USER_INFO:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
//					appId = ((Integer) params[1]).intValue();
					String mDeviceId = null;
					String mSubsId = null;	
					String mSoftId = null;
					String mMessageId =	 null;
					String mDeviceModelId = null;
					String UserId = null;
					
					try {
						mDeviceId = (String)params[0];
						mSubsId = (String)params[1];
						mSoftId = (String)params[2]; 
						mMessageId = (String)params[3]; 
						mDeviceModelId = (String)params[4]; 
//						Log.v("RequestHandler","JimmyJin mDeviceId="+mDeviceId);
//						Log.v("RequestHandler","JimmyJin mSubsId="+mSubsId);
//						Log.v("RequestHandler","JimmyJin mSoftId="+mSoftId);
//						Log.v("RequestHandler","JimmyJin mMessageId="+mMessageId);
//						Log.v("RequestHandler","JimmyJin mDeviceModelId="+mDeviceModelId);
						UserId = mAgent.userRegister(mDeviceId,
								mSubsId,
								mSoftId,
								mMessageId,
								mDeviceModelId);
						Log.v("RequestHandler","JimmyJin UserId="+UserId);
						request.setStatus(Constant.STATUS_SUCCESS);
						request.notifyObservers(UserId);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
				/*************Added-e by JimmyJin**************/	
				
				
				/****************Added-s by JimmyJin 20120509*****************/
			case Constant.TYPE_DOWNLOAD_BEGIN:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
//					appId = ((Integer) params[1]).intValue();
					String mUserId = null;
					String mAppId = null;	
					String mAppPackage = null;
					String mFromWhere =	 null;
					
					try {
						mUserId = (String)params[0];
						mAppId = (String)params[1];
						mAppPackage = (String)params[2]; 
						mFromWhere = (String)params[3]; 
						Log.v("RequestHandler","JimmyJin mUserId1="+mUserId);
						Log.v("RequestHandler","JimmyJin mAppId1="+mAppId);
						Log.v("RequestHandler","JimmyJin mAppPackage1="+mAppPackage);
						Log.v("RequestHandler","JimmyJin mFromWhere1="+mFromWhere);

						mAgent.postDownloadBegin(mUserId,
								mAppId,
								mAppPackage,
								mFromWhere);

						request.setStatus(Constant.STATUS_SUCCESS);
//						request.notifyObservers(Result);
					}
					catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
				
			case Constant.TYPE_DOWNLOAD_END:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
//					appId = ((Integer) params[1]).intValue();
					String mUserId = null;
					String mAppId = null;	
					String mAppPackage = null;
					String mFromWhere =	 null;
					
					try {
						mUserId = (String)params[0];
						mAppId = (String)params[1];
						mAppPackage = (String)params[2]; 
						mFromWhere = (String)params[3]; 
						Log.v("RequestHandler","JimmyJin mUserId2="+mUserId);
						Log.v("RequestHandler","JimmyJin mAppId2="+mAppId);
						Log.v("RequestHandler","JimmyJin mAppPackage2="+mAppPackage);
						Log.v("RequestHandler","JimmyJin mFromWhere2="+mFromWhere);

						mAgent.postDownloadEnd(mUserId,
								mAppId,
								mAppPackage,
								mFromWhere);

						request.setStatus(Constant.STATUS_SUCCESS);
//						request.notifyObservers(Result);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
				
			case Constant.TYPE_INSTALL_INFO:
				if (request.getData() != null) {
					params = (Object[]) request.getData();
//					appId = ((Integer) params[1]).intValue();
					String mUserId = null;
					String mAppId = null;	
					String mAppPackage = null;
					String mFromWhere =	 null;
					
					try {
						mUserId = (String)params[0];
						mAppId = (String)params[1];
						mAppPackage = (String)params[2]; 
						mFromWhere = (String)params[3]; 
						Log.v("RequestHandler","JimmyJin mUserId3="+mUserId);
						Log.v("RequestHandler","JimmyJin mAppId3="+mAppId);
						Log.v("RequestHandler","JimmyJin mAppPackage3="+mAppPackage);
						Log.v("RequestHandler","JimmyJin mFromWhere3="+mFromWhere);

						mAgent.postInstallInfo(mUserId,
								mAppId,
								mAppPackage,
								mFromWhere);

						request.setStatus(Constant.STATUS_SUCCESS);
//						request.notifyObservers(Result);
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}
				}
				break;
			case Constant.REBUILD_CONTEXT:					
					try {
						Log.v("RequestHandler","JimmyJin Constant.REBUILD_CONTEXT!");
						mAgent.postRebuildContext();
						request.setStatus(Constant.STATUS_SUCCESS);
					}
					catch (SocketException e) {
						// TODO Auto-generated catch block
						request.setStatus(Constant.STATUS_ERROR);
						request.notifyObservers(null);
					}

				break;
				/****************Added-e by JimmyJin 20120509*****************/	
			default:
				break;
			}
		}
	}
}