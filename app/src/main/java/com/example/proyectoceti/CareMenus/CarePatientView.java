package com.example.proyectoceti.CareMenus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.GeneralMenus.ViewButtonPresses;
import com.example.proyectoceti.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class CarePatientView extends AppCompatActivity {
    TextView CarePacName;
    RoundedImageView CarePacPFP;
    String url;
    PatientsModel Patientmodel;
    BottomNavigationView navView;
    ImageButton ViewEmergency;

    @SuppressLint({"UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_patient_view);
        navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.care_host_layout);
        NavController navCo = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navCo);
        Intent GetIntent = getIntent();
        Patientmodel = GetIntent.getParcelableExtra("PatientModel");
        CarePacName = findViewById(R.id.CarePatientViewName);
        CarePacPFP = findViewById(R.id.CarePatientViewPhoto);
        ViewEmergency = findViewById(R.id.ViewEmergency);
        url = ((Map)Patientmodel.getPatientInfo().get("Photo")).get("url").toString();
        Picasso.get().load(url).into(CarePacPFP);
        CarePacName.setText(Patientmodel.getPatientInfo().get("Name").toString() + " " + Patientmodel.getPatientInfo().get("LastName").toString());

        ViewEmergency.setOnClickListener(view -> {
            Intent viewButton = new Intent(CarePatientView.this, ViewButtonPresses.class);
            viewButton.putExtra("PatientID",Patientmodel.getPatientID());
            startActivity(viewButton);
        });

    }

}


