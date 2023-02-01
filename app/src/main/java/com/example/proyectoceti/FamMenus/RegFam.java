package com.example.proyectoceti.FamMenus;

import static java.lang.Boolean.FALSE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.GeneralMenus.LoginActivity;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegFam extends AppCompatActivity {
    EditText RegFamDate;
    TextInputLayout RegFamName, RegFamLaName, RegFamNumber, RegFamEmail, RegFamPass, RegFamPass2;
    Button BTFamCare;
    ProgressBar RegFamPB;

    DatePickerDialog.OnDateSetListener setListener;
    private String FamUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_fam);
        RegFamDate = findViewById(R.id.ETRegFamDate);
        RegFamName = findViewById(R.id.ETRegFamName);
        RegFamLaName = findViewById(R.id.ETRegFamLaName);
        RegFamNumber = findViewById(R.id.ETRegFamNumber);
        RegFamEmail = findViewById(R.id.ETRegFamMail);
        RegFamPass = findViewById(R.id.ETRegFamPass);
        RegFamPass2 = findViewById(R.id.ETRegFamPass2);
        BTFamCare = findViewById(R.id.BTFamReg);
        RegFamPB = findViewById(R.id.RegFamPB);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
        RegFamDate.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegFam.this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,setListener,year,month,day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });

        setListener = (view, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date= dayOfMonth+"/"+ month1 +"/"+ year1;
            RegFamDate.setText(date);
        };
        BTFamCare.setOnClickListener(view -> {
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            if(TextUtils.isEmpty(RegFamEmail.getEditText().getText().toString())|TextUtils.isEmpty(RegFamDate.getText().toString())|
                    TextUtils.isEmpty(RegFamName.getEditText().getText().toString())|TextUtils.isEmpty(RegFamLaName.getEditText().getText().toString())|
                    TextUtils.isEmpty(RegFamPass.getEditText().getText().toString())|TextUtils.isEmpty(RegFamPass2.getEditText().getText().toString())
                    |TextUtils.isEmpty(RegFamNumber.getEditText().getText().toString())){
                Toast.makeText(RegFam.this,"Por favor rellene todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            Pattern p = Pattern.compile("[^A-Za-zñ ]");
            Matcher m = p.matcher(RegFamName.getEditText().getText().toString());
            if(m.find()) {
                RegFamName.setError("Nombre no puede contener numeros ni caracteres especiales");
                RegFamName.requestFocus();
                RegFamName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamName.setError(null);
                    }
                });
                return;
            }
            RegFamName.setError(null);
            m= p.matcher(RegFamLaName.getEditText().getText().toString());
            if(m.find()) {
                RegFamLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
                RegFamLaName.requestFocus();
                RegFamLaName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamLaName.setError(null);
                    }
                });
                return;
            }
            RegFamLaName.setError(null);
            if(RegFamName.getEditText().getText().toString().length()<2){
                RegFamName.setError("Nombre debe contener por lo menos 2 caracteres");
                RegFamName.requestFocus();
                RegFamName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamName.setError(null);
                    }
                });
                return;
            }
            RegFamName.setError(null);
            if(RegFamLaName.getEditText().getText().toString().length()<2){
                RegFamLaName.setError("Nombre debe contener por lo menos 2 caracteres");
                RegFamLaName.requestFocus();
                RegFamLaName.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamLaName.setError(null);
                    }
                });
                return;
            }
            RegFamLaName.setError(null);
            if(RegFamNumber.getEditText().getText().toString().length() < 8){
                RegFamNumber.setError("Número de telefono deberá tener al menos 8 dígitos");
                RegFamNumber.requestFocus();
                RegFamNumber.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamNumber.setError(null);
                    }
                });
                return;
            }
            RegFamNumber.setError(null);
            if(!RegFamPass.getEditText().getText().toString().equals(RegFamPass2.getEditText().getText().toString())) {
                RegFamPass.setError("Contraseñas no coinciden");
                RegFamPass.requestFocus();
                RegFamPass.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamPass.setError(null);
                    }
                });
                return;
            }
            RegFamPass.setError(null);
            if(!Validations.isValidPassword(RegFamPass.getEditText().getText().toString()) || RegFamPass.getEditText().getText().toString().length()<8){
                RegFamPass.setError("Contraseña deberá tener 8 caracteres, por lo menos un numero, una letra mayuscula y una letra minuscula");
                RegFamPass.requestFocus();
                RegFamPass.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        RegFamPass.setError(null);
                    }
                });
                return;
            }
            RegFamPass.setError(null);
            if(!Validations.AgeValidation(RegFamDate.getText().toString())){
                RegFamDate.setError("+18");
                Toast.makeText(RegFam.this,"Debes ser mayor de edad para registrarte",Toast.LENGTH_SHORT).show();
                return;
            }
            RegFamPB.setVisibility(View.VISIBLE);
            try {
                EmailValidation(RegFamEmail.getEditText().getText().toString());
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
                Toast.makeText(RegFam.this, "Error +", Toast.LENGTH_SHORT).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    RegFam.this.runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(resp);
                            String validjson = jsonObject.getString("is_valid_format");
                            JSONObject jsonObject1 = new JSONObject(validjson);
                            EmailValid[0] = jsonObject1.getBoolean("text");
                            if (!EmailValid[0]) {
                                RegFamPB.setVisibility(View.GONE);
                                RegFamEmail.setError("Correo no valido");
                                RegFamEmail.requestFocus();
                                RegFamEmail.getEditText().addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }
                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                    }
                                    @Override
                                    public void afterTextChanged(Editable editable) {
                                        RegFamEmail.setError(null);
                                    }
                                });
                            }else{
                                RegFamEmail.setError(null);
                                CreateFamUsr();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void CreateFamUsr(){
        String Name = RegFamName.getEditText().getText().toString();
        String LaName = RegFamLaName.getEditText().getText().toString();
        String Birthday = RegFamDate.getText().toString();
        String Number = "";
        String Mail = RegFamEmail.getEditText().getText().toString();
        String Pass = "";
        try {
            Pass = Encrypter.Encrypt(RegFamPass.getEditText().getText().toString());
            Number = Encrypter.Encrypt(RegFamNumber.getEditText().getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String finalPass = Pass;
        String finalNumber = Number;

        mAuth.createUserWithEmailAndPassword(Mail, RegFamPass.getEditText().getText().toString()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FamUserID = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = db.collection("users").document(FamUserID);
                Map<String, Object> user=new HashMap<>();
                Map<String, Object> UserInfo=new HashMap<>();
                String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                db.collection("banned").document(id).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        if (snapshot.getBoolean("Banned")) {
                            user.put("Banned", true);
                            Toast.makeText(this, "Dispositivo baneado, usuario registrado no tendrá acceso a promocionales", Toast.LENGTH_SHORT).show();
                        }
                    }
                    user.put("UserType", "Family");
                    user.put("Rating", 0);
                    user.put("RatingQuantity", 0);
                    user.put("UserInfo", UserInfo);
                    UserInfo.put("UserID", FamUserID);
                    UserInfo.put("Name", Name);
                    UserInfo.put("LastName", LaName);
                    UserInfo.put("Birthday", Birthday);
                    UserInfo.put("CellPhoneNumber", finalNumber);
                    UserInfo.put("Mail", Mail);
                    UserInfo.put("Password", finalPass);
                    documentReference.set(user).addOnSuccessListener(unused -> Log.d("TAG", "onSuccess: Datos registrados" + FamUserID));
                    RegFamPB.setVisibility(View.GONE);
                    Toast.makeText(RegFam.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegFam.this, LoginActivity.class));
                });
            }else {
                RegFamPB.setVisibility(View.GONE);
                Toast.makeText(RegFam.this, "Error al crear cuenta, correo registrado previamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
