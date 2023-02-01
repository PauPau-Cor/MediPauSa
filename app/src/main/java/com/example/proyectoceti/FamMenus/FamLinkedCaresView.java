package com.example.proyectoceti.FamMenus;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.BuildConfig;
import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FamLinkedCaresView extends Fragment {
    RecyclerView CareRecView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String FamUserID;
    private FirestoreRecyclerAdapter adapter;
    private static final String YOUR_CLIENT_ID = BuildConfig.PAYPAL_KEY;

    public FamLinkedCaresView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PayPalCheckout.setConfig(new CheckoutConfig(
                getActivity().getApplication(),
                YOUR_CLIENT_ID,
                Environment.SANDBOX,
                CurrencyCode.MXN,
                UserAction.PAY_NOW
        ));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fam_linked_cares_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FamUserID = mAuth.getUid();
        CareRecView = getView().findViewById(R.id.FamViewLinkedCareList);
        Query query = db.collection("linkedusers").whereEqualTo("famUserID", FamUserID);
        FirestoreRecyclerOptions<LinkedUsersModel> options = new FirestoreRecyclerOptions.Builder<LinkedUsersModel>()
                .setQuery(query, LinkedUsersModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<LinkedUsersModel, SingleCareViewHolder>(options) {
            @NonNull
            @Override
            public SingleCareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fam_view_linked_care_single, parent, false);
                return new SingleCareViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onBindViewHolder(@NonNull SingleCareViewHolder holder, int position, @NonNull LinkedUsersModel model) {
                holder.SingleCareName.setText(model.getName());
                holder.SingleCareAccSalary.setText(String.valueOf(model.getAccumulatedSalary()));
                Picasso.get().load(model.getProfilePicture().url).into(holder.SingleCarePhoto);
                holder.Active.setChecked(model.isActive());
                if(!holder.Active.isChecked()){
                    holder.Active.setEnabled(false);
                }

                holder.Active.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(!compoundButton.isPressed()) {
                        return;
                    }
                    if(!b){
                        db.collection("linkedusers").whereEqualTo("careUserID", model.getCareUserID()).whereEqualTo("famUserID",model.getFamUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot snapshot: snapshotList){
                                holder.Active.setEnabled(false);
                                snapshot.getReference().update("active", false);
                                db.collection("users").document(model.getCareUserID()).get().addOnSuccessListener(snapshot1 -> {
                                    Utilities.OfflineNotification(model.getCareUserID(), "Anulación de turno", "Un encargado ha anulado tu turno");
                                    if(snapshot1.getString("FCM Token") !=null){
                                        FCMSend.pushNotif(getContext(),snapshot1.getString("FCM Token"), "Anulación de turno", "Un encargado ha anulado tu turno");
                                    }
                                });
                            }
                        });

                    }

                });
                db.collection("users").document(model.getCareUserID()).get().addOnSuccessListener(document -> {
                    UserModel user = document.toObject(UserModel.class);
                    holder.SingleCareChat.setOnClickListener(view1 -> {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("UserModel", user);
                        startActivity(intent);
                    });

                    holder.SingleCareInfo.setOnClickListener(view1 -> {
                        Intent intent = new Intent(getActivity(), FamViewSingleLinkedCare.class);
                        intent.putExtra("LinkedUserModel", model);
                        intent.putExtra("UserModel",user);
                        startActivity(intent);
                    });
                });
            }
        };
        CareRecView.setHasFixedSize(false);
        CareRecView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        CareRecView.setAdapter(adapter);
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

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
    }

    class SingleCareViewHolder extends RecyclerView.ViewHolder {
        private TextView SingleCareName;
        private TextView SingleCareAccSalary;
        private MaterialButton SingleCareInfo;
        private MaterialButton SingleCareChat;
        private ImageView SingleCarePhoto;
        private SwitchCompat Active;

        public SingleCareViewHolder(@NonNull View itemView) {
            super(itemView);
            SingleCareAccSalary = itemView.findViewById(R.id.FamLinkedCareAccSalary);
            SingleCareName = itemView.findViewById(R.id.FamLinkedCareName);
            SingleCarePhoto = itemView.findViewById(R.id.FamLinkedCarePhoto);
            SingleCareChat = itemView.findViewById(R.id.BTFamLinkedCareChat);
            SingleCareInfo = itemView.findViewById(R.id.BTFamLinkedCareInfo);
            Active = itemView.findViewById(R.id.SwitchActive);
        }
    }
}