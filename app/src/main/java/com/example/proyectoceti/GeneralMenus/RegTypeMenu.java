package com.example.proyectoceti.GeneralMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.FamMenus.RegFam;
import com.example.proyectoceti.R;
import com.example.proyectoceti.CareMenus.RegCare;

public class RegTypeMenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_type_menu);
    }

    public void clickCaretaker(View view){
        Intent OpcCare = new Intent(this, RegCare.class);
        startActivity(OpcCare);
    }
    public void clickFamily(View view){
        Intent OpcFam = new Intent(this, RegFam.class);
        startActivity(OpcFam);
    }
}