package com.fineos.notes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.notes.bean.Note;
import com.fineos.notes.bean.NoteBg;
import com.fineos.notes.bean.NotePicture;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.NoteDao;
import com.fineos.notes.db.PictureDao;
import com.fineos.notes.util.AnimUtil;
import com.fineos.notes.util.CommonUtil;
import com.fineos.notes.util.ScreenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fineos.app.AlertDialog;
import fineos.app.ProgressDialog;

//import android.app.AlertDialog;
//HQ01573774

//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;

/**
 * intentFlag 0 新增 1预览 2 涂鸦跳到
 */
public class AddNoteActivity extends Activity implements OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private TextView done;

    private ImageView share;
    private TextView back_title;
    private EditText title, detail;
    private GridView gridNotes, bgPictures;
    private List<Note> notes;
    private List<NoteBg> noteBgs;
    private String noteType;// note type
    private int intentFlag;//note type
    private int note_id;// note id
    private String folderName;// forlder name
    private TextView time;// last time edited
    private LinearLayout noteLayout;
    private HorizontalScrollView hScrollView;
    private RadioGroup bg_rg;
    private Integer[] imageIDs = {R.drawable.bg_small_1,
            R.drawable.bg_small_2, R.drawable.bg_small_3,
            R.drawable.bg_small_4, R.drawable.bg_small_5};
    Integer[] imageIDshow = {R.drawable.bg_letter_edit1,
            R.drawable.bg_letter_edit2, R.drawable.bg_letter_edit3,
            R.drawable.bg_letter_edit4, R.drawable.bg_letter_edit5};
    private int bgPositon = 0;
    private View devide;
    private LinearLayout bottomLayout;
    private ScrollView scrollerScreen;
    private CheckBox selectBg, photo, picture, pen;// 添加便签底部4个选项
    private LinearLayout selectBg_ll, photo_ll, picture_ll, pen_ll;//bottom btn
    // 首先在您的Activity中添加如下成员变量
    final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
    private ImageView weibo, weixin, quanzi, email,save;
    private String sharedPicture;
    private TextView add_title;
    private EditText dynamic_editText;
    private AlertDialog shareDialog;
    private boolean isFocus = false;// Save note ?
    private int bg;//note backgroud
    private boolean isHavePicture = false;//note is picuter?
    private int countPicture = 0;////note is picuter?
    private int selectedPosition;
    private RadioButton yellow_bg_rb, aqua_bg_rb, pink_bg_rb, wathet_bg_rb, black_bg_rb;//5 background
    private RelativeLayout bootLayout;
    private View top_view;
    private TextView dynamic_bootom_view;
    private int scrolledY = 0;
    private int TypeEdit = 1;//编辑
    private int TypeAdd = 0;//新建
    private int Type = 0;//default 0

    private int countDetail = 0;
    private ProgressDialog progressDialog;

    private static  Bitmap shareBitmap = null;
    private MyHandler myHandler =new MyHandler();;
    private boolean isAlreadySaved = false;
    private int windWidth = 0;
    private int windHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addnote_layout);

        initView();
        setActionBar();

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        windWidth = mDisplayMetrics.widthPixels;

        windHeight = mDisplayMetrics.heightPixels;

        doIntent();
    }

    private void initView() {
        devide = findViewById(R.id.v_devide);
        detail = (EditText) findViewById(R.id.et_detail);
        scrollerScreen = (ScrollView) findViewById(R.id.scroll_screen);

        // add new note view
        hScrollView = (HorizontalScrollView) findViewById(R.id.hs_view);
        bg_rg = (RadioGroup) findViewById(R.id.rg_select_bg);
        yellow_bg_rb = (RadioButton) findViewById(R.id.rb_bg_yellow);
        aqua_bg_rb = (RadioButton) findViewById(R.id.rb_bg_aqua);
        pink_bg_rb = (RadioButton) findViewById(R.id.rb_bg_pink);
        wathet_bg_rb = (RadioButton) findViewById(R.id.rb_bg_wathet);
        black_bg_rb = (RadioButton) findViewById(R.id.rb_bg_black);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_menu);
        selectBg_ll = (LinearLayout) findViewById(R.id.ly_bootom_bg);
        selectBg = (CheckBox) findViewById(R.id.cb_bootom_bg);
        photo_ll = (LinearLayout) findViewById(R.id.ly_bootom_photo);
        photo = (CheckBox) findViewById(R.id.cb_bootom_photo);
        picture_ll = (LinearLayout) findViewById(R.id.ly_bootom_picture);
        picture = (CheckBox) findViewById(R.id.cb_bootom_picture);
        pen_ll = (LinearLayout) findViewById(R.id.ly_bootom_pen);
        pen = (CheckBox) findViewById(R.id.cb_bootom_pen);

        // edit note view
        noteLayout = (LinearLayout) findViewById(R.id.layout_note);
//        noteBg = (RelativeLayout) findViewById(R.id.relayout_notebg);

        bg_rg.setOnCheckedChangeListener(this);
        selectBg.setOnCheckedChangeListener(this);
        photo.setOnCheckedChangeListener(this);
        picture.setOnCheckedChangeListener(this);
        pen.setOnCheckedChangeListener(this);

        scrollerScreen.setOnTouchListener(new TouchListenerImpl());

