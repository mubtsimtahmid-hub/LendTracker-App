package com.example.lendtracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Screen 3: Create a new account (Firebase Authentication).
 * The user's name is also saved in Realtime Database: users/{uid}/profile
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etEmail, etPassword, etConfirm;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> register());
        tvGoLogin.setOnClickListener(v -> finish()); // back to Login
    }

    private void register() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirm = getText(etConfirm);

        // ---- Validation ----
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);
        boolean valid = true;

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            valid = false;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!confirm.equals(password)) {
            tilConfirm.setError("Passwords do not match");
            valid = false;
        }
        if (!valid) return;

        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.please_wait);

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText(R.string.register);

                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("name", name);
                            profile.put("email", email);
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(user.getUid()).child("profile")
                                    .setValue(profile);
                        }
                        Prefs.saveLogin(this, true, email);
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Registration failed";
                        Snackbar.make(btnRegister, "Error: " + msg, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
