package com.example.proyectoceti.CareMenus;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.RatingModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityCareViewSingleLinkedFamBinding;
import com.example.proyectoceti.databinding.RatingDialogBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CareViewSingleLinkedFam extends AppCompatActivity {

    private ActivityCareViewSingleLinkedFamBinding binding;
    protected UserModel model;
    protected LinkedUsersModel linkedUsersModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String CareUserID;
    private FusedLocationProviderClient fusedLocationClient;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCareViewSingleLinkedFamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        CareUserID = mAuth.getUid();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.care_host_layout);
        NavController navCo = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navCo);
        Intent receive = getIntent();
        model= receive.getParcelableExtra("UserModel");
        binding.CareViewSingleFamName.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName").toString());
        db.collection("linkedusers").whereEqualTo("careUserID", CareUserID).whereEqualTo("famUserID",model.getUserInfo().get("UserID")).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                linkedUsersModel = snapshot.toObject(LinkedUsersModel.class);
            }
            if(linkedUsersModel.isActive()) binding.SwitchActive.setChecked(true);
        });

        binding.SwitchActive.setOnCheckedChangeListener((compoundButton, b) -> {
            if(!compoundButton.isPressed()) {
                return;
            }
            db.collection("linkedusers").whereEqualTo("careUserID", CareUserID).whereEqualTo("famUserID",model.getUserInfo().get("UserID")).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    if(b){
                        GetLocation(snapshot);
                    }
                    else{
                        newShiftEvent(snapshot.getString("name") + " ha terminado turno", snapshot.getString("name") + " ha terminado turno", model.getUserInfo().get("UserID").toString());
                        snapshot.getReference().update("active", false, "shiftsWorked", snapshot.getLong("shiftsWorked")+1);
                    }
                }
            });
        });

        binding.UnlinkLinkedFam.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(CareViewSingleLinkedFam.this);
            builder.setMessage("¿Está segur@ que desvincular este encargado?")
                    .setPositiveButton("Si", (dialogInterface, i) -> unlink())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });

        binding.RateLinkedFam.setOnClickListener(view -> {
            if(linkedUsersModel.isFamRated()){
                Toast.makeText(this, "No puede calificar al mismo usuario dos veces", Toast.LENGTH_SHORT).show();
                return;
            }
            if(linkedUsersModel.getHoursWorked()<1){
                Toast.makeText(this, "No puede calificar a un usuario sin haber trabajado un turno", Toast.LENGTH_SHORT).show();
                return;
            }
            RateFam(false);
        });


        binding.ChatLinkedFam.setOnClickListener(view -> {
            Intent intent = new Intent(CareViewSingleLinkedFam.this, ChatActivity.class);
            intent.putExtra("UserModel", model);
            startActivity(intent);
        });
    }

    private void GetLocation(DocumentSnapshot snapshot) {
        if(ContextCompat.checkSelfPermission(CareViewSingleLinkedFam.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(CareViewSingleLinkedFam.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CareViewSingleLinkedFam.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        try {
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(this);
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                            String Addy = addresses.get(0).getAddressLine(0);
                            SetActiveTrue(snapshot, Addy);
                        } catch (Exception e) {
                            Toast.makeText(CareViewSingleLinkedFam.this, "Hubo un error consiguiendo su ubicacion, vuelvalo a intentar", Toast.LENGTH_SHORT).show();
                            binding.SwitchActive.setChecked(false);
                        }
                    });
        }
    }

    private void SetActiveTrue(DocumentSnapshot snapshot, String addy) {
        Utilities.OfflineNotification(model.getUserInfo().get("UserID").toString(), "Cuidador ha iniciado turno", snapshot.getString("name") + " ha iniciado turno en " + addy);
        if(model.getFCMToken() != null){
            FCMSend.pushNotif(this, model.getFCMToken(), "Cuidador ha iniciado turno", snapshot.getString("name") + " ha iniciado turno en " + addy);
        }
        newShiftEvent(snapshot.getString("name") + " ha iniciado turno", snapshot.getString("name") + " ha iniciado turno en " + addy, model.getUserInfo().get("UserID").toString());
        snapshot.getReference().update("active", true);
    }

    private void newShiftEvent(String name, String description, String FamUserID) {
        CollectionReference collectionReference = db.collection("events");
        Date timestamp = new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat("d/M/yyyy");
        String date = dateFormat.format(timestamp);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String time = timeFormat.format(timestamp);
        db.collection("patients").whereEqualTo("FamUserID", FamUserID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                EventModel newevent = new EventModel("shift", timestamp, date, time, name, description, snapshot.getString("PatientID"));
                collectionReference.add(newevent);
            }
        });

    }

    private void unlink(){
        RateFam(true);
        db.collection("linkedusers").whereEqualTo("careUserID", CareUserID).whereEqualTo("famUserID", model.getUserInfo().get("UserID"))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        LinkedUsersModel LinkedModel = snapshot.toObject(LinkedUsersModel.class);
                        db.collection("users").document(LinkedModel.getCareUserID()).update("VinculatedFam", FieldValue.arrayRemove(LinkedModel.getFamUserID()));
                        db.collection("users").document(LinkedModel.getFamUserID()).update("VinculatedCare", FieldValue.arrayRemove(LinkedModel.getCareUserID()));
                        db.collection("patients").whereEqualTo("FamUserID", LinkedModel.getFamUserID()).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            List<DocumentSnapshot> snapshotList1 = queryDocumentSnapshots1.getDocuments();
                            for (DocumentSnapshot snapshot1: snapshotList1){
                                snapshot1.getReference().update("CareUserIDs", FieldValue.arrayRemove(LinkedModel.getCareUserID()));
                            }
                        });
                        snapshot.getReference().delete();
                    }
                });
        Intent intent = new Intent(CareViewSingleLinkedFam.this, CareMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void RateFam(boolean updater){
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RatingDialogBinding dialogBinding = RatingDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.RatingDialogButton.setOnClickListener(view -> {
            Double Rating = (double) dialogBinding.RatingDialogRB.getRating();
            db.collection("linkedusers").whereEqualTo("famUserID", linkedUsersModel.getFamUserID()).whereEqualTo("careUserID", linkedUsersModel.getCareUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    snapshot.getReference().update("famRated", true);
                }
            });
            RatingModel newRatingModel = new RatingModel(dialogBinding.RatingCritique.getEditText().getText().toString(), linkedUsersModel.getFamUserID(), linkedUsersModel.getCareUserID(), Rating);
            DocumentReference UserDocument = db.collection("users").document(linkedUsersModel.getFamUserID());
            UserDocument.get().addOnSuccessListener(documentSnapshot -> {
                Double CurrentRating = documentSnapshot.getDouble("Rating");
                Double RatingQuantity = documentSnapshot.getDouble("RatingQuantity");
                Double NewRating = (CurrentRating * RatingQuantity + Rating)/ (RatingQuantity+1);
                UserDocument.update("Rating", NewRating, "RatingQuantity", RatingQuantity + 1);
            });
            if(updater){
                db.collection("ratings").whereEqualTo("receiverID", linkedUsersModel.getFamUserID()).whereEqualTo("senderID", linkedUsersModel.getCareUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        snapshot.getReference().set(newRatingModel);
                    }
                });
            }else{
                db.collection("ratings").add(newRatingModel);
                linkedUsersModel.setFamRated(true);
            }
            dialog.dismiss();
        });
    }
}