package com.plf.minimalshop.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plf.minimalshop.R;
import com.plf.minimalshop.model.Productos;

import java.util.ArrayList;
import java.util.List;

public class ProductosAdapter extends FirestoreRecyclerAdapter<Productos, ProductosAdapter.ProductosViewHolder> implements Filterable {

    private Activity activity;
    private List<Productos> originalList = new ArrayList<>();
    private List<Productos> filteredList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductosAdapter(@NonNull FirestoreRecyclerOptions<Productos> options, Activity activity) {
        super(options);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProductosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_productos, parent, false);
        return new ProductosViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductosViewHolder holder, int position, @NonNull Productos producto) {
        holder.textViewProductName.setText(producto.getProductName());
        holder.textViewProductDescription.setText(producto.getProductDescription());
        holder.textViewProductBrand.setText("Marca: " + producto.getProductBrand());
        holder.textViewProductCategory.setText("Categoría: " + producto.getProductCategory());
        holder.textViewProductStock.setText("Stock: " + producto.getProductStock());
        holder.textViewProductPrice.setText("Precio: " + producto.getProductPrice());

        Glide.with(activity.getApplicationContext())
                .load(producto.getProductImageUrl())
                .placeholder(R.mipmap.logo_minimal) // Placeholder image
                .into(holder.imageViewProduct);

        holder.imageButtonAddToCart.setOnClickListener(v -> {
            // Verificar si hay suficiente stock
            if (Integer.parseInt(producto.getProductStock()) > 0) {
                // Obtener el ID del producto seleccionado
                String productId = getSnapshots().getSnapshot(position).getId();

                // Obtener el ID del usuario actual
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Verificar si el producto ya está en el carrito del usuario
                db.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    // Si el producto ya está en el carrito, actualizar solo la cantidad
                                    String currentQuantityStr = task.getResult().getString("productStock");
                                    int currentQuantity = Integer.parseInt(currentQuantityStr);
                                    int newQuantity = currentQuantity + 1;
                                    db.collection("users")
                                            .document(userId)
                                            .collection("cart")
                                            .document(productId)
                                            .update("productStock", String.valueOf(newQuantity))
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("ProductosAdapter", "Cantidad del producto actualizada en el carrito: " + productId);
                                                Toast.makeText(activity, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProductosAdapter", "Error al actualizar cantidad del producto en el carrito", e);
                                                Toast.makeText(activity, "Error al agregar producto al carrito", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Si el producto no está en el carrito, agregarlo con cantidad 1
                                    producto.setProductStock("1"); // Setear la cantidad en el carrito
                                    db.collection("users")
                                            .document(userId)
                                            .collection("cart")
                                            .document(productId)
                                            .set(producto)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("ProductosAdapter", "Producto agregado al carrito: " + productId);
                                                Toast.makeText(activity, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ProductosAdapter", "Error al agregar producto al carrito", e);
                                                Toast.makeText(activity, "Error al agregar producto al carrito", Toast.LENGTH_SHORT).show();
                                            });
                                }

                                // Verificar nuevamente el stock general del producto antes de actualizar
                                db.collection("productos")
                                        .document(productId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String currentStockStr = documentSnapshot.getString("productStock");
                                                int currentStock = Integer.parseInt(currentStockStr);
                                                if (currentStock > 0) {
                                                    // Actualizar el stock en Firestore: restar 1 al stock actual
                                                    int newStock = currentStock - 1;
                                                    db.collection("productos")
                                                            .document(productId)
                                                            .update("productStock", String.valueOf(newStock))
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("ProductosAdapter", "Stock general del producto actualizado: " + newStock);
                                                                notifyDataSetChanged();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("ProductosAdapter", "Error al actualizar stock general del producto", e);
                                                            });
                                                } else {
                                                    Log.d("ProductosAdapter", "No hay suficiente stock general del producto para actualizar.");
                                                    // Mostrar mensaje o tomar otra acción según sea necesario
                                                }
                                            } else {
                                                Log.d("ProductosAdapter", "Documento del producto no encontrado en Firestore.");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("ProductosAdapter", "Error al obtener documento del producto en Firestore", e);
                                        });
                            } else {
                                Log.e("ProductosAdapter", "Error al verificar producto en el carrito", task.getException());
                                Toast.makeText(activity, "Error al agregar producto al carrito", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Mostrar mensaje si no hay suficiente stock
                Toast.makeText(activity, "¡Producto sin stock!", Toast.LENGTH_SHORT).show();
            }
        });




    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public void onDataChanged() {
        originalList.clear();
        for (int i = 0; i < getSnapshots().size(); i++) {
            Productos producto = getSnapshots().getSnapshot(i).toObject(Productos.class);
            if (producto != null) {
                originalList.add(producto);
            }
        }
        filteredList.clear();
        filteredList.addAll(originalList);
        Log.d("ProductosAdapter", "Datos cargados: " + originalList.size());
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<Productos> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered.addAll(originalList);
                } else {
                    for (Productos producto : originalList) {
                        if (producto.getProductCategory().toLowerCase().contains(query)) {
                            filtered.add(producto);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                Log.d("ProductosAdapter", "Filtrando por: " + query + ", resultados: " + filtered.size());
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                filteredList.addAll((List<Productos>) results.values);
                Log.d("ProductosAdapter", "Resultados publicados: " + filteredList.size());
                notifyDataSetChanged();
            }
        };
    }

    public void updateList(List<Productos> productos) {
        originalList.clear();
        originalList.addAll(productos);
        filteredList.clear();
        filteredList.addAll(productos);
        notifyDataSetChanged();
    }

    public static class ProductosViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductDescription, textViewProductBrand, textViewProductCategory, textViewProductStock, textViewProductPrice;
        ImageButton imageButtonAddToCart;

        public ProductosViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductDescription = itemView.findViewById(R.id.textViewProductDescription);
            textViewProductBrand = itemView.findViewById(R.id.textViewProductBrand);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductStock = itemView.findViewById(R.id.textViewProductStock);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            imageButtonAddToCart = itemView.findViewById(R.id.imageButtonAddToCart);
        }
    }
}
