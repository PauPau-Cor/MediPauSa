package com.example.proyectoceti.GeneralMenus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Validations;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PassReset extends AppCompatActivity {
    TextInputLayout PassResPa, PassResPa2;
    Button BTPassRes;

    private String UserID;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent GotHere = getIntent();
        UserID = GotHere.getStringExtra("UserID");
        setContentView(R.layout.activity_pass_reset);
        PassResPa=findViewById(R.id.PassResPa);
        PassResPa2=findViewById(R.id.PassResPa2);
        BTPassRes=findViewById(R.id.BTPassRes);

        db= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();


        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            mAuth.signOut();
        }
        BTPassRes.setOnClickListener(view -> {
            if(!PassResPa.getEditText().getText().toString().equals(PassResPa2.getEditText().getText().toString())){
                PassResPa.setError("Contraseñas no coinciden");
                PassResPa.requestFocus();
                return;
            }
            if(!Validations.isValidPassword(PassResPa.getEditText().getText().toString()) ||PassResPa.getEditText().getText().toString().length()<8){
                PassResPa.setError("Contraseña deberá tener 8 caracteres, por lo menos un numero, una letra mayuscula y una letra minuscula");
                PassResPa.requestFocus();
                return;
            }
            ChangePassword();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void ChangePassword(){
        DocumentReference documentReference = db.collection("users").document(UserID);
        documentReference.get().addOnCompleteListener(task -> {
            try {
                String  Password = Encrypter.Decrypt(task.getResult().getString("UserInfo.Password"));
                String  NewPassword = Encrypter.Encrypt(PassResPa.getEditText().getText().toString());
                if (!Password.equals(PassResPa.getEditText().getText().toString())) {
                    mAuth.signInWithEmailAndPassword(task.getResult().getString("UserInfo.Mail"), Password).addOnSuccessListener(authResult -> {
                        user = mAuth.getCurrentUser();
                        user.updatePassword(PassResPa.getEditText().getText().toString()).addOnSuccessListener(unused -> {
                            Toast.makeText(PassReset.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                            documentReference.update("UserInfo.Password", NewPassword);
                            Intent intent = new Intent(PassReset.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        });
                    });
                } else
                    Toast.makeText(this, "Contraseña no puede ser igual a la anterior", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}