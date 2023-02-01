package com.example.proyectoceti.OperatorMenus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorViewUserForDocBinding;
import com.example.proyectoceti.databinding.DialogAcceptdocBinding;
import com.example.proyectoceti.databinding.DialogRejectBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OperatorViewUserForDoc extends AppCompatActivity {

    ActivityOperatorViewUserForDocBinding binding;
    UserModel model;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorViewUserForDocBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent receive = getIntent();
        model = receive.getParcelableExtra("UserModel");
        db= FirebaseFirestore.getInstance();
        binding.UserForDocName.setText(model.getUserInfo().get("Name") + " " + model.getUserInfo().get("LastName"));
        String photoUrl = ((Map)model.getUserInfo().get("Profile Picture")).get("url").toString();
        String pdfUrl = ((Map)model.getUserInfo().get("ExpertiseFile")).get("url").toString();
        Picasso.get().load(photoUrl).into(binding.UserForDocPhoto);
        binding.UserForDocOpen.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("application/pdf");
            intent.setData(Uri.parse(pdfUrl));
            startActivity(intent);
        });

        binding.UserForDocReject.setOnClickListener(view -> RejectDoc());
        binding.UserForDocAccept.setOnClickListener(view -> AcceptDoc());

    }

    private void RejectDoc(){
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DialogRejectBinding dialogBinding = DialogRejectBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.rejectDialogSend.setOnClickListener(view -> {
            String reason = dialogBinding.rejectDialogReason.getEditText().getText().toString();
            if(reason.isEmpty()) {
                Toast.makeText(this, "Debe especificar una razon", Toast.LENGTH_SHORT).show();
                return;
            }
            DocumentReference UserDocument = db.collection("users").document(model.getUserInfo().get("UserID").toString());
            UserDocument.update("UserInfo.ExpertiseFile.Accepted", false, "UserInfo.ExpertiseFile.Validated", true, "UserInfo.ExpertiseFile.Reason", reason);
            dialog.dismiss();
            String FCMToken = model.getFCMToken();
            if(FCMToken != null){
                FCMSend.pushNotif(this, FCMToken,"Revision de documentos", "Tu certificado ha sido rechazado: " + reason);
            }
            Utilities.OfflineNotification(model.getUserInfo().get("UserID").toString(), "Revision de documentos", "Tu certificado ha sido rechazado: " + reason);
            finish();
        });
    }

    private void AcceptDoc(){

        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DialogAcceptdocBinding dialogBinding = DialogAcceptdocBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();

        dialogBinding.acceptDialogSend.setOnClickListener(view -> {
            String Expertise = "";
            switch (dialogBinding.DialogAcceptDocButtons.getCheckedButtonId()){
                case R.id.DialogAcceptDocFirstAid:{
                    Expertise = "Primeros auxilios";
                    break;
                }
                case R.id.DialogAcceptDocStudent:{
                    Expertise = "Estudiante";
                    break;
                }
                case R.id.DialogAcceptDocNurse:{
                    Expertise = "Enfermer@";
                    break;
                }
            }
            DocumentReference UserDocument = db.collection("users").document(model.getUserInfo().get("UserID").toString());
            UserDocument.update("UserInfo.ExpertiseFile.Accepted", true, "UserInfo.ExpertiseFile.Validated", true, "Expertise", Expertise);
            String FCMToken = model.getFCMToken();
            if(FCMToken != null){
                FCMSend.pushNotif(this, FCMToken,"Revision de documentos", "Tu certificado ha sido aceptado como " + Expertise);
            }
            Utilities.OfflineNotification(model.getUserInfo().get("UserID").toString(), "Revision de documentos", "Tu certificado ha sido aceptado como " + Expertise);
            dialog.dismiss();
            finish();
        });
    }
}