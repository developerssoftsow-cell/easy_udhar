package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.deleted_customer;
import com.Easy_Udhar_SoftSow.R;

import java.util.List;

public class deleted_customers_adapter extends RecyclerView.Adapter<deleted_customers_adapter.ViewHolder> {

    private List<deleted_customer> deletedCustomersList;
    private OnRestoreClickListener listener;
    private OnDeleteClickListener deleteListener;

    public deleted_customers_adapter(List<deleted_customer> deletedCustomersList,
                                     OnRestoreClickListener listener,
                                     OnDeleteClickListener deleteListener) {
        this.deletedCustomersList = deletedCustomersList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deleted_customer_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        deleted_customer deletedCustomer = deletedCustomersList.get(position);

        // ✅ BASIC INFO SET KARO
        holder.tvCustomerName.setText(deletedCustomer.getName());

        // ✅ DELETED DATE SET KARO
        if (deletedCustomer.getDeletedDate() != null && !deletedCustomer.getDeletedDate().isEmpty()) {
            holder.tvDeletedDate.setText("Deleted on: " + deletedCustomer.getDeletedDate());
        } else {
            holder.tvDeletedDate.setText("Deleted on: Unknown date");
        }

        // ✅ RESTORE BUTTON CLICK LISTENER
        holder.btnRestore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestoreClick(deletedCustomer, position);
            }
        });

        // ✅ CARD LONG PRESS LISTENER
        holder.cardView.setOnLongClickListener(v -> {
            showDeleteOverlay(holder);
            return true;
        });

        // ✅ CANCEL DELETE BUTTON
        holder.btnCancelDelete.setOnClickListener(v -> {
            hideDeleteOverlay(holder);
        });

        // ✅ CONFIRM DELETE BUTTON
        holder.btnConfirmDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(deletedCustomer, position);
            }
            hideDeleteOverlay(holder);
        });

        // ✅ CARD TOUCH LISTENER FOR SWIPE/CANCEL
        holder.cardView.setOnTouchListener((v, event) -> {
            if (holder.deleteOverlay.getVisibility() == View.VISIBLE) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Touch anywhere to cancel delete mode
                    hideDeleteOverlay(holder);
                    return true;
                }
            }
            return false;
        });
    }

    private void showDeleteOverlay(ViewHolder holder) {
        holder.deleteOverlay.setVisibility(View.VISIBLE);
        holder.mainContent.setVisibility(View.GONE);
        holder.cardView.setCardBackgroundColor(0xFFFFEBEE); // Light red background
    }

    private void hideDeleteOverlay(ViewHolder holder) {
        holder.deleteOverlay.setVisibility(View.GONE);
        holder.mainContent.setVisibility(View.VISIBLE);
        holder.cardView.setCardBackgroundColor(0xFFFFFFFF); // White background
    }

    @Override
    public int getItemCount() {
        return deletedCustomersList != null ? deletedCustomersList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCustomerName, tvDeletedDate;
        Button btnRestore;
        LinearLayout deleteOverlay, mainContent;
        Button btnCancelDelete, btnConfirmDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDeletedDate = itemView.findViewById(R.id.tvDeletedDate);
            btnRestore = itemView.findViewById(R.id.btnRestore);

            // Delete overlay components
            deleteOverlay = itemView.findViewById(R.id.deleteOverlay);
            mainContent = itemView.findViewById(R.id.mainContent);
            btnCancelDelete = itemView.findViewById(R.id.btnCancelDelete);
            btnConfirmDelete = itemView.findViewById(R.id.btnConfirmDelete);
        }
    }

    public interface OnRestoreClickListener {
        void onRestoreClick(deleted_customer deletedCustomer, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(deleted_customer deletedCustomer, int position);
    }
}