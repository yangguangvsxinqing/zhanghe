package com.fineos.themecoloreditor;

public class ThemeColors {
	private String colorName;
	private int titleColor;
	private int context1Color;
	private int context2Color;
	private int bottomButtonColor;

	public void setColorName(String name) {
		colorName = name;
	}

	public void setTitleColor(int color) {
		titleColor = color;
	}

	public void setContext1Color(int color) {
		context1Color = color;
	}

	public void setContext2Color(int color) {
		context2Color = color;
	}

	public void setBottomButtonColor(int color) {
		bottomButtonColor = color;
	}
	
	public String getColorName() {
		return colorName;
	}
	
	public int getTitleColor() {
		return titleColor;
	}
	
	public int getContext1Color() {
		return context1Color;
	}
	
	public int getContext2Color() {
		return context2Color;
	}
	
	public int getBottomButtonColor() {
		return bottomButtonColor;
	}

}
