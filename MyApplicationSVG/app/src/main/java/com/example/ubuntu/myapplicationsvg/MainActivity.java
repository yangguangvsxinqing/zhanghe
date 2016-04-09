package com.example.ubuntu.myapplicationsvg;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView androidImageView = (ImageView) findViewById(R.id.iv);
        Drawable drawable = androidImageView.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }

       /* AnimatedVectorDrawable mAnimatedVectorDrawable =  (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.weather_vector);//得到对应的AnimatedVectorDrawable对象
        androidImageView.setImageDrawable(mAnimatedVectorDrawable);
        if(mAnimatedVectorDrawable!=null){
            mAnimatedVectorDrawable.start();
        }*/
    }

}
