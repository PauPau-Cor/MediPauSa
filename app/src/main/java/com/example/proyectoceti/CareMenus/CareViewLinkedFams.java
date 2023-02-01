package com.example.proyectoceti.CareMenus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCareViewLinkedFamsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class CareViewLinkedFams extends Fragment {
    private FirebaseFirestore db;
    private FragmentCareViewLinkedFamsBinding binding;
    private FirestoreRecyclerAdapter adapter;

    public CareViewLinkedFams() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding= FragmentCareViewLinkedFamsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String careUserID = mAuth.getUid();
        db.collection("users").document(careUserID).get().addOnSuccessListener(documentSnapshot -> {
            List<UserModel> FamUsers = (List<UserModel>)documentSnapshot.get("VinculatedFam");
            if(FamUsers!=null){
                if(!FamUsers.isEmpty()){
                    Query query = db.collection("users").whereIn("UserInfo.UserID", FamUsers);
                    FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                            .setQuery(query, UserModel.class)
                            .build();
                    adapter = new FirestoreRecyclerAdapter<UserModel, CareViewLinkedFamsHolder>(options) {
                        @NonNull
                        @Override
                        public CareViewLinkedFamsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linked_fam_single, parent, false);
                            return new CareViewLinkedFamsHolder(view);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull CareViewLinkedFamsHolder holder, int position, @NonNull UserModel model) {
                            Log.d("tag", "token " + model.getFCMToken());
                            holder.FamName.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName"));
                            holder.itemView.setOnClickListener(view -> {
                                Intent intent = new Intent(getActivity(), CareViewSingleLinkedFam.class);
                                intent.putExtra("UserModel", model);
                                startActivity(intent);
                            });
                        }
                    };

                    binding.CareViewLinkedFamList.setHasFixedSize(false);
                    binding.CareViewLinkedFamList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
                    binding.CareViewLinkedFamList.setAdapter(adapter);
                    adapter.startListening();
                }
            }
        });


    }

    private static class CareViewLinkedFamsHolder extends RecyclerView.ViewHolder {

        private final TextView FamName;

        public CareViewLinkedFamsHolder(@NonNull View itemView) {
            super(itemView);
            FamName = itemView.findViewById(R.id.LinkedFamName);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }
}