package com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
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

import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.model_class.customer_class;
import com.Easy_Udhar_SoftSow.Easy_Udhar_SoftSow.repository.data_holder;
import com.Easy_Udhar_SoftSow.R;

import java.util.List;

public class dashboard_adapter extends RecyclerView.Adapter<dashboard_adapter.ViewHolder> {

    private List<customer_class> customerList;
    private OnCustomerClickListener listener;
    private Context context;
    private data_holder dbHelper;
    private int currentSelectedPosition = -1;

    public dashboard_adapter(List<customer_class> customerList, OnCustomerClickListener listener, Context context) {
        this.customerList = customerList;
        this.listener = listener;
        this.context = context;
        this.dbHelper = new data_holder(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        customer_class customer = customerList.get(position);

        // ✅ Basic data set karo
        holder.name.setText(customer.getName());

        // Amount set karo based on net amount
        double netAmount = customer.getNetAmount();
        if (netAmount > 0) {
            holder.amount.setText("Rs. " + String.format("%.2f", Math.abs(netAmount)));
            holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else if (netAmount < 0) {
            holder.amount.setText("Rs. " + String.format("%.2f", Math.abs(netAmount)));
            holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
        } else {
            holder.amount.setText("Rs. 0");
            holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        }

        // ✅ Profile image handling
        if (customer.hasProfileImage()) {
            holder.avatar.setVisibility(View.GONE);
            holder.imgAvatar.setVisibility(View.VISIBLE);
            loadProfileImageInBackground(holder.imgAvatar, customer.getProfileImagePath(), holder);
        } else {
            holder.avatar.setVisibility(View.VISIBLE);
            holder.imgAvatar.setVisibility(View.GONE);
            if (!customer.getName().isEmpty()) {
                holder.avatar.setText(customer.getInitial());
            } else {
                holder.avatar.setText("?");
            }
        }

        // ✅ Delete options visibility - SIRF AMOUNT HIDE KARO
        if (position == currentSelectedPosition) {
            holder.deleteOptions.setVisibility(View.VISIBLE);
            holder.amount.setVisibility(View.GONE); // ✅ SIRF AMOUNT HIDE
        } else {
            holder.deleteOptions.setVisibility(View.GONE);
            holder.amount.setVisibility(View.VISIBLE); // ✅ AMOUNT WAPAS SHOW
        }

        // ✅ Normal click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && currentSelectedPosition == -1) {
                listener.onCustomerClick(customer);
            } else if (currentSelectedPosition != -1) {
                // Agar delete options visible hain to unhe hide karo
                currentSelectedPosition = -1;
                notifyDataSetChanged();
            }
        });

        // ✅ Long press listener for delete options
        holder.itemView.setOnLongClickListener(v -> {
            if (currentSelectedPosition != -1) {
                // Agar koi aur card already selected hai, to usko hide karo
                currentSelectedPosition = -1;
                notifyDataSetChanged();
            }
            currentSelectedPosition = position;
            notifyDataSetChanged();
            return true;
        });

        // ✅ Cancel button click
        holder.btnCancel.setOnClickListener(v -> {
            currentSelectedPosition = -1;
            notifyDataSetChanged();
        });

        // ✅ Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            deleteCustomer(customer, position);
        });
    }

    // ✅ CORRECTED: Delete customer method (NO NAVIGATION)
// ✅ CORRECTED: Delete customer method (INSTANT UPDATE)
    private void deleteCustomer(customer_class customer, int position) {
        Log.d("Delete", "🔄 Deleting customer: " + customer.getName() + ", ID: " + customer.getId());

        boolean success = dbHelper.deleteCustomer(customer.getId());

        if (success) {
            // 1. Remove from local list
            customerList.remove(position);
            notifyItemRemoved(position);

            if (customerList.size() > 0) {
                notifyItemRangeChanged(position, customerList.size());
            }

            Toast.makeText(context, "✅ Customer delete ho gaya", Toast.LENGTH_SHORT).show();

            // 2. Reset selection
            currentSelectedPosition = -1;

            // 3. 🚨 IMPORTANT: Dashboard ko INSTANT NOTIFY karein
            try {
                // Broadcast send karein (Application context use karein)
                Intent broadcastIntent = new Intent("CUSTOMER_DELETED");
                broadcastIntent.putExtra("deleted_customer_id", customer.getId());
                broadcastIntent.putExtra("deleted_customer_name", customer.getName());
                broadcastIntent.putExtra("deleted_amount", customer.getNetAmount());

                // Application context use karein taki crash na ho
                context.getApplicationContext().sendBroadcast(broadcastIntent);
                Log.d("Delete", "📢 Broadcast sent to dashboard");
            } catch (Exception e) {
                Log.e("Delete", "❌ Error sending broadcast: " + e.getMessage());

                // Alternative: Handler se run karein
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            context.sendBroadcast(new Intent("CUSTOMER_DELETED"));
                        } catch (Exception ex) {
                            Log.e("Delete", "❌ Retry failed: " + ex.getMessage());
                        }
                    }
                }, 100);
            }

            Log.d("Delete", "✅ Customer deleted successfully: " + customer.getName());

        } else {
            Toast.makeText(context, "❌ Delete fail ho gaya", Toast.LENGTH_SHORT).show();
            Log.e("Delete", "Delete failed for customer: " + customer.getName());

            // Reset selection
            currentSelectedPosition = -1;
            notifyDataSetChanged();
        }
    }
    // ✅ Background thread mein image load karo
    private void loadProfileImageInBackground(ImageView imageView, String imagePath, ViewHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(circularBitmap);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.imgAvatar.setVisibility(View.GONE);
                            holder.avatar.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public void updateCustomerList(List<customer_class> newCustomerList) {
        this.customerList.clear();
        this.customerList.addAll(newCustomerList);
        notifyDataSetChanged();
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int min = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, min, min);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, min/2, min/2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,
                new Rect((width - min)/2, (height - min)/2, (width + min)/2, (height + min)/2),
                rect,
                paint);

        return output;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, amount, avatar;
        ImageView imgAvatar, btnCancel, btnDelete;
        LinearLayout deleteOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            amount = itemView.findViewById(R.id.ttamount);
            avatar = itemView.findViewById(R.id.tvAvatar);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            deleteOptions = itemView.findViewById(R.id.deleteOptions);

            // ✅ Ensure initial state
            avatar.setVisibility(View.VISIBLE);
            imgAvatar.setVisibility(View.GONE);
            deleteOptions.setVisibility(View.GONE);
            amount.setVisibility(View.VISIBLE); // ✅ Amount by default visible
        }
    }

    public interface OnCustomerClickListener {
        void onCustomerClick(customer_class customer);
    }
}