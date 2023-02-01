package com.example.proyectoceti.OperatorMenus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class OperatorPendingDocuments extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView UserDocsList;
    private FirestoreRecyclerAdapter adapter;


    public OperatorPendingDocuments() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operator_pending_documents,container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        UserDocsList = getView().findViewById(R.id.UsersDocsList);
        Query query = db.collection("users").whereEqualTo("UserInfo.ExpertiseFile.Validated", false);
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<UserModel, UsersDocsViewHolder>(options) {
            @NonNull
            @Override
            public UsersDocsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.operator_single_document_user, parent, false);
                return new UsersDocsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersDocsViewHolder holder, int position, @NonNull UserModel model) {
                holder.UserName.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName"));
                String url = ((Map)model.getUserInfo().get("Profile Picture")).get("url").toString();
                Picasso.get().load(url).into(holder.UserPhoto);
                holder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), OperatorViewUserForDoc.class);
                    intent.putExtra("UserModel", model);
                    startActivity(intent);
                });
            }
        };
        UserDocsList.setHasFixedSize(false);
        UserDocsList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        UserDocsList.setAdapter(adapter);
    }

    private static class UsersDocsViewHolder extends RecyclerView.ViewHolder {

        private final TextView UserName;
        private final ImageView UserPhoto;

        public UsersDocsViewHolder(@NonNull View itemView) {
            super(itemView);
            UserName= itemView.findViewById(R.id.DocUserName);
            UserPhoto = itemView.findViewById(R.id.DocUserPhoto);
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