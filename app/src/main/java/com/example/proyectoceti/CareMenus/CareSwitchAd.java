package com.example.proyectoceti.CareMenus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ViewProfile;
import com.example.proyectoceti.databinding.ActivityCareSwitchAdBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CareSwitchAd extends AppCompatActivity {

    private ActivityCareSwitchAdBinding binding;
    private String CareUserID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private UserModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCareSwitchAdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        CareUserID = mAuth.getCurrentUser().getUid();
        documentReference = db.collection("users").document(CareUserID);

        documentReference.get().addOnSuccessListener(snapshot -> {
            model = snapshot.toObject(UserModel.class);
            binding.SwitchAdActive.setChecked(snapshot.getBoolean("Advertised"));
            String ApprovedState = snapshot.getString("Approved");
            switch (ApprovedState){
                case "pending": {
                    int yellow = Color.parseColor("#FDD835");
                    binding.StatusYellow.setBackgroundColor(yellow);
                    break;
                }
                case "approved": {
                    int green = Color.parseColor("#7CB342");
                    binding.StatusGreen.setBackgroundColor(green);
                    break;
                }
                case "rejected": {
                    int red = Color.parseColor("#E53935");
                    binding.StatusRed.setBackgroundColor(red);
                    binding.RejectReason.setText(snapshot.getString("RejectReason"));
                    binding.RejectReason.setVisibility(View.VISIBLE);
                    break;
                }
            }
            binding.AdViewRatings.setOnClickListener(view -> {
                Intent intent = new Intent(CareSwitchAd.this, ViewProfile.class);
                intent.putExtra("UserModel",model);
                startActivity(intent);
            });
        });

        binding.EditAd.setOnClickListener(view -> startActivity(new Intent(CareSwitchAd.this, EditCare.class)));

        binding.SwitchAdActive.setOnCheckedChangeListener((compoundButton, b) -> {
            if(compoundButton.isPressed()){
                documentReference.update("Advertised", b);
            }
        });
    }
}