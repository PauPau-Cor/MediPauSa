package com.example.proyectoceti.GeneralMenus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.Frequency;
import com.example.proyectoceti.ClassesAndModels.NeedModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

public class AddNeeds extends AppCompatActivity {

    TextInputLayout AddNeedsDescription;
    EditText AddNeedsTermination;
    Button BTAddFreq, AddNeed;
    LinearLayout layoutFrequencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_needs);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        layoutFrequencyList= findViewById(R.id.LayoutFreqList);
        AddNeedsDescription = findViewById(R.id.ETAddNeedDes);
        AddNeedsTermination = findViewById(R.id.ETAddNeedTermination);
        BTAddFreq= findViewById(R.id.BTAddNeedAddFreq);
        AddNeed= findViewById(R.id.BTAddNeed);

        Intent receive = getIntent();
        boolean updater;
        NeedModel needModel = receive.getParcelableExtra("NeedModel");
        if(needModel != null){
            updater = true;
            AddNeedsDescription.getEditText().setText(needModel.getDescription());
            AddNeedsTermination.setText(needModel.getNeedDate());
            if(needModel.getFrequencies()!=null) for (Frequency frequency: needModel.getFrequencies()) {
                addView(frequency);
            }
        }else{
            updater = false;
            addView(null);
        }
        String PatientID = receive.getStringExtra("PatientID");

        BTAddFreq.setOnClickListener(view -> {
            addView(null);
        });

        AddNeedsTermination.setOnClickListener(view -> popDatePicker(AddNeedsTermination));

        AddNeed.setOnClickListener(view -> {
            View FrequencyEntryView;
            List<Frequency> Frequencies = new ArrayList<>();
            for(int i = 0; i<layoutFrequencyList.getChildCount(); i++){
                FrequencyEntryView = layoutFrequencyList.getChildAt(i);
                EditText Freq = FrequencyEntryView.findViewById(R.id.AddMedsFreq);
                Frequency NewFreq = new Frequency(Freq.getText().toString(), false);
                if(!TextUtils.isEmpty(Freq.getText().toString()) && !Frequencies.contains(NewFreq)) Frequencies.add(NewFreq);
            }
            if(TextUtils.isEmpty(AddNeedsDescription.getEditText().getText().toString())|| TextUtils.isEmpty(AddNeedsTermination.getText().toString()) || Frequencies.isEmpty()){
                Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            NeedModel newNeed = new NeedModel(AddNeedsDescription.getEditText().getText().toString(), AddNeedsTermination.getText().toString(), PatientID, Frequencies);
            if(updater){
                db.collection("needs").whereEqualTo("description", needModel.getDescription()).whereEqualTo("patientID", needModel.getPatientID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        snapshot.getReference().set(newNeed).addOnSuccessListener(unused -> {
                            finish();
                        });
                    }
                });
            }else{
                db.collection("needs").add(newNeed).addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddNeeds.this, "Necesidad registrada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
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
        else Toast.makeText(AddNeeds.this, "Debe tener por lo menos un campo de frecuencia", Toast.LENGTH_SHORT).show();
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
                AddNeeds.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }
}