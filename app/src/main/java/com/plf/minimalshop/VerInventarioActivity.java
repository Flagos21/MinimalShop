package com.plf.minimalshop;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.plf.minimalshop.adapter.InventarioAdapter;
import com.plf.minimalshop.model.Productos;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class VerInventarioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventarioAdapter inventarioAdapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference productosRef = db.collection("productos");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_inventario);

        recyclerView = findViewById(R.id.InventoryMinimal);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = productosRef.orderBy("productName", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Productos> options = new FirestoreRecyclerOptions.Builder<Productos>()
                .setQuery(query, Productos.class)
                .build();

        inventarioAdapter = new InventarioAdapter(options, this, getSupportFragmentManager());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(inventarioAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        inventarioAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        inventarioAdapter.stopListening();
    }
}
