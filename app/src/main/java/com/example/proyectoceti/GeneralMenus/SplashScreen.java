package com.example.proyectoceti.GeneralMenus;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.CareMenus.CareMenu;
import com.example.proyectoceti.FamMenus.FamMenu;
import com.example.proyectoceti.OperatorMenus.OperatorMenu;
import com.example.proyectoceti.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView(R.layout.activity_splash_screen);

        Animation animacion1= AnimationUtils.loadAnimation(this,R.anim.des_arriba);
        Animation animacion2= AnimationUtils.loadAnimation(this,R.anim.des_abajo);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView txt_Medi = findViewById(R.id.txt_Medi);
        ImageView logoImage = findViewById(R.id.logoImage);

        txt_Medi.setAnimation(animacion2);
        logoImage.setAnimation(animacion1);

        new Handler().postDelayed(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user == null){
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            }else{
                String uId = user.getUid();
                DocumentReference UserDoc = db.collection("users").document(uId);
                UserDoc.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String UserType = document.get("UserType").toString();
                            if(UserType.equals("Caretaker")){
                                Toast.makeText(SplashScreen.this,"Bienvenido cuidador",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashScreen.this, CareMenu.class));
                                finish();
                            }else if(UserType.equals("Family")){
                                Toast.makeText(SplashScreen.this,"Bienvenido encargado",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashScreen.this, FamMenu.class));
                                finish();
                            }else if(UserType.equals("Operator")){
                                Toast.makeText(SplashScreen.this,"Bienvenido operador",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SplashScreen.this, OperatorMenu.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(SplashScreen.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                        finish();
                    }
                });
            }
        },4000);
    }
}