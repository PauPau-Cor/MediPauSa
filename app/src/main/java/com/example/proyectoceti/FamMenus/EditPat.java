package com.example.proyectoceti.FamMenus;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.ClassesAndModels.EncryptedLocation;
import com.example.proyectoceti.ClassesAndModels.Location;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.ClassesAndModels.PutFile;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.databinding.ActivityEditPatBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditPat extends AppCompatActivity {
    ActivityEditPatBinding binding;

    Place Address;
    String PictureName, Sex, CareType;
    Uri PictureUri;
    EncryptedLocation PacCipherLoc;
    Location PacLoc;
    PatientsModel Patientmodel;
    PutFile putPfp;

    private FirebaseFirestore db;
    private StorageReference storageReference;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent ReceiveIntent = getIntent();
        Patientmodel = ReceiveIntent.getParcelableExtra("PatientModel");

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);


        binding.ETPatAddre.setFocusable(false);

        String url = ((Map)Patientmodel.getPatientInfo().get("Photo")).get("url").toString();
        Picasso.get().load(url).into(binding.EdiPatPfp);
        binding.ETPatName.getEditText().setText(Patientmodel.getPatientInfo().get("Name").toString());
        binding.ETPatLaName.getEditText().setText(Patientmodel.getPatientInfo().get("LastName").toString());
        binding.ETPatAller.getEditText().setText(Patientmodel.getPatientInfo().get("Allergies").toString());
        binding.ETPatSick.getEditText().setText(Patientmodel.getPatientInfo().get("Illness").toString());

        CareType = Patientmodel.getPatientInfo().get("CareType").toString();
        switch (CareType){
            case "Compañia":{
                binding.CareTypeButtons.check(binding.CompanyBT.getId());
                break;
            }
            case "Normal":{
                binding.CareTypeButtons.check(binding.RegularBT.getId());
                break;
            }
            case "Intensivo":{
                binding.CareTypeButtons.check(binding.IntensiveBT.getId());
                break;
            }
        }

        Sex = Patientmodel.getPatientInfo().get("Sex").toString();
        switch (Sex){
            case "Masculino":{
                binding.GenderButtons.check(binding.MaleBT.getId());
                break;
            }
            case "Femenino":{
                binding.GenderButtons.check(binding.FemaleBT.getId());
                break;
            }
            case "No binario":{
                binding.GenderButtons.check(binding.NonBinaryBT.getId());
                break;
            }
        }

        PacCipherLoc = new EncryptedLocation(((Map)Patientmodel.getPatientInfo().get("Location")).get("latitude").toString(), ((Map)Patientmodel.getPatientInfo().get("Location")).get("longitude").toString(),((Map)Patientmodel.getPatientInfo().get("Location")).get("address").toString());
        try {
            PacLoc = new Location(Double.valueOf(Encrypter.Decrypt(PacCipherLoc.getLatitude())), Double.valueOf(Encrypter.Decrypt(PacCipherLoc.getLongitude())), Encrypter.Decrypt(PacCipherLoc.getAddress()) );
            binding.ETPatAddre.setText(PacLoc.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.EdiPatPfp.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(EditPat.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(EditPat.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                Intent FileBrowse = new Intent();
                FileBrowse.setType("image/*");
                FileBrowse.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(FileBrowse,"IMAGE FILE SELECT"),1000);
            }
        });

        binding.ETPatAddre.setOnClickListener(view -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent Autoco = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EditPat.this);
            startActivityForResult(Autoco, 100);
        });

        binding.BTFamPatientEditKeep.setOnClickListener(view -> {
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(TextUtils.isEmpty(binding.ETPatName.getEditText().getText().toString())  || TextUtils.isEmpty(binding.ETPatLaName.getEditText().getText().toString()) ||
                    TextUtils.isEmpty(CareType) || TextUtils.isEmpty(Sex) ||TextUtils.isEmpty(binding.ETPatAddre.getText().toString())){
                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("[^A-Za-zñ ]");
            Matcher m = p.matcher(binding.ETPatName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETPatName.setError("Nombre no puede contener numeros ni caracteres especiales");
                binding.ETPatName.requestFocus();
                return;
            }
            binding.ETPatName.setError(null);
            m= p.matcher(binding.ETPatLaName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETPatLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
                binding.ETPatLaName.requestFocus();
                return;
            }
            binding.ETPatLaName.setError(null);
            if(binding.ETPatName.getEditText().getText().toString().length()<2){
                binding.ETPatName.setError("Nombre debe contener por lo menos 2 caracteres");
                binding.ETPatName.requestFocus();
                return;
            }
            binding.ETPatName.setError(null);
            if(binding.ETPatLaName.getEditText().getText().toString().length()<2){
                binding.ETPatLaName.setError("Apellidos deben contener por lo menos 2 caracteres");
                binding.ETPatLaName.requestFocus();
                return;
            }
            binding.ETPatLaName.setError(null);
            if(!Validations.IsInMexico(EditPat.this, PacLoc.getLatitude(), PacLoc.getLongitude())){
                Toast.makeText(this, "Domicilio debe estar dentro de mexico", Toast.LENGTH_SHORT).show();
                return;
            }
            UploadPhoto();

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void UploadPhoto() {
        if(PictureUri == null){
            EditPatient(false);
            return;
        }
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        StorageReference pfpref=storageReference.child("ProfilePictures/"+binding.ETPatName.getEditText().getText().toString()+"_" + binding.ETPatLaName.getEditText().getText().toString() + "_" + Patientmodel.getPatientID()+"."+mime.getExtensionFromMimeType(contentResolver.getType(PictureUri)));
        pfpref.putFile(PictureUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete());
            Uri uri = uriTask.getResult();
            putPfp = new PutFile(PictureName, uri.toString());
            EditPatient(true);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void EditPatient(boolean Pic) {
        String Name = binding.ETPatName.getEditText().getText().toString(), LaName = binding.ETPatLaName.getEditText().getText().toString();
        String Allergies = (TextUtils.isEmpty(binding.ETPatAller.getEditText().getText().toString())) ? "Ninguna" : binding.ETPatAller.getEditText().getText().toString();
        String Sickness = (TextUtils.isEmpty(binding.ETPatSick.getEditText().getText().toString())) ? "Ninguna" : binding.ETPatSick.getEditText().getText().toString();

        DocumentReference documentReference = db.collection("patients").document(Patientmodel.getPatientID());

        try {
            PacCipherLoc = new EncryptedLocation(Encrypter.Encrypt(String.valueOf(PacLoc.getLatitude())), Encrypter.Encrypt(String.valueOf(PacLoc.getLongitude())), Encrypter.Encrypt(PacLoc.getAddress()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        OnSuccessListener Edited = o -> {
            Toast.makeText(EditPat.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
            finish();
        };

        if(Pic){
            documentReference.update("PatientInfo.Name", Name, "PatientInfo.LastName", LaName, "PatientInfo.CareType", CareType, "PatientInfo.Sex", Sex, "PatientInfo.Location", PacCipherLoc, "PatientInfo.Illness", Sickness, "PatientInfo.Allergies", Allergies, "PatientInfo.Photo", putPfp).addOnSuccessListener(Edited);
            return;
        }
        documentReference.update("PatientInfo.Name", Name, "PatientInfo.LastName", LaName, "PatientInfo.CareType", CareType, "PatientInfo.Sex", Sex, "PatientInfo.Location", PacCipherLoc, "PatientInfo.Illness", Sickness, "PatientInfo.Allergies", Allergies).addOnSuccessListener(Edited);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){

            if(requestCode==1000){
                PictureName = data.getDataString().substring(data.getDataString().lastIndexOf("/")+1);
                binding.EdiPatPfp.setImageURI(data.getData());
                PictureUri = data.getData();
            }
            if(requestCode== 100){
                Address = Autocomplete.getPlaceFromIntent(data);
                binding.ETPatAddre.setText(Address.getAddress());
                PacLoc = new Location(Address.getLatLng().latitude, Address.getLatLng().longitude, Address.getAddress());
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}