//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.support.percent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

public class PercentLayoutHelper {
    private static final String TAG = "PercentLayout";
    private final ViewGroup mHost;
    private static final String REGEX_PERCENT = "^(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)%([wh]?)$";
    public PercentLayoutHelper(ViewGroup host) {
        this.mHost = host;
    }

    public static void fetchWidthAndHeight(LayoutParams params, TypedArray array, int widthAttr, int heightAttr) {
        params.width = array.getLayoutDimension(widthAttr, 0);
        params.height = array.getLayoutDimension(heightAttr, 0);
    }

    public void adjustChildren(int widthMeasureSpec, int heightMeasureSpec) {
        if(Log.isLoggable("PercentLayout", 3)) {
            Log.d("PercentLayout", "adjustChildren: " + this.mHost + " widthMeasureSpec: " + MeasureSpec.toString(widthMeasureSpec) + " heightMeasureSpec: " + MeasureSpec.toString(heightMeasureSpec));
        }

        int widthHint = MeasureSpec.getSize(widthMeasureSpec);
        int heightHint = MeasureSpec.getSize(heightMeasureSpec);
        int i = 0;

        for(int N = this.mHost.getChildCount(); i < N; ++i) {
            View view = this.mHost.getChildAt(i);
            LayoutParams params = view.getLayoutParams();
            if(Log.isLoggable("PercentLayout", 3)) {
                Log.d("PercentLayout", "should adjust " + view + " " + params);
            }

            if(params instanceof PercentLayoutHelper.PercentLayoutParams) {
                PercentLayoutHelper.PercentLayoutInfo info = ((PercentLayoutHelper.PercentLayoutParams)params).getPercentLayoutInfo();
                if(Log.isLoggable("PercentLayout", 3)) {
                    Log.d("PercentLayout", "using " + info);
                }

                if(info != null) {
                    if(params instanceof MarginLayoutParams) {
                        info.fillMarginLayoutParams((MarginLayoutParams)params, widthHint, heightHint);
                    } else {
                        info.fillLayoutParams(params, widthHint, heightHint);
                    }
                }
            }
        }

    }
    
    public static class PercentData {
    	public float percent = -1;
        public boolean isBaseWidth;
        public boolean isBaseHeight;

        public PercentData(float percent,boolean isBaseWidth,boolean isBaseHeight)
        {
             this.percent = percent;
             this.isBaseWidth = isBaseWidth;
             this.isBaseHeight = isBaseHeight;
        }
    }
    
    public static PercentData getPercentData(String percentStr){
        if (percentStr == null)
        {
            return null;
        }
        Pattern p = Pattern.compile(REGEX_PERCENT);
        Matcher matcher = p.matcher(percentStr);
        if (!matcher.matches())
        {
            throw new RuntimeException("the value of layout_xxxPercent invalid! ==>" + percentStr);
        }
        int len = percentStr.length();
        //extract the float value
        String floatVal = matcher.group(1);
        String lastAlpha = percentStr.substring(len - 1);

        float percent = Float.parseFloat(floatVal) / 100f;
        boolean isBasedWidth = lastAlpha.equals("w");
        boolean isBaseHeight = lastAlpha.equals("h");

        return new PercentData(percent, isBasedWidth, isBaseHeight);
    }

