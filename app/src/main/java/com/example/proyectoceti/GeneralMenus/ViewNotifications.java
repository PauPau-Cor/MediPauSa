package com.example.proyectoceti.GeneralMenus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.NotificationModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ViewNotifications extends AppCompatActivity {

    private FirebaseFirestore db;
    private String UserId;
    private FirebaseAuth mAuth;

    private RecyclerView NotificationList;
    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notifications);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        UserId = mAuth.getCurrentUser().getUid();

        NotificationList = findViewById(R.id.NotifList);

        Query query = db.collection("notifications").whereEqualTo("receiverID", UserId).orderBy("timeStamp", Query.Direction.DESCENDING).limit(99);
        FirestoreRecyclerOptions<NotificationModel> options = new FirestoreRecyclerOptions.Builder<NotificationModel>()
                .setQuery(query, NotificationModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<NotificationModel, NotificationViewHolder>(options) {
            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single, parent, false);
                return new NotificationViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull NotificationModel model) {
                holder.NotifTitle.setText(model.getTitle());
                holder.NotifBody.setText(model.getBody());
                String date = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(model.getTimeStamp());
                holder.NotifTime.setText(date);
            }
        };
        NotificationList.setHasFixedSize(false);
        NotificationList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        NotificationList.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                DocumentSnapshot snapshot = (DocumentSnapshot) adapter.getSnapshots().getSnapshot(viewHolder.getAbsoluteAdapterPosition());
                snapshot.getReference().delete();
            }
        }).attachToRecyclerView(NotificationList);


    }

    private static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final TextView NotifTitle;
        private final TextView NotifBody;
        private final TextView NotifTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            NotifTitle = itemView.findViewById(R.id.NotifTitle);
            NotifBody = itemView.findViewById(R.id.NotifBody);
            NotifTime = itemView.findViewById(R.id.NotifTime);
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