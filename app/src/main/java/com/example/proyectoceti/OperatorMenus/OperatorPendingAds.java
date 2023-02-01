package com.example.proyectoceti.OperatorMenus;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.DialogRejectBinding;
import com.example.proyectoceti.databinding.FragmentOperatorPendingAdsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OperatorPendingAds extends Fragment {

    private FragmentOperatorPendingAdsBinding binding;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;

    public OperatorPendingAds() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOperatorPendingAdsBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        Query query = db.collection("users").whereEqualTo("Approved", "pending");
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<UserModel, OpeAdsViewHolder>(options) {
            @NonNull
            @Override
            public OpeAdsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ope_view_ad, parent, false);
                return new OpeAdsViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull OpeAdsViewHolder holder, int position, @NonNull UserModel model) {
                holder.Name.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName"));
                String url = ((Map)model.getUserInfo().get("Profile Picture")).get("url").toString();
                Picasso.get().load(url).into(holder.Photo);
                holder.Rating.setRating(model.getRating());
                holder.Expertise.setText(model.getExpertise());
                holder.Sex.setText(model.getUserInfo().get("Sex").toString());
                holder.Mail.setText(model.getUserInfo().get("Mail").toString());
                try {
                    holder.Address.setText(Encrypter.Decrypt(((Map)model.getUserInfo().get("Location")).get("address").toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.Salary.setText("$"+(model.getUserInfo().get("Salary").toString()));
                holder.RatingQuant.setText(String.valueOf(model.getRatingQuantity()));

                holder.Approve.setOnClickListener(view1 -> {
                    db.collection("users").document(model.getUserInfo().get("UserID").toString()).update("Approved", "approved");
                    if(model.getFCMToken() != null){
                        FCMSend.pushNotif(getContext(), model.getFCMToken(),"Revision de promocional", "Tu promocional ha sido aceptado");
                    }
                    Utilities.OfflineNotification(model.getUserInfo().get("UserID").toString(), "Revision de promocional", "Tu promocional ha sido aceptado");
                });

                holder.Reject.setOnClickListener(view1 -> {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    DialogRejectBinding dialogBinding = DialogRejectBinding.inflate(getLayoutInflater());
                    dialog.setContentView(dialogBinding.getRoot());
                    dialog.show();
                    dialogBinding.rejectDialogSend.setOnClickListener(view -> {
                        String reason = "Razon: " + dialogBinding.rejectDialogReason.getEditText().getText().toString();
                        if (reason.isEmpty()) {
                            Toast.makeText(getContext(), "Debe especificar una razon", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        db.collection("users").document(model.getUserInfo().get("UserID").toString()).update("Approved", "rejected", "RejectReason", reason);
                        if(model.getFCMToken() != null){
                            FCMSend.pushNotif(getContext(), model.getFCMToken(),"Revision de promocional", "Tu promocional ha sido rechazado: " + reason);
                        }
                        Utilities.OfflineNotification(model.getUserInfo().get("UserID").toString(), "Revision de promocional", "Tu promocional ha sido rechazado: " + reason);
                        dialog.dismiss();
                    });
                });
            }
        };

        binding.UsersAdsList.setHasFixedSize(false);
        binding.UsersAdsList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.UsersAdsList.setAdapter(adapter);
    }

    private static class OpeAdsViewHolder extends RecyclerView.ViewHolder {

        private final TextView Name;
        private final TextView RatingQuant;
        private final TextView Salary;
        private final TextView Expertise;
        private final TextView Sex;
        private final TextView Address;
        private final TextView Mail;
        private final ImageView Photo;
        private final MaterialButton Approve;
        private final MaterialButton Reject;
        private final RatingBar Rating;

        public OpeAdsViewHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.AdName);
            RatingQuant = itemView.findViewById(R.id.AdRatingQuantity);
            Salary = itemView.findViewById(R.id.AdSalary);
            Expertise = itemView.findViewById(R.id.AdExpertise);
            Sex = itemView.findViewById(R.id.AdSex);
            Address = itemView.findViewById(R.id.AdAddress);
            Mail = itemView.findViewById(R.id.AdMail);
            Photo = itemView.findViewById(R.id.AdPhoto);
            Approve = itemView.findViewById(R.id.ApproveAd);
            Reject = itemView.findViewById(R.id.RejectAd);
            Rating = itemView.findViewById(R.id.AdRating);
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