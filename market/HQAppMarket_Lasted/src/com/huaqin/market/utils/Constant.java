package com.huaqin.market.utils;

public class Constant {

	/**Application(Package)״̬����*/
	public static final int PKG_NOT_INSTALLED = 0;
	public static final int PKG_INSTALLED = 1;
	public static final int PKG_UPDATE_AVAILABLE = 2;

	/**List����*/
	public static final int LIST_TYPE_INSTALLED = 100;
	public static final int LIST_TYPE_UPGRADE = 101;
	public static final int LIST_COUNT_PER_TIME = 10;
	public static final int RECOMMANDLIST_COUNT_PER_TIME = 15;
	public static final int LIST_ITEMTYPE_NORMAL = 1001;
	public static final int LIST_ITEMTYPE_DATE = 1002;
	public static final int LIST_ITEMTYPE_DOWNLOAD = 1003;
	public static final int LIST_VIEWPAGER_FLAG = 1013;

	/**Sort Type����*/
	public static final int LIST_SORT_BY_DOWNLOAD = 1;
	public static final int LIST_SORT_BY_RATING = 2;
	public static final int LIST_SORT_BY_DATE = 3;

	/**Request*/
	/**Request����״̬*/
	public static final int STATUS_PENDING = 0x30000;
	public static final int STATUS_FAIL = 0x30001;
	public static final int STATUS_SUCCESS = 0x30002;
	public static final int STATUS_CANCELED = 0x30003;
	public static final int STATUS_ERROR = 0x30004;

	/**Request����*/
	public static final int TYPE_LOGIN = 0x10001;
	public static final int TYPE_APP_LIST = 0x10002;
	public static final int TYPE_APP_ICON = 0x10003;
	public static final int TYPE_APP_DETAIL = 0x10004;
	public static final int TYPE_APP_PREVIEW = 0x10005;
	public static final int TYPE_APP_COMMENT_LIST = 0x10006;
	public static final int TYPE_APP_SEND_COMMENT = 0x10007;
	public static final int TYPE_APP_CONTENT_STREAM = 0x10008;
	public static final int TYPE_APP_DOWNLOAD_LIST = 0x10009;
	public static final int TYPE_APP_LIST_BY_KEYWORDS = 0x1000a;	
	public static final int TYPE_APP_REPORT_ERROR = 0x1000c;

	public static final int TYPE_TOP_APP_ICON = 0x1000d;
	public static final int TYPE_TOP_APP_DETAIL = 0x1000e;
	public static final int TYPE_UPDATE_AVALIABLE_APP = 0x1000f;

	public static final int TYPE_APP_RECOMMAND_LIST = 0x10010;
	public static final int TYPE_SUBJECT_LIST = 0x10011;
	public static final int TYPE_SUBJECT_TOPIC_LIST = 0x10012;
	public static final int TYPE_APP_NEW_LIST = 0x10013;
	public static final int TYPE_APP_RANKING_LIST = 0x10014;
	public static final int TYPE_CATEGORY_LIST = 0x10015;
	public static final int TYPE_APP_RELATED_LIST = 0x10016;
	
	public static final int TYPE_SORT_APP_LIST = 0x20001;
	public static final int TYPE_SORT_GAME_LIST = 0x20002;

	public static final int TYPE_SEARCH_HOTWORDS = 0x10017;
	public static final int TYPE_SEARCH_APP_LIST = 0x10018;
	public static final int TYPE_SEARCH_DATABASE = 0x10019;

	public static final int TYPE_SUBS_ICON = 0x1001a;
	public static final int TYPE_SUBS_TOPIC_IMAGE = 0x1001b;
	public static final int TYPE_SUBS_TOPIC_APP = 0x1001c;
	public static final int TYPE_SUBS_TOPIC_THUMB = 0x1001d;

	public static final int TYPE_CATE_ICON = 0x1001e;
	public static final int TYPE_CATE_APP_LIST = 0x1001f;

	
	public static final int TYPE_CHECK_SELF_UPDATE = 0x10020;
	public static final int TYPE_CHECK_APP_UPDATE = 0x10021;
	public static final int TYPE_CHECK_USER_NOTIFY = 0x10022;
	
	public static final int TYPE_APP_RELATE = 0x10024;
	public static final int TYPE_RELATE_APP_ICON = 0x10025;
	
	public static final int TYPE_ADD_FAVORITE = 0x10050;

	public static final int TYPE_PAYMENT_BALANCE = 268500992;
	public static final int TYPE_PAYMENT_APP_BUYED = 268500993;
	public static final int TYPE_PAYMENT_BUY_BY_BEAN = 268500994;
	public static final int TYPE_PAYMENT_BUY_BY_YEEPAY = 268500995;
	public static final int TYPE_PAYMENT_BUY_BY_ALIPAY = 268500996;
	
	public static final int TYPE_ADD_APP_COMMENT = 268501000;
	public static final int TYPE_UPDATE_APP_COMMENT = 268501001;
	
	public static final int TYPE_TOP_LAYOUT_DETIAL = 268501002;
	
	public static final int TYPE_POST_MARKET_COMMENT = 268501005;
	public static final int TYPE_POST_VERSION_REGISTER = 268501006;
	public static final int TYPE_POST_PARTNER = 268501007;
	public static final int TYPE_POST_PV = 268501008;
	
	public static final String MARKET_SELF_PACKAGE_NAME = "com.huaqin.market";
	
	/*************Added-s by JimmyJin**************/
	//Report
	public static final int TYPE_USER_INFO = 0X10030;
	public static final int TYPE_DOWNLOAD_BEGIN = 0X10031;
	public static final int TYPE_DOWNLOAD_END = 0X10032;
	public static final int TYPE_INSTALL_INFO = 0X10033;
	public static final int TYPE_ACTIVITE_INFO = 0X10034;
	/*************Added-e by JimmyJin**************/	
	public static final int REBUILD_CONTEXT = 0X10035;
}