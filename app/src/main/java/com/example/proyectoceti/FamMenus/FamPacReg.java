package com.example.proyectoceti.FamMenus;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.ClassesAndModels.ContactModel;
import com.example.proyectoceti.ClassesAndModels.EncryptedLocation;
import com.example.proyectoceti.ClassesAndModels.Location;
import com.example.proyectoceti.ClassesAndModels.PutFile;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityFamPacRegBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FamPacReg extends AppCompatActivity {


    ActivityFamPacRegBinding binding;

    DatePickerDialog.OnDateSetListener setListener;
    Place Address;
    EncryptedLocation RegPacLatLng;
    Location RawLocation;
    Uri RegPacPhotoUri;
    String RegPacPhotoName;

    private String FamUserId, Sex, TypeCare, Blood;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private List<String> VinculatedFamIDs;
    private String Number, FamName;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamPacRegBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        FamUserId = mAuth.getCurrentUser().getUid();
        Sex = "Masculino";
        Blood = "A+";
        TypeCare = "Compañia";

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        binding.ETRegPacAddre.setFocusable(false);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        binding.IVRegPacPhoto.setImageDrawable(getDrawable(R.drawable.profilephotopat));

        DocumentReference documentReference = db.collection("users").document(FamUserId);
        documentReference.get().addOnCompleteListener(task -> {
            VinculatedFamIDs = (List<String>)task.getResult().get("VinculatedCare");
            Number = task.getResult().getString("UserInfo.CellPhoneNumber");
            FamName = task.getResult().getString("UserInfo.Name") + " " + task.getResult().getString("UserInfo.LastName");
        });

        //Si el usuario selecciona el campo de fecha, abre un cuadro de seleccion de fechas
        binding.ETRegPacDate.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FamPacReg.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });
        setListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date= dayOfMonth+"/"+ month1 +"/"+ year1;
            binding.ETRegPacDate.setText(date);
        };

        binding.IVRegPacPhoto.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(FamPacReg.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(FamPacReg.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                Intent FileBrowse = new Intent();
                FileBrowse.setType("image/*");
                FileBrowse.setAction(FileBrowse.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(FileBrowse,"IMAGE FILE SELECT"),1000);
            }
        });

        binding.BloodTypeButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked){
                MaterialButton button = findViewById(checkedId);
                Blood = button.getText().toString();
            }
        });

        binding.CareTypeButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked){
                MaterialButton button = findViewById(checkedId);
                TypeCare = button.getText().toString();
            }
        });

        binding.GenderButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked){
                switch(checkedId){
                    case R.id.MaleBT:
                        Sex = "Masculino";
                        break;
                    case R.id.FemaleBT:
                        Sex = "Femenino";
                        break;
                    case R.id.NonBinaryBT:
                        Sex = "No binario";
                        break;
                }
            }

        });

        binding.ETRegPacAddre.setOnClickListener(view -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent Autoco = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(FamPacReg.this);
            startActivityForResult(Autoco, 100);
        });

        binding.BTCareReg.setOnClickListener(view -> {
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(TextUtils.isEmpty(binding.ETRegPacName.getEditText().getText().toString())||TextUtils.isEmpty(binding.ETRegPacLaName.getEditText().getText().toString())||
                    TextUtils.isEmpty(binding.ETRegPacDate.getText().toString())||TextUtils.isEmpty(Sex)||
                    TextUtils.isEmpty(Blood)||TextUtils.isEmpty(TypeCare)|| TextUtils.isEmpty(binding.ETRegPacAddre.getText().toString())){
                Toast.makeText(FamPacReg.this,"Por favor rellene los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("[^A-Za-zñ ]");
            Matcher m = p.matcher(binding.ETRegPacName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETRegPacName.setError("Nombre no puede contener numeros ni caracteres especiales");
                binding.ETRegPacName.requestFocus();
                return;
            }
            binding.ETRegPacName.setError(null);
            m= p.matcher(binding.ETRegPacLaName.getEditText().getText().toString());
            if(m.find()) {
                binding.ETRegPacLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
                binding.ETRegPacLaName.requestFocus();
                return;
            }
            binding.ETRegPacLaName.setError(null);
            if(binding.ETRegPacName.getEditText().getText().toString().length()<2){
                binding.ETRegPacName.setError("Nombre debe contener por lo menos 2 caracteres");
                binding.ETRegPacName.requestFocus();
                return;
            }
            binding.ETRegPacName.setError(null);
            if(binding.ETRegPacLaName.getEditText().getText().toString().length()<2){
                binding.ETRegPacLaName.setError("Apellidos deben contener por lo menos 2 caracteres");
                binding.ETRegPacLaName.requestFocus();
                return;
            }
            binding.ETRegPacLaName.setError(null);
            if(RegPacPhotoUri == null){
                Toast.makeText(FamPacReg.this,"Favor de seleccionar una foto de perfil", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!Validations.IsInMexico(FamPacReg.this, RawLocation.getLatitude(), RawLocation.getLongitude())){
                Toast.makeText(FamPacReg.this, "Debe tener un domicilio dentro de mexico para usar la aplicación", Toast.LENGTH_SHORT).show();
                return;
            }else{
                binding.RegPacPB.setVisibility(View.VISIBLE);
                CreatePac();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){

            if(requestCode==1000){
                RegPacPhotoName = data.getDataString().substring(data.getDataString().lastIndexOf("/")+1);
                binding.IVRegPacPhoto.setImageURI(data.getData());
                RegPacPhotoUri = data.getData();
            }
            if(requestCode== 100){
                Address = Autocomplete.getPlaceFromIntent(data);
                binding.ETRegPacAddre.setText(Address.getAddress());
                RawLocation= new Location(Address.getLatLng().latitude, Address.getLatLng().longitude, Address.getAddress());
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CreatePac(){
        String Name = binding.ETRegPacName.getEditText().getText().toString();
        String LaName = binding.ETRegPacLaName.getEditText().getText().toString();
        String Birthday = binding.ETRegPacDate.getText().toString();
        String Aller = binding.ETRegPacAller.getEditText().getText().toString();
        String Sick = binding.ETRegPacSick.getEditText().getText().toString();

        try {
            RegPacLatLng = new EncryptedLocation(Encrypter.Encrypt(String.valueOf(RawLocation.getLatitude())), Encrypter.Encrypt(String.valueOf(RawLocation.getLongitude())), Encrypter.Encrypt(RawLocation.getAddress()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        CollectionReference collectionReference = db.collection("patients");
        Map<String, Object> patient=new HashMap<>();
        Map<String, Object> PatientInfo=new HashMap<>();
        patient.put("FamUserID", FamUserId);
        patient.put("PatientInfo", PatientInfo);
        PatientInfo.put("Name", Name);
        PatientInfo.put("LastName", LaName);
        PatientInfo.put("Birthday", Birthday);
        PatientInfo.put("Sex", Sex);
        PatientInfo.put("Bloodtype", Blood);
        if(VinculatedFamIDs != null) patient.put("CareUserIDs", VinculatedFamIDs);
        if(!TextUtils.isEmpty(binding.ETRegPacAller.getEditText().getText().toString())) PatientInfo.put("Allergies", Aller);
        else PatientInfo.put("Allergies", "Ninguna");
        PatientInfo.put("CareType", TypeCare);
        if(!TextUtils.isEmpty(binding.ETRegPacSick.getEditText().getText().toString()))PatientInfo.put("Illness", Sick);
        else PatientInfo.put("Illness", "Ninguna");
        PatientInfo.put("Location", RegPacLatLng);
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        StorageReference pfpref=storageReference.child("PatientPhotos/"+Name + "_" + LaName + "_" + FamUserId +"."+mime.getExtensionFromMimeType(contentResolver.getType(RegPacPhotoUri)));
        pfpref.putFile(RegPacPhotoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while(!uriTask.isComplete());
            Uri uri = uriTask.getResult();
            PutFile putPfp = new PutFile(RegPacPhotoName, uri.toString());
            PatientInfo.put("Photo", putPfp);
            collectionReference.add(patient).addOnSuccessListener(documentReference -> {
                documentReference.update("PatientID", documentReference.getId());
                ContactModel famContact = new ContactModel(FamName, Number, documentReference.getId());
                db.collection("contacts").add(famContact);
            });
            binding.RegPacPB.setVisibility(View.GONE);
            Toast.makeText(FamPacReg.this, "Paciente registrado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(FamPacReg.this, FamMenu.class));
            finish();
        });


    }

}
