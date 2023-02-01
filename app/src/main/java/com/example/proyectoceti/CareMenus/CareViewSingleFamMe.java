package com.example.proyectoceti.CareMenus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectoceti.ClassesAndModels.DayAndTime;
import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.databinding.FragmentCareViewSingleFamMeBinding;
import com.example.proyectoceti.databinding.LinkedCareShiftRowBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CareViewSingleFamMe extends Fragment {

    private FragmentCareViewSingleFamMeBinding binding;
    private UserModel model;
    private LinkedUsersModel LinkedModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String CareUserID;

    public CareViewSingleFamMe() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCareViewSingleFamMeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ((CareViewSingleLinkedFam)getActivity()).model;
        LinkedModel = ((CareViewSingleLinkedFam)getActivity()).linkedUsersModel;
        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        CareUserID = mAuth.getUid();

        db.collection("linkedusers").whereEqualTo("careUserID", CareUserID).whereEqualTo("famUserID",model.getUserInfo().get("UserID")).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                LinkedModel = snapshot.toObject(LinkedUsersModel.class);
            }
            Picasso.get().load(LinkedModel.getProfilePicture().url).into(binding.CareViewSingleFamPhoto);
            binding.CareViewSingleFamname.setText(LinkedModel.getName());
            binding.CareViewSingleFamAccSalary.setText(String.valueOf(LinkedModel.getAccumulatedSalary()));
            binding.CareViewSingleFamSalary.setText(String.valueOf(LinkedModel.getSalary()));
            binding.CareViewSingleFamShiftsWorked.setText(String.valueOf(LinkedModel.getHoursWorked()/60));
            if(LinkedModel.getShifts()!=null) for (DayAndTime shift: LinkedModel.getShifts()) {
                addView(shift);
            }

        });

    }


    private void addView(DayAndTime shift) {
        LinkedCareShiftRowBinding RowBinding = LinkedCareShiftRowBinding.inflate(getLayoutInflater());
        final View frequencyList = RowBinding.getRoot();
        RowBinding.LinkedCareDay.setText(shift.getDay());
        RowBinding.LinkedCareHours.setText(shift.getStartTime() + " " + shift.getEndTime());
        binding.LayoutLinkedCareShifts.addView(frequencyList);
    }
}