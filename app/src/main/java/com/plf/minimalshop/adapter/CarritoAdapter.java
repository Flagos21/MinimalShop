package com.plf.minimalshop.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plf.minimalshop.R;
import com.plf.minimalshop.model.Productos;

public class CarritoAdapter extends FirestoreRecyclerAdapter<Productos, CarritoAdapter.CarritoViewHolder> {

    private Activity activity;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CarritoAdapter(@NonNull FirestoreRecyclerOptions<Productos> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull CarritoViewHolder holder, int position, @NonNull Productos producto) {
        holder.textViewProductName.setText(producto.getProductName());
        holder.textViewProductDescription.setText(producto.getProductDescription());
        holder.textViewProductBrand.setText("Marca: " + producto.getProductBrand());
        holder.textViewProductCategory.setText("Categoría: " + producto.getProductCategory());
        holder.textViewProductStock.setText("Stock en carrito: " + producto.getProductStock());
        holder.textViewProductPrice.setText("Precio: " + producto.getProductPrice());

        Glide.with(activity.getApplicationContext())
                .load(producto.getProductImageUrl())
                .placeholder(R.mipmap.logo_minimal) // Placeholder image
                .into(holder.imageViewProduct);

        holder.imageButtonDeleteProduct.setOnClickListener(v -> {
            // Obtener el ID del producto seleccionado
            DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
            String productId = snapshot.getId();

            // Obtener el ID del usuario actual
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Obtener el stock actual en el carrito
            int currentStockInCart = Integer.parseInt(producto.getProductStock());

            if (currentStockInCart > 1) {
                // Si hay más de 1 en el carrito, reducir el stock en el carrito
                int newStockInCart = currentStockInCart - 1;
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId)
                        .update("productStock", String.valueOf(newStockInCart))
                        .addOnSuccessListener(aVoid -> Log.d("CarritoAdapter", "Stock en carrito actualizado: " + newStockInCart))
                        .addOnFailureListener(e -> Log.e("CarritoAdapter", "Error al actualizar stock en carrito", e));
            } else {
                // Si solo hay 1 en el carrito, eliminar el producto del carrito
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d("CarritoAdapter", "Producto eliminado del carrito: " + productId))
                        .addOnFailureListener(e -> Log.e("CarritoAdapter", "Error al eliminar producto del carrito", e));
            }

            // Obtener el stock actual en el inventario principal
            db.collection("productos")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String stockString = documentSnapshot.getString("productStock");
                            if (stockString != null) {
                                int currentStock = Integer.parseInt(stockString);

                                // Incrementar el stock en el inventario principal
                                int newStock = currentStock + 1;
                                db.collection("productos")
                                        .document(productId)
                                        .update("productStock", String.valueOf(newStock))
                                        .addOnSuccessListener(aVoid -> Log.d("CarritoAdapter", "Stock en inventario principal actualizado: " + newStock))
                                        .addOnFailureListener(e -> Log.e("CarritoAdapter", "Error al actualizar stock en inventario principal", e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("CarritoAdapter", "Error al obtener stock en inventario principal", e));
        });
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductDescription, textViewProductBrand, textViewProductCategory, textViewProductStock, textViewProductPrice;
        ImageButton imageButtonDeleteProduct;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductBrand = itemView.findViewById(R.id.textViewProductBrand);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductStock = itemView.findViewById(R.id.textViewProductStock);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            imageButtonDeleteProduct = itemView.findViewById(R.id.imageButtonDeleteProduct);
        }
    }
}
