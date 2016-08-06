package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.SystemProperties;
import com.android.systemui.statusbar.phone.KeyguardBouncerHQ;
import com.fineos.keyguard.ZipLoader;
import android.util.Log;
public class FineOSUtilities {

  private final String TAG = "FineOSUtilities" ;
  private static FineOSUtilities sInstance;
  private final Context mContext;
  private ZipLoader mZipLoader = null ;
  private String mLockType = null ;
  private boolean mIsecure = false ;
  private boolean mUseFineOS = false ;
   
  private FineOSUtilities(Context context) {
     
     mContext = context;
     mZipLoader = new ZipLoader(mContext);
     mLockType = mZipLoader.getCurrentLockType();
  }

  public static FineOSUtilities getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FineOSUtilities(context);
        }
        return sInstance;
  }
  public boolean useFineOSKeyguard(){

     return SystemProperties.getBoolean("ro.fineos.framework", false);

  }

  private boolean isKeyguardSecure(){

      return KeyguardBouncerHQ.isKeyguardSecure();
  }

  private String reGetLockType(){
	  
	  mLockType = mZipLoader!=null?mZipLoader.getCurrentLockType():null;
	  return mLockType ;
	  
  }

  public boolean useFineOSlideKeyguardNew(){
      
	  mIsecure = isKeyguardSecure() ;
     if(mIsecure||!useFineOSKeyguard()){
    	 mUseFineOS = false;
     }else{
    	 mLockType = reGetLockType() ;
		 mUseFineOS =(mLockType!=null&&!mLockType.equals("system"));
      }
     Log.v(TAG,"use new flag ,mLockType: "+mLockType+",mIsecure:"+mIsecure+",mUseFineOS:"+mUseFineOS);
     return mUseFineOS ;
  }
  
  
  public boolean useFineOSlideKeyguard(){
     
      //mIsecure = isKeyguardSecure() ;
      //if(mIsecure||!useFineOSKeyguard()){
     //	 mUseFineOS = false;
      // }
      Log.v(TAG,"use old flag ,mLockType:"+mLockType+",mUseFineOS:"+mUseFineOS);
      return mUseFineOS ;
  }




}
