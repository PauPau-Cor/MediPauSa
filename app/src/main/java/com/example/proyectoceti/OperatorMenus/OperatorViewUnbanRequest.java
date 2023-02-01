package com.example.proyectoceti.OperatorMenus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ReportsModel;
import com.example.proyectoceti.ClassesAndModels.UnbanRequestModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityOperatorViewUnbanRequestBinding;
import com.example.proyectoceti.databinding.DialogRejectBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OperatorViewUnbanRequest extends AppCompatActivity {

    ActivityOperatorViewUnbanRequestBinding binding;
    private UnbanRequestModel model;
    private UserModel Sender;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperatorViewUnbanRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        Intent receiver = getIntent();
        model= receiver.getParcelableExtra("UnbanModel");

        db.collection("users").document(model.getUserID()).get().addOnSuccessListener(snapshot -> {
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

        Query query = db.collection("reports").whereEqualTo("receiverID", model.getUserID());
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
            Dialog dialog = new Dialog(this);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DialogRejectBinding dialogBinding = DialogRejectBinding.inflate(getLayoutInflater());
            dialog.setContentView(dialogBinding.getRoot());
            dialog.show();
            dialogBinding.rejectDialogSend.setOnClickListener(view1 -> {
                String reason = dialogBinding.rejectDialogReason.getEditText().getText().toString();
                if(reason.isEmpty()) {
                    Toast.makeText(this, "Debe especificar una razon", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.collection("unbanrequests").document(model.getUserID()).update("reviewed", true).addOnSuccessListener(unused -> {
                    if(Sender.getFCMToken()!=null){
                        FCMSend.pushNotif(OperatorViewUnbanRequest.this,Sender.getFCMToken(), "Tu petición de desbaneo ha sido rechazada", "Tu petición de desbaneo ha sido rechazada por un operador por: " + reason);
                    }
                    Utilities.OfflineNotification(model.getUserID(),"Tu petición de desbaneo ha sido rechazada", "Tu petición de desbaneo ha sido rechazada por un operador por: " + reason);
                    finish();
                });
            });

        });

        binding.UnbanAccount.setOnClickListener(view -> {
            db.collection("users").document(model.getUserID()).update("Banned", false).addOnSuccessListener(unused -> {
                if(Sender.getFCMToken()!=null){
                    FCMSend.pushNotif(OperatorViewUnbanRequest.this,Sender.getFCMToken(), "Tu cuenta ha sido desbaneada", "Tu cuenta ha sido desbaneada por un operador.");
                }
                Utilities.OfflineNotification(model.getUserID(),"Tu cuenta ha sido desbaneada", "Tu cuenta ha sido desbaneada por un operador.");
                db.collection("unbanrequests").document(model.getUserID()).update("reviewed", true).addOnSuccessListener(unused1 -> finish());
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