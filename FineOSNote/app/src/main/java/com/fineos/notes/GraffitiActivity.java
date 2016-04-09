package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.notes.anim.ShareAnim;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.imple.ColorPopup;
import com.fineos.notes.imple.PaintPopup;
import com.fineos.notes.util.ImageTools;
import com.fineos.notes.util.ScreenUtils;
import com.fineos.notes.view.MoveView;
import com.fineos.notes.view.WriteView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import fineos.app.AlertDialog;

//import android.app.AlertDialog;

public class GraffitiActivity extends Activity implements View.OnClickListener,
        WriteView.OnDrawChange, ColorPopup.ColorPopupChooseListener,
        PaintPopup.PaintPopupChooseListener {


    private ImageView m_choose_image, m_choose_color, paint_size, delete_last_paint, clear_canvas;
    private TextView back;//actionbar back
    private TextView graffiti, done;//actionbar
    private MoveView mMoveView;
    private Bitmap mBitmap;
    private ImageButton title_back;
    private Button saveButton;
    public final static int REQUEST_SELECT_IMAGE = 1;
    public final static int REQUEST_SELECT_COLOR = 2;
    public final static int REQUEST_SELECT_PAINT = 3;
    //    private ImageButton m_choose_color;
    private WriteView mWriteView;
    private ImageView show_left_image;
    private ImageView show_right_image;
    //    public static final String SAVE_PATH = "notegraffiti";
    private ImageView show_top_image;
    private ImageView show_bottom_image;
    private LinearLayout mShowImage;
    private static final int SEND_MESSAGE_SAVE_SUCCESS = 1;
    private static final int SEND_MESSAGE_SAVE_ERROR = 2;
    private ImageButton touchtoBigImage;
    private ImageButton touchtoSmallImage;
    private Button queryButton;
    private ImageButton shareButton;
    private RelativeLayout composerButtonsWrapper;
    private boolean areButtonsShowing;
    private ColorPopup colorPopup;
    private PaintPopup paintPopup;
    private int noteId;
    private int intentFlag;// 0 裁剪跳到 1 便签跳到
    private Bitmap oraginBitmap;
    private int wBig,hBig,wSmall,hSmall,wOrg,hOrg;
    private  int n = 0;
    private int windWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graffiti_layout);

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        windWidth = mDisplayMetrics.widthPixels;

        initViewFromXmlId();
        setActionBar();
        doIntent();
    }

    private void doIntent() {
        Intent intent = getIntent();
        intentFlag = intent.getIntExtra("intentFlag", -1);
        String pictureName = intent.getStringExtra("pictureName");
        noteId = intent.getIntExtra("noteId", -1);
        Log.w(Constant.TAG, "pictureName:" + pictureName);
        if (pictureName != null) {
//            oraginBitmap = BitmapFactory.decodeFile(Constant.Dir + pictureName + Constant.PICTURETYPE);

            oraginBitmap = ImageTools.getZoomImage(Constant.Dir + pictureName + Constant.PICTURETYPE, Constant.PICTURE_WIDTH, Constant.PICTURE_HEIGHT);
            Log.w(Constant.TAG,"Graffitiy  oraginBitmap1:"+oraginBitmap);
            Log.w(Constant.TAG,"Graffitiy  oraginBitmap1:"+oraginBitmap.getWidth()+","+oraginBitmap.getHeight());
            mMoveView.setBitmap(oraginBitmap);
            showImageControlOnOrOff(View.VISIBLE);
            mWriteView.setPaintColor(0x00ffffff);
            done.setVisibility(View.GONE);
        }
    }

    public void initViewFromXmlId() {


        m_choose_image = (ImageView) findViewById(R.id.choose_image);
        mMoveView = (MoveView) findViewById(R.id.move_view_bg);
        m_choose_image.setOnClickListener(this);

        m_choose_color = (ImageView) findViewById(R.id.choose_color);
        m_choose_color.setOnClickListener(this);

        mWriteView = (WriteView) findViewById(R.id.write_view_bg);
        mWriteView.setDrawListen(this);


        paint_size = (ImageView) findViewById(R.id.choose_paint_size);
        paint_size.setOnClickListener(this);

        delete_last_paint = (ImageView) findViewById(R.id.canvas_delete_last);
        delete_last_paint.setOnClickListener(this);

        clear_canvas = (ImageView) findViewById(R.id.canvas_clear);
        clear_canvas.setOnClickListener(this);

        show_left_image = (ImageView) this.findViewById(R.id.show_left_image);
        show_right_image = (ImageView) this.findViewById(R.id.show_right_image);
        show_top_image = (ImageView) this.findViewById(R.id.show_top_image);
        show_bottom_image = (ImageView) this
                .findViewById(R.id.show_bottom_image);
//
        mShowImage = (LinearLayout) findViewById(R.id.control_image_view);
        touchtoBigImage = (ImageButton) findViewById(R.id.image_touch_big);
        touchtoSmallImage = (ImageButton) findViewById(R.id.image_touch_small);

        touchtoBigImage.setOnClickListener(this);
        touchtoSmallImage.setOnClickListener(this);

        queryButton = (Button) findViewById(R.id.image_touch_ok);
        queryButton.setOnClickListener(this);


    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        View view = getLayoutInflater().inflate(R.layout.graffiti_actionbar, null);
//        back = (ImageView) view.findViewById(R.id.iv_back);
        done = (TextView) view.findViewById(R.id.tv_done);
        back = (TextView) view.findViewById(R.id.tv_back);
        actionBar.setCustomView(view);

        back.setOnClickListener(this);
        done.setOnClickListener(this);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {

                case SEND_MESSAGE_SAVE_SUCCESS:
                    showSaveInfo("save sucess");
                    showShareControl(View.VISIBLE);
                    break;
                case SEND_MESSAGE_SAVE_ERROR:
                    showSaveInfo("save failed");
                    break;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            mWriteView.setPaintColor(R.color.black_ColorPopup);
            Log.w(Constant.TAG, "GraffitiActivity data:" + data);
            return;
        }
        if (requestCode == Constant.PICTURE_REQUESTCODE) {
            Uri uri = data.getData();
            Intent intent = new Intent(GraffitiActivity.this, CropActivity.class);
            intent.putExtra("titleName", getString(R.string.graffiti_crop_pictrue));
            intent.putExtra("noteId", noteId);
            intent.putExtra("Type", 1);//picture crop
            intent.setData(uri);
            startActivityForResult(intent, Constant.GRAFFITI_REQUESTCODE);

        } else if (requestCode == Constant.GRAFFITI_REQUESTCODE && resultCode == Constant.CROP_RESULTCODE){
            //裁剪之后返回涂鸦界面处理
            String pictureName = data.getStringExtra("pictureName");
            noteId = data.getIntExtra("noteId", -1);
            Log.w(Constant.TAG, "GraffitiActivity 裁剪之后返回涂鸦界面处理 pictureName" + pictureName);
            oraginBitmap = ImageTools.getZoomImage(Constant.Dir + pictureName + Constant.PICTURETYPE, 636, Constant.PICTURE_HEIGHT);

            Log.w(Constant.TAG, "GraffitiActivity 裁剪之后返回涂鸦界面处理 oraginBitmap" + oraginBitmap.getWidth() + "," + oraginBitmap.getHeight());
            mMoveView.setBitmap(oraginBitmap);
            showImageControlOnOrOff(View.VISIBLE);
            mWriteView.setPaintColor(0x00ffffff);
            done.setVisibility(View.GONE);
        }

    }

    /**
     * 调用系统裁剪图片
     *
     * @param uri
     */
    private void doCropPhoto(Uri uri) {
        if (null == uri) return;
        Intent intent = new Intent();


        intent.setAction("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");// mUri是已经选择的图片Uri
        intent.putExtra("crop", "true");//可裁剪
        intent.putExtra("aspectX", 2);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600);// 输出图片大小
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);//黑边
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);//若为false则表示不返回数据
//      intent.putExtra("output", Uri.fromFile(new File("SDCard/1.jpg")));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());////返回格式
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, Constant.PICTURECROP_REQUESTCODE);
    }


    /**
     * control for edit from other activity **
     */
    public void controlFromActivity(Uri uri) {

        try {
            InputStream is = getContentResolver().openInputStream(uri);
            // mBitmap = BitmapFactory.decodeStream(is);
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay()
                    .getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
            is.close();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            is = getContentResolver().openInputStream(uri);
            if (options.outWidth > screenWidth) {
                options.inSampleSize = options.outWidth / screenWidth;
            }
            // get
            bmp = BitmapFactory.decodeStream(is, null, options);
            if (bmp != null) {
                mBitmap = bmp;
                mMoveView.setBitmap(mBitmap);
//                showImageControlOnOrOff(View.VISIBLE);
            } else {
//                showSaveInfo(getString(R.string.create_message_fail));
            }
            is.close();
        } catch (Exception e) {

        }

    }

    /**
     * show control image width and height adjust **
     */
    public void showImageControlOnOrOff(int state) {

        mMoveView.setVisibility(state);
        show_left_image.setVisibility(state);
        show_right_image.setVisibility(state);
        show_top_image.setVisibility(state);
        show_bottom_image.setVisibility(state);
        mShowImage.setVisibility(state);
    }

    /**
     * show control anima state **
     */
    public void showShareControl(int state) {

//        composerButtonsWrapper.setVisibility(state);
    }


    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.choose_image:
                Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(picture, Constant.PICTURE_REQUESTCODE);
                break;
            case R.id.choose_color:
                if (colorPopup == null) {
                    colorPopup = new ColorPopup(GraffitiActivity.this,windWidth);
                } else {
                    colorPopup.show(windWidth);
                }
                colorPopup.setListener(this);
                break;
            case R.id.choose_paint_size:
                if (paintPopup == null) {
                    paintPopup = new PaintPopup(this,windWidth);
                } else {
                    paintPopup.show(windWidth);
                }
                paintPopup.setListener(this);
                break;
            case R.id.canvas_delete_last:
                // createDeleteDialog();
                if (!mWriteView.isPathEmpty()) {
                    mWriteView.undo();
                }
                break;
            case R.id.canvas_clear:
                createClearDialog();
                break;
            case R.id.image_touch_big:
                int screenWidth = ScreenUtils.getScreenWidth(GraffitiActivity.this);
                int screenHeight = ScreenUtils.getScreenHeight(GraffitiActivity.this);
                int oW = 0;
                int oH =0;
                if (mBitmap != null) {
                     oW = mBitmap.getWidth();
                     oH = mBitmap.getHeight();
                }
                Log.w(Constant.TAG, "GraffitiActivity oW,oH:" + oW + "," + oH);
                if (oW >= screenWidth || oH >= screenHeight) {
                    return;
                } else {
                    n =n-1;
                    wBig = (int)(oraginBitmap.getWidth() * Math.pow(8.0 / 10.0, n));
                    hBig = (int)(oraginBitmap.getHeight() *  Math.pow(8.0 / 10.0, n));
                    Log.w(Constant.TAG,"GraffitiActivity wBig:"+wBig);
                    Log.w(Constant.TAG, "GraffitiActivity hBig:"+hBig);
                    adjustImage(wBig, hBig);
                }
                break;
            case R.id.image_touch_small:
                n=n+1;
                wSmall = (int)(oraginBitmap.getWidth() * Math.pow(8.0 / 10.0, n));
                hSmall = (int)(oraginBitmap.getHeight() *  Math.pow(8.0 / 10.0, n));
                Log.w(Constant.TAG,"GraffitiActivity wSmall:"+wSmall);
                Log.w(Constant.TAG, "GraffitiActivity hSmall:" + hSmall);
                adjustImage(wSmall, hSmall);
                break;
            case R.id.image_touch_ok:
                done.setVisibility(View.VISIBLE);
		//dpc 	HQ01595795	
		int color = 0xFF000000;	
		if (colorPopup != null) {
                  color = colorPopup.getColorPopupChoose();
                }		
		mWriteView.setPaintColor(color);
                //mWriteView.setPaintColor(0xFF000000);
                if (mBitmap == null) {
                    mWriteView.setBitmaps(oraginBitmap, mMoveView.getImagex(),
                            mMoveView.getImagey());
                } else {
                    Log.w(Constant.TAG, "Graffitti mBitmap:" + mBitmap.getWidth() + "," +mBitmap.getHeight());
                    mWriteView.setBitmaps(mBitmap, mMoveView.getImagex(),
                            mMoveView.getImagey());
                }
                showImageControlOnOrOff(View.GONE);
                break;
            case R.id.tv_back:
                if (!mWriteView.isPathEmpty()) {
                    createSaveDialog();
                } else {
                    finish();
                }
                break;
            case R.id.tv_done:
                Log.w(Constant.TAG, "GraffitiActivity mWriteView.isPathEmpty():" + mWriteView.isPathEmpty());
                if (mWriteView.isPathEmpty()) {
                    finish();
                } else {

                    //涂鸦 点击完成 自动插入图片
                    if (mBitmap != null && mMoveView.getVisibility() == View.VISIBLE) {
                        mWriteView.setBitmaps(mBitmap, mMoveView.getImagex(),
                                mMoveView.getImagey());
                        showImageControlOnOrOff(View.GONE);
                    }

                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String pictureName = "IMG_Graffitti_" + sDateFormat.format(curDate);
                    mWriteView.savaBitMap(Constant.Dir, pictureName);
                    Log.w(Constant.TAG, "GraffittiAc" +
                            "tivity onClick tv_done mBitmap:" + mBitmap);
                    if (intentFlag == 1) {
                        //便签到涂鸦的返回
                        Log.w(Constant.TAG, "GraffittiActivity onClick tv_done  pictureName:" + pictureName);
                        Intent intent = new Intent();
                        intent.putExtra("pictureName", pictureName);
                        intent.putExtra("noteId", noteId);
                        setResult(Constant.GRAFFITTI_RESULTCODE, intent);
                        finish();
                    } else {
                        //裁剪到涂鸦的返回
                        Intent intent = new Intent();
                        intent.putExtra("pictureName", pictureName);
                        intent.putExtra("noteId", noteId);
                        setResult(Constant.GRAFFITTI_RESULTCODE, intent);
                        finish();
                    }
                }

                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {

            mBitmap.recycle();
        }
    }

    /**
     * change the image size
     * <p/>
     * *
     */
    public Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    /**
     * adjust the image
     * **
     */
    public void adjustImage(int w, int h) {
        int screenWidth = ScreenUtils.getScreenWidth(GraffitiActivity.this);
        int screenHeight = ScreenUtils.getScreenHeight(GraffitiActivity.this);
//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        Log.w(Constant.TAG, "GraffitiActivity screenWidth,screenHeight:" + screenWidth + "," + screenHeight);
        Log.w(Constant.TAG, "GraffitiActivity screenWidth* 2,screenHeight* 2:" + screenWidth* 2 + "," + screenHeight* 2);
        Log.w(Constant.TAG, "GraffitiActivity w,h:" + w + "," + h);
        if (w <= 0 || h <= 0 || w > screenWidth * 2 || h > screenHeight * 2) {
            return;
        }
        Bitmap zBitmap = oraginBitmap;
        Log.w(Constant.TAG, "GraffitiActivity adjustImage oraginBitmap:"+zBitmap.hashCode());
        mBitmap = zoomBitmap(zBitmap, w, h);
        mMoveView.setBitmap(mBitmap);

    }

    // for save image thread
    public void save(final String name) {

        new Thread() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (saveImage(name)) {
                    mHandler.sendEmptyMessage(SEND_MESSAGE_SAVE_SUCCESS);

                } else {
                    mHandler.sendEmptyMessage(SEND_MESSAGE_SAVE_ERROR);

                }

            }
        }.start();
    }

    /**
     * save the image
     * *
     */
    public boolean saveImage(String name) {

        return mWriteView.save(name);

    }

    /* show save info */
    public void showSaveInfo(String message) {

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * adjust for anim **
     */
    public void playAnim() {

        if (!areButtonsShowing) {
            // 图标的动画
            ShareAnim.startAnimationsIn(composerButtonsWrapper, 300);
            // 加号的动画
            shareButton.startAnimation(ShareAnim.getRotateAnimation(0, -225,
                    300));
        } else {

            ShareAnim.startAnimationsOut(composerButtonsWrapper, 50);
            // 加号的动画
            shareButton.startAnimation(ShareAnim.getRotateAnimation(-225, 0,
                    300));
        }
        areButtonsShowing = !areButtonsShowing;
    }

//    /* share */
//    public void share() {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);
//        intent.setType("image/*");
//        Uri uri = Uri.fromFile(new File(mWriteView.getPath()));
//        intent.putExtra(Intent.EXTRA_STREAM, uri);
//
//        try {
//            startActivity(Intent.createChooser(intent,
//                    getString(R.string.send_message)));
//        } catch (android.content.ActivityNotFoundException e) {
//            // TODO add a toast to notify user
//        }
//    }

    /**
     * for small *
     */
    public void listenerForSmallIcon(View v) {

        // v.startAnimation(ShareAnim.getMaxAnimation(400));

    }

    // for save input name

    @Override
    public void onDrawChange() {
        // TODO Auto-generated method stub
        showShareControl(View.GONE);
    }

    @Override
    public void setPaintSizeListener(int size) {
        // TODO Auto-generated method stub
        mWriteView.setPaintSize(size);
        paint_size.setBackgroundResource(R.drawable.graffiti_choose_thickness);
    }

    @Override
    public void setColor(int color) {
        // TODO Auto-generated method stub
        mWriteView.setPaintColor(color);
        m_choose_color.setBackgroundResource(R.drawable.graffiti_choose_color);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (!mWriteView.isPathEmpty()) {
                    createSaveDialog();
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    // dialog to clear
    public void createSaveDialog() {

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.save_dialog_graffity))
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                finish();

                            }
                        }).setNegativeButton(getString(R.string.cancel), null)
                .show();

    }

    // delete
    public void createDeleteDialog() {

        if (mWriteView.isPathEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)

                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                mWriteView.undo();

                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    // delete
    public void createClearDialog() {

        if (mWriteView.isPathEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.clear_dialog_graffity))
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                mWriteView.redo();

                            }
                        }).setNegativeButton(getString(R.string.cancel), null)
                .show();

    }
}

