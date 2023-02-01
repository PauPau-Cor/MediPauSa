package com.example.proyectoceti.FamMenus;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.databinding.ActivityEditFamBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditFam extends AppCompatActivity {

    private ActivityEditFamBinding binding;
    private String FamUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditFamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FamUserID = mAuth.getCurrentUser().getUid();

        db.collection("users").document(FamUserID).get().addOnSuccessListener(snapshot -> {
            binding.ETEdiFamName.getEditText().setText(snapshot.getString("UserInfo.Name"));
            binding.ETEdiFamLaName.getEditText().setText(snapshot.getString("UserInfo.LastName"));
            try {
                String Number = Encrypter.Decrypt(snapshot.getString("UserInfo.CellPhoneNumber"));
                binding.ETEdiFamNumber.getEditText().setText(Number);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.BTFamEdi.setOnClickListener(view -> {
            if(Validate()){
                try {
                    String newNumber = Encrypter.Encrypt(binding.ETEdiFamNumber.getEditText().getText().toString());
                    db.collection("users").document(FamUserID).update("UserInfo.Name", binding.ETEdiFamName.getEditText().getText().toString(),
                            "UserInfo.LastName", binding.ETEdiFamLaName.getEditText().getText().toString(), "UserInfo.CellPhoneNumber", newNumber).addOnSuccessListener(unused -> {
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean Validate() {
        if(TextUtils.isEmpty(binding.ETEdiFamName.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETEdiFamLaName.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETEdiFamNumber.getEditText().getText().toString())){
            return false;
        }
        Pattern p = Pattern.compile("[^A-Za-zñ ]");
        Matcher m = p.matcher(binding.ETEdiFamName.getEditText().getText().toString());
        if(m.find()) {
            binding.ETEdiFamName.setError("Nombre no puede contener numeros ni caracteres especiales");
            binding.ETEdiFamName.requestFocus();
            return false;
        }
        binding.ETEdiFamName.setError(null);
        m= p.matcher(binding.ETEdiFamLaName.getEditText().getText().toString());
        if(m.find()) {
            binding.ETEdiFamLaName.setError("Apellidos no pueden contener numeros ni caracteres especiales");
            binding.ETEdiFamLaName.requestFocus();
            return false;
        }
        binding.ETEdiFamLaName.setError(null);
        if(binding.ETEdiFamName.getEditText().getText().toString().length()<2){
            binding.ETEdiFamName.setError("Nombre debe contener por lo menos 2 caracteres");
            binding.ETEdiFamName.requestFocus();
            return false;
        }
        binding.ETEdiFamName.setError(null);
        if(binding.ETEdiFamLaName.getEditText().getText().toString().length()<2){
            binding.ETEdiFamLaName.setError("Apellidos deben contener por lo menos 2 caracteres");
            binding.ETEdiFamLaName.requestFocus();
            return false;
        }
        binding.ETEdiFamLaName.setError(null);
        if(binding.ETEdiFamNumber.getEditText().getText().toString().length() < 8){
            binding.ETEdiFamNumber.setError("Número de telefono deberá tener al menos 8 dígitos");
            binding.ETEdiFamNumber.requestFocus();
            return false;
        }
        binding.ETEdiFamNumber.setError(null);
        return true;
    }


}