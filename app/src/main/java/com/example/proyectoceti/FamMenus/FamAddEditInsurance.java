package com.example.proyectoceti.FamMenus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.databinding.ActivityFamAddEditInsuranceBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FamAddEditInsurance extends AppCompatActivity {

    private ActivityFamAddEditInsuranceBinding binding;
    private FirebaseFirestore db;
    private PatientsModel model;
    private Calendar timestampCal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamAddEditInsuranceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent receiver = getIntent();
        model = receiver.getParcelableExtra("PatientModel");
        db = FirebaseFirestore.getInstance();

        if(model.getInsurance() != null){
            binding.ETAddInsurCompany.getEditText().setText(model.getInsurance().get("Company").toString());
            binding.ETAddInsurEmployer.getEditText().setText(model.getInsurance().get("Employer").toString());
            binding.ETAddInsurPolicy.getEditText().setText(model.getInsurance().get("Policy").toString());
            binding.ETAddEventDate.setText(model.getInsurance().get("Date").toString());
        }

        binding.ETAddEventDate.setOnClickListener(view -> popDatePicker(binding.ETAddEventDate));

        binding.BTAddInsur.setOnClickListener(view -> {
            if(Validate()){
                Map<String, Object> NewInsur = new HashMap<>();
                NewInsur.put("Company",binding.ETAddInsurCompany.getEditText().getText().toString());
                NewInsur.put("Employer", binding.ETAddInsurEmployer.getEditText().getText().toString());
                NewInsur.put("Policy", binding.ETAddInsurPolicy.getEditText().getText().toString());
                NewInsur.put("Date", binding.ETAddEventDate.getText().toString());
                db.collection("patients").document(model.getPatientID()).update("Insurance", NewInsur);
                finish();
            }
        });

    }

    private boolean Validate() {
        if(TextUtils.isEmpty(binding.ETAddInsurPolicy.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETAddEventDate.getText().toString()) ||
                TextUtils.isEmpty(binding.ETAddInsurEmployer.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETAddInsurCompany.getEditText().getText().toString())){
            Toast.makeText(this, "favor de no dejar campos vacios", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!Validations.AfterToday(binding.ETAddEventDate.getText().toString())){
            Toast.makeText(this, "Fecha de expiración ya pasó", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
}