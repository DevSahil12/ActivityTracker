package com.master.dailydose.signactivities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.master.dailydose.R;
import com.master.dailydose.mainactivities.MainPage;


public class LoginActivity extends AppCompatActivity {
   Button button;
   EditText username,password;
   TextView signUp;
   ProgressBar progressBar;
   FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.userName);
        password=findViewById(R.id.password);
        signUp=findViewById(R.id.signUp);
        progressBar=findViewById(R.id.progress);


        auth=FirebaseAuth.getInstance();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,SignUp.class);
                startActivity(intent);
                finish();
            }
        });

       FirebaseUser currentUser=auth.getCurrentUser();
       if (currentUser!=null){
           Intent intent=new Intent(LoginActivity.this, MainPage.class);
           startActivity(intent);
           finish();
       }
        button=findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               LoginUser();
            }
        });
    }
    public void LoginUser(){
     progressBar.setVisibility(View.VISIBLE);

     String email=username.getText().toString().trim();
     String Password=password.getText().toString().trim();

     if (TextUtils.isEmpty(email)){
         username.setError("PLease Enter Email");
         return;
     }
     if (TextUtils.isEmpty(Password)){
         password.setError("Please Enter Password");
     }
     auth.signInWithEmailAndPassword(email,Password)
             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                    Intent intent=new Intent(LoginActivity.this, MainPage.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                 }
             });
    }
}