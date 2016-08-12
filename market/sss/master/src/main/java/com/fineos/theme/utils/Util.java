package com.fineos.theme.utils;



import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.wallpaper.WallpaperService;
import android.text.format.Time;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fineos.theme.R;
import com.fineos.theme.activity.ThemeDetailActivity;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.download.ReportProvider;
import com.fineos.theme.fragment.DeleteDialogFragment;
import com.fineos.theme.fragment.ProgressDialogFragment;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.provider.ThemeSQLiteHelper;
import static android.content.Intent.EXTRA_USER;

public class Util {
	public static final String TAG = "com.fineos.theme";
	public static String PREFERENCES_NAME = "fineos_theme";

	public static final String DIALOG_TAG_DELETE = "Delete";
	public static final String DIALOG_TAG_PROGRESS = "Progress";
	public static final String KEY_NETWORK_HINT = "network_hint";
	
	protected static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
	protected static final String SHOULD_NOT_RESOLVE = "SHOULDN'T RESOLVE!";
	protected static final String KEY_CALLER_IDENTITY = "pendingIntent";
	protected static final String EXTRA_HAS_MULTIPLE_USERS = "hasMultipleUsers";
	protected static final int CHOOSE_ACCOUNT_REQUEST = 1;
	protected static final int ADD_ACCOUNT_REQUEST = 2;
	protected static final String ACCOUNT_TYPE_GOOGLE = "com.google";
	protected static PendingIntent mPendingIntent;
	protected static UserHandle mUserHandle;
	protected static Context mContext;
	
	public static boolean getNetworkHint(Context context) {
		SharedPreferences settingPreference = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		boolean netHint = settingPreference.getBoolean(KEY_NETWORK_HINT, true);
		return netHint;
	}

	public static void setNetworkHint(Context context, boolean hint) {
		SharedPreferences settingPreference = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		Editor it = settingPreference.edit();
		it.putBoolean(KEY_NETWORK_HINT, hint);
		it.commit();
	}

