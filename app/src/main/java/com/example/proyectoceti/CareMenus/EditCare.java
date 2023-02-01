package com.example.proyectoceti.CareMenus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.ClassesAndModels.EncryptedLocation;
import com.example.proyectoceti.ClassesAndModels.Location;
import com.example.proyectoceti.ClassesAndModels.PutFile;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityEditCareBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditCare extends AppCompatActivity {
    //Declara elementos
    ActivityEditCareBinding binding;
    Place Address;
    //Declara variables
    EncryptedLocation CareLocation;
    Location EdiCareLatLng;
    private String countryName = "";
    Uri EdiCarePfpUri, EditCareFileUri;
    String EdiCarePfpName, Sex;

    PutFile putfile, putPfp;


    //Variables de Firebase
    private String CareUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityEditCareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        CareUserID = mAuth.getCurrentUser().getUid();

        //Inicializa la API de google places
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        binding.ETRegCareAddre.setFocusable(false);

        DocumentReference documentReference = db.collection("users").document(CareUserID);
        documentReference.get().addOnCompleteListener(task -> {
            String url = task.getResult().getString("UserInfo.Profile Picture.url");
            Picasso.get().load(url).into(binding.RegCarePfp);
            CareLocation = new EncryptedLocation(task.getResult().getString("UserInfo.Location.latitude"), task.getResult().getString("UserInfo.Location.longitude"), task.getResult().getString("UserInfo.Location.address"));
            try {
                EdiCareLatLng = new Location(Double.valueOf(Encrypter.Decrypt(CareLocation.getLatitude())), Double.valueOf(Encrypter.Decrypt(CareLocation.getLongitude())), Encrypter.Decrypt(CareLocation.getAddress()));
                binding.ETRegCareAddre.setText(EdiCareLatLng.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.ETRegCareName.getEditText().setText(task.getResult().getString("UserInfo.Name"));
            binding.ETRegCareLaName.getEditText().setText(task.getResult().getString("UserInfo.LastName"));
            Sex = task.getResult().getString("UserInfo.Sex");
            binding.ETRegCareSalar.getEditText().setText(task.getResult().get("UserInfo.Salary").toString());
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
        });

        binding.RegCarePfp.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(EditCare.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(EditCare.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                Intent FileBrowse = new Intent();
                FileBrowse.setType("image/*");
                FileBrowse.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(FileBrowse,"IMAGE FILE SELECT"),1000);
            }
        });

        binding.ETRegCareAddre.setOnClickListener(view -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent Autoco = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EditCare.this);
            startActivityForResult(Autoco, 100);
        });

        binding.BTRegCareFile.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(EditCare.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(EditCare.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                SelectCareFile();
            }
        });

        binding.GenderButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            switch(checkedId){
                case R.id.MaleBT:
                    if(isChecked){
                        Sex = "Masculino";
                    }
                    break;
                case R.id.FemaleBT:
                    if(isChecked){
                        Sex = "Femenino";
                    }
                    break;
                case R.id.NonBinaryBT:
                    if(isChecked){
                        Sex = "No binario";
                    }
                    break;
            }
        });

        binding.BTCareEdit.setOnClickListener(view -> {
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(TextUtils.isEmpty(binding.ETRegCareName.getEditText().getText().toString())|
                    TextUtils.isEmpty(binding.ETRegCareLaName.getEditText().getText().toString())| TextUtils.isEmpty(Sex)|
                    TextUtils.isEmpty(binding.ETRegCareSalar.getEditText().getText().toString())| TextUtils.isEmpty(binding.ETRegCareAddre.getText().toString())){
                Toast.makeText(EditCare.this,"Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("[^A-Za-zñ ]");
            Matcher m = p.matcher(binding.ETRegCareName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETRegCareName.setError("Nombre no puede contener numeros ni caracteres especiales");
                binding.ETRegCareName.requestFocus();
                return;
            }
            binding.ETRegCareName.setError(null);
            m= p.matcher(binding.ETRegCareLaName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETRegCareLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
                binding.ETRegCareLaName.requestFocus();
                return;
            }
            binding.ETRegCareLaName.setError(null);
            if(binding.ETRegCareName.getEditText().getText().toString().length()<2){
                binding.ETRegCareName.setError("Nombre debe contener por lo menos 2 caracteres");
                binding.ETRegCareName.requestFocus();
                return;
            }
            binding.ETRegCareName.setError(null);
            if(binding.ETRegCareLaName.getEditText().getText().toString().length()<2){
                binding.ETRegCareLaName.setError("Apellidos deben contener por lo menos 2 caracteres");
                binding.ETRegCareLaName.requestFocus();
                return;
            }
            binding.ETRegCareLaName.setError(null);
            if(!Validations.IsInMexico(EditCare.this, EdiCareLatLng.getLatitude(), EdiCareLatLng.getLongitude())){
                Toast.makeText(EditCare.this, "Debe tener un domicilio dentro de mexico para usar la aplicación", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.RegCarePB.setVisibility(View.VISIBLE);
            UploadFiles();
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){

            if(requestCode==1000){
                EdiCarePfpName = data.getDataString().substring(data.getDataString().lastIndexOf("/")+1);
                binding.RegCarePfp.setImageURI(data.getData());
                EdiCarePfpUri = data.getData();
            }
            if(requestCode== 100){
                Address = Autocomplete.getPlaceFromIntent(data);
                binding.ETRegCareAddre.setText(Address.getAddress());
                EdiCareLatLng = new Location(Address.getLatLng().latitude, Address.getLatLng().longitude, Address.getAddress());
            }
            if(requestCode==10){
                binding.TVRegCareFile.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/")+1));
                EditCareFileUri = data.getData();
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void SelectCareFile(){
        Intent FileBrowse = new Intent();
        FileBrowse.setType("application/pdf");
        FileBrowse.setAction(FileBrowse.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(FileBrowse,"PDF FILE SELECT"),10);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void UploadFiles(){
        if(EdiCarePfpUri != null && EditCareFileUri != null){
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            StorageReference pfpref = storageReference.child("ProfilePictures/" + binding.ETRegCareName.getEditText().getText().toString() + "_" + binding.ETRegCareLaName.getEditText().getText().toString() + "_" + CareUserID + "." + mime.getExtensionFromMimeType(contentResolver.getType(EdiCarePfpUri)));
            pfpref.putFile(EdiCarePfpUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri PfpUri = uriTask.getResult();
                putPfp = new PutFile(EdiCarePfpName, PfpUri.toString());
                StorageReference reference=storageReference.child("ExpertiseFiles/"+binding.ETRegCareName.getEditText().getText().toString()+"_" + binding.ETRegCareLaName.getEditText().getText().toString() + "_" +CareUserID+".pdf");
                reference.putFile(EditCareFileUri).addOnSuccessListener(taskSnapshot1 -> {
                    Task<Uri> uriTask1 = taskSnapshot1.getStorage().getDownloadUrl();
                    while (!uriTask1.isComplete()) ;
                    Uri FileUri = uriTask1.getResult();
                    putfile = new PutFile(binding.TVRegCareFile.getText().toString(), FileUri.toString());
                    EditCareUsr(true, true);
                });
            });
            return;
        }
        if(EdiCarePfpUri != null) {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            StorageReference pfpref = storageReference.child("ProfilePictures/" + binding.ETRegCareName.getEditText().getText().toString() + "_" + binding.ETRegCareLaName.getEditText().getText().toString() + "_" + CareUserID + "." + mime.getExtensionFromMimeType(contentResolver.getType(EdiCarePfpUri)));
            pfpref.putFile(EdiCarePfpUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri PfpUri = uriTask.getResult();
                putPfp = new PutFile(EdiCarePfpName, PfpUri.toString());
                EditCareUsr(true, false);
            });
            return;
        }
        if(EditCareFileUri != null){
            StorageReference reference=storageReference.child("ExpertiseFiles/"+binding.ETRegCareName.getEditText().getText().toString()+"_" + binding.ETRegCareLaName.getEditText().getText().toString() + "_" +CareUserID+".pdf");
            reference.putFile(EditCareFileUri).addOnSuccessListener(taskSnapshot1 -> {
                Task<Uri> uriTask1 = taskSnapshot1.getStorage().getDownloadUrl();
                while (!uriTask1.isComplete()) ;
                Uri FileUri = uriTask1.getResult();
                putfile = new PutFile(binding.TVRegCareFile.getText().toString(), FileUri.toString());
                EditCareUsr(false, true);
            });
            return;
        }
        EditCareUsr(false, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void EditCareUsr(boolean pfp, boolean file){
        String Name = binding.ETRegCareName.getEditText().getText().toString();
        String LaName = binding.ETRegCareLaName.getEditText().getText().toString();
        try {
            CareLocation = new EncryptedLocation(Encrypter.Encrypt(String.valueOf(EdiCareLatLng.getLatitude())), Encrypter.Encrypt(String.valueOf(EdiCareLatLng.getLongitude())), Encrypter.Encrypt(EdiCareLatLng.getAddress()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        float Salar = Float.parseFloat(binding.ETRegCareSalar.getEditText().getText().toString());
        CareUserID = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(CareUserID);
        Map<String, Object> UserInfo=new HashMap<>();
        if(pfp) UserInfo.put("UserInfo.Profile Picture", putPfp);
        if(file){
            Map<String, Object> ExpertiseFile=new HashMap<>();
            ExpertiseFile.put("name", putfile.getName());
            ExpertiseFile.put("url", putfile.getUrl());
            ExpertiseFile.put("Validated", false);
            UserInfo.put("UserInfo.ExpertiseFile",ExpertiseFile);
        }
        UserInfo.put("Approved", "pending");
        UserInfo.put("UserInfo.Name", Name);
        UserInfo.put("UserInfo.LastName", LaName);
        UserInfo.put("UserInfo.Sex", Sex);
        UserInfo.put("UserInfo.Salary", Salar);
        UserInfo.put("UserInfo.Location", CareLocation);
        documentReference.update(UserInfo).addOnSuccessListener(unused -> {
            binding.RegCarePB.setVisibility(View.GONE);
            Toast.makeText(EditCare.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
            finish();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            SelectCareFile();
        }else{
            Toast.makeText(EditCare.this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
        }
    }
}