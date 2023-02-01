package com.example.proyectoceti.OperatorMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ReportsModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentOperatorPendingReportsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class OperatorPendingReports extends Fragment {

    private FragmentOperatorPendingReportsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;

    public OperatorPendingReports() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding= FragmentOperatorPendingReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db= FirebaseFirestore.getInstance();
        Query query = db.collection("reports").whereEqualTo("reviewed", false).orderBy("receiverID");
        FirestoreRecyclerOptions<ReportsModel> options = new FirestoreRecyclerOptions.Builder<ReportsModel>()
                .setQuery(query, ReportsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ReportsModel, ReportsViewHolder>(options) {
            @NonNull
            @Override
            public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operator_single_report, parent, false);
                return new ReportsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReportsViewHolder holder, int position, @NonNull ReportsModel model) {
                holder.ReceiverName.setText(model.getReceiverName());
                holder.SenderName.setText(model.getSenderName());
                holder.ReportReason.setText(model.getReason());
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), OperatorViewReport.class);
                    intent.putExtra("ReportModel", model);
                    startActivity(intent);
                });
            }
        };
        binding.ReportsList.setHasFixedSize(false);
        binding.ReportsList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.ReportsList.setAdapter(adapter);

        binding.UnbanRequestsBT.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), OperatorPendingUnbans.class)));
    }

    private static class ReportsViewHolder extends RecyclerView.ViewHolder {

        private final TextView SenderName;
        private final TextView ReceiverName;
        private final TextView ReportReason;

        public ReportsViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderName= itemView.findViewById(R.id.SenderName);
            ReceiverName= itemView.findViewById(R.id.ReceiverName);
            ReportReason = itemView.findViewById(R.id.ReportReason);
        }
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

}