package com.owasp.reverser;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

public class campfire extends AppCompatActivity {

    /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/
    AnimationDrawable fireAnimation;
    /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campfire);

        /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/
        ImageView imageView = findViewById(R.id.campfire);
        imageView.setBackgroundResource(R.drawable.animation);
        fireAnimation = (AnimationDrawable) imageView.getBackground();
        /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/
        fireAnimation.start();
        /*----------FIRE-----------FIRE------------FIRE----------------FIRE-----------------------*/
    }

}