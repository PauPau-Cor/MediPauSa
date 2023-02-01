package com.example.proyectoceti.CareMenus;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.PutFile;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CareLinkFamIn extends AppCompatActivity {

    Button BtLinkCodeIn;
    EditText ETLinkCode;

    private String CareUserID;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_link_fam_in);
        BtLinkCodeIn = findViewById(R.id.BtLinkCodeIn);
        ETLinkCode = findViewById(R.id.ETLinkCode);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        CareUserID = mAuth.getUid();

        BtLinkCodeIn.setOnClickListener(view -> {
            if(TextUtils.isEmpty(ETLinkCode.getText().toString())){
                Toast.makeText(this, "Por favor ingrese un codigo", Toast.LENGTH_SHORT).show();
            }else if(ETLinkCode.getText().length()!=8){
                Toast.makeText(this, "El codigo deberá tener 8 digitos", Toast.LENGTH_SHORT).show();
            }else{
                DocumentReference documentReference = db.collection("users").document(CareUserID);
                Query query = db.collection("users").whereEqualTo("LinkCode.code", ETLinkCode.getText().toString());
                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    if(snapshotList.isEmpty()){
                        Toast.makeText(this, "Codigo no encontrado", Toast.LENGTH_SHORT).show();
                    }
                    for (DocumentSnapshot snapshot: snapshotList){
                        if(Boolean.FALSE.equals(snapshot.getBoolean("LinkCode.used"))){
                            if(!daysBetween(snapshot.getDate("LinkCode.date"))){
                                documentReference.update("VinculatedFam", FieldValue.arrayUnion(snapshot.getString("UserInfo.UserID")));
                                String FamUserID = snapshot.getString("UserInfo.UserID");
                                DocumentReference famDocRef  = db.collection("users").document(FamUserID);
                                famDocRef.update("VinculatedCare", FieldValue.arrayUnion(CareUserID), "LinkCode.used", true);
                                Query PacQuery = db.collection("patients").whereEqualTo("FamUserID",FamUserID);
                                PacQuery.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    List<DocumentSnapshot> snapshotList1 = queryDocumentSnapshots1.getDocuments();
                                    for (DocumentSnapshot snapshot1: snapshotList1){
                                        db.collection("patients").document(snapshot1.getId()).update("CareUserIDs", FieldValue.arrayUnion(CareUserID));
                                    }
                                    CreateLinkedUser(FamUserID);
                                });
                                String FCMToken = snapshot.getString("FCM Token");
                                if(FCMToken != null){
                                    FCMSend.pushNotif(this, FCMToken,"Usuario vinculado", "Un cuidador ha usado su codigo para vincularse a su cuenta.");
                                }
                                Utilities.OfflineNotification(FamUserID, "Usuario vinculado", "Un cuidador ha usado su codigo para vincularse con su cuenta");
                                Toast.makeText(this, "Usuario vinculado con " + snapshot.getString("UserInfo.Name"), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(this, "Este codigo ha caducado", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(this, "Este codigo ya ha sido usado", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    public boolean daysBetween(Date date) {
        Calendar first = Calendar.getInstance();
        first.setTime(date);
        Calendar second = Calendar.getInstance();
        long diffInMillis = second.getTimeInMillis() - first.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diffInMillis) >= 1;
    }

    private void CreateLinkedUser(String FamUserID){
        db.collection("users").document(CareUserID).get().addOnSuccessListener(documentSnapshot -> {
            PutFile ProfilePicture = new PutFile(documentSnapshot.get("UserInfo.Profile Picture.name").toString(), documentSnapshot.get("UserInfo.Profile Picture.url").toString());
            LinkedUsersModel LinkedUser = new LinkedUsersModel(false, 0, 0, documentSnapshot.getString("UserInfo.UserID"), FamUserID,
                    documentSnapshot.getString("UserInfo.Name")+" "+documentSnapshot.getString("UserInfo.LastName"), ProfilePicture,
                    false, false, Double.valueOf(documentSnapshot.get("UserInfo.Salary").toString()), 0);
            db.collection("linkedusers").add(LinkedUser);
        });

    }
}