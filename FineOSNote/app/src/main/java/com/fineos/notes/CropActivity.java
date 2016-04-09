package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.notes.constant.Constant;
import com.fineos.notes.util.CommonUtil;
import com.fineos.notes.util.ImageTools;
import com.fineos.notes.view.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import fineos.app.ProgressDialog;

/**
 * Created by ubuntu on 15-7-9.
 */
public class CropActivity extends Activity implements View.OnClickListener{
    private ImageView back;
    private TextView title;
    private Button graffiti,ok;
    private Bitmap croppedImage;// image of croped
    private CropImageView cropImageView;
    private Bitmap photo;
    private String titleName;
    private int noteId;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_main_layout);

        doIntent();
    }

    private void doIntent(){
        Intent intent = getIntent();
        titleName = intent.getStringExtra("titleName");
        noteId = intent.getIntExtra("noteId", -1);
        Uri uri = intent.getData();
        int type = intent.getIntExtra("Type", -1);
        Log.w(Constant.TAG, "CropActivity doIntent() uri:" + uri);
        initView();
        setActionBar();
        if(uri != null){
            cropImage(uri,type);
        }else{
            Toast.makeText(CropActivity.this,"Get picture fail ",Toast.LENGTH_SHORT).show();
        }

    }
    private void initView(){
        graffiti = (Button) findViewById(R.id.btn_graffiti);
        ok = (Button) findViewById(R.id.btn_ok);
        cropImageView =(CropImageView)findViewById(R.id.CropImageView);

        cropImageView.setAspectRatio(50, 100);
        graffiti.setOnClickListener(this);
        ok.setOnClickListener(this);

        if(titleName.equals(getString(R.string.insert_pictrue))){
            graffiti.setText(getString(R.string.graffiti));
            ok.setText(getString(R.string.done));
        }else {
            graffiti.setText(getString(R.string.cancel));
            ok.setText(getString(R.string.done));
        }
    }

    private void setActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View view= getLayoutInflater().inflate(R.layout.crop_actionbar,null);
        back = (ImageView) view.findViewById(R.id.iv_back);
        title = (TextView) view.findViewById(R.id.tv_title);

        title.setText(titleName);
        back.setOnClickListener(this);
        actionBar.setCustomView(view);
        actionBar.show();
    }
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
    private void cropImage( Uri originalUri ,int type){
        String path = null;
        if (type == 0) {//camera path
//            path = originalUri.toString();
            path = Environment.getExternalStorageDirectory()+File.separator+"tempImage.PNG";
        }else if (type == 1) {// picture uri
            path = getRealPathFromURI(originalUri);
        }
        Log.w(Constant.TAG, "CropActivity cropImage path" + path);

        Bitmap bitmap = ImageTools.getZoomImage(path, 720, Constant.PICTURE_HEIGHT);
        cropImageView.setImageBitmap(bitmap);
//        // file:///storage/sdcard0/tempImage.PNG
//

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(croppedImage != null){
            croppedImage.recycle();
        }
        if (photo!=null) {
            photo.recycle();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // addnote - crop --> graffiti ---> crop ---> addnote
        Log.w(Constant.TAG, "CropActivity onActivityResult");
        Log.w(Constant.TAG, "CropActivity requestCode,resultCode:"+requestCode+","+resultCode);
        Log.w(Constant.TAG, "CropActivitydata:"+data);
        if(requestCode == Constant.CROP_REQUESTCODE && resultCode == Constant.GRAFFITTI_RESULTCODE && data != null){
            setResult(Constant.CROP_RESULTCODE,data);
            finish();
        }
    }

    private void showLoadDialog() {
        ProgressDialog progressDialog = new ProgressDialog(CropActivity.this);
        progressDialog.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                title.setTextColor(getResources().getColor(R.color.titlechangeColor));
                if(photo !=null){
                    photo.recycle();
                }

                String path = Environment.getExternalStorageDirectory()+"/tempImage.PNG";
                CommonUtil.deleteFile(path);
                finish();
                break;
            case R.id.btn_graffiti:
                Log.w(Constant.TAG,"CropActivity btn_graffiti "+titleName);
                if(titleName.equals(getString(R.string.insert_pictrue))){ //涂鸦按钮
                    Log.w(Constant.TAG,"CropActivity btn_graffiti "+titleName);
                    croppedImage = cropImageView.getCroppedImage();
                    SimpleDateFormat sDateFormat =new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String  pictureName =  "IMG_"+sDateFormat.format(curDate);
                    Intent intent = new Intent(CropActivity.this,GraffitiActivity.class);
                    CommonUtil.saveBitMap(CropActivity.this,croppedImage, Constant.Dir, pictureName);
                    intent.putExtra("pictureName",pictureName);
                    intent.putExtra("noteId",noteId);
                    intent.putExtra("intentFlag", 0);//裁剪跳到涂鸦0
                    startActivityForResult(intent, Constant.CROP_REQUESTCODE);
                    String path1 = Environment.getExternalStorageDirectory()+"/tempImage.PNG";
                    CommonUtil.deleteFile(path1);
                }else if(titleName.equals(getString(R.string.graffiti_crop_pictrue))){ //取消按钮
                    Log.w(Constant.TAG,"CropActivity btn_graffiti "+titleName);
                    finish();
                }
                break;
            case R.id.btn_ok:
                croppedImage = cropImageView.getCroppedImage();
                if(croppedImage == null)return;
//                showLoadDialog();
                if(titleName.equals(getString(R.string.insert_pictrue))){
                    //插入图片的裁剪 返回到便签
                    SimpleDateFormat sDateFormat =new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String  pictureName =  "IMG_"+sDateFormat.format(curDate);
                    CommonUtil.saveBitMap(CropActivity.this,croppedImage, Constant.Dir, pictureName);

                    Intent intent = new Intent();
                    intent.putExtra("pictureName",pictureName);
                    intent.putExtra("noteId", noteId);
                    setResult(Constant.CROP_RESULTCODE, intent);
                    String path2 = Environment.getExternalStorageDirectory()+"/tempImage.PNG";
                    CommonUtil.deleteFile(path2);
//                    progressDialog.dismiss();
                    finish();
                }else if(titleName.equals(getString(R.string.graffiti_crop_pictrue))){
                    //涂鸦裁剪 返回到涂鸦
                    SimpleDateFormat sDateFormat =new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String  pictureName =  "IMG_Graffiti_"+sDateFormat.format(curDate);
                    CommonUtil.saveBitMap(CropActivity.this,croppedImage, Constant.Dir,pictureName);

                    Intent intent = new Intent();
                    intent.putExtra("pictureName", pictureName);
                    intent.putExtra("noteId",noteId);
                    setResult(Constant.CROP_RESULTCODE, intent);
//                    progressDialog.dismiss();
                    finish();
                }

                break;
            default:
                break;
        }

    }
}
