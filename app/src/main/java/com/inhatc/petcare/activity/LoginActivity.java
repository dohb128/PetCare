package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inhatc.petcare.model.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText, passwordEditText;
    private Button signinButton, signupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        signinButton = findViewById(R.id.signin_button);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Extract a default nickname from the email
                                String nickname = email.substring(0, email.indexOf("@"));
                                writeNewUser(user.getUid(), email, nickname); // Updated method call
                                Toast.makeText(LoginActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            if (task.getException() instanceof FirebaseAuthException) {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                String errorMessage = "회원가입 실패.";
                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        errorMessage = "이메일 주소 형식이 잘못되었습니다.";
                                        break;
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        errorMessage = "이미 사용 중인 이메일 주소입니다.";
                                        break;
                                    case "ERROR_WEAK_PASSWORD":
                                        errorMessage = "비밀번호가 너무 짧거나 약합니다. 6자 이상이어야 합니다.";
                                        break;
                                    default:
                                        errorMessage = "회원가입에 실패했습니다. 오류 코드: " + errorCode;
                                        break;
                                }
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                Log.e(TAG, "회원가입 실패: " + task.getException().getMessage());
                            } else {
                                Toast.makeText(LoginActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "회원가입 실패: " + task.getException().getMessage());
                            }
                        }
                    });
        });

        signinButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "로그인 실패: " + task.getException().getMessage());
                        }
                    });
        });
    }

    private void writeNewUser(String userId, String email, String nickname) {
        User user = new User(email, nickname);
        mDatabase.child("users").child(userId).setValue(user);
    }
}