    public static PercentLayoutHelper.PercentLayoutInfo getPercentLayoutInfo(Context context, AttributeSet attrs) {
        PercentLayoutHelper.PercentLayoutInfo info = null;
        
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PercentLayout_Layout);
        String sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_widthPercent);
        PercentData percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent width: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            info.widthPercent = percentData;
        } 
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_heightPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent height: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            info.heightPercent = percentData;
        } 
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.leftMarginPercent = percentData;
            info.topMarginPercent = percentData;
            info.rightMarginPercent = percentData;
            info.bottomMarginPercent = percentData;
        } 
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginLeftPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent left margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.leftMarginPercent = percentData;
        }
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginRightPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent right margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.rightMarginPercent = percentData;
        }
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginTopPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent top margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.topMarginPercent = percentData;
        }
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginBottomPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent bottom margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.bottomMarginPercent = percentData;
        }
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginStartPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent start margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.startMarginPercent = percentData;
        }
        
        sizeStr = array.getString(R.styleable.PercentLayout_Layout_layout_marginEndPercent);
        percentData = getPercentData(sizeStr);
        if (percentData != null)
        {
            if (Log.isLoggable(TAG, Log.VERBOSE))
            {
                Log.v(TAG, "percent end margin: " + percentData.percent);
            }
            info = info != null ? info : new PercentLayoutInfo();
            
            info.endMarginPercent = percentData;
        }
        
   /*     float value = array.getFraction(R.styleable.PercentLayout_Layout_layout_widthPercent, 1, 1, -1.0F);
        
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent width: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.widthPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_heightPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent height: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.heightPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.leftMarginPercent = value;
            info.topMarginPercent = value;
            info.rightMarginPercent = value;
            info.bottomMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginLeftPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent left margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.leftMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginTopPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent top margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.topMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginRightPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent right margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.rightMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginBottomPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent bottom margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.bottomMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginStartPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent start margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.startMarginPercent = value;
        }

        value = array.getFraction(R.styleable.PercentLayout_Layout_layout_marginEndPercent, 1, 1, -1.0F);
        if(value != -1.0F) {
            if(Log.isLoggable("PercentLayout", 2)) {
                Log.v("PercentLayout", "percent end margin: " + value);
            }

            info = info != null?info:new PercentLayoutHelper.PercentLayoutInfo();
            info.endMarginPercent = value;
        }*/

        array.recycle();
        if(Log.isLoggable("PercentLayout", 3)) {
            Log.d("PercentLayout", "constructed: " + info);
        }

        return info;
    }

    public void restoreOriginalParams() {
        int i = 0;

        for(int N = this.mHost.getChildCount(); i < N; ++i) {
            View view = this.mHost.getChildAt(i);
            LayoutParams params = view.getLayoutParams();
            if(Log.isLoggable("PercentLayout", 3)) {
                Log.d("PercentLayout", "should restore " + view + " " + params);
            }
            
            Log.v(TAG, "restoreOriginalParams sss params="+params);
            if(params instanceof PercentLayoutHelper.PercentLayoutParams) {
                PercentLayoutHelper.PercentLayoutInfo info = ((PercentLayoutHelper.PercentLayoutParams)params).getPercentLayoutInfo();
                Log.v(TAG, "sss params.width="+params.width);
                Log.v(TAG, "sss params.height="+params.height);
//                Log.v(TAG, "sss mPreservedParams.width="+info.mPreservedParams.width);
//                Log.v(TAG, "sss mPreservedParams.height="+info.mPreservedParams.height);
                if(Log.isLoggable("PercentLayout", 3)) {
                    Log.d("PercentLayout", "using " + info);
                }

                if(info != null) {
                    if(params instanceof MarginLayoutParams) {
                        info.restoreMarginLayoutParams((MarginLayoutParams)params);
                    } else {
                        info.restoreLayoutParams(params);
                    }
                }
            }
        }

    }

    public boolean handleMeasuredStateTooSmall() {
        boolean needsSecondMeasure = false;
        int i = 0;

        for(int N = this.mHost.getChildCount(); i < N; ++i) {
            View view = this.mHost.getChildAt(i);
            LayoutParams params = view.getLayoutParams();
            if(Log.isLoggable("PercentLayout", 3)) {
                Log.d("PercentLayout", "should handle measured state too small " + view + " " + params);
            }

            if(params instanceof PercentLayoutHelper.PercentLayoutParams) {
                PercentLayoutHelper.PercentLayoutInfo info = ((PercentLayoutHelper.PercentLayoutParams)params).getPercentLayoutInfo();
                if(info != null) {
                    if(shouldHandleMeasuredWidthTooSmall(view, info)) {
                        needsSecondMeasure = true;
                        params.width = -2;
                    }

                    if(shouldHandleMeasuredHeightTooSmall(view, info)) {
                        needsSecondMeasure = true;
                        params.height = -2;
                    }
                }
            }
        }

        if(Log.isLoggable("PercentLayout", 3)) {
            Log.d("PercentLayout", "should trigger second measure pass: " + needsSecondMeasure);
        }

        return needsSecondMeasure;
    }

    private static boolean shouldHandleMeasuredWidthTooSmall(View view, PercentLayoutHelper.PercentLayoutInfo info) {
        int state = ViewCompat.getMeasuredWidthAndState(view) & -16777216;
        return state == 16777216 && info.widthPercent.percent >= 0.0F && info.mPreservedParams.width == -2;
    }

    private static boolean shouldHandleMeasuredHeightTooSmall(View view, PercentLayoutHelper.PercentLayoutInfo info) {
        int state = ViewCompat.getMeasuredHeightAndState(view) & -16777216;
        return state == 16777216 && info.heightPercent.percent >= 0.0F && info.mPreservedParams.height == -2;
    }

    public interface PercentLayoutParams {
        PercentLayoutHelper.PercentLayoutInfo getPercentLayoutInfo();
    }

    public static class PercentLayoutInfo {
        public PercentData widthPercent;
        public PercentData heightPercent;
        public PercentData leftMarginPercent;
        public PercentData topMarginPercent;
        public PercentData rightMarginPercent;
        public PercentData bottomMarginPercent;
        public PercentData startMarginPercent;
        public PercentData endMarginPercent;
        final MarginLayoutParams mPreservedParams = new MarginLayoutParams(0, 0);

        public PercentLayoutInfo() {
        }

        public void fillLayoutParams(LayoutParams params, int widthHint, int heightHint) {
            this.mPreservedParams.width = params.width;
            this.mPreservedParams.height = params.height;
            if(this.widthPercent!=null&&this.widthPercent.percent >= 0.0F) {
            	if(this.widthPercent.isBaseHeight){
            		 params.width = (int)((float)heightHint * this.widthPercent.percent);
            	}else{
            		params.width = (int)((float)widthHint * this.widthPercent.percent);
            	}
            }
            
            if(this.heightPercent!=null&&this.heightPercent.percent  >= 0.0F) {
            	if(this.heightPercent.isBaseWidth){
            		params.height = (int)((float)widthHint * this.heightPercent.percent );
            	}else{
            		params.height = (int)((float)heightHint * this.heightPercent.percent );
            	}
            }

            if(Log.isLoggable("PercentLayout", 3)) {
                Log.d("PercentLayout", "after fillLayoutParams: (" + params.width + ", " + params.height + ")");
            }

        }

        public void fillMarginLayoutParams(MarginLayoutParams params, int widthHint, int heightHint) {
            this.fillLayoutParams(params, widthHint, heightHint);
            this.mPreservedParams.leftMargin = params.leftMargin;
            this.mPreservedParams.topMargin = params.topMargin;
            this.mPreservedParams.rightMargin = params.rightMargin;
            this.mPreservedParams.bottomMargin = params.bottomMargin;
            MarginLayoutParamsCompat.setMarginStart(this.mPreservedParams, MarginLayoutParamsCompat.getMarginStart(params));
            MarginLayoutParamsCompat.setMarginEnd(this.mPreservedParams, MarginLayoutParamsCompat.getMarginEnd(params));
            if(this.leftMarginPercent!=null&&this.leftMarginPercent.percent >= 0.0F) {
            	if(this.leftMarginPercent.isBaseHeight){
            		params.leftMargin = (int)((float)heightHint * this.leftMarginPercent.percent);
            	}else{
            		params.leftMargin = (int)((float)widthHint * this.leftMarginPercent.percent);
            	}
            }

            if(this.topMarginPercent!=null&&this.topMarginPercent.percent >= 0.0F) {
            	if(this.topMarginPercent.isBaseWidth){
            		params.topMargin = (int)((float)widthHint * this.topMarginPercent.percent);
            	}else{
            		params.topMargin = (int)((float)heightHint * this.topMarginPercent.percent);
            	}
            }

            if(this.rightMarginPercent!=null&&this.rightMarginPercent.percent >= 0.0F) {
            	if(this.rightMarginPercent.isBaseHeight){
            		params.rightMargin = (int)((float)heightHint * this.rightMarginPercent.percent);
            	}else{
            		params.rightMargin = (int)((float)widthHint * this.rightMarginPercent.percent);
            	}
            }

            if(this.bottomMarginPercent!=null&&this.bottomMarginPercent.percent >= 0.0F) {
            	if(this.bottomMarginPercent.isBaseWidth){
            		params.bottomMargin = (int)((float)widthHint * this.bottomMarginPercent.percent);
            	}else{
            		params.bottomMargin = (int)((float)heightHint * this.bottomMarginPercent.percent);
            	}
            }

            if(this.startMarginPercent!=null&&this.startMarginPercent.percent >= 0.0F) {
            	if(this.startMarginPercent.isBaseHeight){
            		MarginLayoutParamsCompat.setMarginStart(params, (int)((float)heightHint * this.startMarginPercent.percent));
            	}else{
            		MarginLayoutParamsCompat.setMarginStart(params, (int)((float)widthHint * this.startMarginPercent.percent));
            	}
            }

            if(this.endMarginPercent!=null&&this.endMarginPercent.percent >= 0.0F) {
            	if(this.endMarginPercent.isBaseHeight){
            		MarginLayoutParamsCompat.setMarginStart(params, (int)((float)heightHint * this.endMarginPercent.percent));
            	}else{
            		MarginLayoutParamsCompat.setMarginStart(params, (int)((float)widthHint * this.endMarginPercent.percent));
            	}
            }

            if(Log.isLoggable("PercentLayout", 3)) {
                Log.d("PercentLayout", "after fillMarginLayoutParams: (" + params.width + ", " + params.height + ")");
            }

        }

        public String toString() {
            return String.format("PercentLayoutInformation width: %f height %f, margins (%f, %f,  %f, %f, %f, %f)", new Object[]{Float.valueOf(this.widthPercent.percent), Float.valueOf(this.heightPercent.percent), Float.valueOf(this.leftMarginPercent.percent), Float.valueOf(this.topMarginPercent.percent), Float.valueOf(this.rightMarginPercent.percent), Float.valueOf(this.bottomMarginPercent.percent), Float.valueOf(this.startMarginPercent.percent), Float.valueOf(this.endMarginPercent.percent)});
        }

        public void restoreMarginLayoutParams(MarginLayoutParams params) {
            this.restoreLayoutParams(params);
            params.leftMargin = this.mPreservedParams.leftMargin;
            params.topMargin = this.mPreservedParams.topMargin;
            params.rightMargin = this.mPreservedParams.rightMargin;
            params.bottomMargin = this.mPreservedParams.bottomMargin;
            MarginLayoutParamsCompat.setMarginStart(params, MarginLayoutParamsCompat.getMarginStart(this.mPreservedParams));
            MarginLayoutParamsCompat.setMarginEnd(params, MarginLayoutParamsCompat.getMarginEnd(this.mPreservedParams));
        }

        public void restoreLayoutParams(LayoutParams params) {
            params.width = this.mPreservedParams.width;
            params.height = this.mPreservedParams.height;
        }
    }
}
