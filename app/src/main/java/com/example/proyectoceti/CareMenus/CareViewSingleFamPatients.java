package com.example.proyectoceti.CareMenus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCareViewSingleFamPatientsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class CareViewSingleFamPatients extends Fragment {

    private FragmentCareViewSingleFamPatientsBinding binding;
    private UserModel model;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String CareUserID;
    private FirestoreRecyclerAdapter adapter;

    public CareViewSingleFamPatients() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCareViewSingleFamPatientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ((CareViewSingleLinkedFam)getActivity()).model;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        CareUserID = mAuth.getUid();
        Query query = db.collection("patients").whereEqualTo("FamUserID", model.getUserInfo().get("UserID")).whereArrayContains("CareUserIDs", CareUserID);
        FirestoreRecyclerOptions<PatientsModel> options = new FirestoreRecyclerOptions.Builder<PatientsModel>()
                .setQuery(query, PatientsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PatientsModel, FamPatientsViewHolder>(options) {
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
        binding.CarePatientList.setHasFixedSize(false);
        binding.CarePatientList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
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