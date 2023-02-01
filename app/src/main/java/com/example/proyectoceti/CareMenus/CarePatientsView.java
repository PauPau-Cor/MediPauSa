package com.example.proyectoceti.CareMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCarePatientsViewBinding;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class CarePatientsView extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String CareUserID;
    private FragmentCarePatientsViewBinding binding;
    private FirestorePagingAdapter adapter;

    public CarePatientsView() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarePatientsViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        CareUserID = mAuth.getUid();
        Query query = db.collection("patients").whereArrayContains("CareUserIDs", CareUserID);
        PagingConfig config = new PagingConfig(10, 5,false);
        FirestorePagingOptions<PatientsModel> options = new FirestorePagingOptions.Builder<PatientsModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, PatientsModel.class)
                .build();
        adapter = new FirestorePagingAdapter<PatientsModel, FamPatientsViewHolder>(options) {
            @NonNull
            @Override
            public FamPatientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fam_patient_item_single, parent, false);
                return new FamPatientsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FamPatientsViewHolder holder, int position, @NonNull PatientsModel model) {

                holder.FamPatName.setText(model.getPatientInfo().get("Name").toString() + " " + model.getPatientInfo().get("LastName"));
                holder.FamPatCareType.setText(model.getPatientInfo().get("CareType").toString());
                String url = ((Map)model.getPatientInfo().get("Photo")).get("url").toString();
                Picasso.get().load(url).into(holder.FamPatPhoto);
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), CarePatientView.class);
                    intent.putExtra("PatientModel", model);
                    startActivity(intent);
                });
            }
        };

        binding.CarePatientList.setHasFixedSize(true);
        binding.CarePatientList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.CarePatientList.setAdapter(adapter);
    }

    private static class FamPatientsViewHolder extends RecyclerView.ViewHolder {

        private final TextView FamPatName;
        private final TextView FamPatCareType;
        private final ImageView FamPatPhoto;

        public FamPatientsViewHolder(@NonNull View itemView) {
            super(itemView);
            FamPatName = itemView.findViewById(R.id.FamPatSingleName);
            FamPatCareType = itemView.findViewById(R.id.FamPatSingleCareType);
            FamPatPhoto = itemView.findViewById(R.id.FamPatSinglePhoto);
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