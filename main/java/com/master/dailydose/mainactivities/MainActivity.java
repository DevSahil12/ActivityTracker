package com.master.dailydose.mainactivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.master.dailydose.R;
import com.master.dailydose.signactivities.LoginActivity;
import com.master.dailydose.signactivities.SignUp;


public class MainActivity extends AppCompatActivity {
Button button;
TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
button=findViewById(R.id.button);
textView=findViewById(R.id.textView2);
textView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent i=new Intent(MainActivity.this, SignUp.class);
        startActivity(i);
        finish();
    }
});



button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
   Intent i=new Intent(MainActivity.this, LoginActivity.class);
   startActivity(i);
   finish();
    }
});


    }
}