package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginScreen extends AppCompatActivity implements View.OnClickListener {

    public EditText email, password;
    public Button submit;
    public TextView sign_up_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.editTextText);
        password = findViewById(R.id.editTextTextPassword);
        submit = findViewById(R.id.loginBtn);
        sign_up_link = findViewById(R.id.signUpLink);

        submit.setOnClickListener(this::onClick);
        sign_up_link.setOnClickListener(this::onLinkClick);
    }

    @Override
    public void onClick(View v) {
        String usernameInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if (usernameInput.equals("noberto") && passwordInput.equals("123456")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        } else if (usernameInput.isBlank() || passwordInput.isBlank()) {
            String message = "Please fill required inputs!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            String message = "Wrong username or password!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void onLinkClick(View view){
        Intent b = new Intent(this, SignUpScreen.class);
        startActivity(b);
        this.finish();
    }
}