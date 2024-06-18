package com.plf.minimalshop;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarProductosActivity extends AppCompatActivity {

    private EditText editTextProductName, editTextProductDescription, editTextProductPrice, editTextProductStock;
    private Spinner spinnerProductCategory, spinnerProductBrand;
    private ImageView imageViewProduct;
    private Button btnSelectImage, btnAddProduct, btnAddBrand;
    private Uri imageUri;
    private FirebaseFirestore firestore;
    private StorageReference mStorage;
    private ArrayAdapter<String> brandAdapter;
    private List<String> brandList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_productos);

        // Inicializar Firestore y Storage
        firestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference("productos");

        // Inicializar vistas
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductStock = findViewById(R.id.editTextProductStock);
        spinnerProductCategory = findViewById(R.id.spinnerProductCategory);
        spinnerProductBrand = findViewById(R.id.spinnerProductBrand);
        imageViewProduct = findViewById(R.id.imageViewProduct);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnAddBrand = findViewById(R.id.btnAddBrand);

        // Configurar Spinner de Categorías
        configureCategorySpinner();

        // Configurar Spinner de Marcas
        configureBrandSpinner();

        // Configurar selector de imágenes
        btnSelectImage.setOnClickListener(view -> selectImage());

        // Configurar botón para agregar producto
        btnAddProduct.setOnClickListener(view -> addProduct());

        // Configurar botón para agregar marca
        btnAddBrand.setOnClickListener(view -> showAddBrandDialog());
    }

    private void configureCategorySpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("PROTEINAS");
        categories.add("CREATINAS");
        categories.add("VITAMINAS");
        categories.add("PREENTRENOS");
        categories.add("SHAKER");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductCategory.setAdapter(adapter);
    }

    private void configureBrandSpinner() {
        brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brandList);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductBrand.setAdapter(brandAdapter);

        loadBrandsFromFirestore();
    }

    private void loadBrandsFromFirestore() {
        firestore.collection("marcas").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                brandList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String brand = document.getString("name");
                    brandList.add(brand);
                }
                brandList.add("AGREGAR MARCA"); // Añadir opción para agregar marca
                brandAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error al cargar marcas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBrandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Marca");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String newBrand = input.getText().toString().trim();
            if (!newBrand.isEmpty()) {
                addBrandToFirestore(newBrand);
            } else {
                Toast.makeText(this, "El nombre de la marca no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addBrandToFirestore(String brandName) {
        Map<String, String> brand = new HashMap<>();
        brand.put("name", brandName);

        firestore.collection("marcas").add(brand).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Marca agregada exitosamente", Toast.LENGTH_SHORT).show();
                loadBrandsFromFirestore(); // Recargar las marcas después de agregar una nueva
            } else {
                Toast.makeText(this, "Error al agregar marca", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imageViewProduct.setImageURI(imageUri);
                }
            }
    );

    private void addProduct() {
        String productName = editTextProductName.getText().toString().trim();
        String productDescription = editTextProductDescription.getText().toString().trim();
        String productPrice = editTextProductPrice.getText().toString().trim();
        String productStock = editTextProductStock.getText().toString().trim();
        String productCategory = spinnerProductCategory.getSelectedItem().toString();
        String productBrand = spinnerProductBrand.getSelectedItem().toString();

        if (productName.isEmpty() || productDescription.isEmpty() || productPrice.isEmpty() || productStock.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference filePath = mStorage.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        filePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    Map<String, Object> product = new HashMap<>();
                    product.put("productName", productName);
                    product.put("productDescription", productDescription);
                    product.put("productPrice", productPrice);
                    product.put("productStock", productStock);
                    product.put("productCategory", productCategory);
                    product.put("productBrand", productBrand);
                    product.put("productImageUrl", imageUrl);

                    firestore.collection("productos")
                            .add(product)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AgregarProductosActivity.this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
                                    // Limpiar los campos después de agregar el producto
                                    clearFields();
                                } else {
                                    Toast.makeText(AgregarProductosActivity.this, "Error al agregar producto", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarProductosActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("AgregarProductos", "Error al subir la imagen", e);
                });
    }

    private void clearFields() {
        editTextProductName.setText("");
        editTextProductDescription.setText("");
        editTextProductPrice.setText("");
        editTextProductStock.setText("");
        spinnerProductCategory.setSelection(0);
        spinnerProductBrand.setSelection(0);
        imageViewProduct.setImageResource(android.R.color.transparent);
    }

    private String getFileExtension(Uri uri) {
        return getContentResolver().getType(uri).split("/")[1];
    }
}
