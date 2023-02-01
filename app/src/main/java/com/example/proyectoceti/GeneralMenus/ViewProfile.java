package com.example.proyectoceti.GeneralMenus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.RatingModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityViewProfileBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class ViewProfile extends AppCompatActivity {

    private ActivityViewProfileBinding binding;
    private UserModel user;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent receive = getIntent();
        user = receive.getParcelableExtra("UserModel");
        db = FirebaseFirestore.getInstance();

        binding.Name.setText(user.getUserInfo().get("Name").toString() + " " + user.getUserInfo().get("LastName"));
        if(user.getUserInfo().get("Profile Picture")!= null){
            String url = ((Map<String, Object>)user.getUserInfo().get("Profile Picture")).get("url").toString();
            Picasso.get().load(url).into(binding.PFP);
        }
        binding.UserRating.setRating(user.getRating());
        binding.RatingQuan.setText("(" + user.getRatingQuantity() + ")");
        Query query = db.collection("ratings").whereEqualTo("receiverID", user.getUserInfo().get("UserID").toString());
        FirestoreRecyclerOptions<RatingModel> options = new FirestoreRecyclerOptions.Builder<RatingModel>()
                .setQuery(query, RatingModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<RatingModel, RatingViewHolder>(options) {
            @NonNull
            @Override
            public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_prof_review, parent, false);
                return new RatingViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull RatingViewHolder holder, int position, @NonNull RatingModel model) {
                if(model.getCritique() != null){
                    holder.Critique.setVisibility(View.VISIBLE);
                    holder.Critique.setText(model.getCritique());
                }
                holder.Rating.setRating(model.getRating().floatValue());
            }
        };
        binding.ReviewsList.setHasFixedSize(false);
        binding.ReviewsList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(ViewProfile.this, LinearLayoutManager.VERTICAL,false));
        binding.ReviewsList.setAdapter(adapter);
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

    private static class RatingViewHolder extends RecyclerView.ViewHolder {

        private final RatingBar Rating;
        private final TextView Critique;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            Rating = itemView.findViewById(R.id.Rating);
            Critique = itemView.findViewById(R.id.Review);
        }
    }
}