package com.evchargers.westyorkshire.ui.auth.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evchargers.westyorkshire.databinding.ItemAdminChargepointBinding;
import com.evchargers.westyorkshire.model.Chargepoint;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;



public class AdminChargepointAdapter extends RecyclerView.Adapter<AdminChargepointAdapter.ViewHolder> {
    private List<Chargepoint> chargepoints = new ArrayList<>();
    private OnChargepointActionListener listener;

    public interface OnChargepointActionListener {
        void onEditClick(Chargepoint chargepoint);
        void onDeleteClick(Chargepoint chargepoint);
    }

    public void setListener(OnChargepointActionListener listener) {
        this.listener = listener;
    }

    public void setChargepoints(List<Chargepoint> chargepoints) {
        try {
            this.chargepoints = new ArrayList<>(chargepoints);
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("AdminAdapter", "Error setting chargepoints: " + e.getMessage());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminChargepointBinding binding = ItemAdminChargepointBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chargepoint chargepoint = chargepoints.get(position);
        holder.bind(chargepoint);
    }

    @Override
    public int getItemCount() {
        return chargepoints.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminChargepointBinding binding;

        ViewHolder(ItemAdminChargepointBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Chargepoint chargepoint) {
            binding.chargepointName.setText(chargepoint.getName());
            binding.chargepointStatus.setText(chargepoint.getStatus());
            binding.chargepointType.setText(chargepoint.getChargerType());

            binding.editButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(chargepoint);
            });

            binding.deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(chargepoint);
            });
        }
    }
}
