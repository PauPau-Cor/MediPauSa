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

import com.example.proyectoceti.ClassesAndModels.MedsModel;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.GeneralMenus.AddMeds;
import com.example.proyectoceti.GeneralMenus.ViewMed;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentFamPatientViewMedsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FamPatientViewMeds extends Fragment {

    private FirebaseFirestore db;
    private FragmentFamPatientViewMedsBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private PatientsModel model;


    public FamPatientViewMeds() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFamPatientViewMedsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ((FamPatientView)getActivity()).Patientmodel;
        db = FirebaseFirestore.getInstance();
        Query query = db.collection("medications").whereEqualTo("patientID", model.getPatientID());
        FirestoreRecyclerOptions<MedsModel> options = new FirestoreRecyclerOptions.Builder<MedsModel>()
                .setQuery(query, MedsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<MedsModel, FamPatMedsViewHolder>(options) {
            @NonNull
            @Override
            public FamPatMedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_meds_item_single, parent, false);
                return new FamPatMedsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FamPatMedsViewHolder holder, int position, @NonNull MedsModel model) {
                holder.PatientMedName.setText(model.getMedName());
                String doses = model.getFrequencies().get(0).getFreq();
                if(model.getFrequencies().size() > 0){
                    for (int i = 1; i < model.getFrequencies().size(); i++) {
                        doses = doses.concat(", "+ model.getFrequencies().get(i).getFreq());
                    }
                }
                holder.PatientMedDoses.setText(doses);
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), ViewMed.class);
                    intent.putExtra("MedModel", model);
                    startActivity(intent);
                });
            }
        };
        binding.FamPatientMedList.setHasFixedSize(false);
        binding.FamPatientMedList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.FamPatientMedList.setAdapter(adapter);

        binding.AddMedBT.setOnClickListener(view -> {
            Intent addmeds = new Intent(getActivity(), AddMeds.class);
            addmeds.putExtra("PatientID", model.getPatientID());
            startActivity(addmeds);
        });

    }

    private static class FamPatMedsViewHolder extends RecyclerView.ViewHolder {

        private final TextView PatientMedName;
        private final TextView PatientMedDoses;

        public FamPatMedsViewHolder(@NonNull View itemView) {
            super(itemView);
            PatientMedName = itemView.findViewById(R.id.PatMedName);
            PatientMedDoses = itemView.findViewById(R.id.PatMedDoses);
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