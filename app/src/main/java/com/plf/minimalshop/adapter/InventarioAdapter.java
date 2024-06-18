package com.plf.minimalshop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plf.minimalshop.EditarProductosActivity;
import com.plf.minimalshop.R;
import com.plf.minimalshop.model.Productos;

public class InventarioAdapter extends FirestoreRecyclerAdapter<Productos, InventarioAdapter.InventarioViewHolder> {

    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private Activity activity;
    private FragmentManager fm;

    public InventarioAdapter(@NonNull FirestoreRecyclerOptions<Productos> options, Activity activity, FragmentManager fm) {
        super(options);
        this.activity = activity;
        this.fm = fm;
    }

    @NonNull
    @Override
    public InventarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_inventory, parent, false);
        return new InventarioViewHolder(view);
    }

// Dentro del método onBindViewHolder del adaptador InventarioAdapter

    @Override
    protected void onBindViewHolder(@NonNull InventarioViewHolder holder, int position, @NonNull Productos producto) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
        final String id = documentSnapshot.getId();

        holder.textViewProductName.setText(producto.getProductName());
        holder.textViewProductStock.setText("Stock: " + producto.getProductStock());

        Glide.with(activity.getApplicationContext())
                .load(producto.getProductImageUrl())
                .placeholder(R.mipmap.logo_minimal) // Placeholder image
                .into(holder.imageViewProduct);

        holder.imageButtonEditProduct.setOnClickListener(v -> {
            // Handle edit button click
            Intent intent = new Intent(activity, EditarProductosActivity.class);
            intent.putExtra("product_id", id); // Pasar el ID del producto a EditarProductosActivity
            activity.startActivity(intent);
        });

        holder.imageButtonDeleteProduct.setOnClickListener(v -> deleteProduct(id));
    }


    private void deleteProduct(String id) {
        mFirestore.collection("productos").document(id).delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(activity, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(activity, "Error al eliminar el producto", Toast.LENGTH_SHORT).show());
    }

    public static class InventarioViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductStock;
        ImageButton imageButtonEditProduct, imageButtonDeleteProduct;

        public InventarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductStock = itemView.findViewById(R.id.textViewProductStock);
            imageButtonEditProduct = itemView.findViewById(R.id.imageButtonEditProduct);
            imageButtonDeleteProduct = itemView.findViewById(R.id.imageButtonDeleteProduct);
        }
    }
}
