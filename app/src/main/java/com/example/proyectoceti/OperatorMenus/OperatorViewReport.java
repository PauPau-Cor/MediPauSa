package com.example.proyectoceti.OperatorMenus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ReportsModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorViewReportBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorViewReport extends AppCompatActivity {

    private ActivityOperatorViewReportBinding binding;
    private ReportsModel model;
    private UserModel Sender, Receiver;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorViewReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        Intent receiver = getIntent();
        model= receiver.getParcelableExtra("ReportModel");

        db.collection("users").document(model.getSenderID()).get().addOnSuccessListener(snapshot -> {
            Sender = snapshot.toObject(UserModel.class);
            binding.SenderName.setText(Sender.getUserInfo().get("Name").toString() + " " + Sender.getUserInfo().get("LastName").toString());

            binding.SenderChat.setOnClickListener(view -> {
                Intent chat = new Intent(this, ChatActivity.class);
                chat.putExtra("UserModel", Sender);
                startActivity(chat);
            });

            if(Sender.getUserInfo().get("Profile Picture") != null){
                String url = ((Map)Sender.getUserInfo().get("Profile Picture")).get("url").toString();
                Picasso.get().load(url).into(binding.SenderPhoto);
            }else{
                binding.SenderPhoto.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.profilephotopat));
            }
        });

        db.collection("users").document(model.getReceiverID()).get().addOnSuccessListener(snapshot -> {
            Receiver = snapshot.toObject(UserModel.class);
            binding.ReceiverName.setText(Receiver.getUserInfo().get("Name").toString() + " " + Receiver.getUserInfo().get("LastName").toString());
            binding.ReceiverChat.setOnClickListener(view -> {
                Intent chat = new Intent(this, ChatActivity.class);
                chat.putExtra("UserModel", Receiver);
                startActivity(chat);
            });

            if(Receiver.getUserInfo().get("Profile Picture") != null){
                String url = ((Map)Receiver.getUserInfo().get("Profile Picture")).get("url").toString();
                Picasso.get().load(url).into(binding.ReceiverPhoto);
            }else{
                binding.ReceiverPhoto.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.profilephotopat));
            }
        });


        Query query = db.collection("reports").whereEqualTo("receiverID", model.getReceiverID());
        FirestoreRecyclerOptions<ReportsModel> options = new FirestoreRecyclerOptions.Builder<ReportsModel>()
                .setQuery(query, ReportsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ReportsModel, ReportsViewHolder>(options) {
            @NonNull
            @Override
            public ReportsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operator_single_report, parent, false);
                return new ReportsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReportsViewHolder holder, int position, @NonNull ReportsModel model) {
                holder.ReceiverName.setText(model.getReceiverName());
                holder.SenderName.setText(model.getSenderName());
                holder.ReportReason.setText(model.getReason());
            }
        };
        binding.PreviousReportsList.setHasFixedSize(false);
        binding.PreviousReportsList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        binding.PreviousReportsList.setAdapter(adapter);


        binding.IgnoreReport.setOnClickListener(view -> {
            db.collection("reports").whereEqualTo("receiverID", model.getReceiverID()).whereEqualTo("senderID", model.getSenderID()).whereEqualTo("reason", model.getReason())
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot: snapshotList){
                            snapshot.getReference().update("reviewed", true);
                        }
                        finish();
                    });
        });

        binding.BanAccount.setOnClickListener(view -> {
            WriteBatch batch = db.batch();
            db.collection("users").document(model.getReceiverID()).get().addOnSuccessListener(snapshot -> {
                if(snapshot.contains("Advertised")){
                    batch.update(snapshot.getReference(), "Advertised", false);
                }
                batch.update(snapshot.getReference(), "Banned", true);
                db.collection("reports").whereEqualTo("receiverID", model.getReceiverID())
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot snapshot1: snapshotList){
                                batch.update(snapshot1.getReference(),"reviewed", true);
                            }
                            batch.commit();
                            finish();
                        });
            });

        });

        binding.BanDevice.setOnClickListener(view -> {
            db.collection("users").document(model.getReceiverID()).get().addOnSuccessListener(snapshot -> {
                String deviceID = snapshot.getString("DeviceID");
                HashMap<String, Object> BannedDevice = new HashMap<>();
                BannedDevice.put("DeviceID", deviceID);
                BannedDevice.put("Banned", true);
                db.collection("banned").document(deviceID).set(BannedDevice);
                snapshot.getReference().update("Banned", true);
                db.collection("reports").whereEqualTo("receiverID", model.getReceiverID())
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot Querysnapshot: snapshotList){
                                Querysnapshot.getReference().update("reviewed", true);
                            }
                            finish();
                        });
            });
        });
    }


    private static class ReportsViewHolder extends RecyclerView.ViewHolder {

        private final TextView SenderName;
        private final TextView ReceiverName;
        private final TextView ReportReason;

        public ReportsViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderName= itemView.findViewById(R.id.SenderName);
            ReceiverName= itemView.findViewById(R.id.ReceiverName);
            ReportReason = itemView.findViewById(R.id.ReportReason);
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