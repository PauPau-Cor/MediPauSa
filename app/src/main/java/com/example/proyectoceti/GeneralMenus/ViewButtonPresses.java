package com.example.proyectoceti.GeneralMenus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ButtonEventModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityViewButtonPressesBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ViewButtonPresses extends AppCompatActivity {

    private ActivityViewButtonPressesBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewButtonPressesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent receiver = getIntent();
        id = receiver.getStringExtra("PatientID");

        db = FirebaseFirestore.getInstance();
        Query query = db.collection("buttonevents").whereEqualTo("reviewed", false).whereEqualTo("patientID", id);
        FirestoreRecyclerOptions<ButtonEventModel> options = new FirestoreRecyclerOptions.Builder<ButtonEventModel>()
                .setQuery(query, ButtonEventModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ButtonEventModel, ButtonViewHolder>(options) {
            @NonNull
            @Override
            public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_single_button, parent, false);
                return new ButtonViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull ButtonViewHolder holder, int position, @NonNull ButtonEventModel model) {
                holder.Title.setText("Paciente ha presionado su botón");
                holder.Time.setText(model.getTime());
                holder.Help.setOnClickListener(view -> {
                    if(ContextCompat.checkSelfPermission(ViewButtonPresses.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage("3311419415", null,"Paciente esta teniendo una emergencia revisa tu aplicacion para obtener la direccion", null,null);
                        db.collection("buttonevents").whereEqualTo("patientID",model.getPatientID()).whereEqualTo("time", model.getTime()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot doc: snapshotList){
                                doc.getReference().update("operatorWarned", true);
                            }
                        });
                    }else{
                        ActivityCompat.requestPermissions(ViewButtonPresses.this,
                                new String[]{Manifest.permission.SEND_SMS}, 1);
                    }
                });

                holder.Dismiss.setOnClickListener(view -> {
                    db.collection("buttonevents").whereEqualTo("patientID",model.getPatientID()).whereEqualTo("time", model.getTime()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc: snapshotList){
                            doc.getReference().update("reviewed", true);
                        }
                    });
                });
            }
        };
        binding.ButtonList.setHasFixedSize(false);
        binding.ButtonList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        binding.ButtonList.setAdapter(adapter);
    }

    private static class ButtonViewHolder extends RecyclerView.ViewHolder {

        private final TextView Title;
        private final TextView Time;
        private final MaterialButton Help;
        private final MaterialButton Dismiss;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.ButTitle);
            Time = itemView.findViewById(R.id.ButTime);
            Help = itemView.findViewById(R.id.ButHelp);
            Dismiss = itemView.findViewById(R.id.ButDismiss);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

}