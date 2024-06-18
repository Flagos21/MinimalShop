package com.plf.minimalshop;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

        // Cargar los datos del producto actual desde Firestore
        productoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Productos producto = documentSnapshot.toObject(Productos.class);
                if (producto != null) {
                    // Mostrar los detalles del producto en los campos de edición
                    editTextProductName.setText(producto.getProductName());
                    editTextProductDescription.setText(producto.getProductDescription());
                    editTextProductPrice.setText(String.valueOf(producto.getProductPrice()));
                    editTextProductStock.setText(String.valueOf(producto.getProductStock()));

                    // Cargar la imagen del producto usando Glide
                    Glide.with(getApplicationContext())
                            .load(producto.getProductImageUrl())
                            .placeholder(R.mipmap.logo_minimal)
                            .into(imageViewProduct);

                    // Escuchar el evento de actualización del producto
                    btnUpdateProduct.setOnClickListener(v -> updateProduct());
                }
            } else {
                Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateProduct() {
        // Obtener los nuevos valores de los campos de edición
        String newName = editTextProductName.getText().toString();
        String newDescription = editTextProductDescription.getText().toString();
        double newPrice = Double.parseDouble(editTextProductPrice.getText().toString());
        int newStock = Integer.parseInt(editTextProductStock.getText().toString());

        // Actualizar el producto en Firestore
        productoRef.update("productName", newName,
                        "productDescription", newDescription,
                        "productPrice", newPrice,
                        "productStock", newStock)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
