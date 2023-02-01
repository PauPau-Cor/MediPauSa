package com.example.proyectoceti.GeneralMenus;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.ClassesAndModels.Frequency;
import com.example.proyectoceti.ClassesAndModels.MedsModel;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.databinding.ActivityViewMedBinding;
import com.example.proyectoceti.databinding.DialogAddMedStockBinding;
import com.example.proyectoceti.databinding.FrequencyRowBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewMed extends AppCompatActivity {

    private ActivityViewMedBinding binding;
    private MedsModel model;
    private FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        Intent receive = getIntent();
        model = receive.getParcelableExtra("MedModel");

        binding.ViewMedName.setText(model.getMedName());
        binding.ViewMedQuantity.setText(model.getMedQuantity());
        binding.ViewMedDosage.setText(model.getDosage());
        binding.ViewMedExpir.setText(model.getExpirationDate());
        binding.ViewMedEndTreatment.setText(model.getTreatmentEndDate());
        if(model.getFrequencies()!=null) for (Frequency frequency: model.getFrequencies()) {
            addView(frequency);
        }

        binding.ViewMedDelete.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(ViewMed.this);
            builder.setMessage("¿Está seguro que quiere borrar el medicamento").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.collection("medications").whereEqualTo("medName", model.getMedName()).whereEqualTo("patientID",model.getPatientID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
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

        binding.ViewMedEdit.setOnClickListener(view -> {
            Intent intent = new Intent(ViewMed.this, AddMeds.class);
            intent.putExtra("MedModel",model);
            intent.putExtra("PatientID", model.getPatientID());
            startActivity(intent);
        });

        binding.ViewMedAddMore.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DialogAddMedStockBinding dialogBinding = DialogAddMedStockBinding.inflate(getLayoutInflater());
            dialog.setContentView(dialogBinding.getRoot());
            dialog.show();
            dialogBinding.ETAddMedsExpir.setOnClickListener(view1 -> popDatePicker(dialogBinding.ETAddMedsExpir));
            dialogBinding.AddMedDialogAdd.setOnClickListener(view1 -> {
                if(TextUtils.isEmpty(dialogBinding.ETAddMedsExpir.getText().toString()) || TextUtils.isEmpty(dialogBinding.AddMedDialogQuantity.getEditText().getText().toString())){
                    Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Integer.valueOf(dialogBinding.AddMedDialogQuantity.getEditText().toString()) <1){
                    Toast.makeText(this, "Cantidad no podrá ser menor a 1", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Validations.AfterToday(dialogBinding.ETAddMedsExpir.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("La fecha de expiracion de este medicamento ya pasó ¿registrar?")
                            .setPositiveButton("Si", (dialogInterface, i) -> {
                                addStock(dialogBinding.AddMedDialogQuantity.getEditText().getText().toString(), dialogBinding.ETAddMedsExpir.getText().toString());
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                    AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                    return;
                }
                addStock(dialogBinding.AddMedDialogQuantity.getEditText().getText().toString(), dialogBinding.ETAddMedsExpir.getText().toString());
                dialog.dismiss();
            });
        });
    }

    private void popDatePicker(EditText DateToSet){
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog.OnDateSetListener setListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date= dayOfMonth+"/"+ month1 +"/"+ year1;
            DateToSet.setText(date);
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ViewMed.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    private void addStock(String newStock, String ExpiryDate) {
        db.collection("medications").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("medName", model.getMedName()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot : snapshotList) {
                snapshot.getReference().update("newExpirationDate", ExpiryDate, "newMedQuantity", newStock).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Stock añadido", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addView(Frequency frequency) {
        FrequencyRowBinding RowBinding = FrequencyRowBinding.inflate(getLayoutInflater());
        final View frequencyList = RowBinding.getRoot();
        RowBinding.FrequencyTime.setText(frequency.getFreq());
        RowBinding.DoFreqBT.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(ViewMed.this);
            builder.setMessage("¿El paciente se ha tomado este medicamento?")
                    .setPositiveButton("Si", (dialogInterface, i) -> ValidateExpir(frequency))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });
        binding.ViewMedFrequencies.addView(frequencyList);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ValidateExpir(Frequency frequency){
        if(Validations.AfterToday(model.getExpirationDate())){
            TakeMed(frequency);
        }else{
            AlertDialog.Builder builder=new AlertDialog.Builder(ViewMed.this);
            builder.setMessage("La fecha de expiracion de este medicamento ya pasó ¿tomar?")
                    .setPositiveButton("Si", (dialogInterface, i) -> TakeMed(frequency))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void TakeMed(Frequency frequency){
        int index = model.getFrequencies().indexOf(frequency);
        model.getFrequencies().set(index,new Frequency(frequency.getFreq(), true));
        db.collection("medications").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("medName", model.getMedName()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                Date date = new Date();
                String strdate = new SimpleDateFormat("d/M/yyyy").format(date);
                String strtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                EventModel newevent = new EventModel("medication", date, strdate, strtime, "Toma de medicamento: " + model.getMedName(), "Toma de medicamento", model.getPatientID(), snapshot.getReference().getId());
                db.collection("events").add(newevent);
                int NewQuantity = Integer.valueOf(model.getMedQuantity()) - Integer.valueOf(model.getDosage());
                if(NewQuantity < 1){
                    if(model.getNewMedQuantity() != null){
                        NewQuantity += Integer.valueOf(model.getNewMedQuantity());
                        snapshot.getReference().update("frequencies", model.getFrequencies(), "medQuantity", String.valueOf(NewQuantity),
                                "expirationDate", model.getNewExpirationDate(), "newExpirationDate", null, "newMedQuantity", null);
                    }else{
                        NewQuantity = 0;
                        snapshot.getReference().update("frequencies", model.getFrequencies(), "medQuantity", String.valueOf(NewQuantity));
                    }
                }
            }
            finish();
        });
    }
}