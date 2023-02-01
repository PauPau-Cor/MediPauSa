package com.example.proyectoceti.FamMenus;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.databinding.FragmentFamPatientViewInfoBinding;

import java.util.Map;

public class FamPatientViewInfo extends Fragment {

    private FragmentFamPatientViewInfoBinding binding;
    private PatientsModel model;

    public FamPatientViewInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFamPatientViewInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ((FamPatientView)getActivity()).Patientmodel;

        String address = "";
        try {
            address = Encrypter.Decrypt(((Map<String, Object>)model.getPatientInfo().get("Location")).get("address").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.FamPatientViewSex.setText(model.getPatientInfo().get("Sex").toString());
        binding.FamPatientViewAddre.setText(address);
        binding.FamPatientViewBirth.setText(model.getPatientInfo().get("Birthday").toString());
        binding.FamPatientViewBlood.setText(model.getPatientInfo().get("Bloodtype").toString());
        binding.FamPatientViewCareType.setText(model.getPatientInfo().get("CareType").toString());
        binding.FamPatientViewAller.setText(model.getPatientInfo().get("Allergies").toString());
        binding.FamPatientViewIll.setText(model.getPatientInfo().get("Illness").toString());
    }
}