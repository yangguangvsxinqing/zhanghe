package android.content.pm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.fineos.theme.R;
import com.fineos.theme.download.DownloadReceiver;
import com.fineos.theme.utils.ThemeLog;

public class PackageInstallObserver extends IPackageInstallObserver.Stub {
	private int mInstallResult;
	private String TAG = "PackageInstallObserver";
	private final int INSTALL_SUCCESS = 1;
	private final int INSTALL_FAIL = 0;
	private Context mContext;
	String mFilepath;
	public PackageInstallObserver(Context context, String filepath){
		mContext = context;
		mFilepath = filepath;
	}
	
        public void packageInstalled(String packageName, int returnCode) {
        	try {
                if(checkInstallResult(returnCode)) {
                    mInstallResult = INSTALL_SUCCESS;
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
					editor.remove(packageName);
					editor.commit();
                    ThemeLog.v(TAG, "packageInstalled INSTALL_SUCCESS");
                } else {
                    mInstallResult = INSTALL_FAIL;
                    ThemeLog.v(TAG, "packageInstalled INSTALL_FAIL");
                    ThemeLog.v(TAG, "packageInstalled mContext ="+mContext);
                    ThemeLog.v(TAG, "packageInstalled packageName ="+packageName);
                    
//                    String path = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).getString(packageName, null);
//            		ThemeLog.w(TAG, "packageInstalled packageName :" + packageName + "path :" + path);
            		
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).edit();
					editor.remove(packageName);
					editor.commit();
					
//					String path2 = mContext.getSharedPreferences("DOWNLOAD_FILE", Context.MODE_PRIVATE).getString(packageName, null);
//            		ThemeLog.w(TAG, "packageInstalled packageName :" + packageName + "path2 :" + path2);
					
					Intent intent = new Intent(DownloadReceiver.ACTION_INSTALL_FAIL);
					ThemeLog.v(TAG, "packageInstalled intent ="+intent);
					mContext.sendBroadcast(intent);
                }
            } catch (Exception e) {
            	ThemeLog.v(TAG, "packageInstalled e ="+e.getMessage());
                e.printStackTrace();
            }
        }
        
        private boolean checkInstallResult(int returnCode) {
        	ThemeLog.d(TAG, " return code ==================== " + returnCode);
            switch(returnCode) {
            case PackageManager.INSTALL_SUCCEEDED: //1
            	ThemeLog.v(TAG, "install success!");
 //           	Toast.makeText(mContext, "install success", Toast.LENGTH_SHORT).show();
                return true;
            //data partition error
            case PackageManager.INSTALL_FAILED_ALREADY_EXISTS: //-1
            	ThemeLog.d(TAG, "install failed: package already exists");
                break;
            case PackageManager.INSTALL_FAILED_INVALID_APK: //-2
            	ThemeLog.d(TAG, "install failed: invalid apk file");
                break;
            case PackageManager.INSTALL_FAILED_INVALID_URI: //-3
            	ThemeLog.d(TAG, "install failed: passed invalid uri ");
                break;
            case PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE: //-4
            	ThemeLog.d(TAG, "install failed: didn't have enough space ");
                break;
            case PackageManager.INSTALL_FAILED_DUPLICATE_PACKAGE: //-5
            	ThemeLog.d(TAG, "install failed: already installed with the same name");
                break;
            case PackageManager.INSTALL_FAILED_NO_SHARED_USER: //-6
            	ThemeLog.d(TAG, "install failed: requested shared user does not exist ");
                break;
            case PackageManager.INSTALL_FAILED_UPDATE_INCOMPATIBLE: //-7
            	ThemeLog.d(TAG, "install failed: previously installed package of the same name has a different signature ");
                break;
            case PackageManager.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE: //-8
            	ThemeLog.d(TAG, "install failed: requested a shared user which is already installed on the device and does not have matching signature ");
                break;
            case PackageManager.INSTALL_FAILED_MISSING_SHARED_LIBRARY: //-9
            	ThemeLog.d(TAG, "install failed: package uses a shared library that is not available ");
                break;
            case PackageManager.INSTALL_FAILED_REPLACE_COULDNT_DELETE: //-10
            	ThemeLog.d(TAG, "install failed: replace couldn't delete ");
                break;
            case PackageManager.INSTALL_FAILED_DEXOPT: //-11
            	ThemeLog.d(TAG, "install failed: optimizing and validating its dex files failed ");
                break;
            case PackageManager.INSTALL_FAILED_OLDER_SDK: //-12
            	ThemeLog.d(TAG, "install failed: current SDK version is older than that required by the package ");
                break;
            case PackageManager.INSTALL_FAILED_CONFLICTING_PROVIDER: //-13
            	ThemeLog.d(TAG, "install failed: a provider already installed ");
                break;
            case PackageManager.INSTALL_FAILED_NEWER_SDK: //-14
            	ThemeLog.d(TAG, "install failed: current SDK version is newer than that required by the package ");
                break;
            case PackageManager.INSTALL_FAILED_TEST_ONLY: //-15
            	ThemeLog.d(TAG, "install failed: it is a test-only package");
                break;
            case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE: //-16
            	ThemeLog.d(TAG, "install failed: contains native code, but none that is compatible with the the device's CPU_ABI ");
                break;
            case PackageManager.INSTALL_FAILED_MISSING_FEATURE: //-17
            	ThemeLog.d(TAG, "install failed: package uses a feature that is not available ");
                break;
            //sdcard error
            case PackageManager.INSTALL_FAILED_CONTAINER_ERROR: //-18
            	ThemeLog.d(TAG, "install failed: a secure container mount point couldn't be accessed on external media ");
                break;
            case PackageManager.INSTALL_FAILED_INVALID_INSTALL_LOCATION: //-19
            	ThemeLog.d(TAG, "install failed: the new package couldn't be installed in the specified install location ");
                break;
            case PackageManager.INSTALL_FAILED_MEDIA_UNAVAILABLE: //-20
            	ThemeLog.d(TAG, "install failed: the media is not available ");
                break;
            //parser error
            case PackageManager.INSTALL_PARSE_FAILED_NOT_APK: //-100
            	ThemeLog.d(TAG, "install failed: a path that is not a file, or does not end with the expected '.apk' extension");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST: //-101
            	ThemeLog.d(TAG, "install failed: the parser was unable to retrieve the AndroidManifest.xml file");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION: //-102
            	ThemeLog.d(TAG, "install failed: the parser encountered an unexpected exception");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES: //-103
            	ThemeLog.d(TAG, "install failed: parser did not find any certificates in the .apk ");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES: //-104
            	ThemeLog.d(TAG, "install failed: parser found inconsistent certificates on the files in the .apk");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING: //-105
            	ThemeLog.d(TAG, "install failed: parser encountered a CertificateEncodingException");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME: //-106
            	ThemeLog.d(TAG, "install failed: parser encountered a bad or missing package name in the manifest ");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID: //-107
            	ThemeLog.d(TAG, "install failed: parser encountered a bad shared user id name in the manifest ");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED: //-108
            	ThemeLog.d(TAG, "install failed: parser encountered some structural problem in the manifest ");
                break;
            case PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY: //-109
            	ThemeLog.d(TAG, "install failed: parser did not find any actionable tags (instrumentation or application) in the manifest");
                break;
            case PackageManager.INSTALL_FAILED_INTERNAL_ERROR: //-110
            	ThemeLog.d(TAG, "install failed: the system failed to install the package because of system issues ");
                break;
            default:
            	ThemeLog.d(TAG, "install failed: unknow reason ");
                break;
            }
            return false;
        }

}