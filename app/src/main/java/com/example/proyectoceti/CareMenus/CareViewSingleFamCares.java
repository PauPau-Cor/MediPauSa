package com.example.proyectoceti.CareMenus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.RatingModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCareViewSingleFamCaresBinding;
import com.example.proyectoceti.databinding.RatingDialogBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CareViewSingleFamCares extends Fragment {
    private FragmentCareViewSingleFamCaresBinding binding;
    private UserModel model;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String CareUserID;
    private FirestoreRecyclerAdapter adapter;

    public CareViewSingleFamCares() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding= FragmentCareViewSingleFamCaresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ((CareViewSingleLinkedFam)getActivity()).model;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        CareUserID = mAuth.getUid();
        Query query = db.collection("linkedusers").whereEqualTo("famUserID", model.getUserInfo().get("UserID")).whereNotEqualTo("careUserID", CareUserID);
        FirestoreRecyclerOptions<LinkedUsersModel> options = new FirestoreRecyclerOptions.Builder<LinkedUsersModel>()
                .setQuery(query, LinkedUsersModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<LinkedUsersModel, SingleCareViewHolder>(options) {
            @NonNull
            @Override
            public SingleCareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.care_view_single_linked_fam_care, parent, false);
                return new SingleCareViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onBindViewHolder(@NonNull SingleCareViewHolder holder, int position, @NonNull LinkedUsersModel model) {
                holder.SingleCareName.setText(model.getName());
                Picasso.get().load(model.getProfilePicture().url).into(holder.SingleCarePhoto);

                holder.SingleCareRate.setOnClickListener(view1 -> {
                    RateCare(model);
                });

                holder.SingleCareChat.setOnClickListener(view1 -> {
                    db.collection("users").document(model.getCareUserID()).get().addOnSuccessListener(snapshot -> {
                        UserModel user = snapshot.toObject(UserModel.class);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("UserModel", user);
                        startActivity(intent);
                    });
                });
            }
        };
        binding.CareViewLinkedFamCareList.setHasFixedSize(false);
        binding.CareViewLinkedFamCareList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.CareViewLinkedFamCareList.setAdapter(adapter);
    }

    private void RateCare(LinkedUsersModel model) {
        LinkedUsersModel me = ((CareViewSingleLinkedFam)getActivity()).linkedUsersModel;
        if(me.getShiftsWorked()<3 && model.getShiftsWorked()<3){
            Toast.makeText(getContext(), "Ambos cuidadores deben haber trabajado por lo menos 3 turnos para poder calificarse entre sí", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("ratings").whereEqualTo("receiverID", model.getCareUserID()).whereEqualTo("senderID", me.getCareUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            if(!snapshotList.isEmpty()){
                Toast.makeText(getContext(), "No puedes calificar al mismo usuario dos veces", Toast.LENGTH_SHORT).show();
                return;
            }
            Dialog dialog = new Dialog(getContext());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            RatingDialogBinding dialogBinding = RatingDialogBinding.inflate(getLayoutInflater());
            dialog.setContentView(dialogBinding.getRoot());
            dialog.show();
            dialogBinding.RatingDialogButton.setOnClickListener(view -> {
                Double Rating = (double) dialogBinding.RatingDialogRB.getRating();
                DocumentReference UserDocument = db.collection("users").document(model.getCareUserID());
                UserDocument.get().addOnSuccessListener(documentSnapshot -> {
                    Double CurrentRating = documentSnapshot.getDouble("Rating");
                    Double RatingQuantity = documentSnapshot.getDouble("RatingQuantity");
                    Double NewRating = (CurrentRating * RatingQuantity + Rating)/ (RatingQuantity+1);
                    UserDocument.update("Rating", NewRating, "RatingQuantity", RatingQuantity + 1);
                });
                RatingModel newRatingModel = new RatingModel(dialogBinding.RatingCritique.getEditText().getText().toString(), model.getCareUserID(), me.getCareUserID(), Rating);
                db.collection("ratings").add(newRatingModel);
                dialog.dismiss();
            });
        });
    }


    class SingleCareViewHolder extends RecyclerView.ViewHolder{
        private final TextView SingleCareName;
        private final ImageView SingleCarePhoto;
        private final MaterialButton SingleCareChat;
        private final MaterialButton SingleCareRate;

        public SingleCareViewHolder(@NonNull View itemView) {
            super(itemView);
            SingleCareName= itemView.findViewById(R.id.FamLinkedCareName);
            SingleCarePhoto= itemView.findViewById(R.id.FamLinkedCarePhoto);
            SingleCareChat= itemView.findViewById(R.id.BTFamLinkedCareChat);
            SingleCareRate= itemView.findViewById(R.id.BTFamLinkedCareRate);
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