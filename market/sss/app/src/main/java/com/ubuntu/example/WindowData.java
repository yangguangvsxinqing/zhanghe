package com.ubuntu.example;

/**
 * Created by ubuntu on 16-7-6.
 */
public class WindowData {

    private int winType;
    private String winName;
    private String winTitle1;
    private String winTitle2;
    private int winColor;
    public String getWinTitle1() {
        return winTitle1;
    }
    public void setWinTitle1(String winTitle1) {
        this.winTitle1 = winTitle1;
    }
    public String getWinTitle2() {
        return winTitle2;
    }
    public void setWinTitle2(String winTitle2) {
        this.winTitle2 = winTitle2;
    }

    public int getWinType() {
        return winType;
    }
    public void setWinType(int winType) {
        this.winType = winType;
    }
    public String getWinName() {
        return winName;
    }
    public void setWinName(String winName) {
        this.winName = winName;
    }
    public int getWinColor() {
        return winColor;
    }
    public void setWinColor(int winColor) {
        this.winColor = winColor;
    }
}