	public static boolean checkDownload(Context mContext, String packageName) {
		Cursor c = null;
		try {
			c = mContext.getContentResolver().query(ThemeSQLiteHelper.CONTENT_URI, null, "file_name=?", new String[] { packageName }, null);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return false;
	}
	
	public static String checkDownload(Context mContext, String packageName, boolean flag) {
		Cursor c = null;
		try {
			c = mContext.getContentResolver().query(ThemeSQLiteHelper.CONTENT_URI, null, "file_name=?", new String[] { packageName }, null);
			Log.v(TAG, "ssss checkDownload c ="+c.getCount());
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					Log.v(TAG, "ssss checkDownload COLUMN_THEME_PATH ="+c.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PATH));
					return c.getString(c.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PATH));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	public static boolean checkDownload(String url) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File f = new File(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
		if (f.exists()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkInstall(Context context, String packageName) {
		PackageManager mPackageManager = context.getPackageManager();

		List<ResolveInfo> list = mPackageManager.queryIntentServices(new Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
		for (ResolveInfo resolveInfo : list) {
			WallpaperInfo info = null;
			try {
				info = new WallpaperInfo(context, resolveInfo);
				ThemeLog.v(TAG, "WallpaperInfo packageName=" + packageName);
				ThemeLog.v(TAG, "WallpaperInfo info=" + info.getPackageName());
				if (info.getPackageName().equals(packageName)) {
					return true;
				}
			} catch (XmlPullParserException e) {
				ThemeLog.w(TAG, "Skipping wallpaper " + resolveInfo.serviceInfo, e);
				continue;
			} catch (IOException e) {
				ThemeLog.w(TAG, "Skipping wallpaper " + resolveInfo.serviceInfo, e);
				continue;
			}
		}

		return false;
	}
	
	public static boolean checkDownloadSratus(String url,ThemeData theme) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File f = new File(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
		if (f.length() == theme.getSize()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkThemeDownloadSratus(String url,ThemeData theme) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File f = new File(Environment.getExternalStorageDirectory() + FileManager.THEME_DIR_PATH + "/" + fileName);	
		Log.v("", "changeViewLp f ="+f.length());
		Log.v("", "changeViewLp theme.getSize()="+theme.getSize());
		if (f.length() > 0 && f.length() == theme.getSize()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkApkDownloadSratus(String url,ThemeData theme) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File f = new File(Environment.getExternalStorageDirectory() + FileManager.APP_DIR_NAME + "/" + fileName);
		if (f.length() > 0 && f.length() == theme.getSize()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkThemeFileSratus(String fileName) {
		if(fileName!=null&&fileName.length()>0){
		File f_theme = new File(Environment.getExternalStorageDirectory() + FileManager.THEME_DIR_PATH + "/" + fileName); 
		if (f_theme.exists()) {
			return true;
		}
		}
		return false;
	}
	
	public static boolean checkAppFileSratus(String path) {
		if(path!=null&&path.length()>0){
			File f_app = new File(path);
			if (f_app.exists()) {
				return true;
			}
		}
		return false;
	}
	
	public static String ByteToM(Long b) {
		
		Float iM = b/(1024.0f*1024.0f) ;
		BigDecimal   bd  =   new BigDecimal(iM);  
		Float f = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();  
		String sM = String.valueOf(f);
		return sM ;
	}
	
   public static String HKToDollar( double h) {
		
	   double dolar= h* 0.12902738654314 ;
		BigDecimal   bd  =   new BigDecimal(dolar);  
		Float f = bd.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();  
		String sDolar = String.valueOf(f);
		Log.v(TAG,"double h :"+h+",dolar:"+dolar+",sDolar:"+sDolar);
		return sDolar ;
	}
	public static String FileSize(String path) {
		
		File f= new File(path);  
	    if (f.exists() && f.isFile()){  
	    	return FormetFileSize(f.length());
	    }else{  
	    	return "0B";
	    } 
	    
	}
	
	public static String FormetFileSize(long fileS)
	{
	DecimalFormat df = new DecimalFormat("#.00");
	String fileSizeString = "";
	String wrongSize="0B";
	if(fileS==0){
	return wrongSize;
	}
	if (fileS < 1024){
	fileSizeString = df.format((double) fileS) + "B";
	 }
	else if (fileS < 1048576){
	fileSizeString = df.format((double) fileS / 1024) + "KB";
	}
	else if (fileS < 1073741824){
	    fileSizeString = df.format((double) fileS / 1048576) + "MB";
	  }
	else{
	    fileSizeString = df.format((double) fileS / 1073741824) + "GB";
	  }
	return fileSizeString;
	}
	
	/// add for add google account begin
	

	/**
	    * Returns true if the user provided is in the same profiles group as the current user.
	    */
	   private static boolean isProfileOf(UserManager um, UserHandle otherUser) {
	       if (um == null || otherUser == null) return false;
	       return (UserHandle.myUserId() == otherUser.getIdentifier())
	               || um.getUserProfiles().contains(otherUser);
	   }
	
	/**
     * Returns the target user for a Settings activity.
     *
     * The target user can be either the current user, the user that launched this activity or
     * the user contained as an extra in the arguments or intent extras.
     *
     * Note: This is secure in the sense that it only returns a target user different to the current
     * one if the app launching this activity is the Settings app itself, running in the same user
     * or in one that is in the same profile group, or if the user id is provided by the system.
     */
    public static UserHandle getSecureTargetUser(IBinder activityToken,
           UserManager um, Bundle arguments, Bundle intentExtras) {
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        android.app.IActivityManager am = ActivityManagerNative.getDefault();
        try {
            String launchedFromPackage = am.getLaunchedFromPackage(activityToken);
            boolean launchedFromSettingsApp = SETTINGS_PACKAGE_NAME.equals(launchedFromPackage);

            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(
                    am.getLaunchedFromUid(activityToken)));
            if (launchedFromUser != null && !launchedFromUser.equals(currentUser)) {
                // Check it's secure
                if (isProfileOf(um, launchedFromUser)) {
                    return launchedFromUser;
                }
            }
            UserHandle extrasUser = intentExtras != null
                    ? (UserHandle) intentExtras.getParcelable(EXTRA_USER) : null;
            if (extrasUser != null && !extrasUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, extrasUser)) {
                    return extrasUser;
                }
            }
            UserHandle argumentsUser = arguments != null
                    ? (UserHandle) arguments.getParcelable(EXTRA_USER) : null;
            if (argumentsUser != null && !argumentsUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, argumentsUser)) {
                    return argumentsUser;
                }
            }
        } catch (RemoteException e) {
            // Should not happen
        	ThemeLog.v(TAG, "Could not talk to activity manager.", e);
        }
        return currentUser;
   }
	
	public static boolean hasMultipleUsers() {
		return false ;
    }
	
	

	private static final AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            boolean done = true;
            try {
                Bundle bundle = future.getResult();
                //bundle.keySet();
                Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (intent != null) {
                    done = false;
                    Bundle addAccountOptions = new Bundle();
                    addAccountOptions.putParcelable(KEY_CALLER_IDENTITY, mPendingIntent);
                    addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS,
                            Util.hasMultipleUsers());
                    addAccountOptions.putParcelable(EXTRA_USER, mUserHandle);
                    intent.putExtras(addAccountOptions);
                    ((Activity)mContext).startActivityForResultAsUser(intent, ADD_ACCOUNT_REQUEST, mUserHandle);
                } else {
                	 ((Activity)mContext).setResult(((Activity)mContext).RESULT_OK);
                    if (mPendingIntent != null) {
                        mPendingIntent.cancel();
                        mPendingIntent = null;
                    }
                }

                ThemeLog.v(TAG, "account added: " + bundle);
            } catch (OperationCanceledException e) {
            	  ThemeLog.v(TAG, "addAccount was canceled");
            } catch (IOException e) {
            	 ThemeLog.v(TAG, "addAccount failed: " + e);
            } catch (AuthenticatorException e) {
            	 ThemeLog.v(TAG, "addAccount failed: " + e);
            } finally {
                if (done) {
                	((Activity)mContext).finish();
                }
            }
        }
    };
	
	
	
