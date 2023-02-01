package com.example.proyectoceti.GeneralMenus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.Frequency;
import com.example.proyectoceti.ClassesAndModels.MedsModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

public class AddMeds extends AppCompatActivity {

    EditText AddMedsExpir, AddMedsTermination;
    TextInputLayout AddMedsName, AddMedsQuantity, AddMedsDosage;
    Button BTAddFreq, AddMed;
    LinearLayout layoutFrequencyList;
    MedsModel medsModel;
    FirebaseFirestore db;
    String PatientID;
    boolean updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meds);
        Intent receive = getIntent();
        db = FirebaseFirestore.getInstance();;
        layoutFrequencyList= findViewById(R.id.LayoutFreqList);
        AddMedsName = findViewById(R.id.ETAddMedsName);
        AddMedsQuantity = findViewById(R.id.ETAddMedsQuantity);
        AddMedsExpir = findViewById(R.id.ETAddMedsExpir);
        AddMedsDosage = findViewById(R.id.ETAddMedsDosage);
        AddMedsTermination = findViewById(R.id.ETAddMedsTermination);
        BTAddFreq= findViewById(R.id.BTAddMedsAddFreq);
        AddMed= findViewById(R.id.BTAddMeds);

        medsModel = receive.getParcelableExtra("MedModel");
        if(medsModel != null){
            updater = true;
            AddMedsName.getEditText().setText(medsModel.getMedName());
            AddMedsQuantity.getEditText().setText(medsModel.getMedQuantity());
            AddMedsExpir.setText(medsModel.getExpirationDate());
            AddMedsDosage.getEditText().setText(medsModel.getDosage());
            AddMedsTermination.setText(medsModel.getTreatmentEndDate());
            if(medsModel.getFrequencies()!=null) for (Frequency frequency: medsModel.getFrequencies()) {
                addView(frequency);
            }
        }else{
            updater = false;
            addView(null);
        }
        PatientID = receive.getStringExtra("PatientID");

        BTAddFreq.setOnClickListener(view -> {
            addView(null);
        });

        AddMedsExpir.setOnClickListener(view -> popDatePicker(AddMedsExpir));

        AddMedsTermination.setOnClickListener(view -> popDatePicker(AddMedsTermination));

        AddMed.setOnClickListener(view -> {
            if(!Validations.AfterToday(AddMedsExpir.getText().toString())){
                AlertDialog.Builder builder=new AlertDialog.Builder(AddMeds.this);
                builder.setMessage("La fecha de expiracion de este medicamento ya pasó ¿registrar?").setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AddMedication();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }else{
                AddMedication();
            }
        });
    }

    private void AddMedication(){
        View FrequencyEntryView;
        List<Frequency> Frequencies = new ArrayList<>();
        for(int i = 0; i<layoutFrequencyList.getChildCount(); i++){
            FrequencyEntryView = layoutFrequencyList.getChildAt(i);
            EditText Freq = FrequencyEntryView.findViewById(R.id.AddMedsFreq);
            Frequency NewFreq = new Frequency(Freq.getText().toString(), false);
            if(!TextUtils.isEmpty(Freq.getText().toString()) && !Frequencies.contains(NewFreq)) Frequencies.add(NewFreq);
        }
        if(TextUtils.isEmpty(AddMedsName.getEditText().getText().toString())|| TextUtils.isEmpty(AddMedsQuantity.getEditText().getText().toString()) || TextUtils.isEmpty(AddMedsExpir.getText().toString())
                || TextUtils.isEmpty(AddMedsDosage.getEditText().getText().toString()) || TextUtils.isEmpty(AddMedsTermination.getText().toString()) || Frequencies.isEmpty()){
            Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Integer.valueOf(AddMedsQuantity.getEditText().getText().toString()) < 1){
            AddMedsQuantity.getEditText().setError("Cantidad no puede ser menor a 1");
            AddMedsQuantity.getEditText().requestFocus();
            return;
        }
        if(Integer.valueOf(AddMedsDosage.getEditText().getText().toString())<1){
            AddMedsDosage.setError("Dosis no puede ser menor a 1");
            AddMedsDosage.getEditText().requestFocus();
            return;
        }
        if(!Validations.AfterToday(AddMedsTermination.getText().toString())){
            Toast.makeText(AddMeds.this, "Fecha de termino de tratamiento no puede ser previa a la fecha actual.", Toast.LENGTH_SHORT).show();
            return;
        }
        MedsModel newmed = new MedsModel(AddMedsName.getEditText().getText().toString(), AddMedsQuantity.getEditText().getText().toString(),
                AddMedsExpir.getText().toString(), AddMedsDosage.getEditText().getText().toString(), AddMedsTermination.getText().toString(), PatientID, Frequencies);
        if(updater){
            db.collection("medications").whereEqualTo("medName", medsModel.getMedName()).whereEqualTo("patientID",medsModel.getPatientID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    snapshot.getReference().set(newmed).addOnSuccessListener(unused -> {
                        finish();
                    });
                }
            });
        }else{
            db.collection("medications").add(newmed).addOnSuccessListener(documentReference -> {
                Toast.makeText(AddMeds.this, "Medicamento registrado", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void addView(@Nullable Frequency frequency) {
        final View frequencyList = getLayoutInflater().inflate(R.layout.add_meds_frequency_row,null, false);
        Button RemoveFreq = frequencyList.findViewById(R.id.AddMedsFreqRemove);
        EditText Frequency = frequencyList.findViewById(R.id.AddMedsFreq);
        Frequency.setOnClickListener(view -> popTimePicker(frequencyList));
        RemoveFreq.setOnClickListener(view -> removeView(frequencyList));
        if(frequency!=null)Frequency.setText(frequency.getFreq());
        layoutFrequencyList.addView(frequencyList);
    }

    private void removeView(View view){
        if(layoutFrequencyList.getChildCount()!=1) layoutFrequencyList.removeView(view);
        else Toast.makeText(AddMeds.this, "Debe tener por lo menos un campo de frecuencia", Toast.LENGTH_SHORT).show();
    }

    private void popTimePicker(View view){
        EditText Frequency = view.findViewById(R.id.AddMedsFreq);
        final int[] hour = new int[1];
        final int[] minute = new int[1];
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, SelectedHour, SelectedMinute) -> {
            hour[0] = SelectedHour;
            minute[0] = SelectedMinute;
            Frequency.setText(Utilities.Round5Minutes(SelectedHour,SelectedMinute));
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour[0], minute[0], true);
        timePickerDialog.setTitle("Selecciona una frecuencia");
        timePickerDialog.show();
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
                AddMeds.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }
}