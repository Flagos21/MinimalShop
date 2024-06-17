package com.plf.minimalshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private RadioGroup groupType;
    private Button btnRegister;
    private ImageButton btnShowR;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        editTextUsername = findViewById(R.id.editTextText);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        groupType = findViewById(R.id.groupType);
        btnRegister = findViewById(R.id.btnRegister);
        btnShowR = findViewById(R.id.btnShowR);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btnShowR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnShowR.setImageResource(R.mipmap.icon_password); // Cambia a un ícono que represente ocultar la contraseña
        } else {
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnShowR.setImageResource(R.mipmap.icon_password); // Cambia a un ícono que represente mostrar la contraseña
        }
        isPasswordVisible = !isPasswordVisible;
        editTextPassword.setSelection(editTextPassword.length());
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        int selectedTypeId = groupType.getCheckedRadioButtonId();
        RadioButton selectedTypeButton = findViewById(selectedTypeId);
        String userType = selectedTypeButton == null ? "" : selectedTypeButton.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Por favor ingresa un nombre de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor ingresa una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Por favor selecciona un tipo de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(username + "@minimal.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso, guarda datos adicionales en Firebase Database
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            User newUser = new User(username, userType);
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Error al guardar datos de usuario", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String username;
        public String userType;

        public User() {
            // Constructor vacío necesario para Firebase
        }

        public User(String username, String userType) {
            this.username = username;
            this.userType = userType;
        }
    }
}
