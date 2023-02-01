package com.example.proyectoceti.GeneralMenus;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.CareMenus.CareMenu;
import com.example.proyectoceti.FamMenus.FamMenu;
import com.example.proyectoceti.OperatorMenus.OperatorMenu;
import com.example.proyectoceti.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputLayout ETLogMail, ETLogPass;
    private TextView TVLogReg, TVLogReset;
    private Button BTLog;
    private ProgressBar LoginPB;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ETLogMail = findViewById(R.id.ETLogMail);
        ETLogPass = findViewById(R.id.ETLogPass);
        TVLogReg = findViewById(R.id.TVLogReg);
        TVLogReset = findViewById(R.id.TVLogReset);
        BTLog = findViewById(R.id.BTLog);
        LoginPB = findViewById(R.id.LoginPB);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        BTLog.setOnClickListener(view -> UserLogin());


        TVLogReg.setOnClickListener(view -> {
            Intent OpcReg = new Intent(view.getContext(), RegTypeMenu.class);
            startActivity(OpcReg);
        });

        TVLogReset.setOnClickListener(view -> {
            Intent OpcReset = new Intent(view.getContext(), ForgotPass.class);
            startActivity(OpcReset);
        });
    }



    protected void UserLogin(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        LoginPB.setVisibility(View.VISIBLE);
        String mail = ETLogMail.getEditText().getText().toString();
        String password = ETLogPass.getEditText().getText().toString();
        if(TextUtils.isEmpty(mail)){
            ETLogMail.setError("Ingrese un correo");
            ETLogMail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this,"Ingrese una contraseña", Toast.LENGTH_SHORT).show();
            ETLogPass.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String uId = mAuth.getCurrentUser().getUid();
                    DocumentReference UserDoc = db.collection("users").document(uId);
                    UserDoc.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document = task1.getResult();
                            if (document.exists()) {
                                String UserType = document.get("UserType").toString();
                                if(UserType.equals("Caretaker")){
                                    LoginPB.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this,"Bienvenido cuidador",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, CareMenu.class));
                                    finish();
                                }else if(UserType.equals("Family")){
                                    LoginPB.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this,"Bienvenido encargado",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, FamMenu.class));
                                    finish();
                                }else if(UserType.equals("Operator")){
                                    LoginPB.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this,"Bienvenido operador",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, OperatorMenu.class));
                                    finish();
                                }
                            } else {
                                LoginPB.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            LoginPB.setVisibility(View.GONE);
                            Log.d(TAG, "get failed with ", task1.getException());
                        }
                    });

                }else{
                    Log.w("TAG","Error: ", task.getException());
                    LoginPB.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this,"Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


}
