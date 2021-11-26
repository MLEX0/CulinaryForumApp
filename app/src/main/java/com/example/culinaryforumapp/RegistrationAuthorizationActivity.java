package com.example.culinaryforumapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationAuthorizationActivity extends AppCompatActivity {

    private LinearLayout LinearEnter;
    private LinearLayout LinearRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase database;
    DatabaseReference UserDataBase;


    private EditText textEmail;
    private EditText textPassword;

    private EditText textEmailReg;
    private EditText textPasswordReg;
    private EditText textNickNameReg;
    private EditText textPasswordReg2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_authorization);

        Init();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }

            }
        };

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            Intent auth = new Intent(RegistrationAuthorizationActivity.this, MainFrameActivity.class);
            startActivity(auth);
            finish();
        }



    }

    private void Init()
    {
        LinearEnter = findViewById(R.id.LinearLayoutEnter);
        LinearRegistration = findViewById(R.id.LinearLayoutRegistration);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://culinaryforumapp-default-rtdb.europe-west1.firebasedatabase.app");
        UserDataBase = database.getReference(Constants.USER_KEY);

        textEmail = findViewById(R.id.editTextTextAuthEmail);
        textPassword = findViewById(R.id.editTextTextPassword);

        textEmailReg = findViewById(R.id.editTextRegEmail);
        textPasswordReg = findViewById(R.id.editTextRegPassword);
        textNickNameReg = findViewById(R.id.editTextRegNickName);
        textPasswordReg2 = findViewById(R.id.editTextReg2Password);

    }

    public void OnClickAuthorization(View view) {

        String Email = textEmail.getText().toString();
        String Password = textPassword.getText().toString();

        if(Email.isEmpty()) {

            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Поле 'Email' не может быть пустым!", Toast.LENGTH_LONG).show();

        }
        else if (Password.isEmpty()) {
            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Поле 'Пароль' не может быть пустым!", Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()) {
                        Toast.makeText(RegistrationAuthorizationActivity.this,
                                "Email или пароль введены неверно!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegistrationAuthorizationActivity.this,
                                "Авторизация успешна!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(RegistrationAuthorizationActivity.this,MainFrameActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }

    }

    public void OnClickRegistration(View view)
    {
        String EmailReg = textEmailReg.getText().toString();
        String PasswordReg = textPasswordReg.getText().toString();
        String PasswordReg2 = textPasswordReg2.getText().toString();
        String NickName = textNickNameReg.getText().toString();

        if(EmailReg.isEmpty()) {

            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Поле 'Email' не может быть пустым!", Toast.LENGTH_LONG).show();

        }
        else if (PasswordReg.isEmpty()) {
            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Поле 'Пароль' не может быть пустым!", Toast.LENGTH_LONG).show();

        } else if (NickName.isEmpty()) {
            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Поле 'Имя пользователя' не может быть пустым!", Toast.LENGTH_LONG).show();

        } else if (!PasswordReg.equals(PasswordReg2)) {
            Toast.makeText(RegistrationAuthorizationActivity.this,
                    "Пароли не совпадают!", Toast.LENGTH_LONG).show();

        } else {

            mAuth.createUserWithEmailAndPassword(EmailReg, PasswordReg).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(RegistrationAuthorizationActivity.this,
                                "Регистрация провалена!", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(RegistrationAuthorizationActivity.this,
                                "Регистрация успешна!", Toast.LENGTH_LONG).show();

                        mAuth.signInWithEmailAndPassword(EmailReg, PasswordReg).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                   if(task.isSuccessful())
                                   {
                                       FirebaseUser user = mAuth.getCurrentUser();
                                       if(user != null)
                                       {
                                           User newUser = new User( UserDataBase.getKey(),user.getUid(), NickName);
                                           UserDataBase.push().setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if(task.isSuccessful())
                                                   {
                                                       Intent intent_reg = new Intent(RegistrationAuthorizationActivity.this,MainFrameActivity.class);
                                                       startActivity(intent_reg);
                                                   }
                                               }
                                           });

                                       }
                                   }
                            }
                        });

                    }
                }
            });
        }

    }

    public void OnClickChangeToRegistration(View view) {
        LinearEnter.setVisibility(View.INVISIBLE);
        LinearRegistration.setVisibility(View.VISIBLE);

        textEmail.setText("");
        textPassword.setText("");
    }

    public void OnClickChangeToEnter(View view) {
        LinearRegistration.setVisibility(View.INVISIBLE);
        LinearEnter.setVisibility(View.VISIBLE);

        textEmailReg.setText("");
        textPasswordReg.setText("");
        textNickNameReg.setText("");
        textPasswordReg2.setText("");
    }
}
