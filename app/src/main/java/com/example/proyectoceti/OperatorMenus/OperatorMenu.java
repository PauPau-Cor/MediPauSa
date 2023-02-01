package com.example.proyectoceti.OperatorMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectoceti.GeneralMenus.LoginActivity;
import com.example.proyectoceti.GeneralMenus.ViewNotifications;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorMenuBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class OperatorMenu extends AppCompatActivity {
    private ActivityOperatorMenuBinding binding;
    private FirebaseFirestore db;
    private String UserId;
    private FirebaseAuth mAuth;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.operator_host_layout);
        NavController navCo = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navCo);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        UserId = mAuth.getUid();
        GetToken();
        SetUpDrawer();

        binding.SeeNotifsBT.setOnClickListener(view -> startActivity(new Intent(OperatorMenu.this, ViewNotifications.class)));
        binding.SeeButtonPressesBT.setOnClickListener(view -> startActivity(new Intent(OperatorMenu.this, OperatorViewButton.class)));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SetUpDrawer(){
        setSupportActionBar(binding.OperatorAppBar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.OperatorDrawer, R.string.app_name, R.string.app_name);
        actionBarDrawerToggle.syncState();
        View HeaderView = binding.OperatorDrawerNavView.getHeaderView(0);
        TextView UserName = HeaderView.findViewById(R.id.Operator_main_name);
        TextView HoursWorked = HeaderView.findViewById(R.id.Operator_hours);
        SwitchCompat ActiveSwitch = HeaderView.findViewById(R.id.SwitchActive);
        db.collection("users").document(UserId).get().addOnSuccessListener(documentSnapshot -> {
            UserName.setText(documentSnapshot.getString("UserInfo.Name") + " " + documentSnapshot.getString("UserInfo.LastName"));
            Integer hours = documentSnapshot.getLong("hoursWorked").intValue();
            HoursWorked.setText(String.valueOf(hours/60));
            ActiveSwitch.setChecked(documentSnapshot.getBoolean("active"));
        });

        ActiveSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            DocumentReference documentReference = db.collection("users").document(UserId);
            documentReference.update("active", b);
        });

        binding.OperatorDrawerNavView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.OperatorLogOutBT:{
                    SignOut();
                    break;
                }
            }
            binding.OperatorDrawer.closeDrawers();
            return true;
        });
    }

    private void GetToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        DocumentReference documentReference = db.collection("users").document(UserId);
        documentReference.update("FCM Token", token);
    }

    private void SignOut(){
        DocumentReference documentReference = db.collection("users").document(UserId);
        documentReference.update("FCM Token", FieldValue.delete()).addOnSuccessListener(unused -> {
            mAuth.signOut();
            startActivity(new Intent(OperatorMenu.this, LoginActivity.class));
            finish();
        });
    }
}