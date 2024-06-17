package com.plf.minimalshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUser, inputCode;
    private Button btnLogin, btnRegisterP;
    private ImageButton imageButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        inputUser = findViewById(R.id.inputUser);
        inputCode = findViewById(R.id.inputCode);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterP = findViewById(R.id.btnRegisterP);
        imageButton = findViewById(R.id.imageButton);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnRegisterP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
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
            inputCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            inputCode.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        isPasswordVisible = !isPasswordVisible;
        inputCode.setSelection(inputCode.getText().length());
    }

    private void loginUser() {
        String email = inputUser.getText().toString().trim() + "@minimal.com";
        String password = inputCode.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String userType = dataSnapshot.child("userType").getValue(String.class);
                                        if ("VENDEDOR".equals(userType)) {
                                            Intent intent = new Intent(LoginActivity.this, VendedorMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if ("CLIENTE".equals(userType)) {
                                            Intent intent = new Intent(LoginActivity.this, ClienteMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Tipo de usuario desconocido", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "No se encontraron datos de usuario", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(LoginActivity.this, "Error al leer datos de usuario", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error en el inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
