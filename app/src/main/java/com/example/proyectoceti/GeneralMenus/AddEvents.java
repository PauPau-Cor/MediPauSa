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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AddEvents extends AppCompatActivity {
    TextInputLayout EventName, EventDescription;
    EditText Date, Time;
    boolean updater;
    Button AddEvent;
    EventModel eventModel;
    Calendar timestampCal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_events);
        Intent receive = getIntent();

        String dateString;
        String PatientID;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        EventName = findViewById(R.id.ETAddEventName);
        EventDescription = findViewById(R.id.ETAddEventDes);
        AddEvent = findViewById(R.id.BTAddEvent);
        Date = findViewById(R.id.ETAddEventDate);
        Time = findViewById(R.id.ETAddEventTime);

        eventModel = receive.getParcelableExtra("EventModel");
        if(eventModel != null){
            updater = true;
            dateString= eventModel.getDate();
            PatientID = eventModel.getPatientID();
            Date.setText(eventModel.getDate());
            Time.setText(eventModel.getTime());
            EventName.getEditText().setText(eventModel.getName());
            EventDescription.getEditText().setText(eventModel.getDescription());
        }else{
            dateString = receive.getStringExtra("Today");
            PatientsModel PatientModel = receive.getParcelableExtra("PatientModel");
            PatientID = PatientModel.getPatientID();
        }

        Date.setText(dateString);
        try {
            timestampCal.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date.setOnClickListener(view -> popDatePicker(Date));

        Time.setOnClickListener(view -> popTimePicker(view, Time));



        AddEvent.setOnClickListener(view -> {
            if (TextUtils.isEmpty(EventName.getEditText().getText().toString()) || TextUtils.isEmpty(Date.getText().toString()) ||
                    TextUtils.isEmpty(EventDescription.getEditText().getText().toString()) || TextUtils.isEmpty(Time.getText().toString())) {
                Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if(EventDescription.getEditText().getText().toString().length()>500){
                EventDescription.getEditText().setError("Ha excedido el limite de 500 caracteres");
                EventDescription.getEditText().requestFocus();
                return;
            }
            java.util.Date timestamp = timestampCal.getTime();
            EventModel newevent = new EventModel("event", timestamp, Date.getText().toString(), Time.getText().toString(), EventName.getEditText().getText().toString(), EventDescription.getEditText().getText().toString(), PatientID);
            if(updater){
                newevent.setTimeStamp(eventModel.getTimeStamp());
                db.collection("events").whereEqualTo("patientID", eventModel.getPatientID())
                        .whereEqualTo("time", eventModel.getTime()).whereEqualTo("name",eventModel.getName()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                    for (DocumentSnapshot snapshot: snapshotList){
                                        snapshot.getReference().set(newevent).addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Evento registrado", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                                    }
                        });
            }else{
                db.collection("events").add(newevent).addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Evento registrado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void popDatePicker (EditText DateToSet){
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog.OnDateSetListener setListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 + 1;
            String date = dayOfMonth + "/" + month1 + "/" + year1;
            DateToSet.setText(date);
            timestampCal.set(Calendar.YEAR, year1);
            timestampCal.set(Calendar.MONTH,month1);
            timestampCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth, setListener, year, month, day);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }

    private void popTimePicker(View view, EditText TimeToSet){
        final int[] hour = new int[1];
        final int[] minute = new int[1];
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, SelectedHour, SelectedMinute) -> {
            hour[0] = SelectedHour;
            minute[0] = SelectedMinute;
            TimeToSet.setText(Utilities.Round5Minutes(SelectedHour,SelectedMinute));
            timestampCal.set(Calendar.HOUR, SelectedHour);
            timestampCal.set(Calendar.MINUTE, SelectedMinute);
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour[0], minute[0], true);
        timePickerDialog.setTitle("Selecciona una hora");
        timePickerDialog.show();
    }
}