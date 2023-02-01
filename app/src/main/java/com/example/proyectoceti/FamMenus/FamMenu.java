package com.example.proyectoceti.FamMenus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectoceti.ClassesAndModels.UnbanRequestModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.LoginActivity;
import com.example.proyectoceti.GeneralMenus.PassReset;
import com.example.proyectoceti.GeneralMenus.ViewNotifications;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityFamMenuBinding;
import com.example.proyectoceti.databinding.DialogConfirmPassBinding;
import com.example.proyectoceti.databinding.DialogReportBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class FamMenu extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String UserId;
    private ActivityFamMenuBinding binding;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private UserModel model;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fam_host_layout);
        NavController navCo = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navCo);
        mAuth = FirebaseAuth.getInstance();
        UserId = mAuth.getUid();
        db = FirebaseFirestore.getInstance();

        GetToken();
        SetUpDrawer();

        binding.SeeNotifsBT.setOnClickListener(view -> startActivity(new Intent(FamMenu.this, ViewNotifications.class)));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SetUpDrawer(){
        setSupportActionBar(binding.FamAppBar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.FamDrawer, R.string.app_name, R.string.app_name);
        actionBarDrawerToggle.syncState();
        View HeaderView = binding.FamDrawerNavView.getHeaderView(0);
        TextView UserName = HeaderView.findViewById(R.id.Fam_main_name);
        db.collection("users").document(UserId).get().addOnSuccessListener(documentSnapshot -> {
            model = documentSnapshot.toObject(UserModel.class);
            UserName.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName").toString());
        });
        binding.FamDrawerNavView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.FamBTProfile:{
                    startActivity(new Intent(FamMenu.this, EditFam.class));
                    break;
                }
                case R.id.FamResetPass:{
                    CheckAuth();
                    break;
                }
                case R.id.FamViewAdsMenuBT:{
                    if(model.getBanned()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Su cuenta ha sido banneada");
                        builder.setMessage("Con una cuenta banneada no podrá acceder a los promocionales, esto previniendo su acceso con el resto de usuarios. El resto de la aplicación funcionará normalmente.");
                        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                        builder.setNegativeButton("Petición de desbaneo", ((dialogInterface, i) -> UnbanRequest(dialogInterface)));
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                    startActivity(new Intent(FamMenu.this, FamViewCareAds.class));
                    break;
                }
                case R.id.FamLinkFamBT:{
                    startActivity(new Intent(FamMenu.this, FamCareLink.class));
                    break;
                }
                case R.id.ConfigButtonBT:{
                    startActivity(new Intent(FamMenu.this, FamSelectButton.class));
                    break;
                }
                case R.id.FamLogOutBT:{
                    SignOut();
                    break;
                }
            }
            binding.FamDrawer.closeDrawers();
            return true;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CheckAuth() {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DialogConfirmPassBinding dialogBinding = DialogConfirmPassBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.rejectDialogSend.setOnClickListener(view -> {
            String reason = dialogBinding.rejectDialogReason.getEditText().getText().toString();
            if(reason.isEmpty()) {
                Toast.makeText(this, "Debe introducir una contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String EncryptedPass = Encrypter.Encrypt(reason);
                if(model.getUserInfo().get("Password").toString().equals(EncryptedPass)){
                    startActivity(new Intent(FamMenu.this, PassReset.class).putExtra("UserID", UserId));
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void UnbanRequest(DialogInterface dialogInterface){
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DialogReportBinding dialogBinding = DialogReportBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.Title.setText("Revision de reportes");
        dialogBinding.reportDialogSend.setOnClickListener(view -> {
            if(TextUtils.isEmpty(dialogBinding.reportDialogReason.getEditText().getText().toString())){
                Toast.makeText(this, "Favor de indicar una razon", Toast.LENGTH_SHORT).show();
                return;
            }
            UnbanRequestModel unbanModel = new UnbanRequestModel(model.getUserInfo().get("Name").toString(), UserId, dialogBinding.reportDialogReason.getEditText().getText().toString(), false);
            db.collection("unbanrequests").document(UserId).set(unbanModel).addOnSuccessListener(unused -> {
                dialog.dismiss();
                dialogInterface.dismiss();
            });
        });

    }

    private void GetToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference documentReference = db.collection("users").document(UserId);
        documentReference.update("FCM Token", token, "DeviceID", id);
    }

    private void SignOut(){
        DocumentReference documentReference = db.collection("users").document(UserId);
        documentReference.update("FCM Token", FieldValue.delete()).addOnSuccessListener(unused -> {
            mAuth.signOut();
            startActivity(new Intent(FamMenu.this, LoginActivity.class));
            finish();
        });
    }

}