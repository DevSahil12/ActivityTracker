package com.master.dailydose.signactivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.master.dailydose.R;
import com.master.dailydose.mainactivities.MainPage;

public class SignUp extends AppCompatActivity {
Button button;
EditText FullName,Password,Email,Phone;
ProgressBar progressBar;
CheckBox keepMeLoggedIn;
FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        button=findViewById(R.id.buttonLogin);
        FullName=findViewById(R.id.FullName);
        Password=findViewById(R.id.Password);
        Email=findViewById(R.id.Email);
        Phone=findViewById(R.id.PhoneNumber);
        progressBar=findViewById(R.id.ProgressBar);
        keepMeLoggedIn=findViewById(R.id.checkBoxKeepSignedIn);

        mAuth=FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterNewUser();
            }
        });


    }
    public void RegisterNewUser(){
        progressBar.setVisibility(View.VISIBLE);
        String email,password;
        email=Email.getText().toString();
        password=Password.getText().toString();

        if(TextUtils.isEmpty(email)){
        Email.setError("Please Enter Email");
        return;
                }
        if (TextUtils.isEmpty(password)){
            Password.setError("Please Enter Password");
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SignUp.this, "Registration Successfully", Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                    Intent intent=new Intent(SignUp.this, MainPage.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(SignUp.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
}