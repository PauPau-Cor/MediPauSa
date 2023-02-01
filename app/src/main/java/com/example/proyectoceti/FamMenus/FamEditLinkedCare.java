package com.example.proyectoceti.FamMenus;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.DayAndTime;
import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityFamEditLinkedCareBinding;
import com.example.proyectoceti.databinding.LinkedCareEditShiftRowBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FamEditLinkedCare extends AppCompatActivity {
    ActivityFamEditLinkedCareBinding binding;
    LinkedUsersModel linkedUserModel;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamEditLinkedCareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        linkedUserModel = getIntent().getParcelableExtra("LinkedUserModel");

        binding.FamViewSingleCarename.setText(linkedUserModel.getName());
        binding.FamEditSingleCareAccSalary.getEditText().setText(String.valueOf(linkedUserModel.getAccumulatedSalary()));
        Picasso.get().load(linkedUserModel.getProfilePicture().url).into(binding.FamViewSingleCarePhoto);
        binding.FamEditSingleCareSalary.getEditText().setText(String.valueOf(linkedUserModel.getSalary()));
        binding.FamEditSingleCareShiftsWorked.getEditText().setText(String.valueOf(linkedUserModel.getShiftsWorked()));

        if(linkedUserModel.getShifts() != null) for (DayAndTime shift: linkedUserModel.getShifts()) {
            addView(shift);
        }else{
            addView(new DayAndTime("Lunes", "00:00", "00:00"));
        }

        binding.FamEditSingleCareAddShiftRow.setOnClickListener(view -> addView(new DayAndTime("Lunes", "00:00", "00:00")));

        binding.FamEditSingleCareBT.setOnClickListener(view -> {
            UpdateLinkedCare();
        });
    }

    private void removeView(View view){
        if(binding.LayoutEditLinkedCareShifts.getChildCount()!=1) binding.LayoutEditLinkedCareShifts.removeView(view);
        else Toast.makeText(FamEditLinkedCare.this, "Debe tener por lo menos un turno", Toast.LENGTH_SHORT).show();
    }

    private void addView(DayAndTime shift) {
        LinkedCareEditShiftRowBinding RowBinding = LinkedCareEditShiftRowBinding.inflate(getLayoutInflater());
        final View frequencyList = RowBinding.getRoot();
        switch (shift.getDay()){
            case "Lunes":{
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowMonday.getId());
                break;
            }
            case "Martes": {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowTuesday.getId());
                break;
            }
            case "Miercoles" : {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowWednesday.getId());
                break;
            }
            case "Jueves" : {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowThursday.getId());
                break;
            }
            case "Viernes" : {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowFriday.getId());
                break;
            }
            case "Sabado" : {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowSaturday.getId());
                break;
            }
            case "Domingo" : {
                RowBinding.EditShiftRowDays.check(RowBinding.EditShiftRowSunday.getId());
                break;
            }
        }
        RowBinding.EditShiftRowStart.setText(shift.getStartTime());
        RowBinding.EditShiftRowEnd.setText(shift.getEndTime());
        RowBinding.EditShiftRowStart.setOnClickListener(view -> popTimePicker(RowBinding.EditShiftRowStart));
        RowBinding.EditShiftRowEnd.setOnClickListener(view -> popTimePicker(RowBinding.EditShiftRowEnd));
        RowBinding.RemoveShift.setOnClickListener(view -> removeView(frequencyList));
        binding.LayoutEditLinkedCareShifts.addView(frequencyList);
    }

    private void popTimePicker(EditText timeToSet){
        final int[] hour = new int[1];
        final int[] minute = new int[1];
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, SelectedHour, SelectedMinute) -> {
            hour[0] = SelectedHour;
            minute[0] = SelectedMinute;
            timeToSet.setText(Utilities.Round5Minutes(SelectedHour,SelectedMinute));
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour[0], minute[0], true);
        timePickerDialog.setTitle("Selecciona una frecuencia");
        timePickerDialog.show();
    }

    private void UpdateLinkedCare() {
        if(TextUtils.isEmpty(binding.FamEditSingleCareAccSalary.getEditText().getText()) || TextUtils.isEmpty(binding.FamEditSingleCareSalary.getEditText().getText())
                || TextUtils.isEmpty(binding.FamEditSingleCareShiftsWorked.getEditText().getText())){
            Toast.makeText(this, "Favor de no dejar campos vacios", Toast.LENGTH_SHORT).show();
            return;
        }
        View ShiftEntryView;
        List<DayAndTime> ShiftsList = new ArrayList<>();
        for(int i = 0; i<binding.LayoutEditLinkedCareShifts.getChildCount(); i++) {
            ShiftEntryView = binding.LayoutEditLinkedCareShifts.getChildAt(i);
            MaterialButtonToggleGroup Days = ShiftEntryView.findViewById(R.id.EditShiftRowDays);
            String Day="Monday";
            EditText Start = ShiftEntryView.findViewById(R.id.EditShiftRowStart);
            EditText End = ShiftEntryView.findViewById(R.id.EditShiftRowEnd);
            if(!StartBeforeEnd(Start.getText().toString(), End.getText().toString())){
                Toast.makeText(this, "Error en turnos, hora de termino antes de hora de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (Days.getCheckedButtonId()){
                case R.id.EditShiftRowMonday :{
                    Day="Lunes";
                    break;
                }
                case R.id.EditShiftRowTuesday :{
                    Day="Martes";
                    break;
                }
                case R.id.EditShiftRowWednesday :{
                    Day="Miercoles";
                    break;
                }
                case R.id.EditShiftRowThursday :{
                    Day="Jueves";
                    break;
                }
                case R.id.EditShiftRowFriday :{
                    Day="Viernes";
                    break;
                }
                case R.id.EditShiftRowSaturday :{
                    Day="Sabado";
                    break;
                }
                case R.id.EditShiftRowSunday :{
                    Day="Domingo";
                    break;
                }
            }
            ShiftsList.add(new DayAndTime(Day, Start.getText().toString(), End.getText().toString()));
        }
        double AccSalary = Double.parseDouble(binding.FamEditSingleCareAccSalary.getEditText().getText().toString());
        double Salary = Double.parseDouble(binding.FamEditSingleCareSalary.getEditText().getText().toString());
        int WorkedMinutes = (int) Math.round(AccSalary/Salary*60);
        int shiftsWorked = Integer.parseInt(binding.FamEditSingleCareShiftsWorked.getEditText().getText().toString());
        db.collection("linkedusers").whereEqualTo("careUserID", linkedUserModel.getCareUserID()).whereEqualTo("famUserID", linkedUserModel.getFamUserID())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        snapshot.getReference().update("accumulatedSalary", AccSalary, "salary", Salary, "shiftsWorked", shiftsWorked, "shifts", ShiftsList, "hoursWorked", WorkedMinutes);
                    }
                    Intent intent = new Intent(FamEditLinkedCare.this, FamMenu.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });
    }

    private boolean StartBeforeEnd(String Start, String End) {
        Date StartDate = new Date();
        String[] StartParts = Start.split(":");
        StartDate.setHours(Integer.parseInt(StartParts[0]));
        StartDate.setMinutes(Integer.parseInt(StartParts[1]));
        Date EndDate = new Date();
        String[] EndParts = End.split(":");
        EndDate.setHours(Integer.parseInt(EndParts[0]));
        EndDate.setMinutes(Integer.parseInt(EndParts[1]));
        return StartDate.before(EndDate);
    }
}