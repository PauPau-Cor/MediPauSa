package com.example.proyectoceti.OperatorMenus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ButtonEventModel;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorViewButtonBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class OperatorViewButton extends AppCompatActivity {

    ActivityOperatorViewButtonBinding binding;
    FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorViewButtonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        Query query = db.collection("buttonevents").whereEqualTo("operatorWarned", true).whereEqualTo("reviewed", false);
        FirestoreRecyclerOptions<ButtonEventModel> options = new FirestoreRecyclerOptions.Builder<ButtonEventModel>()
                .setQuery(query, ButtonEventModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ButtonEventModel, ButtonViewHolder>(options) {
            @NonNull
            @Override
            public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ope_view_single_button, parent, false);
                return new ButtonViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull ButtonViewHolder holder, int position, @NonNull ButtonEventModel model) {
                db.collection("patients").document(model.getPatientID()).get().addOnSuccessListener(snapshot -> {
                    String address, lat, lng;
                    try {
                        address = Encrypter.Decrypt(snapshot.getString("PatientInfo.Location.address"));
                        lat = Encrypter.Decrypt(snapshot.getString("PatientInfo.Location.latitude"));
                        lng = Encrypter.Decrypt(snapshot.getString("PatientInfo.Location.longitude"));
                        holder.Addy.setText(address);
                        holder.GoAddy.setOnClickListener(view -> {
                            Uri gmmIntentUri = Uri.parse("geo: " + lat + " " + lng + "?q=" + address);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    holder.Time.setText(model.getTime());
                    holder.Call.setOnClickListener(view -> {
                        if (ActivityCompat.checkSelfPermission(OperatorViewButton.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(OperatorViewButton.this,new String[]{Manifest.permission.CALL_PHONE},10);
                            return;
                        }
                        Intent call = new Intent(Intent.ACTION_CALL);
                        try {
                            call.setData(Uri.parse("tel: 911"));
                            startActivity(call);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                });
            }
        };
        binding.ButtonList.setHasFixedSize(false);
        binding.ButtonList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        binding.ButtonList.setAdapter(adapter);
    }

    private static class ButtonViewHolder extends RecyclerView.ViewHolder {

        private final TextView Addy;
        private final TextView Time;
        private final ImageButton Call;
        private final ImageButton GoAddy;
        private final MaterialButton Dismiss;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            Addy = itemView.findViewById(R.id.OpeButAddy);
            Time = itemView.findViewById(R.id.OpeButTime);
            Call = itemView.findViewById(R.id.CallButton);
            GoAddy = itemView.findViewById(R.id.DirectionsButton);
            Dismiss = itemView.findViewById(R.id.OpeButDismiss);
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