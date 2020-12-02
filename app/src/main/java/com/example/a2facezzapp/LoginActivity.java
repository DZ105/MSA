package com.example.a2facezzapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //views
    EditText mEmailInput, mPasswordInput;
    Button mLoginBtn;
    TextView noAccountTV, mRecoverPasswordTV;

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailInput = findViewById(R.id.emailInput);
        mPasswordInput = findViewById(R.id.passwordInput);
        mLoginBtn = findViewById(R.id.loginBtn);
        noAccountTV = findViewById(R.id.no_accountTV);
        mRecoverPasswordTV = findViewById(R.id.recoverPasswordTV);

        mAuth = FirebaseAuth.getInstance();

        //click on login button
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input email and password
                String email = mEmailInput.getText().toString().trim();
                String password = mPasswordInput.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus email input box
                    mEmailInput.setError("Invalid email");
                    mEmailInput.setFocusable(true);
                }else {
                    //if email is valid, login
                    loginUser(email, password);
                }
            }
        });

        //if user click no account
        noAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //recover password
        mRecoverPasswordTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                showRecoverPasswordDialog();
            }
        });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover password");

        //set layout linear
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        final EditText emailInput = new EditText(this);
        emailInput.setHint("Email");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        /*set the min width of EditView to fit a text of n 'M' letters regardless of the actual text extension and text size*/
        emailInput.setMinEms(16);

        linearLayout.addView(emailInput);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //button recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which){
                //input email
               String email = emailInput.getText().toString().trim();
               beginRecovery(email);
           }
        });

        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }
            }
        } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = user.getEmail();
                            String uid = user.getUid();
                            String name = user.getDisplayName();
                            String phone = user.getPhoneNumber();
                            Uri avatar = user.getPhotoUrl();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put info in hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", ""); //will add later (e.g. edit profile)
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");

                            //firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data within hashmap to database
                           //reference.child(uid).setValue(hashMap);
                            //if user is logged in, start profile activity
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //show error
                Toast.makeText(LoginActivity.this, "" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}