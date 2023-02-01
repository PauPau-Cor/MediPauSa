package com.example.proyectoceti.GeneralMenus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.ClassesAndModels.Frequency;
import com.example.proyectoceti.ClassesAndModels.NeedModel;
import com.example.proyectoceti.databinding.ActivityViewNeedBinding;
import com.example.proyectoceti.databinding.FrequencyRowBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ViewNeed extends AppCompatActivity {

    private ActivityViewNeedBinding binding;
    private NeedModel model;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewNeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        Intent receive = getIntent();
        model = receive.getParcelableExtra("NeedModel");

        binding.ViewNeedName.setText(model.getDescription());
        binding.ViewNeedEnd.setText(model.getNeedDate());
        if(model.getFrequencies()!=null) for (Frequency frequency: model.getFrequencies()) {
            addView(frequency);
        }

        binding.ViewNeedEdit.setOnClickListener(view -> {
            Intent intent = new Intent(ViewNeed.this, AddNeeds.class);
            intent.putExtra("NeedModel", model);
            intent.putExtra("PatientID",model.getPatientID());
            startActivity(intent);
        });

        binding.ViewNeedDelete.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(ViewNeed.this);
            builder.setMessage("¿Está seguro que quiere borrar la necesidad").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.collection("needs").whereEqualTo("description", model.getDescription()).whereEqualTo("patientID",model.getPatientID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot: snapshotList){
                            snapshot.getReference().delete().addOnSuccessListener(unused -> {
                                dialogInterface.dismiss();
                                finish();
                            });
                        }
                    });
                }
            }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });
    }

    private void addView(Frequency frequency) {
        FrequencyRowBinding RowBinding = FrequencyRowBinding.inflate(getLayoutInflater());
        final View frequencyList = RowBinding.getRoot();
        RowBinding.FrequencyTime.setText(frequency.getFreq());
        RowBinding.DoFreqBT.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(ViewNeed.this);
            builder.setMessage("¿El paciente ha realizado esta necesidad?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DoNeed(frequency);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });
        binding.ViewNeedFrequencies.addView(frequencyList);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DoNeed(Frequency frequency){
        int index = model.getFrequencies().indexOf(frequency);
        model.getFrequencies().set(index,new Frequency(frequency.getFreq(), true));
        db.collection("needs").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("description", model.getDescription()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                Date date = new Date();
                String strdate = new SimpleDateFormat("dd/MM/yyyy").format(date);
                String strtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                EventModel newevent = new EventModel("need", date, strdate, strtime, "Necesidad realizada : " + model.getDescription(), "Realizacion de necesidad", model.getPatientID(), snapshot.getReference().getId());
                db.collection("events").add(newevent);
                snapshot.getReference().update("frequencies", model.getFrequencies());
            }
            finish();
        });
    }
}