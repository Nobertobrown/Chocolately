package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpScreen extends AppCompatActivity {
    private static final String TAG = "SignUpScreen";
    public EditText email, password, confirm_password;
    public TextView login_link;
    public Button sign_up_btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        login_link = findViewById(R.id.loginLink);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirmPassword);
        sign_up_btn = findViewById(R.id.signUpBtn);

        login_link.setOnClickListener(this::onLinkClick);
        sign_up_btn.setOnClickListener(this::handleSignUp);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    public void handleSignUp(View view){
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirm_password.getText().toString().trim();
        String message;

        if (emailText.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            message = "Enter a valid email address!";
            email.setError(message);
//           You can display an error in the component using email.setEror('Error msg');
        } else if (passwordText.isBlank()) {
            message = "Password is a required field!";
            password.setError(message);
        } else if(!passwordText.equals(confirmPasswordText)) {
            message = "Entered passwords don't match!";
            confirm_password.setError(message);
        }else{
            createAccount(emailText, passwordText);
        }
    }

    public void onLinkClick(View view) {
        Intent b = new Intent(this, LoginScreen.class);
        startActivity(b);
        this.finish();
    }

    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpScreen.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        // [END create_user_with_email]
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("name", user.getDisplayName());
            startActivity(intent);
            this.finish();
        }
    }
}