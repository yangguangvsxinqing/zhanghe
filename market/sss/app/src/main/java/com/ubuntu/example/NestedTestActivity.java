package com.ubuntu.example;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 16-7-25.
 */
public class NestedTestActivity extends AppCompatActivity{
    private List<String> mList;
    private RecyclerView mListView;
    private String TAG = "NestedTestActivity";
    private int dragPointX;//开始拖拽的x坐标
    private int dragPointY;//开始拖拽的y坐标
    private int dragOffsetX;
    private int dragOffsetY;
    private int dragImageId;
    private int itemHeight;
    private int itemWidth;
    private int moveHeight = 0;
    private int upScrollBounce;
    private int downScrollBounce;
    private int dragColor = Color.GRAY;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private int changePosition = -1;
    private long scrollDelayMillis = 10L;

    private int middleX;
    private int middleY;
    private boolean isDrag = true;
    private TextView depentent;
    private Boolean flag = false;
    private int i = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nestedscroll);
        initListData();
        mListView = (RecyclerView) findViewById(R.id.list);
        MyAdapter adapter = new MyAdapter(this,mList);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(adapter);

        depentent = (TextView) findViewById(R.id.depentent);
        depentent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewCompat.offsetTopAndBottom(v, 10);
            }
        });

        depentent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                startDrag(view.getTop(),view.getLeft());
                flag = true;
                return false;
            }
        });

        depentent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                if (view != null&&flag) {
                    int action = ev.getAction();
                    int moveY = (int)ev.getRawY();
                    int moveX = (int)ev.getRawX();
                    if(isDrag){
                        isDrag = false;
                        middleX = moveX;
                        middleY = moveY;
                    }
                    switch(action) {
                        case MotionEvent.ACTION_UP:
                            flag = false;
                            isDrag = true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            onDrag(moveX, moveY);
                            break;
                    }
                    return true;
                }

                return false;
            }
        });
        int tmp[] = new int[2];
        depentent.getLocationInWindow(tmp);
        dragPointX = tmp[0];
        dragPointY = tmp[1];
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initListData() {
        mList = new ArrayList<String>();
        for(int i=0;i<16;i++){
            mList.add("item"+i);
        }
    }

    private boolean Flag(int middleX,int x, int middleY, int y){
        if(Math.abs(x - middleX) < 2&&Math.abs(y - middleY) < 2){
            return false;
        }
        return true;
    }

    private void onDrag(int x, int y) {
        Log.v(TAG,"sss onDrag x="+(x-middleX));
        Log.v(TAG,"sss onDrag y="+(y-middleY));
        Log.v(TAG,"sss onDrag dragPointX="+dragPointX);
        Log.v(TAG,"sss onDrag dragPointY="+dragPointY);
        if (depentent != null&&Flag(middleX,x,middleY,y)) {

//            AnimatorSet animatorSet = new AnimatorSet();
//
//            ObjectAnimator obj1 = ObjectAnimator.ofFloat(depentent, "translationX", dragPointX, dragPointX+x-middleX);
//            ObjectAnimator obj2 = ObjectAnimator.ofFloat(depentent, "translationY", dragPointY, dragPointY+y-middleY);
//            animatorSet.play(obj1).with(obj2);
//            animatorSet.setDuration(10);
//            animatorSet.start();
            ViewCompat.offsetTopAndBottom(depentent, y-middleY);
            dragPointX+=(x-middleX);
            dragPointY+=(y-middleY);
            middleX = x;
            middleY = y;
        }
    }

    private void startDrag(int x, int y) {
        stopDrag();
    }

    private void stopDrag() {
        this.changePosition = -1;
        this.isDrag = false;
        flag = false;
    }


    class RefreshHandler extends Handler {
        boolean isUp;

        public RefreshHandler(Looper looper, boolean isUp){
            super(looper);
            this.isUp = isUp;
        }

        public RefreshHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg){
            if (isDrag) {
//                if (this.isUp)
//                    actUp();
//                else {
//                    actDown();
//                }
                sleep(scrollDelayMillis);
            }
        }

        public void sleep(long delayMillis) {
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }



    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private LayoutInflater mLayoutInflater;
        public MyAdapter(Context context, List<String> list){
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder myViewHolder = new MyViewHolder(mLayoutInflater.inflate(R.layout.item_lsit,parent,false));
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Log.v("ListFragment","onBindViewHolder position="+position);
            holder.text.setText(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            public TextView text;
            public MyViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.list_item);
            }
        }
    }
}
