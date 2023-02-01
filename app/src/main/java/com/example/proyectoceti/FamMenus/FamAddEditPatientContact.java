package com.example.proyectoceti.FamMenus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.ContactModel;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.databinding.ActivityFamAddEditPatientContactBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FamAddEditPatientContact extends AppCompatActivity {

    private ActivityFamAddEditPatientContactBinding binding;
    private ContactModel model;
    private String PatientID;
    private FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityFamAddEditPatientContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        Intent receiver = getIntent();
        PatientID = receiver.getStringExtra("PatientID");
        model = receiver.getParcelableExtra("ContactModel");

        if(model != null){
            try {
                String Number = Encrypter.Decrypt(model.getCellphoneNumber());
                binding.ETAddContactName.getEditText().setText(model.getName());
                binding.ETAddContactNumber.getEditText().setText(Number);



                binding.BTAddContact.setOnClickListener(view -> {
                    if(Validate()){
                        db.collection("contacts").whereEqualTo("patientID", PatientID).whereEqualTo("cellphoneNumber",model.getCellphoneNumber()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot snapshot: snapshotList){
                                snapshot.getReference().set(getData());
                            }
                            finish();
                        });
                    }
                });

                binding.BTDeleteContact.setOnClickListener(view -> {
                    db.collection("contacts").whereEqualTo("patientID", PatientID).whereEqualTo("cellphoneNumber",model.getCellphoneNumber()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot: snapshotList){
                            snapshot.getReference().delete();
                        }
                        finish();
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            binding.BTAddContact.setOnClickListener(view -> {
                if(Validate()){
                    db.collection("contacts").add(getData());
                    finish();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ContactModel getData() {
        try {
            String EncNumber = Encrypter.Encrypt(binding.ETAddContactNumber.getEditText().getText().toString());
            return new ContactModel(binding.ETAddContactName.getEditText().getText().toString(), EncNumber, PatientID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    private boolean Validate() {
        if(TextUtils.isEmpty(binding.ETAddContactName.getEditText().getText().toString()) || TextUtils.isEmpty(binding.ETAddContactNumber.getEditText().getText().toString())){
            Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(binding.ETAddContactNumber.getEditText().getText().toString().length()<3){
            binding.ETAddContactNumber.setError("Número de contacto deberá contener al menos 3 dígitos");
            binding.ETAddContactNumber.requestFocus();
            return false;
        }
        return true;
    }
}