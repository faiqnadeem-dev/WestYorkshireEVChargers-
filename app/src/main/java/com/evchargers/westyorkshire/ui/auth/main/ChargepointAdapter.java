package com.evchargers.westyorkshire.ui.auth.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evchargers.westyorkshire.R;
import com.evchargers.westyorkshire.model.Chargepoint;
import java.util.ArrayList;
import java.util.List;

public class ChargepointAdapter extends RecyclerView.Adapter<ChargepointAdapter.ChargepointViewHolder> {
    private List<Chargepoint> chargepoints = new ArrayList<>();
    private OnChargepointClickListener listener;

    public interface OnChargepointClickListener {
        void onChargepointClick(Chargepoint chargepoint);
    }

    public ChargepointAdapter(OnChargepointClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChargepointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chargepoint, parent, false);
        return new ChargepointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChargepointViewHolder holder, int position) {
        Chargepoint chargepoint = chargepoints.get(position);
        holder.bind(chargepoint);
    }

    @Override
    public int getItemCount() {
        return chargepoints.size();
    }

    public void setChargepoints(List<Chargepoint> chargepoints) {
        this.chargepoints = chargepoints;
        notifyDataSetChanged();
    }

    class ChargepointViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView countyText;
        private final TextView chargerTypeText;
        private final TextView statusText;

        ChargepointViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            countyText = itemView.findViewById(R.id.countyText);
            chargerTypeText = itemView.findViewById(R.id.chargerTypeText);
            statusText = itemView.findViewById(R.id.statusText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChargepointClick(chargepoints.get(position));
                }
            });
        }

        void bind(Chargepoint chargepoint) {
            nameText.setText(chargepoint.getName());
            countyText.setText(chargepoint.getCounty());
            chargerTypeText.setText(chargepoint.getChargerType());
            statusText.setText(chargepoint.getStatus());
        }
    }
}
