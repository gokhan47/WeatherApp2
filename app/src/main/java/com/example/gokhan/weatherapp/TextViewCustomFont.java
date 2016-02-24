package com.example.gokhan.weatherapp;
 
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.gokhan.weatherapp.cache.FontCache;

/**
 * Created by GOKHAN on 2/23/2016.
 */

public class TextViewCustomFont extends TextView {  

	public TextViewCustomFont(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	public TextViewCustomFont(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
		
	}
	
	public TextViewCustomFont(Context context) {
		super(context);
		init(null);
	}
	
	private void init(AttributeSet attrs) {
		if (attrs!=null) {
			 TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.TextViewCustomFont);
			 String fontName = a.getString(R.styleable.TextViewCustomFont_customfont);
			 if (fontName!=null) {
				 Typeface myTypeface = FontCache.get("fonts/" + fontName, getContext());
				 setTypeface(myTypeface);
			 }
			 a.recycle();
		}
	}

}
 