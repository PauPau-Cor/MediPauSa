package com.example.proyectoceti.FamMenus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.GeneralMenus.ViewButtonPresses;
import com.example.proyectoceti.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class FamPatientView extends AppCompatActivity {
    TextView FamPacName;
    ImageView FamPacPFP;
    ImageButton BTDeletePatient, BTUpdatePatient;
    String url;
    PatientsModel Patientmodel;
    BottomNavigationView navView;
    ImageButton ViewEmergency;

    @SuppressLint({"UseCompatLoadingForDrawables", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fam_patient_view);
        navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fam_host_layout);
        NavController navCo = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navCo);
        Intent GetIntent = getIntent();
        Patientmodel = GetIntent.getParcelableExtra("PatientModel");

        //Asocia las variables de elementos con los elementos de layout
        FamPacName = findViewById(R.id.FamPatientViewName);
        FamPacPFP = findViewById(R.id.FamPatientViewPhoto);
        BTDeletePatient = findViewById(R.id.FamDeletePatBT);
        BTUpdatePatient = findViewById(R.id.FamEditPatBT);
        ViewEmergency = findViewById(R.id.ViewEmergency);

        url = ((Map)Patientmodel.getPatientInfo().get("Photo")).get("url").toString();
        Picasso.get().load(url).into(FamPacPFP);
        FamPacName.setText(Patientmodel.getPatientInfo().get("Name").toString() + " " + Patientmodel.getPatientInfo().get("LastName").toString());

        BTDeletePatient.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(FamPatientView.this);
            builder.setMessage("¿Está seguro que quiere borrar al paciente?")
                    .setPositiveButton("Si", (dialogInterface, i) -> Delete(Patientmodel.getPatientID(), url))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });

        ViewEmergency.setOnClickListener(view -> {
            Intent viewButton = new Intent(FamPatientView.this, ViewButtonPresses.class);
            viewButton.putExtra("PatientID",Patientmodel.getPatientID());
            startActivity(viewButton);
        });

        BTUpdatePatient.setOnClickListener(view -> {
            Intent SinglePatient = new Intent(FamPatientView.this, EditPat.class);
            SinglePatient.putExtra("PatientModel", Patientmodel);
            startActivity(SinglePatient);
            finish();
        });
    }

    private void Delete(String patientID, String PFPurl){
        FirebaseStorage Storage = FirebaseStorage.getInstance();
        StorageReference PatientPFP = Storage.getReferenceFromUrl(PFPurl);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("medications").whereEqualTo("patientID", patientID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                snapshot.getReference().delete();
            }
        });
        DocumentReference PatientDoc = db.collection("patients").document(patientID);
        PatientDoc.delete().addOnSuccessListener(unused -> {
            PatientPFP.delete().addOnSuccessListener(unused1 -> {
                Toast.makeText(this, "Paciente borrado", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}