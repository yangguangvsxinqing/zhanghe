package com.fineos.themecoloreditor;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 15-11-20.
 */
public  class ArrayHelper  {
   private static ThemeColors mColor;
    private static boolean[] mChecks = new boolean[6];
    public static int getCheckedCount()
    {
        int i=0;
        for( i=0;i<mChecks.length;i++)
        {
            if(mChecks[i])
            {
                i++;
            }
        }
        return i;
    }
    public static void setColor(ThemeColors color){
        mColor = color;
    }
   public static boolean ifnotchecked()
   {
       for(int i=0;i<mChecks.length;i++)
       {
           if(mChecks[i])
           {
               return false;
           }
       }
       return true;
   }
    public static ThemeColors getColor(){
        return mColor;
    }
    public static void setChecked(int position, boolean checked){
        mChecks[position] = checked;
    }

    public static boolean getChecked(int position){
        return mChecks[position];
    }

    private static ThemeColorEditorActivity.sharePreferencesHelper helper=new ThemeColorEditorActivity.sharePreferencesHelper() {
        @Override
        public void readPreference(JSONArray array) throws JSONException {
            for(int i=0;i<mChecks.length;i++)
            {
                setChecked(i,array.getBoolean(i));
            }
        }

        @Override
        public void writePreference(JSONArray array) {
            for(int i=0;i<mChecks.length;i++)
            {
                array.put(getChecked(i));
            }
        }
    };

   public static ThemeColorEditorActivity.sharePreferencesHelper getHelper()
   {
       return  helper;
   }
}