	private static void  addAccount(String accountType,Context context) {
        Bundle addAccountOptions = new Bundle();
        mContext = context ;
        /*
         * The identityIntent is for the purposes of establishing the identity
         * of the caller and isn't intended for launching activities, services
         * or broadcasts.
         *
         * Unfortunately for legacy reasons we still need to support this. But
         * we can cripple the intent so that 3rd party authenticators can't
         * fill in addressing information and launch arbitrary actions.
         */
         
        ThemeLog.v(TAG,"addAccount,accountType"+accountType);
        final UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        mUserHandle = Util.getSecureTargetUser(((Activity)context).getActivityToken(), um, null /* arguments */,
        		((Activity)context).getIntent().getExtras());
        Intent identityIntent = new Intent();
        identityIntent.setComponent(new ComponentName(SHOULD_NOT_RESOLVE, SHOULD_NOT_RESOLVE));
        identityIntent.setAction(SHOULD_NOT_RESOLVE);
        identityIntent.addCategory(SHOULD_NOT_RESOLVE);

        mPendingIntent = PendingIntent.getBroadcast(context, 0, identityIntent, 0);
        addAccountOptions.putParcelable(KEY_CALLER_IDENTITY, mPendingIntent);
        addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS, Util.hasMultipleUsers());
        AccountManager.get(context).addAccountAsUser(
                accountType,
                null, /* authTokenType */
                null, /* requiredFeatures */
                addAccountOptions,
                null,
                mCallback,
                null /* handler */,
                mUserHandle);
    }
	

	public static void  showAddaccountDialog( final Context context) {
		
		fineos.app.AlertDialog.Builder mBuilder = new fineos.app.AlertDialog.Builder(context);
		String title = context.getString(R.string.sign_in_google);
		String message = context.getString(R.string.message_sign_in_google);
		mBuilder//.setTitle(title)
		        .setMessage(message)
		        //.setNegativeButton(R.string.btn_no, null)
				 .setPositiveButton(R.string.btn_next, new android.content.DialogInterface.OnClickListener() {
				@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						addAccount(ACCOUNT_TYPE_GOOGLE,context);
						//((Activity)context).finish();
					}
				});
				
		fineos.app.AlertDialog tip = mBuilder.create();
		
		tip.show();
	}
	
	
	
	
	
	
	/// add for add google account end
	
}
