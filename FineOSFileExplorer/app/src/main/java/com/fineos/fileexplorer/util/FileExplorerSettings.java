package com.fineos.fileexplorer.util;

import java.util.Locale;

/**
 * Created by xiaoyue on 15-12-17.
 */
public class FileExplorerSettings {

    private static boolean mIgnoreCase = true;

    public static boolean compareFileIgnoreCase() {
        return mIgnoreCase;
    }

    public static boolean currentLanguageIsEnglish() {
        boolean isEnglish = false;
        try {
            isEnglish = Locale.getDefault().toString().contains("en_");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnglish;
    }
}
