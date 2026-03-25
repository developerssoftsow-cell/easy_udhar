package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.customer_detail;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.add_contact_class;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.R;

import java.util.List;

public class add_contact_adapter extends RecyclerView.Adapter<add_contact_adapter.ViewHolder> {

    private Context context;
    private List<add_contact_class> contactList;
    private data_holder dbHelper;

    public add_contact_adapter(Context context, List<add_contact_class> contactList) {
        this.context = context;
        this.contactList = contactList;
        this.dbHelper = new data_holder(context);

        // Check which contacts are already added
        checkAlreadyAddedContacts();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        add_contact_class contact = contactList.get(position);

        if (contact.getName() != null && !contact.getName().isEmpty()) {
            holder.txtName.setText(contact.getName());
        } else {
            holder.txtName.setText(contact.getNumber());
        }

        holder.txtNumber.setText(contact.getNumber());

        // DEBUG: Log each contact status
        Log.d("CONTACT_DEBUG", "Name: " + contact.getName() + ", Added: " + contact.isAdded());

        if (contact.isAdded()) {
            holder.btnAdd.setText("ADDED");
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setTextColor(context.getResources().getColor(R.color.grey));
            holder.btnAdd.setBackgroundResource(R.drawable.add_button_disabled);
        } else {
            holder.btnAdd.setText("+ ADD");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setTextColor(context.getResources().getColor(R.color.blue));
            holder.btnAdd.setBackgroundResource(R.drawable.add_button_bg);
        }

        // Add button click listener
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact.isAdded()) {
                    Toast.makeText(context, "Customer already added!", Toast.LENGTH_SHORT).show();
                } else {
                    addCustomerToDatabase(contact, holder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private void checkAlreadyAddedContacts() {
        Log.d("CONTACT_DEBUG", "Checking already added contacts...");

        // Database se check karein kaun se contacts already added hain
        for (add_contact_class contact : contactList) {
            boolean exists = dbHelper.checkCustomerExists(contact.getName(), contact.getNumber());
            contact.setAdded(exists);
            Log.d("CONTACT_DEBUG", "Contact: " + contact.getName() + " - Exists: " + exists);
        }

        // UI update karein
        notifyDataSetChanged();
    }

    private void addCustomerToDatabase(add_contact_class contact, ViewHolder holder) {
        Log.d("CONTACT_DEBUG", "Adding customer: " + contact.getName());

        // Database mein customer add karein
        long customerId = dbHelper.addCustomer(contact.getName(), contact.getNumber());

        if (customerId != -1) {
            // Success
            contact.setAdded(true);

            // Update button UI immediately
            holder.btnAdd.setText("ADDED");
            holder.btnAdd.setEnabled(false);
            holder.btnAdd.setTextColor(context.getResources().getColor(R.color.grey));
            holder.btnAdd.setBackgroundResource(R.drawable.add_button_disabled);

            Toast.makeText(context, "Customer added successfully!", Toast.LENGTH_SHORT).show();

            // Customer detail screen par navigate karein
            navigateToCustomerDetail((int) customerId, contact.getName());
        } else {
            Toast.makeText(context, "Failed to add customer!", Toast.LENGTH_SHORT).show();
        }

        // Refresh all contacts to update any duplicates
        checkAlreadyAddedContacts();
    }

    private void navigateToCustomerDetail(int customerId, String customerName) {
        Intent intent = new Intent(context, customer_detail.class);
        intent.putExtra("customer_id", customerId);
        intent.putExtra("customer_name", customerName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Public method to refresh contacts from outside
    public void refreshContacts() {
        checkAlreadyAddedContacts();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtNumber;
        Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtNumber = itemView.findViewById(R.id.txtNumber);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}