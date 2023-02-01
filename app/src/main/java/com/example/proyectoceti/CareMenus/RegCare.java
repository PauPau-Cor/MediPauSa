package com.example.proyectoceti.CareMenus;

import static java.lang.Boolean.FALSE;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.proyectoceti.GeneralMenus.LoginActivity;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegCare extends AppCompatActivity{
    //Declara elementos
    TextInputLayout RegCareName, RegCareLaName, RegCareEmail, RegCarePass, RegCarePass2, RegCareSalar;
    EditText RegCareDate,  RegCareAddre;
    Button BTRegCare, BTRegCareFile;
    MaterialButtonToggleGroup sex;
    TextView TVRegCareFile;
    DatePickerDialog.OnDateSetListener setListener;
    ImageView RegCarePFP;
    ProgressBar RegCarePb;
    Place Address;
    //Declara variables
    Location RegCareLatLng;
    Uri RegCareFileUri, RegCarePfpUri;
    String RegCarePfpName, RegCareSex;

    //Variables de Firebase
    private String CareUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_care);
        //Asocia las variables de elementos con los elementos de layout
        RegCareDate = findViewById(R.id.ETRegCareDate);
        RegCareName = findViewById(R.id.ETRegCareName);
        RegCareLaName = findViewById(R.id.ETRegCareLaName);
        RegCareEmail = findViewById(R.id.ETRegCareMail);
        RegCarePass = findViewById(R.id.ETRegCarePass);
        RegCarePass2 = findViewById(R.id.ETRegCarePass2);
        RegCareSalar = findViewById(R.id.ETRegCareSalar);
        RegCareAddre = findViewById(R.id.ETRegCareAddre);
        BTRegCare = findViewById(R.id.BTCareReg);
        BTRegCareFile = findViewById(R.id.BTRegCareFile);
        TVRegCareFile = findViewById(R.id.TVRegCareFile);
        RegCarePFP = findViewById(R.id.RegCarePfp);
        sex = findViewById(R.id.GenderButtons);
        RegCarePb = findViewById(R.id.RegCarePB);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        //Inicializa la API de google places
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        RegCareAddre.setFocusable(false);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        RegCarePFP.setImageDrawable(getDrawable(R.drawable.profilephotocare));

        //Si el usuario selecciona el campo de fecha, abre un cuadro de seleccion de fechas
        RegCareDate.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegCare.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        //Cuando una fecha sea selecionada en el cuadro de seleccion de fechas, se guarda la informacion en un string y se muestra en el campo de texto correspondiente
        setListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date= dayOfMonth+"/"+ month1 +"/"+ year1;
            RegCareDate.setText(date);
        };
        RegCarePFP.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(RegCare.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(RegCare.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                Intent FileBrowse = new Intent();
                FileBrowse.setType("image/*");
                FileBrowse.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(FileBrowse,"IMAGE FILE SELECT"),1000);
            }
        });

        BTRegCareFile.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(RegCare.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(RegCare.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                Intent FileBrowse = new Intent();
                FileBrowse.setType("application/pdf");
                FileBrowse.setAction(FileBrowse.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(FileBrowse,"PDF FILE SELECT"),10);
            }
        });

        RegCareAddre.setOnClickListener(view -> {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent Autoco = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(RegCare.this);
            startActivityForResult(Autoco, 100);
        });

        sex.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            switch(checkedId){
                case R.id.MaleBT:
                    if(isChecked){
                        RegCareSex = "Masculino";
                    }
                    break;
                case R.id.FemaleBT:
                    if(isChecked){
                        RegCareSex = "Femenino";
                    }
                    break;
                case R.id.NonBinaryBT:
                    if(isChecked){
                        RegCareSex = "No binario";
                    }
                    break;
            }
        });

        BTRegCare.setOnClickListener(view -> {
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(TextUtils.isEmpty(RegCareEmail.getEditText().getText().toString())|TextUtils.isEmpty(RegCareDate.getText().toString())|
                    TextUtils.isEmpty(RegCareName.getEditText().getText().toString())|TextUtils.isEmpty(RegCareLaName.getEditText().getText().toString())|
                    TextUtils.isEmpty(RegCarePass.getEditText().getText().toString())|TextUtils.isEmpty(RegCarePass2.getEditText().getText().toString())|
                    TextUtils.isEmpty(RegCareSex)|TextUtils.isEmpty(RegCareSalar.getEditText().getText().toString())|
                    TextUtils.isEmpty(RegCareAddre.getText().toString())){
                Toast.makeText(RegCare.this,"Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("[^A-Za-zñ ]");
            Matcher m = p.matcher(RegCareName.getEditText().getText().toString());
            if(m.find()) {
                RegCareName.setError("Nombre no puede contener numeros ni caracteres especiales");
                RegCareName.requestFocus();
                RegCareName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCareName.setError(null);
                    }
                });
                return;
            }
            RegCareName.setError(null);
            m= p.matcher(RegCareLaName.getEditText().getText().toString());
            if(m.find()) {
                RegCareLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
                RegCareLaName.requestFocus();
                RegCareLaName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCareLaName.setError(null);
                    }
                });
                return;
            }
            RegCareLaName.setError(null);
            if(RegCareName.getEditText().getText().toString().length()<2){
                RegCareName.setError("Nombre debe contener por lo menos 2 caracteres");
                RegCareName.requestFocus();
                RegCareName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCareName.setError(null);
                    }
                });
                return;
            }
            RegCareName.setError(null);
            if(RegCareLaName.getEditText().getText().toString().length()<2){
                RegCareLaName.setError("Apellidos deben contener por lo menos 2 caracteres");
                RegCareLaName.requestFocus();
                RegCareLaName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCareLaName.setError(null);
                    }
                });
                return;
            }
            RegCareLaName.setError(null);
            if(!RegCarePass.getEditText().getText().toString().equals(RegCarePass2.getEditText().getText().toString())){
                RegCarePass.setError("Contraseñas no coinciden");
                RegCarePass.requestFocus();
                RegCarePass.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCarePass.setError(null);
                    }
                });
                return;
            }
            RegCarePass.setError(null);
            if(!Validations.isValidPassword(RegCarePass.getEditText().getText().toString()) || RegCarePass.getEditText().getText().toString().length()<8) {
                RegCarePass.setError("Contraseña deberá tener 8 caracteres, por lo menos un numero, una letra mayuscula y una letra minuscula");
                RegCarePass.requestFocus();
                RegCarePass.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegCarePass.setError(null);
                    }
                });
                return;
            }
            RegCarePass.setError(null);
            if(!Validations.AgeValidation(RegCareDate.getText().toString())){
                RegCareDate.setError("+18");
                Toast.makeText(RegCare.this,"Debes ser mayor de edad para registrarte",Toast.LENGTH_SHORT).show();
                return;
            }
            RegCareDate.setError(null);
            if(RegCarePfpUri == null){
                Toast.makeText(RegCare.this,"Favor de seleccionar una foto de perfil", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!Validations.IsInMexico(RegCare.this, RegCareLatLng.getLatitude(), RegCareLatLng.getLongitude())){
                Toast.makeText(RegCare.this, "Debe tener un domicilio dentro de mexico para usar la aplicación", Toast.LENGTH_SHORT).show();
                return;
            }
            RegCarePb.setVisibility(View.VISIBLE);
            try {
                EmailValidation(RegCareEmail.getEditText().getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    protected void EmailValidation(String EmailToVal) throws IOException {
        final Boolean[] EmailValid = {FALSE};
        String url = BuildConfig.ABSTRACT_KEY + EmailToVal;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(RegCare.this, "Error +", Toast.LENGTH_SHORT).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    RegCare.this.runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(resp);
                            String validjson = jsonObject.getString("is_valid_format");
                            JSONObject jsonObject1 = new JSONObject(validjson);
                            EmailValid[0] = jsonObject1.getBoolean("text");
                            if (!EmailValid[0]) {
                                RegCareEmail.setError("Correo no valido");
                                RegCareEmail.requestFocus();
                                RegCareEmail.getEditText().addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }
                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }
                                    @Override
                                    public void afterTextChanged(Editable editable) {
                                        RegCareEmail.setError(null);
                                    }
                                });
                                RegCarePb.setVisibility(View.GONE);
                            }else{
                                RegCareEmail.setError(null);
                                CreateCareUsr();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){

            if(requestCode==1000){
                RegCarePfpName= data.getDataString().substring(data.getDataString().lastIndexOf("/")+1);
                RegCarePFP.setImageURI(data.getData());
                RegCarePfpUri = data.getData();
            }
            if(requestCode== 100){
                Address = Autocomplete.getPlaceFromIntent(data);
                RegCareAddre.setText(Address.getAddress());
                RegCareLatLng= new Location(Address.getLatLng().latitude, Address.getLatLng().longitude, Address.getAddress());
            }
            if(requestCode==10){
                TVRegCareFile.setText(data.getDataString().substring(data.getDataString().lastIndexOf("/")+1));
                RegCareFileUri = data.getData();
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent FileBrowse = new Intent();
            FileBrowse.setType("application/pdf");
            FileBrowse.setAction(FileBrowse.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(FileBrowse,"PDF FILE SELECT"),10);
        }else{
            Toast.makeText(RegCare.this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CreateCareUsr(){
        String Name = RegCareName.getEditText().getText().toString();
        String LaName = RegCareLaName.getEditText().getText().toString();
        String Birthday = RegCareDate.getText().toString();
        String Mail = RegCareEmail.getEditText().getText().toString();
        EncryptedLocation CareLocation = new EncryptedLocation();
        String Pass = "";
        try {
            CareLocation = new EncryptedLocation(Encrypter.Encrypt(String.valueOf(RegCareLatLng.getLatitude())), Encrypter.Encrypt(String.valueOf(RegCareLatLng.getLongitude())), Encrypter.Encrypt(RegCareLatLng.getAddress()));
            Pass = Encrypter.Encrypt(RegCarePass.getEditText().getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        float Salar = Float.parseFloat(RegCareSalar.getEditText().getText().toString());
        EncryptedLocation finalCareLocation = CareLocation;
        String finalPass = Pass;
        mAuth.createUserWithEmailAndPassword(Mail, RegCarePass.getEditText().getText().toString()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                CareUserID = mAuth.getUid();
                DocumentReference documentReference = db.collection("users").document(CareUserID);
                Map<String, Object> user=new HashMap<>();
                Map<String, Object> UserInfo=new HashMap<>();
                String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                db.collection("banned").document(id).get().addOnSuccessListener(snapshot -> {
                    if(snapshot.exists()){
                        if(snapshot.getBoolean("Banned")){
                            user.put("Banned",true);
                            Toast.makeText(this, "Dispositivo baneado, usuario registrado no tendrá acceso a promocionales", Toast.LENGTH_SHORT).show();
                        }
                    }
                    user.put("UserType", "Caretaker");
                    user.put("UserInfo", UserInfo);
                    user.put("Rating", 0);
                    user.put("RatingQuantity", 0);
                    user.put("Expertise", "Sin experiencia");
                    user.put("Advertised", false);
                    user.put("Approved","pending");
                    UserInfo.put("UserID", CareUserID);
                    UserInfo.put("Name", Name);
                    UserInfo.put("LastName", LaName);
                    UserInfo.put("Birthday", Birthday);
                    UserInfo.put("Mail", Mail);
                    UserInfo.put("Password", finalPass);
                    UserInfo.put("Sex", RegCareSex);
                    UserInfo.put("Salary", Salar);
                    UserInfo.put("Location", finalCareLocation);
                    ContentResolver contentResolver = getContentResolver();
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    StorageReference pfpref=storageReference.child("ProfilePictures/"+Name+"_" + LaName + "_" +CareUserID+"."+mime.getExtensionFromMimeType(contentResolver.getType(RegCarePfpUri)));
                    pfpref.putFile(RegCarePfpUri).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isComplete());
                        Uri uri = uriTask.getResult();
                        PutFile putPfp = new PutFile(RegCarePfpName, uri.toString());
                        UserInfo.put("Profile Picture", putPfp);
                        if (RegCareFileUri != null){
                            StorageReference reference=storageReference.child("ExpertiseFiles/"+Name+"_" + LaName + "_" +CareUserID+".pdf");
                            reference.putFile(RegCareFileUri).addOnSuccessListener(taskSnapshot1 -> {
                                Task<Uri> uriTask1 = taskSnapshot1.getStorage().getDownloadUrl();
                                while(!uriTask1.isComplete());
                                Uri uri1 = uriTask1.getResult();
                                PutFile putfile = new PutFile(TVRegCareFile.getText().toString(), uri1.toString());
                                Map<String, Object> ExpertiseFile=new HashMap<>();
                                ExpertiseFile.put("name", putfile.getName());
                                ExpertiseFile.put("url", putfile.getUrl());
                                ExpertiseFile.put("Validated", false);
                                UserInfo.put("ExpertiseFile", ExpertiseFile);
                                documentReference.set(user).addOnSuccessListener(unused -> Log.d("TAG", "onSuccess: Datos registrados" + CareUserID));
                                RegCarePb.setVisibility(View.GONE);
                                Toast.makeText(RegCare.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegCare.this, LoginActivity.class));
                                finish();
                            });
                        }else{
                            documentReference.set(user).addOnSuccessListener(unused -> Log.d("TAG", "onSuccess: Datos registrados" + CareUserID));
                            RegCarePb.setVisibility(View.GONE);
                            Toast.makeText(RegCare.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegCare.this, LoginActivity.class));
                            finish();
                        }
                    });
                });
            }else {
                RegCarePb.setVisibility(View.GONE);
                Toast.makeText(RegCare.this, "Error al crear cuenta, correo registrado previamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}