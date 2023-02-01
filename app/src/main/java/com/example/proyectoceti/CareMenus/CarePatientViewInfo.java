package com.example.proyectoceti.CareMenus;

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
import com.example.proyectoceti.databinding.FragmentCarePatientViewInfoBinding;

import java.util.Map;

public class CarePatientViewInfo extends Fragment {

    private FragmentCarePatientViewInfoBinding binding;
    private PatientsModel model;

    public CarePatientViewInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarePatientViewInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ((CarePatientView)getActivity()).Patientmodel;
        String address = "";
        try {
            address = Encrypter.Decrypt(((Map<String, Object>)model.getPatientInfo().get("Location")).get("address").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        binding.CarePatientViewSex.setText(model.getPatientInfo().get("Sex").toString());
        binding.CarePatientViewAddre.setText(address);
        binding.CarePatientViewBirth.setText(model.getPatientInfo().get("Birthday").toString());
        binding.CarePatientViewBlood.setText(model.getPatientInfo().get("Bloodtype").toString());
        binding.CarePatientViewCareType.setText(model.getPatientInfo().get("CareType").toString());
        binding.CarePatientViewAller.setText(model.getPatientInfo().get("Allergies").toString());
        binding.CarePatientViewIll.setText(model.getPatientInfo().get("Illness").toString());
    }
}