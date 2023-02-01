package com.example.proyectoceti.GeneralMenus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectoceti.ClassesAndModels.ChatMessageModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityChatRecentConvosBinding;
import com.example.proyectoceti.databinding.ChatRecentChatsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChatRecentConvos extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String UserID;
    private List<ChatMessageModel> Convos;
    private ActivityChatRecentConvosBinding binding;
    private RecentConvosAdapter ConvosAdapter;
    private FirebaseFirestore db;
    private String Type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Type = getIntent().getStringExtra("UserType");
        if(Type.equals("fam")){
            setTheme(R.style.Theme_ProyectoCetiFam);
        }else{
            setTheme(R.style.Theme_ProyectoCetiCareTaker);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityChatRecentConvosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        listenConvos();
    }

    private void init(){
        Convos = new ArrayList<>();
        ConvosAdapter = new RecentConvosAdapter(Convos);
        binding.RecentChatsList.setAdapter(ConvosAdapter);
        db= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
    }

    private void listenConvos(){
        db.collection("conversations").whereEqualTo("Sender ID", UserID).addSnapshotListener(eventListener);
        db.collection("conversations").whereEqualTo("Receiver ID", UserID).addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if(error != null){
            return;
        }
        if(value!= null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString("Sender ID");
                    String receiverId = documentChange.getDocument().getString("Receiver ID");
                    ChatMessageModel chatMessage = new ChatMessageModel();
                    chatMessage.setSenderID(senderId);
                    chatMessage.setReceiverID(receiverId);
                    if(UserID.equals(senderId)){
                        chatMessage.setPFP(documentChange.getDocument().getString("Receiver pfp"));
                        chatMessage.setConvoName(documentChange.getDocument().getString("Receiver Name"));
                        chatMessage.setConvoID(documentChange.getDocument().getString("Receiver ID"));
                    }else{
                        chatMessage.setPFP(documentChange.getDocument().getString("Sender pfp"));
                        chatMessage.setConvoName(documentChange.getDocument().getString("Sender Name"));
                        chatMessage.setConvoID(documentChange.getDocument().getString("Sender ID"));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString("Last Message"));
                    chatMessage.setDateObject(documentChange.getDocument().getDate("TimeStamp"));
                    Convos.add(chatMessage);
                }else if(documentChange.getType()== DocumentChange.Type.MODIFIED){
                    for(int i = 0; i < Convos.size(); i++){
                        String senderId = documentChange.getDocument().getString("Sender ID");
                        String receiverId = documentChange.getDocument().getString("Receiver ID");
                        if(Convos.get(i).getSenderID().equals(senderId) && Convos.get(i).getReceiverID().equals(receiverId)){
                            Convos.get(i).setMessage(documentChange.getDocument().getString("Last Message"));
                            Convos.get(i).setDateObject(documentChange.getDocument().getDate("TimeStamp"));
                            break;
                        }
                    }

                }
            }
            Collections.sort(Convos, (obj1, obj2) -> obj2.getDateObject().compareTo(obj1.getDateObject()));
            ConvosAdapter.notifyDataSetChanged();
            binding.RecentChatsList.smoothScrollToPosition(0);
            binding.RecentChatsList.setVisibility(View.VISIBLE);
            binding.RecentChatsPb.setVisibility(View.GONE);
        }
    };

    private class RecentConvosAdapter extends RecyclerView.Adapter<RecentConvosAdapter.ConvoViewHolder>{
        private final List<ChatMessageModel> ConvosList;

        public RecentConvosAdapter(List<ChatMessageModel> convosList) {
            this.ConvosList = convosList;
        }

        @NonNull
        @Override
        public ConvoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ConvoViewHolder(
                    ChatRecentChatsBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false
                    )
            );
        }

        @Override
        public void onBindViewHolder(@NonNull ConvoViewHolder holder, int position) {
            holder.setData(ConvosList.get(position));
        }

        @Override
        public int getItemCount() {
            return ConvosList.size();
        }

        class ConvoViewHolder extends RecyclerView.ViewHolder{
            ChatRecentChatsBinding binding;

            ConvoViewHolder(ChatRecentChatsBinding chatRecentChatsBinding){
                super(chatRecentChatsBinding.getRoot());
                binding= chatRecentChatsBinding;
            }

            void setData(ChatMessageModel chatMessage){
                if(chatMessage.getPFP() == null){
                    binding.RecentChatsPfp.setImageDrawable(getDrawable(R.drawable.profilephotopat));
                }else{
                    Picasso.get().load(chatMessage.getPFP()).into(binding.RecentChatsPfp);
                }
                binding.RecentChatsName.setText(chatMessage.getConvoName());
                binding.RecentChatsMessage.setText(chatMessage.getMessage());
                binding.getRoot().setOnClickListener(view -> {
                    String chatUserID = chatMessage.getConvoID();
                    DocumentReference documentReference= db.collection("users").document(chatUserID);
                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                        UserModel chatUser = new UserModel();
                        chatUser.setUserType(documentSnapshot.getString("UserType"));
                        chatUser.setFCMToken(documentSnapshot.getString("FCM Token"));
                        chatUser.setUserInfo((HashMap<String, Object>) documentSnapshot.get("UserInfo"));
                        Intent intent= new Intent(ChatRecentConvos.this, ChatActivity.class);
                        intent.putExtra("UserModel", chatUser);
                        intent.putExtra("UserType", Type);
                        startActivity(intent);
                    });
                });
            }
        }
    }
}