//        bootLayout = (RelativeLayout) findViewById(R.id.relayout_boot);


    }

    private class TouchListenerImpl implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_MOVE:
//                    int scrollY = view.getScrollY();
//                    int height = view.getHeight();
//                    int scrollViewMeasuredHeight = scrollerScreen.getChildAt(0).getMeasuredHeight();
//                    if(scrollY==0){
//                        System.out.println("滑动到了顶端 view.getScrollY()="+scrollY);
//                    }
//                    if((scrollY+height)==scrollViewMeasuredHeight){
//                        System.out.println("滑动到了底部 scrollY="+scrollY);
//                        System.out.println("滑动到了底部 height="+height);
//                        System.out.println("滑动到了底部 scrollViewMeasuredHeight="+scrollViewMeasuredHeight);
//                    }
                    break;

                default:
                    break;
            }
            return false;
        }

    }

    ;

    private void doIntent() {
        Intent intent = getIntent();
        intentFlag = intent.getIntExtra("intentFlag", -1);
        selectedPosition = intent.getIntExtra("selectedPosition", 0);
        folderName = intent.getStringExtra(Constant.TABLE_NOTE_FOLDER);
        note_id = intent.getIntExtra(Constant.TABLE_NOTE_ID, -1);
        switch (intentFlag) {
            case 0://新建
                yellow_bg_rb.setChecked(true);
                share.setVisibility(View.GONE);
                isFocus = true;
                detail.setHint(getString(R.string.hint_detail));
                detail.requestFocus();
                Type = 0;

                detail.addTextChangedListener(new TextAddListener());
                detail.setOnFocusChangeListener(new TextOnFocusListener(TypeAdd));
                noteLayout.setBackgroundResource(imageIDshow[0]);
                mSetTextColor(0);

 		//HQ01573740
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 0, 80);
                scrollerScreen.setLayoutParams(params);
                break;
            case 1://预览 编辑

                back_title.setText(getString(R.string.preview_note));
                selectBg.setChecked(false);
                bottomLayout.setVisibility(View.GONE);
                detail.setOnFocusChangeListener(new TextOnFocusListener(TypeEdit));
                Type = 1;

                showEditNote(note_id);


                PictureDao pictureDao = new PictureDao(this);
                List<NotePicture> notePictures;
                notePictures = pictureDao.selectPictureByNoteId(note_id);
                int size = notePictures.size();

                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        LayoutInflater inflater = getLayoutInflater();
                        View view = inflater.inflate(R.layout.dynamic_note_view, null);
                        final ImageView imageView = (ImageView) view.findViewById(R.id.iv_show_insertImage);
                        dynamic_editText = (EditText) view.findViewById(R.id.et_show_inertContext);
                        dynamic_bootom_view = (TextView) view.findViewById(R.id.view_dynamic_bootom);

                        String path = notePictures.get(i).getImagePath();
                        String content = notePictures.get(i).getContent();
                        int pictureId = notePictures.get(i).getImageId();
                        Log.w(Constant.TAG, "AddNoteActivity pictureId:" + pictureId);

                        String name = null;
                        mSetTextColor(bg);
                        if (!TextUtils.isEmpty(path)) {
                            name = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
                        }

                        imageView.setOnClickListener(new ImageListener(pictureId, name, imageView));
                        dynamic_editText.addTextChangedListener(new EditContentWatch(pictureId, name, content, dynamic_editText));
                        dynamic_editText.setOnFocusChangeListener(new TextOnFocusListener(TypeEdit));
			//HQ01573774
			dynamic_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1500)});
                        noteLayout.addView(view);

                        if (content != null) {
                            dynamic_editText.setText(content);
                        }
                        if (path != null) {
                            //HQ01784660 @{
                            File file = new File(path);
                            boolean isFileExit = file.exists();
                            //@}
                            if (isFileExit) {
                                countPicture++;
                                /**
                                 * 1
                                 */
//                            Bitmap smalBitmap = ImageTools.getZoomImage(path, 636, 1120);
//                            Log.w(Constant.TAG, "AddNoteActivity imageView.getWidth() imageView.getHeight():" + imageView.getWidth() + " " + imageView.getHeight());
//                            Log.w(Constant.TAG, "AddNoteActivity smalBitmap.getWidth() smalBitmap.getHeight():" + smalBitmap.getWidth() + " " + smalBitmap.getHeight());
//                            imageView.setMaxHeight(1120);
//                            imageView.setMaxWidth(636);
//                            imageView.setAdjustViewBounds(true);
//                            imageView.setImageBitmap(smalBitmap);

//                            /**
//                             * 2
//                             */
                            String uri = "file://"+path;
                            ImageLoader imageLoader = ImageLoader.getInstance();
//                          imageLoader.displayImage(uri,imageView);
                            imageLoader.displayImage(uri, imageView, null, new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {

                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {

                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                                    int width = bitmap.getWidth();
                                    int height = bitmap.getHeight();
                                    if (width > windWidth-32) {
                                        width = windWidth-32;
                                    }
                                    if (height > windHeight-32) {
                                        height = windHeight-32;
                                    }
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                                    imageView.setLayoutParams(params);
//                                    params.setMargins(32, 16, 32, 16);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                    }
                                });
                            } else {
                                imageView.setBackgroundResource(R.drawable.ic_picture_no_big);
                            }
                        }

                    }
                }

                mSetTextColor(bg);
                break;
        }
    }


    private void mSetTextColor(int colorId) {
        switch (colorId) {
            case 0:
                if (dynamic_editText != null) {
                    dynamic_editText.setTextColor(getResources().getColor(R.color.bg_letter_0_detail));
                }
                detail.setTextColor(getResources().getColor(R.color.bg_letter_0_detail));
                break;
            case 1:
                if (dynamic_editText != null) {
                    dynamic_editText.setTextColor(getResources().getColor(R.color.bg_letter_1_detail));
                }
                detail.setTextColor(getResources().getColor(R.color.bg_letter_1_detail));
                break;
            case 2:
                if (dynamic_editText != null) {
                    dynamic_editText.setTextColor(getResources().getColor(R.color.bg_letter_2_detail));
                }
                detail.setTextColor(getResources().getColor(R.color.bg_letter_2_detail));

                break;
            case 3:
                if (dynamic_editText != null) {
                    dynamic_editText.setTextColor(getResources().getColor(R.color.bg_letter_3_detail));
                }
                detail.setTextColor(getResources().getColor(R.color.bg_letter_3_detail));

                break;
            case 4:
                if (dynamic_editText != null) {
                    dynamic_editText.setTextColor(getResources().getColor(R.color.bg_letter_4_detail));
                }
                detail.setTextColor(getResources().getColor(R.color.bg_letter_4_detail));

                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_bg_yellow:
                bgPositon = 0;
                mSetTextColor(0);
                selectBg.setChecked(false);
                hScrollView.setVisibility(View.GONE);
                noteLayout.setBackgroundResource(imageIDshow[0]);
                break;
            case R.id.rb_bg_aqua:
                bgPositon = 1;
                mSetTextColor(1);
                selectBg.setChecked(false);
                hScrollView.setVisibility(View.GONE);
                noteLayout.setBackgroundResource(imageIDshow[1]);
                break;
            case R.id.rb_bg_pink:
                bgPositon = 2;
                mSetTextColor(2);
                selectBg.setChecked(false);
                hScrollView.setVisibility(View.GONE);
                noteLayout.setBackgroundResource(imageIDshow[2]);
                break;
            case R.id.rb_bg_wathet:
                bgPositon = 3;
                mSetTextColor(3);
                selectBg.setChecked(false);
                hScrollView.setVisibility(View.GONE);
                noteLayout.setBackgroundResource(imageIDshow[3]);
                break;
            case R.id.rb_bg_black:
                bgPositon = 4;
                mSetTextColor(4);
                selectBg.setChecked(false);
                hScrollView.setVisibility(View.GONE);
                noteLayout.setBackgroundResource(imageIDshow[4]);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.cb_bootom_bg:
                if (hScrollView.getVisibility() == View.VISIBLE) {
                    hScrollView.setVisibility(View.GONE);
                } else {
                    hScrollView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.cb_bootom_photo:
                selectBg.setChecked(false);

                // 打开照相机的方法
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
                    intent.setPackage("com.fineos.camera");
                    // 指定拍摄照片保存路径 否则获取的是缩略图。 tempImage.PNG
                    Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tempImage.PNG"));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, Constant.CAMERAR_REQUESTCODE);
                    // HQ01679644 dpc @{
                    overridePendingTransition(0, 0);
                    //@}
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(AddNoteActivity.this, "can not open Camera", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.cb_bootom_picture:
                selectBg.setChecked(false);

                Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                picture.setPackage("com.fineos.gallery3d");
                startActivityForResult(picture, Constant.PICTURE_REQUESTCODE);

                break;
            case R.id.cb_bootom_pen:
                selectBg.setChecked(false);

                Intent intent = new Intent(this, GraffitiActivity.class);
                intent.putExtra("noteId", note_id);
                intent.putExtra("intentFlag", 1);
                startActivityForResult(intent, Constant.ADDNOTE_REQUESTCODE);
                break;

        }

    }


    class ImageListener implements OnClickListener {
        private String pictrueName;
        private int pictureId;
        private ImageView imageView;

        public ImageListener(int pictureId, String pictrueName, ImageView imageView) {
            this.pictrueName = pictrueName;
            this.pictureId = pictureId;
            this.imageView = imageView;

        }

        @Override
        public void onClick(View v) {
            showImageDialog(pictrueName, pictureId, imageView);
        }
    }

    class EditContentWatch implements TextWatcher {
        private String pictureName;
        private NotePicture notePicture;
        private PictureDao pictureDao;
        private EditText et;
        private int pictureId;
        private String edStr;
        private String content;

        public EditContentWatch(int pictureId, final String pictureName, String content, EditText et) {
            this.pictureName = pictureName;
            this.pictureId = pictureId;
            this.et = et;
            this.content = content;
            pictureDao = new PictureDao(AddNoteActivity.this);

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            if (s.length() == 0) {
//                done.setVisibility(View.GONE);
//                share.setVisibility(View.VISIBLE);
//            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            edStr = s.toString().trim();
            if (!TextUtils.isEmpty(edStr)) {
                countDetail++;
            } else {
                countDetail--;
            }
            pictureDao.updateContentByPictureId(pictureId, edStr);
        }
    }


    class TextOnFocusListener implements OnFocusChangeListener {
        private int type;

        public TextOnFocusListener(int type) {
            this.type = type;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                isFocus = true;
                done.setVisibility(View.VISIBLE);
                done.setOnClickListener(AddNoteActivity.this);
                share.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.VISIBLE);
                if (type == TypeAdd) {//add
                    back_title.setText(getString(R.string.new_note));
                } else {//edit
                    back_title.setText(getString(R.string.edit));
                }
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 0, 80);
                scrollerScreen.setLayoutParams(params);

            } else {

            }
        }

    }


    class TextAddListener implements TextWatcher {

        @Override
        public void afterTextChanged(Editable s) {
            Log.i("dpc", "Editable-- " + s.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (!(s.toString().trim().length() == 0) && !TextUtils.isEmpty(s)) {
                done.setVisibility(View.VISIBLE);
                share.setVisibility(View.GONE);
                countDetail++;
            } else {
                done.setVisibility(View.GONE);
                countDetail--;
            }

        }

    }


    private void setAddActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.addnote_actionbar, null);
        actionBar.setCustomView(v, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        add_title = (TextView) v.findViewById(R.id.tv_title);
        done = (TextView) v.findViewById(R.id.tv_ad_done);

        add_title.setOnClickListener(this);
        done.setOnClickListener(this);
        actionBar.show();
    }

    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.editnote_actionbar, null);
        actionBar.setCustomView(v, new ActionBar.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        share = (ImageView) findViewById(R.id.iv_share);
        back_title = (TextView) findViewById(R.id.tv_back_title);
        done = (TextView) findViewById(R.id.tv_ed_done);

        share.setOnClickListener(this);
        back_title.setOnClickListener(this);
        done.setOnClickListener(this);
        actionBar.show();
    }

    private void showEditNote(int id) {
        NoteDao dao = new NoteDao(this);
        notes = dao.selectById(id);
        String titleString = notes.get(0).getTitle();
        bg = notes.get(0).getBackground();
        bgPositon = bg;
        String detailString = notes.get(0).getDetail();
        Long dataString = notes.get(0).getData();
//        SimpleDateFormat formatter = new SimpleDateFormat(
//                "yyyy年-MM月dd日-HH时mm分ss秒");
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy年-MM月dd日-HH时mm分");
        Date date = new Date(dataString);

        switch (bg) {
            case 0:
                if (detailString != "") {
                    detail.setText(detailString);
//                    detail.setTextColor(getResources().getColor(R.color.bg_letter_1_detail));
                }
//                time.setText(getString(R.string.last_time) + formatter.format(date));
//                time.setTextColor(getResources().getColor(R.color.bg_letter_time));
                noteLayout.setBackground(getResources().getDrawable(imageIDshow[0]));
                yellow_bg_rb.setChecked(true);
                break;
            case 1:
                if (detailString != "") {
                    detail.setText(detailString);
//                    detail.setTextColor(getResources().getColor(R.color.bg_letter_2_detail));
                }
//
//                time.setText(getString(R.string.last_time) + formatter.format(date));
//                time.setTextColor(getResources().getColor(R.color.bg_letter_time));
                noteLayout.setBackground(getResources().getDrawable(imageIDshow[1]));
                aqua_bg_rb.setChecked(true);
                break;
            case 2:
                if (detailString != "") {
                    detail.setText(detailString);
//                    detail.setTextColor(getResources().getColor(R.color.bg_letter_3_detail));
                }
//
//                time.setText(getString(R.string.last_time) + formatter.format(date));
//                time.setTextColor(getResources().getColor(R.color.bg_letter_time));
                noteLayout.setBackground(getResources().getDrawable(imageIDshow[2]));
                pink_bg_rb.setChecked(true);
                break;
            case 3:
                if (detailString != "") {
                    detail.setText(detailString);
//                    detail.setTextColor(getResources().getColor(R.color.bg_letter_4_detail));
                }
//
//                devide.setBackgroundColor(getResources().getColor(R.color.bg_letter_devide));
//                time.setText(getString(R.string.last_time) + formatter.format(date));
//                time.setTextColor(getResources().getColor(R.color.bg_letter_time));
                noteLayout.setBackground(getResources().getDrawable(imageIDshow[3]));
                wathet_bg_rb.setChecked(true);
                break;
            case 4:
                if (detailString != "") {
                    detail.setText(detailString);
//                    detail.setTextColor(getResources().getColor(R.color.bg_letter_5_detail));
                }
                noteLayout.setBackground(getResources().getDrawable(imageIDshow[4]));
                black_bg_rb.setChecked(true);
                break;
        }
    }

    /**
     * share
     */
    private void showShareDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.share_dialog, null);
        weibo = (ImageView) view.findViewById(R.id.iv_weibo);
        weixin = (ImageView) view.findViewById(R.id.iv_weixin);
        quanzi = (ImageView) view.findViewById(R.id.iv_quanzi);
       // email = (ImageView) view.findViewById(R.id.iv_email);
	//HQ01579026
	email = (ImageView) view.findViewById(R.id.iv_more);
        save = (ImageView) view.findViewById(R.id.iv_save);
        weibo.setOnClickListener(this);
        weixin.setOnClickListener(this);
        quanzi.setOnClickListener(this);
        email.setOnClickListener(this);
        save.setOnClickListener(this);
        builder.setView(view);
        builder.setTitle(getString(R.string.share_title));
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (shareBitmap != null && !shareBitmap.isRecycled()) {
                    shareBitmap.recycle();
                    shareBitmap = null;
                    System.gc();  //提醒系统及时回收
                }
            }
        });

        shareDialog = builder.show();

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

    private void saveNote(String folderName, String detail, int bgPositon, int intentFlag) {
        NoteDao dao = new NoteDao(this);
        Note note = new Note();
        note.setFolder(folderName);
        note.setDetail(detail);
        note.setBackground(bgPositon);
        Long data = System.currentTimeMillis();
        note.setData(data);
        if (intentFlag == 1) {
            //1 预览,编辑
            Log.w(Constant.TAG, "AddNoteActivity saveNote note:" + note);
            Log.w(Constant.TAG, "AddNoteActivity saveNote note_id:" + note_id);
            dao.updateById(note, note_id);

        } else {// 0新建
            dao.add(note);
            //此时需要更新一下图片表，的noteId
            int id = dao.selectByData(data).get(0).getId();
            note_id = id;
            PictureDao pictureDao = new PictureDao(AddNoteActivity.this);
            pictureDao.updateByNoteId(-1, id);
        }
    }

    private void setSave(int flag, boolean isFinish) {
        String detail1 = detail.getText().toString().trim();
        if (flag == 1) {//edit note 1
            if (bg != bgPositon) {
                saveNote(folderName, detail1, bgPositon, intentFlag);
            } else {
                if (isFocus) {//focus and changebg
                    saveNote(folderName, detail1, bg, intentFlag);
                }
            }
        } else {//add note 0
            if (!TextUtils.isEmpty(detail1) || countPicture > 0 || countDetail > 0) {
                saveNote(folderName, detail1, bgPositon, intentFlag);
            }
        }
        if (isFinish) {
            finish();
            AnimUtil.onBackPressedAnim(AddNoteActivity.this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back_title:// 1edit note back
                setSave(Type, true);
                if (detail != null) {
                    CommonUtil.closeKeybord(detail,AddNoteActivity.this);
                }
                break;
            case R.id.tv_ed_done:
//                setSave(Type,false);
                share.setVisibility(View.VISIBLE);
  noteLayout.requestFocus();                
  noteLayout.setFocusableInTouchMode(true);   
                done.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 0, 0);
                scrollerScreen.setLayoutParams(params);
                CommonUtil.closeKeybord(detail, AddNoteActivity.this);
                //noteLayout.requestFocus();
                back_title.setText(getString(R.string.preview_note));
                bottomLayout.setVisibility(View.GONE);
noteLayout.requestFocus();
noteLayout.setFocusableInTouchMode(true); 

                break;
            case R.id.iv_share:
                if (dynamic_bootom_view != null) {
                    dynamic_bootom_view.setVisibility(View.VISIBLE);
                }
                if (countPicture <= 10) {

                    recycleBitmap(shareBitmap);

                    if (intentFlag == 0) {//add note
                        shareBitmap = ScreenUtils.getBitmapByView(AddNoteActivity.this, scrollerScreen, note_id, bgPositon);
                    } else if (intentFlag == 1) {//edit note
                        shareBitmap = ScreenUtils.getBitmapByView(AddNoteActivity.this, scrollerScreen, note_id, bgPositon);
                    }

//                MyHandler myHandler =new MyHandler(shareBitmap);
//                new MyThread(myHandler).start();
//                sharedPicture = ScreenUtils.savePic(ScreenUtils.compressImage(bitmap));
                    initSocialSDK(shareBitmap);
                    showShareDialog();
                    if (dynamic_bootom_view != null) {
                        dynamic_bootom_view.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(AddNoteActivity.this,getString(R.string.fileTooLarge),Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.iv_weibo:
                //分享到新浪微博
                shareToSina(AddNoteActivity.this);
                break;
            case R.id.iv_weixin:
                // 分享到微信好友
                shareToWeiXin(AddNoteActivity.this);
                if (shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
                break;
            case R.id.iv_quanzi:
                // 分享到微信朋友圈
                shareToCir(AddNoteActivity.this);
                if (shareDialog.isShowing()) {
                    shareDialog.dismiss();
                }
                break;
            case R.id.iv_more://发送到邮件//HQ01579026 改为more
                shareDialog.dismiss();
                showLoadProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sharedPicture = null;
                        sharedPicture = ScreenUtils.savePic(AddNoteActivity.this,shareBitmap,"more");
                        myHandler.sendEmptyMessage(1);
                    }
                }).start();
                break;
            case R.id.iv_save://保存到本地 picture
                Log.w("dpc", "AddNoteActivity iv_save");
                shareDialog.dismiss();
                showLoadProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.w("dpc", "AddNoteActivity iv_save");
                        sharedPicture = null;
                        sharedPicture = ScreenUtils.savePic(AddNoteActivity.this, shareBitmap, "download");
                        myHandler.sendEmptyMessage(2);
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private class  MyHandler extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:// send to email
                    if(!TextUtils.isEmpty(sharedPicture) ){
                        if (progressDialog != null) {
                            //分享到Email
                            sendEmail(AddNoteActivity.this, null, null, null, sharedPicture);
                            if (shareDialog.isShowing()) {
                                shareDialog.dismiss();
                            }
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                    break;
                case 2:// download picture
                    if(!TextUtils.isEmpty(sharedPicture) ){
                        if (progressDialog != null) {
                            Toast.makeText(AddNoteActivity.this,getString(R.string.savesucess)+":"+sharedPicture,Toast.LENGTH_SHORT).show();
                            if (shareDialog.isShowing()) {
                                shareDialog.dismiss();
                            }
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                    break;
            }
        }
    }


    private void showLoadProgress() {
        progressDialog = new ProgressDialog(AddNoteActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.progress_message));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                recycleBitmap(shareBitmap);
                progressDialog = null;
            }
        });
        progressDialog.show();
    }

    private void recycleBitmap(Bitmap bitmap){
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            shareBitmap = null;
            System.gc();  //提醒系统及时回收
        }
    }

    /**
     * @功能描述 : 添加微信平台分享
     * @return
     */
    private void addWXPlatform() {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = "wx967daebe835fbeac";
        String appSecret = "5bb696d9ccd75a38c8a0bfe0675559b3";
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(AddNoteActivity.this, appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(AddNoteActivity.this, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    /**
     * 初始化SDK，添加一些平台
     */
    private void initSocialSDK(Bitmap bitmap) {
        String wxappId = "wx9c3a792bc7b42664";
        String wxappSecret = "9983539294a1e153b8e64ef7673f995f";


//        //添加新浪微博分享
//        SinaSsoHandler sinaSsoHandler = new SinaSsoHandler();
//        sinaSsoHandler.addToSocialSDK();
//        //设置新浪SSO handler
//        mController.getConfig().setSsoHandler(new SinaSsoHandler());
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(AddNoteActivity.this, wxappId, wxappSecret);
        wxHandler.showCompressToast(false);//关掉“图片太大超过32K”这句
        wxHandler.addToSocialSDK();
        //设置微信好友分享内容
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        //设置分享图片
        UMImage localImage = new UMImage(AddNoteActivity.this, bitmap);
//        UMImage localImage = new UMImage(AddNoteActivity.this, BitmapFactory.decodeFile(picture));
//        UMImage localImage = new UMImage(AddNoteActivity.this, picture);
        weixinContent.setShareImage(localImage);
        mController.setShareMedia(weixinContent);

        //添加微信圈平台
        UMWXHandler wxCircleHandler = new UMWXHandler(AddNoteActivity.this, wxappId, wxappSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
        wxCircleHandler.showCompressToast(false);
        //设置微信朋友圈分享内容
        CircleShareContent circleMedia = new CircleShareContent();
        UMImage umImage = new UMImage(AddNoteActivity.this,  bitmap);
//        UMImage umImage = new UMImage(AddNoteActivity.this,  BitmapFactory.decodeFile(picture));
//        UMImage umImage = new UMImage(AddNoteActivity.this, picture);
        circleMedia.setShareImage(umImage);
        mController.setShareMedia(circleMedia);


    }

    private void shareToSina(final Context context) {
        mController.postShare(context, SHARE_MEDIA.SINA, new SocializeListeners.SnsPostListener() {
            @Override
            public void onStart() {
                Toast.makeText(context, "开始分享.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int eCode, SocializeEntity socializeEntity) {
                if (eCode == 200) {
                    Toast.makeText(context, "分享成功.", Toast.LENGTH_SHORT).show();
                } else {
                    String eMsg = "";
                    if (eCode == -101) {
                        eMsg = "没有授权";
                    }
                    Toast.makeText(context, "分享失败[" + eCode + "] " +
                            eMsg, Toast.LENGTH_SHORT).show();
                }
                shareDialog.dismiss();
            }
        });
    }

    private void shareToWeiXin(final Context context) {
        mController.postShare(context, SHARE_MEDIA.WEIXIN, new SocializeListeners.SnsPostListener() {
            @Override
            public void onStart() {
//                Toast.makeText(context, "分享成功.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int eCode, SocializeEntity socializeEntity) {
                if (eCode == 200) {
//                    Toast.makeText(context, "分享成功.", Toast.LENGTH_SHORT).show();
                } else {
                    String eMsg = "";
                    if (eCode == -101) {
                        eMsg = "没有授权";
                    }
//                    Toast.makeText(context, "分享失败[" + eCode + "] " +
//                            eMsg, Toast.LENGTH_SHORT).show();
                }
                recycleBitmap(shareBitmap);
            }
        });
    }

    private void shareToCir(final Context context) {
        mController.postShare(context, SHARE_MEDIA.WEIXIN_CIRCLE, new SocializeListeners.SnsPostListener() {
            @Override
            public void onStart() {
//                Toast.makeText(context, "分享成功.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int eCode, SocializeEntity socializeEntity) {
                if (eCode == 200) {
//                    Toast.makeText(context, "分享成功.", Toast.LENGTH_SHORT).show();
                } else {
                    String eMsg = "";
                    if (eCode == -101) {
                        eMsg = "没有授权";
                    }
//                    Toast.makeText(context, "分享失败[" + eCode + "] " +
//                            eMsg, Toast.LENGTH_SHORT).show();
                }
                recycleBitmap(shareBitmap);
            }
        });
    }


    /**
     * 调用系统程序发送邮件
     *
     * @author Johnson
     */
    private  void sendEmail(Context context, String[] to, String subject,
                                  String body, String path) {
        Intent email = new Intent(Intent.ACTION_SEND);
//        //邮件发送类型：带附件的邮件
//        email.setType("application/octet-stream");
//        //附件
//        File file = new File(path);
//        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//        //调用系统的邮件系统
//        context.startActivity(Intent.createChooser(email, "请选择邮件发送软件"));

        if (to != null) {
            email.putExtra(android.content.Intent.EXTRA_EMAIL, to);
        }
        if (subject != null) {
            email.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        }
        if (body != null) {
            email.putExtra(android.content.Intent.EXTRA_TEXT, body);
        }
        if (path != null) {
            //邮件发送类型：带附件的邮件
            //email.setType("application/octet-stream");
	   //HQ01579026 
	    email.setType("image/*");
            //附件
            File file = new File(path);
            email.putExtra(android.content.Intent.EXTRA_STREAM,
                    Uri.fromFile(file));
        }
        context.startActivity(Intent.createChooser(email, getString(R.string.chosesoftware)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //camera
        if (requestCode == Constant.CAMERAR_REQUESTCODE) {
            File file = new File(Environment.getExternalStorageDirectory() + "/tempImage.PNG");
            String path = Environment.getExternalStorageDirectory() + "/tempImage.PNG";
            ///storage/sdcard0/tempImage.PNG
            if (file.exists()) {

                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tempImage.PNG"));
//                doCropPhoto(uri);
                // file:///storage/sdcard0/tempImage.PNG
                Log.w(Constant.TAG, "AddNoteActivity onActivityResult camera uri:" + uri);
                Intent intent = new Intent(AddNoteActivity.this, CropActivity.class);
                intent.putExtra("titleName", getString(R.string.insert_pictrue));
                intent.setData(uri);
                intent.putExtra("Type", 0);//camera path
                intent.putExtra("noteId", note_id);
                startActivityForResult(intent, Constant.ADDNOTE_REQUESTCODE);
            }

        }//picture
        else if (requestCode == Constant.PICTURE_REQUESTCODE && data != null) {
//            //1.调用系统裁剪
//            Uri uri = data.getData();
//            doCropPhoto(uri);
            //2.自定义裁剪功能
            Uri uri = data.getData();
            Intent intent = new Intent(AddNoteActivity.this, CropActivity.class);
            intent.putExtra("titleName", getString(R.string.insert_pictrue));
            intent.putExtra("noteId", note_id);
            intent.putExtra("Type", 1);//picture uri
            intent.setData(uri);
            startActivityForResult(intent, Constant.ADDNOTE_REQUESTCODE);
        } else if (requestCode == Constant.ADDNOTE_REQUESTCODE && resultCode == Constant.CROP_RESULTCODE && data != null) {
            //便签 -->裁剪  --> 便签
            String pictureName = data.getStringExtra("pictureName");
            note_id = data.getIntExtra("noteId", -1);
            addView(pictureName, note_id);
        } else if (requestCode == Constant.ADDNOTE_REQUESTCODE && resultCode == Constant.GRAFFITTI_RESULTCODE && data != null) {
            //便签 --> 涂鸦 --> 便签
            String pictureName = data.getStringExtra("pictureName");
            note_id = data.getIntExtra("noteId", -1);
            addView(pictureName, note_id);
        } else if (requestCode == Constant.ADDNOTE_REQUESTCODE && resultCode == Constant.CROP_RESULTCODE && data != null) {
            //便签--> 裁剪 -->涂鸦  (-->裁剪)-->便签
            String pictureName = data.getStringExtra("pictureName");
            note_id = data.getIntExtra("noteId", -1);
            addView(pictureName, note_id);
        }


    }


    /**
     * default noteId == -1
     *
     * @param pictureName
     * @param noteId
     */
    private void addView(String pictureName, int noteId) {
        detail.setHint("");
        String path = Constant.Dir + pictureName + ".PNG";

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dynamic_note_view, null);
        noteLayout.addView(view);

        ImageView imageView = (ImageView) view.findViewById(R.id.iv_show_insertImage);
        imageView.setOnClickListener(new ImageListener(noteId, pictureName, imageView));
//        Bitmap smalBitmap = ImageTools.getZoomImage(path, 636, 1120);

        String uri = "file://" + path;
        imageView.setMaxHeight(windHeight-32);
        imageView.setMaxWidth(windWidth-32);
        imageView.setAdjustViewBounds(true);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(uri, imageView);


        NotePicture notePicture = new NotePicture();
        PictureDao pictureDao = new PictureDao(AddNoteActivity.this);
        notePicture.setNoteId(noteId);
        notePicture.setImagePath(Constant.Dir + pictureName + Constant.PICTURETYPE);
        pictureDao.add(notePicture);
//      isHavePicture = true;
        countPicture++;
        List<NotePicture> notePictures;
        notePictures = pictureDao.selectPictureIdByName(pictureName);

        int pictureId = notePictures.get(0).getImageId();
        imageView.setOnClickListener(new ImageListener(pictureId, pictureName, imageView));
//        imageView.setOnClickListener(new ImageOnClickListener(pictureId, pictureName, imageView));
//        imageView.setMaxHeight(1120);
//        imageView.setMaxWidth(636);
//        imageView.setAdjustViewBounds(true);
//        imageView.setImageBitmap(smalBitmap);
//        Log.w(Constant.TAG, "AddNoteActivity imageView.getWidth() imageView.getHeight():" + imageView.getWidth() + " " + imageView.getHeight());
//        Log.w(Constant.TAG, "AddNoteActivity smalBitmap.getWidth() smalBitmap.getHeight():" + smalBitmap.getWidth() + " " + smalBitmap.getHeight());
//        EditText editText = (EditText) view.findViewById(R.id.et_show_inertContext);
        dynamic_editText = (EditText) view.findViewById(R.id.et_show_inertContext);
//        dynamic_editText.addTextChangedListener(new AddContentWatch(noteId, pictureName));
        dynamic_editText.addTextChangedListener(new EditContentWatch(pictureId, pictureName, null, dynamic_editText));
        dynamic_editText.setOnFocusChangeListener(new TextOnFocusListener(Type));
        mSetTextColor(bgPositon);


        done.setVisibility(View.VISIBLE);
        if (share != null) {
            share.setVisibility(View.GONE);
        }
        hScrollView.setVisibility(View.GONE);
    }


    private void showImageDialog(final String pictureName, final int pictureId, final ImageView imageView) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
        //String save = getString(R.string.save_dialogtitle_addnote);
        String delete = getString(R.string.delete_dialogtitle_addnote);
        String details = getString(R.string.details_dialogtitle_addnote);
        //HQ01658467
        final String[] items = {delete, details};
//        final String[] items = {save, delete, details};

        View view =LayoutInflater.from(AddNoteActivity.this).inflate(R.layout.arraylistview,null);
        ListView mListView = (ListView)view.findViewById(R.id.list1);
        ArrayAdapter<String> arrayAdapter =  new ArrayAdapter<String>(this, R.layout.imagedialog, items);
        mListView.setAdapter(arrayAdapter);
        builder.setView(mListView);
        final AlertDialog dialog = builder.show();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    //HQ01658467
//                    case 0://save
//                        showImage(null, items[0], getString(R.string.save_dialogmessage_addnote), pictureName, -1);
//                        dialog.dismiss();
//                        break;
                    case 0://delete
                        showImage(imageView, items[0], getString(R.string.delete_dialogmessage_addnote), pictureName, pictureId);
                        dialog.dismiss();
                        break;
                    case 1://details
                        showImage(null, items[1], getString(R.string.details_dialogmessage_addnote) + pictureName + Constant.PICTURETYPE, pictureName, -1);
                        dialog.dismiss();
                        break;
                }
            }
        });
    }



    private void showImage(final ImageView imageView, final String title, String message, final String pictureName, final int pictureId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNoteActivity.this);
//        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (title.equals(getString(R.string.save_dialogtitle_addnote))) {
                    Bitmap bitmap = BitmapFactory.decodeFile(Constant.Dir + pictureName + Constant.PICTURETYPE);
                    String outPath = Constant.SAVEPICTUREDIR+getString(R.string.app_name)+File.separator+"myPicture"+File.separator;
                    CommonUtil.saveBitMap(AddNoteActivity.this,bitmap, outPath, pictureName);
                    File file = new File(outPath + pictureName + Constant.PICTURETYPE);
                    if (file.exists()) {
                        Toast.makeText(AddNoteActivity.this, getString(R.string.save_sucess_addnote) + " \n/storage/sdcard0/" + pictureName + Constant.PICTURETYPE, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddNoteActivity.this, getString(R.string.save_failed_addnote), Toast.LENGTH_SHORT).show();
                    }

                } else if (title.equals(getString(R.string.delete_dialogtitle_addnote))) {
                    //delete picture
                    PictureDao pictureDao = null;
                    if (pictureDao == null) {
                        pictureDao = new PictureDao(AddNoteActivity.this);
                    }
                    pictureDao.deletePictureById(pictureId);
                    imageView.setVisibility(View.GONE);
                    noteLayout.removeView(imageView);

                    isFocus = true;
                    countPicture--;
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    params.setMargins(0, 0, 0, 80);
                    scrollerScreen.setLayoutParams(params);

                    done.setVisibility(View.VISIBLE);
                    share.setVisibility(View.GONE);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * when onclick backButton
     */
    private void showSaveNoteDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

//    @Override
//    public void onBackPressed() {
//        Log.w("dpc", "onBackPressed");
//        super.onBackPressed();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (intentFlag == 0) {//add
                setSave(0, true);
            } else if (intentFlag == 1) {// edit
                setSave(1, true);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detail != null) {
            CommonUtil.closeKeybord(detail,AddNoteActivity.this);
        }
    }
}
