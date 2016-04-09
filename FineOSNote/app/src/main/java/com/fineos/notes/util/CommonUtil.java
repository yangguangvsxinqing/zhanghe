package com.fineos.notes.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.notes.R;
import com.fineos.notes.adapter.MenuListAdapter;
import com.fineos.notes.bean.Folder;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.FolderDao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CommonUtil {
	
	/**
	 * 添加文件夹
	 * @param list
	 * @param adapter
	 */
	public static  void addFolder(final Context context,final List<HashMap<String, Object>> list,
			final MenuListAdapter adapter) {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		final EditText editText = new EditText(context);
		new AlertDialog.Builder(context).setTitle("请输入")
				.setIcon(android.R.drawable.ic_dialog_info).setView(editText)
				.setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(!editText.getText().toString().isEmpty()){
							map.put("icon", R.drawable.ic_normal);
							map.put("folderName", editText.getText().toString());
							list.add(list.size() - 1, map);
							
							FolderDao folderDao = new FolderDao(context);
							Folder folder = new Folder();
							folder.setFolder(editText.getText().toString());
							folderDao.add(folder);
							adapter.notifyDataSetChanged();
							dialog.dismiss();
						}else{
							Toast.makeText(context, context.getString(R.string.toast), Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}
    public static void saveBitMap(Context context,Bitmap bitmap,String Dir,String fileName){
        if(bitmap == null) return;
        File file = new File(Dir);
        if (!file.exists()){
            Log.w(Constant.TAG,"file.mkdirs()");
            file.mkdirs();
        }
        try {
            Log.w(Constant.TAG, "file.mkdirs()" + Dir + fileName + ".PNG");
            FileOutputStream out = new FileOutputStream(Dir+fileName+".PNG");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.w(Constant.TAG, "已经保存");

            ScreenUtils.saveImageToGallery(context,file);
        } catch (FileNotFoundException e) {
            Log.w(Constant.TAG, "saveBitMap --FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(Constant.TAG, "saveBitMap --IOException");
            e.printStackTrace();
        }
    }
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
		bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = bitmap.getWidth() / 2;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
		}

    public static void savePhotoCache(Context context,final String resourceId, final Bitmap bitmap) {
        if(bitmap==null)
            return;

        File imageDir = new File(context.getCacheDir(), "images/com.fineos.notes");
        if (!imageDir.isDirectory())
            imageDir.mkdirs();

        File cachedImage = new File(imageDir, resourceId);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(cachedImage);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        } catch (IOException e) {
            Log.d("CACHED_IMAGE", "Exception writing cache image", e);
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (IOException e) {
                    // Ignored
                }
        }
    }

    public static Bitmap getPhotoCache(Context context,final String resourceId) {
        File imageDir = new File(context.getCacheDir(), "images/com.fineos.notes");
        if (!imageDir.isDirectory())
            imageDir.mkdirs();

        File imageFile = new File(imageDir, resourceId);

        if (!imageFile.exists() || imageFile.length() == 0)
            return null;

        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        if (bitmap != null)
            return bitmap;
        else {
            imageFile.delete();
            return null;
        }
    }

    /**
     * delete a file
     * @param path
     */
    public static void deleteFile(String path){
        File file = new File(path);
        Log.w(Constant.TAG,"CommonUtil deleteFile file.exists():"+file.exists());
        if (file.exists()){
            file.delete();
        }
        Log.w(Constant.TAG,"CommonUtil deleteFile path:"+path);
        Log.w(Constant.TAG,"CommonUtil deleteFile file.exists():"+file.exists());
    }


    /**
     * check netWork
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static void showCancelTextView(final Activity activity, final int textColor) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //  try to change return image to text.
                final int contextBarID = activity.getResources().getIdentifier("android:id/action_mode_close_button", null, null);
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                View closeButton = decorView.findViewById(contextBarID);

                if (!(closeButton instanceof LinearLayout)) {
                    return;
                }
                LinearLayout layout = (LinearLayout) closeButton;
                if (layout == null) {
//                    com.fineos.gallery3d.app.Log.d("acmllaugh1", "onCreateActionMode (line 503): layout is null.");
                } else {
                    layout.removeAllViews();
                    layout.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
                    ViewGroup.LayoutParams params = layout.getLayoutParams();
                    params.height = 100;
                    params.width = 100;
                    layout.setLayoutParams(params);
                    TextView textView = new TextView(activity);
                    textView.setText(activity.getString(R.string.cancel));
                    textView.setTextSize(R.dimen.actionbar_left_text);
//                    textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.action_bar_txt_size));
                    textView.setTextColor(textColor);
                    layout.addView(textView);
                }
            }
        });



    }





    private String getRealPathFromURI(Context mContext,Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);

    }

    private String getRealPath(Context mContext,Uri fileUrl){
        String fileName = null;
        Uri filePathUri = fileUrl;
        if(fileUrl!= null){
            if (fileUrl.getScheme().toString().compareTo("content")==0)           //content://开头的uri
            {
                Cursor cursor = mContext.getContentResolver().query(filePathUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst())
                {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    fileName = cursor.getString(column_index);          //取出文件路径
                    if(!fileName.startsWith("/mnt")){
                        //检查是否有”/mnt“前缀

                        fileName = "/mnt" + fileName;
                    }
                    cursor.close();
                }
            }else if (filePathUri.getScheme().compareTo("file")==0)         //file:///开头的uri
            {
                fileName = filePathUri.toString();
                fileName = filePathUri.toString().replace("file://", "");
            //替换file://
                if(!fileName.startsWith("/mnt")){
                //加上"/mnt"头
                    fileName += "/mnt";
                }
            }
        }
        return fileName;
    }

    /**
     * 打卡软键盘
     *
     * @param mEditText
     *            输入框
     * @param mContext
     *            上下文
     */
    public static void openKeybord(EditText mEditText, Context mContext)
    {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param mEditText
     *            输入框
     * @param mContext
     *            上下文
     */
    public static void closeKeybord(EditText mEditText, Context mContext)
    {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    public static void hideSoftInput(View view,Context mContext){
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
