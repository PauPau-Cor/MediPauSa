package com.example.proyectoceti.FamMenus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.NeedModel;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.GeneralMenus.AddNeeds;
import com.example.proyectoceti.GeneralMenus.ViewNeed;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentFamPatientViewNeedsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FamPatientViewNeeds extends Fragment {

    private FragmentFamPatientViewNeedsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private PatientsModel model;
    private FirebaseFirestore db;

    public FamPatientViewNeeds() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFamPatientViewNeedsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ((FamPatientView)getActivity()).Patientmodel;
        db = FirebaseFirestore.getInstance();
        Query query = db.collection("needs").whereEqualTo("patientID", model.getPatientID());
        FirestoreRecyclerOptions<NeedModel> options = new FirestoreRecyclerOptions.Builder<NeedModel>()
                .setQuery(query, NeedModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<NeedModel, FamPatNeedsViewHolder>(options) {
            @NonNull
            @Override
            public FamPatNeedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_meds_item_single, parent, false);
                return new FamPatNeedsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FamPatNeedsViewHolder holder, int position, @NonNull NeedModel model) {
                holder.PatientNeedName.setText(model.getDescription());
                String doses = model.getFrequencies().get(0).getFreq();
                if(model.getFrequencies().size() > 0){
                    for (int i = 1; i < model.getFrequencies().size(); i++) {
                        doses = doses.concat(", "+ model.getFrequencies().get(i).getFreq());
                    }
                }
                holder.PatientNeedDoses.setText(doses);
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), ViewNeed.class);
                    intent.putExtra("NeedModel", model);
                    startActivity(intent);
                });
            }
        };
        binding.FamPatientNeedList.setHasFixedSize(false);
        binding.FamPatientNeedList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.FamPatientNeedList.setAdapter(adapter);

        binding.AddNeedBT.setOnClickListener(v -> {
            Intent addneeds = new Intent(getActivity(), AddNeeds.class);
            addneeds.putExtra("PatientID", model.getPatientID());
            startActivity(addneeds);
        });
    }

    private static class FamPatNeedsViewHolder extends RecyclerView.ViewHolder {

        private final TextView PatientNeedName;
        private final TextView PatientNeedDoses;

        public FamPatNeedsViewHolder(@NonNull View itemView) {
            super(itemView);
            PatientNeedName = itemView.findViewById(R.id.PatMedName);
            PatientNeedDoses = itemView.findViewById(R.id.PatMedDoses);
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

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
    }
}