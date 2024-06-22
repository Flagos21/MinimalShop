package com.plf.minimalshop.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.plf.minimalshop.R;
import com.plf.minimalshop.model.Productos;

import java.util.ArrayList;
import java.util.List;

public class ProductosAdapter extends FirestoreRecyclerAdapter<Productos, ProductosAdapter.ProductosViewHolder> implements Filterable {

    private Activity activity;
    private List<Productos> originalList;
    private List<Productos> filteredList;

    public ProductosAdapter(@NonNull FirestoreRecyclerOptions<Productos> options, Activity activity) {
        super(options);
        this.activity = activity;
        this.originalList = new ArrayList<>(options.getSnapshots());
        this.filteredList = new ArrayList<>(options.getSnapshots());
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
            // Acciones para el botón de agregar producto al carrito
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<Productos> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered = originalList;
                } else {
                    for (Productos producto : originalList) {
                        if (producto.getProductName().toLowerCase().contains(query)) {
                            filtered.add(producto);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Productos>) results.values;
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
