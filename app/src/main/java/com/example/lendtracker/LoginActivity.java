package com.example.lendtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Screen 2: Login with email + password (Firebase Authentication).
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRemember;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        // SharedPreferences: fill the email saved last time
        etEmail.setText(Prefs.getSavedEmail(this));

        btnLogin.setOnClickListener(v -> login());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        // ---- Validation (no crash on empty / wrong input) ----
        tilEmail.setError(null);
        tilPassword.setError(null);
        boolean valid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!valid) return;

        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.please_wait);

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);

                    if (task.isSuccessful()) {
                        Prefs.saveLogin(this, cbRemember.isChecked(), email);
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Login failed";
                        Snackbar.make(btnLogin, "Login failed: " + msg, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
