package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.personal_transaction;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.R;

import java.util.ArrayList;
import java.util.List;

public class transaction_adapter extends RecyclerView.Adapter<transaction_adapter.ViewHolder> {

    private List<personal_transaction> transactionList;
    private OnTransactionClickListener listener;
    private Context context;
    private data_holder dbHelper;
    private int currentSelectedPosition = -1; // ✅ Delete options ke liye

    public interface OnTransactionClickListener {
        void onTransactionClick(personal_transaction transaction);
        void onTransactionLongClick(personal_transaction transaction, int position);
        void onTransactionDeleted(); // ✅ New callback for deletion
        void onTransactionEdited(personal_transaction transaction); // ✅ New callback for edit
    }

    public transaction_adapter(List<personal_transaction> transactionList,
                               OnTransactionClickListener listener,
                               Context context) {
        this.transactionList = transactionList != null ? transactionList : new ArrayList<>();
        this.listener = listener;
        this.context = context;
        this.dbHelper = new data_holder(context);

        Log.d("TransactionAdapter", "🎯 Adapter created with " + this.transactionList.size() + " items");

        // Debug: List ke items log karo
        for (int i = 0; i < this.transactionList.size(); i++) {
            personal_transaction t = this.transactionList.get(i);
            Log.d("TransactionAdapter", "📝 Item " + i + ": " +
                    "ID=" + t.getId() +
                    ", Amount=" + t.getAmount() +
                    ", Desc=" + t.getDescription());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TransactionAdapter", "🔄 onCreateViewHolder() called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("TransactionAdapter", "🎨 onBindViewHolder() - Position: " + position);

        if (transactionList == null || transactionList.isEmpty()) {
            Log.d("TransactionAdapter", "⚠️ Transaction list empty at position: " + position);
            holder.showEmptyState();
            return;
        }

        if (position < 0 || position >= transactionList.size()) {
            Log.d("TransactionAdapter", "❌ Invalid position: " + position);
            holder.showEmptyState();
            return;
        }

        personal_transaction transaction = transactionList.get(position);
        if (transaction == null) {
            Log.d("TransactionAdapter", "❌ Transaction null at position: " + position);
            holder.showEmptyState();
            return;
        }

        Log.d("TransactionAdapter", "📊 Binding transaction - ID: " + transaction.getId() +
                ", Amount: " + transaction.getAmount() +
                ", Type: " + transaction.getType());

        // ✅ Pehle normal data bind karo
        holder.bind(transaction);

        // ✅ Delete options visibility
        if (position == currentSelectedPosition) {
            holder.deleteOptions.setVisibility(View.VISIBLE);
            holder.tvAmount.setVisibility(View.GONE); // ✅ Amount hide karo
            holder.ivTypeIcon.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.GONE);
            holder.tvDate.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
            Log.d("TransactionAdapter", "🗑️ Delete options shown for position: " + position);
        } else {
            holder.deleteOptions.setVisibility(View.GONE);
            holder.tvAmount.setVisibility(View.VISIBLE); // ✅ Amount show karo
            holder.ivTypeIcon.setVisibility(View.VISIBLE);
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvTime.setVisibility(View.VISIBLE);
        }

        // ✅ CRITICAL FIX: NORMAL CLICK PAR EDIT SCREEN OPEN KARO
        holder.itemView.setOnClickListener(v -> {
            if (currentSelectedPosition == -1) {
                // ✅ NORMAL CLICK: EDITING SCREEN OPEN KARO
                Log.d("TransactionAdapter", "👆 Normal click - Edit screen open for position: " + position);
                editTransaction(transaction);
            } else if (currentSelectedPosition != -1) {
                // Agar delete options visible hain, to unhe hide karo
                Log.d("TransactionAdapter", "👆 Click to hide delete options");
                currentSelectedPosition = -1;
                notifyDataSetChanged();
            }
        });

        // ✅ LONG CLICK: Delete options dikhao
        holder.itemView.setOnLongClickListener(v -> {
            Log.d("TransactionAdapter", "👆 Long press on position: " + position);

            if (currentSelectedPosition != -1 && currentSelectedPosition != position) {
                currentSelectedPosition = -1;
                notifyDataSetChanged();
            }

            currentSelectedPosition = position;
            notifyDataSetChanged();
            return true;
        });

        // ✅ EDIT BUTTON CLICK
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                Log.d("TransactionAdapter", "✏️ Edit button clicked for position: " + position);

                // 1. Hide delete options
                currentSelectedPosition = -1;
                notifyDataSetChanged();

                // 2. Edit transaction
                editTransaction(transaction);
            });
        }

        // ✅ CANCEL BUTTON CLICK
        if (holder.btnCancel != null) {
            holder.btnCancel.setOnClickListener(v -> {
                Log.d("TransactionAdapter", "❌ Cancel clicked for position: " + position);
                currentSelectedPosition = -1;
                notifyDataSetChanged();
            });
        }

        // ✅ DELETE BUTTON CLICK
        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                Log.d("TransactionAdapter", "🗑️ Delete clicked for position: " + position);
                deleteTransaction(transaction, holder.getAdapterPosition());
            });
        }
    }

    // ✅ DIRECT DELETE METHOD (NO NAVIGATION)
    private void deleteTransaction(personal_transaction transaction, int position) {
        if (transaction == null) {
            Log.d("TransactionAdapter", "❌ Cannot delete null transaction");
            return;
        }

        Log.d("TransactionDelete", "🔄 Deleting transaction: " +
                transaction.getDescription() + ", ID: " + transaction.getId() +
                ", Position: " + position);

        boolean success = dbHelper.deletePersonalTransaction(transaction.getId());

        if (success) {
            // 1. Remove from local list
            if (position >= 0 && position < transactionList.size()) {
                transactionList.remove(position);
                notifyItemRemoved(position);

                // Agar items bache hain to notify karo
                if (transactionList.size() > 0 && position <= transactionList.size()) {
                    notifyItemRangeChanged(position, transactionList.size() - position);
                }

                Toast.makeText(context, "✅ Transaction deleted", Toast.LENGTH_SHORT).show();

                // 2. Reset selection
                currentSelectedPosition = -1;

                // 3. Callback notify karein ki transaction delete hua hai
                if (listener != null) {
                    listener.onTransactionDeleted();
                }

                Log.d("TransactionDelete", "✅ Transaction deleted successfully. New list size: " + transactionList.size());

            } else {
                Log.d("TransactionDelete", "❌ Invalid position for deletion: " + position);
                Toast.makeText(context, "❌ Cannot delete - invalid position", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(context, "❌ Delete failed", Toast.LENGTH_SHORT).show();
            Log.d("TransactionDelete", "❌ Database deletion failed for ID: " + transaction.getId());

            // Reset selection
            currentSelectedPosition = -1;
            notifyDataSetChanged();
        }
    }

    // ✅ EDIT TRANSACTION METHOD (CRITICAL FIX)
    private void editTransaction(personal_transaction transaction) {
        Log.d("TransactionAdapter", "✏️ Editing transaction ID: " + transaction.getId());

        // 1. Hide delete options
        currentSelectedPosition = -1;
        notifyDataSetChanged();

        // 2. Open edit screen via listener
        if (listener != null) {
            listener.onTransactionEdited(transaction);
        }
    }

    @Override
    public int getItemCount() {
        int count = transactionList != null ? transactionList.size() : 0;
        Log.d("TransactionAdapter", "📊 getItemCount(): " + count);
        return count;
    }

    // ✅ Update method for refreshing data
    public void updateTransactionList(List<personal_transaction> newList) {
        Log.d("TransactionAdapter", "🔄 updateTransactionList() called. New size: " +
                (newList != null ? newList.size() : 0));

        if (newList == null) {
            if (transactionList != null) {
                transactionList.clear();
            } else {
                transactionList = new ArrayList<>();
            }
        } else {
            if (transactionList == null) {
                transactionList = new ArrayList<>();
            }
            transactionList.clear();
            transactionList.addAll(newList);

            // Debug log
            for (int i = 0; i < transactionList.size(); i++) {
                personal_transaction t = transactionList.get(i);
                Log.d("TransactionAdapter", "📝 Updated Item " + i + ": " +
                        "ID=" + t.getId() +
                        ", Amount=" + t.getAmount());
            }
        }

        notifyDataSetChanged();
        Log.d("TransactionAdapter", "✅ List updated. Current size: " + getItemCount());
    }

    // ✅ Set transaction list method
    public void setTransactionList(List<personal_transaction> newList) {
        Log.d("TransactionAdapter", "🔧 setTransactionList() called");
        if (newList != null) {
            transactionList = new ArrayList<>(newList);
        } else {
            transactionList = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    // ✅ Clear selection method
    public void clearSelection() {
        Log.d("TransactionAdapter", "🔧 clearSelection() called");
        currentSelectedPosition = -1;
        notifyDataSetChanged();
    }

    // ✅ Get current list (for debugging)
    public List<personal_transaction> getTransactionList() {
        return transactionList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAmount, tvDescription, tvDate, tvTime;
        ImageView ivTypeIcon, btnCancel, btnDelete, btnEdit;
        View typeIndicator;
        LinearLayout deleteOptions; // ✅ Delete options layout

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize main views
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivTypeIcon = itemView.findViewById(R.id.iv_type_icon);
            typeIndicator = itemView.findViewById(R.id.type_indicator);

            // ✅ Initialize delete options views
            deleteOptions = itemView.findViewById(R.id.deleteOptions);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);

            // ✅ Ensure initial state
            if (deleteOptions != null) {
                deleteOptions.setVisibility(View.GONE);
            }
            if (tvAmount != null) {
                tvAmount.setVisibility(View.VISIBLE);
            }
            if (ivTypeIcon != null) {
                ivTypeIcon.setVisibility(View.VISIBLE);
            }
        }

        public void bind(personal_transaction transaction) {
            if (transaction == null) {
                showEmptyState();
                return;
            }

            // Amount
            if (tvAmount != null) {
                String amount = transaction.getAmount();
                if (amount != null) {
                    // Remove any "Rs" prefix if exists
                    if (amount.startsWith("Rs")) {
                        amount = amount.replace("Rs", "").trim();
                    }
                    tvAmount.setText("Rs " + amount);
                } else {
                    tvAmount.setText("Rs 0");
                }
            }

            // Description
            if (tvDescription != null) {
                if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                    tvDescription.setText(transaction.getDescription());
                    tvDescription.setVisibility(View.VISIBLE);
                } else {
                    tvDescription.setText("No description");
                    tvDescription.setVisibility(View.VISIBLE);
                }
            }

            // Date and time
            if (tvDate != null && tvTime != null) {
                String dateTime = transaction.getDateTime();
                if (dateTime != null && !dateTime.isEmpty()) {
                    try {
                        // Format: "d MMM, hh:mm a" (e.g., "5 Mar, 10:30 AM")
                        String[] parts = dateTime.split(", ");
                        if (parts.length >= 2) {
                            tvDate.setText(parts[0]); // Date part
                            tvTime.setText(parts[1]); // Time part
                        } else {
                            tvDate.setText(dateTime);
                            tvTime.setText("");
                        }
                    } catch (Exception e) {
                        tvDate.setText(dateTime);
                        tvTime.setText("");
                    }
                } else {
                    tvDate.setText("No date");
                    tvTime.setText("");
                }
            }

            // Type color and icon
            if ("liye".equals(transaction.getType())) {
                // Income - Green
                if (tvAmount != null) {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
                }
                if (ivTypeIcon != null) {
                    ivTypeIcon.setImageResource(R.drawable.arrow_up);
                    ivTypeIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.green));
                }
                if (typeIndicator != null) {
                    typeIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.green));
                }
            } else {
                // Expense - Red
                if (tvAmount != null) {
                    tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
                }
                if (ivTypeIcon != null) {
                    ivTypeIcon.setImageResource(R.drawable.arrow_down);
                    ivTypeIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.red));
                }
                if (typeIndicator != null) {
                    typeIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.red));
                }
            }
        }

        public void showEmptyState() {
            Log.d("TransactionAdapter", "📭 Showing empty state in ViewHolder");
            if (tvAmount != null) {
                tvAmount.setText("No Data");
                tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.gray));
            }
            if (tvDescription != null) {
                tvDescription.setText("No transaction available");
                tvDescription.setVisibility(View.VISIBLE);
            }
            if (tvDate != null) tvDate.setText("");
            if (tvTime != null) tvTime.setText("");
            if (ivTypeIcon != null) ivTypeIcon.setVisibility(View.INVISIBLE);
            if (typeIndicator != null) typeIndicator.setVisibility(View.INVISIBLE);
        }
    }
}