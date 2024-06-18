package com.plf.minimalshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VendedorMainActivity extends AppCompatActivity {

    private Button btnAddP, btnViewI;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vendedor_main);

        // Inicializar el botón
        btnAddP = findViewById(R.id.btnAddP);

        // Establecer el listener del botón
        btnAddP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para ir a AgregarProductosActivity
                Intent intent = new Intent(VendedorMainActivity.this, AgregarProductosActivity.class);
                startActivity(intent);
            }
        });

        btnViewI = findViewById(R.id.btnViewI);

        btnViewI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendedorMainActivity.this, VerInventarioActivity.class);
                startActivity(intent);            }
        });

        // Ajustar los Insets para Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
