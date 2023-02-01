package com.example.proyectoceti.OperatorMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.UnbanRequestModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorPendingUnbansBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class OperatorPendingUnbans extends AppCompatActivity {

    private ActivityOperatorPendingUnbansBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorPendingUnbansBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db= FirebaseFirestore.getInstance();
        Query query = db.collection("unbanrequests").whereEqualTo("reviewed", false);
        FirestoreRecyclerOptions<UnbanRequestModel> options = new FirestoreRecyclerOptions.Builder<UnbanRequestModel>()
                .setQuery(query, UnbanRequestModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<UnbanRequestModel, UnbanViewHolder>(options) {
            @NonNull
            @Override
            public UnbanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operator_single_unban, parent, false);
                return new UnbanViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UnbanViewHolder holder, int position, @NonNull UnbanRequestModel model) {
                holder.SenderName.setText(model.getName());
                holder.ReportReason.setText(model.getReason());
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(OperatorPendingUnbans.this, OperatorViewUnbanRequest.class);
                    intent.putExtra("UnbanModel", model);
                    startActivity(intent);
                });
            }
        };
        binding.UnbansList.setHasFixedSize(false);
        binding.UnbansList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(OperatorPendingUnbans.this, LinearLayoutManager.VERTICAL,false));
        binding.UnbansList.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    private static class UnbanViewHolder extends RecyclerView.ViewHolder {

        private final TextView SenderName;
        private final TextView ReportReason;

        public UnbanViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderName= itemView.findViewById(R.id.SenderName);
            ReportReason = itemView.findViewById(R.id.ReportReason);
        }
    }
}