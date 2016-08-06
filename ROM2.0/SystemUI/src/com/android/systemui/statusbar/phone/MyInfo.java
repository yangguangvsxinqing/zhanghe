package com.android.systemui.statusbar.phone;

import android.net.Uri;
import android.provider.BaseColumns;

public class MyInfo {
	  
    /**
     */
    public static final String AUTHORITY="com.fineos.myinfo";
  
    private MyInfo(){}
  
    /**
     *
     *
     */
    public static final class MyInfoColumns implements BaseColumns {
  
        private MyInfoColumns(){}
        /**
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/myInfo");
  
        /**
         */
        public static final String NAME="name";	//姓名
        public static final String SIGN="sign";	//签名
        public static final String TEL="tel";	//电话
        public static final String EMAIL="email";	//邮件
        public static final String ICON="icon";	//头像
        public static final String LS_SWITCH="ls_switch"; //锁屏开关 LockScreen
        public static final String WD_SWITCH="wd_switch"; //小控件 Widget
        public static final String DT_SWITCH="dt_switch"; //桌面 deskTop
        public static final String NB_SWITCH="nb_switch"; //通知栏 notification bar
        public static final String SB_SWITCH="sb_switch"; //状态栏 Status bar
        public static final String CT_SWITCH="ct_switch"; //联系人 contacts
        public static final String[] QUERY = {_ID,NAME,SIGN,TEL,EMAIL,ICON,LS_SWITCH,WD_SWITCH,DT_SWITCH,NB_SWITCH,SB_SWITCH,CT_SWITCH};
        public static final String[] INFO_QUERY = {_ID,NAME,SIGN,TEL,EMAIL,ICON};
        public static final String[] SWITCH_QUERY = {_ID,LS_SWITCH,WD_SWITCH,DT_SWITCH,NB_SWITCH,SB_SWITCH,CT_SWITCH};
    }
  
}
