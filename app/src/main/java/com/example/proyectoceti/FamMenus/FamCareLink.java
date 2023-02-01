package com.example.proyectoceti.FamMenus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectoceti.ClassesAndModels.Code;
import com.example.proyectoceti.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FamCareLink extends AppCompatActivity {
    Button BtLinkCodeGen;
    TextView TVCodeLink;

    private String FamUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fam_care_link);

        BtLinkCodeGen = findViewById(R.id.BtLinkCodeGen);
        TVCodeLink = findViewById(R.id.TVLinkCode);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FamUserID=mAuth.getCurrentUser().getUid();

        BtLinkCodeGen.setOnClickListener(view -> {
            Random rnd = new Random();
            int number = rnd.nextInt(99999999);
            Code code = new Code(String.format(Locale.getDefault(), "%08d", number), Calendar.getInstance().getTime(), false);
            TVCodeLink.setText(code.code);
            DocumentReference documentReference = db.collection("users").document(FamUserID);
            Map<String, Object> LinkCode=new HashMap<>();
            LinkCode.put("LinkCode", code);
            documentReference.set(LinkCode, SetOptions.merge()).addOnSuccessListener(unused -> Toast.makeText(FamCareLink.this, "Codigo creado", Toast.LENGTH_SHORT).show());
        });
    }
}