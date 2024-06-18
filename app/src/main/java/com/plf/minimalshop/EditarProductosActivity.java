package com.plf.minimalshop;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plf.minimalshop.model.Productos;

public class EditarProductosActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductPrice, editTextProductStock;
    private ImageView imageViewProduct;
    private Button btnUpdateProduct;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference productoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_productos);

        // Inicialización de vistas
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductStock = findViewById(R.id.editTextProductStock);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct);

        // Obtener el ID del producto de los extras del Intent
        String productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            // Manejar caso de ID nulo
            Toast.makeText(this, "ID del producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencia al documento del producto en Firestore
        productoRef = db.collection("productos").document(productId);

        // Obtener los datos del producto y mostrarlos en las vistas
        productoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Productos producto = documentSnapshot.toObject(Productos.class);
                if (producto != null) {
                    editTextProductName.setText(producto.getProductName());
                    editTextProductDescription.setText(producto.getProductDescription());
                    editTextProductPrice.setText(producto.getProductPrice());
                    editTextProductStock.setText(producto.getProductStock());
                    Glide.with(this).load(producto.getProductImageUrl()).into(imageViewProduct);
                }
            } else {
                Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al obtener el producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });

        // Configurar el botón para actualizar el producto
        btnUpdateProduct.setOnClickListener(view -> updateProduct());
    }

    private void updateProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String productDescription = editTextProductDescription.getText().toString().trim();
        String productPrice = editTextProductPrice.getText().toString().trim();
        String productStock = editTextProductStock.getText().toString().trim(); // Asegúrate de que productStock sea String

        if (productName.isEmpty() || productDescription.isEmpty() || productPrice.isEmpty() || productStock.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar los datos del producto en Firestore
        productoRef.update(
                "productName", productName,
                "productDescription", productDescription,
                "productPrice", productPrice,
                "productStock", productStock // Asegúrate de que productStock sea String
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(EditarProductosActivity.this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(EditarProductosActivity.this, "Error al actualizar producto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

