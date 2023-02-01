package com.example.proyectoceti.FamMenus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.ClassesAndModels.Location;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.databinding.ActivityFamAddEditHospitalBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamAddEditHospital extends AppCompatActivity {

    private ActivityFamAddEditHospitalBinding binding;
    private FirebaseFirestore db;
    private PatientsModel model;
    private Location HospLocation;
    private Place Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamAddEditHospitalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent receiver = getIntent();
        model = receiver.getParcelableExtra("PatientModel");
        db = FirebaseFirestore.getInstance();
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        if(model.getHospital() != null){
            double lat = (double) ((Map<String, Object>)model.getHospital().get("Location")).get("latitude");
            double lng = (double) ((Map<String, Object>)model.getHospital().get("Location")).get("longitude");
            HospLocation = new Location(lat, lng, ((Map<String, Object>)model.getHospital().get("Location")).get("address").toString());

            binding.ETAddHospName.getEditText().setText(model.getHospital().get("Name").toString());
            binding.ETAddHospNumber.getEditText().setText(model.getHospital().get("Number").toString());
            binding.ETAddHospAddy.setText(HospLocation.getAddress());
        }

        binding.ETAddHospAddy.setOnClickListener(view -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent Autoco = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(FamAddEditHospital.this);
            startActivityForResult(Autoco, 100);
        });

        binding.BTAddHosp.setOnClickListener(view -> {
            if(Validate()){
                Map<String, Object> NewHosp = new HashMap<>();
                NewHosp.put("Name", binding.ETAddHospName.getEditText().getText().toString());
                NewHosp.put("Number", binding.ETAddHospNumber.getEditText().getText().toString());
                NewHosp.put("Location", HospLocation);
                db.collection("patients").document(model.getPatientID()).update("Hospital", NewHosp);
                finish();
            }
        });
    }

    private boolean Validate() {
        if(TextUtils.isEmpty(binding.ETAddHospName.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETAddHospAddy.getText().toString()) ||
        TextUtils.isEmpty(binding.ETAddHospNumber.getEditText().getText().toString())){
            Toast.makeText(this, "favor de no dejar campos vacios", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!Validations.IsInMexico(this,HospLocation.getLatitude(), HospLocation.getLongitude())){
            Toast.makeText(this, "Domicilio debe estar dentro de mexico", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode== 100){
                Address = Autocomplete.getPlaceFromIntent(data);
                binding.ETAddHospAddy.setText(Address.getAddress());
                HospLocation= new Location(Address.getLatLng().latitude, Address.getLatLng().longitude, Address.getAddress());
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}