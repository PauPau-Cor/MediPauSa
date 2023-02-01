package com.example.proyectoceti.GeneralMenus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ChatMessageModel;
import com.example.proyectoceti.ClassesAndModels.ReportsModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityChatBinding;
import com.example.proyectoceti.databinding.ChatReceivedMessageBinding;
import com.example.proyectoceti.databinding.ChatSentMessageBinding;
import com.example.proyectoceti.databinding.DialogReportBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private UserModel ReceivedUser;

    private List<ChatMessageModel> chatMessages;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String SenderID;
    private ChatAdapter chatAdapter;
    private StorageReference storageReference;
    private String ConvoID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceivedUserDetails();
        init();
        listenMessages();
    }
    private void loadReceivedUserDetails(){
        ReceivedUser = getIntent().getParcelableExtra("UserModel");
        binding.ChatTextName.setText(ReceivedUser.getUserInfo().get("Name").toString()+ " " + ReceivedUser.getUserInfo().get("LastName").toString());
    }
    private void setListeners(){
        binding.ChatImageBack.setOnClickListener(view -> onBackPressed());

        binding.ChatlayoutSend.setOnClickListener(view -> {
            String chatMessage = binding.ChatInputMessage.getText().toString();
            sendMessage(chatMessage, false, false);
        });

        binding.ChatlayoutAttach.setOnClickListener(view -> showPopUp(view));

        binding.ChatImageDelete.setOnClickListener(view -> DeleteConvo());
        
        binding.ChatImageReport.setOnClickListener(view -> ReportUser());

        binding.ChatImageProfile.setOnClickListener(view -> {
            if(ReceivedUser.getUserType().equals("Operator")){
                Toast.makeText(this, "Este usuario es un operador, no tiene perfil", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ChatActivity.this, ViewProfile.class);
            intent.putExtra("UserModel", ReceivedUser);
            startActivity(intent);
        });
    }

    @SuppressLint("RestrictedApi")
    private void showPopUp(View view){
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.chat_attach_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menu_item -> {
            switch (menu_item.getItemId()) {
                case R.id.AttachImage:
                    SendImage();
                    break;
                case R.id.AttachFile:
                    SendFile();
                    break;
            }
            return true;
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popup.getMenu(), view);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        menuHelper.show();
    }

    private void ReportUser() {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DialogReportBinding dialogBinding = DialogReportBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.reportDialogSend.setOnClickListener(view -> {
            if(TextUtils.isEmpty(dialogBinding.reportDialogReason.getEditText().getText().toString())){
                Toast.makeText(this, "Favor de indicar una razon", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users").document(SenderID).get().addOnSuccessListener(documentSnapshot -> {
                ReportsModel report = new ReportsModel(SenderID, ReceivedUser.getUserInfo().get("UserID").toString()
                        , dialogBinding.reportDialogReason.getEditText().getText().toString(), ReceivedUser.getUserInfo().get("Name").toString() ,  documentSnapshot.getString("UserInfo.Name"), false);

                db.collection("reports").add(report).addOnSuccessListener(documentReference -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Usuario ha sido reportado", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    private void DeleteConvo() {
        if(ConvoID != null){
            db.collection("chats").whereEqualTo("Sender ID", SenderID).whereEqualTo("Receiver ID", ReceivedUser.getUserInfo().get("UserID")).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    snapshot.getReference().delete();
                }
            });
            db.collection("chats").whereEqualTo("Sender ID", ReceivedUser.getUserInfo().get("UserID")).whereEqualTo("Receiver ID", SenderID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    snapshot.getReference().delete();
                }
                db.collection("conversations").document(ConvoID).delete();
                finish();
            });
        }else{
            Toast.makeText(this, "Conversacion vacia", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void init(){
        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        SenderID = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        chatMessages= new ArrayList<>();
        chatAdapter= new ChatAdapter(chatMessages, SenderID);
        binding.ChatRecyclerView.setAdapter(chatAdapter);

    }

    private void sendMessage(String chatMessage, boolean IsImage, boolean IsFile){
        HashMap<String, Object> Message = new HashMap<>();
        Message.put("Sender ID", SenderID);
        Message.put("Receiver ID", ReceivedUser.getUserInfo().get("UserID"));
        Message.put("Message text", chatMessage);
        Message.put("TimeStamp", new Date());
        Message.put("image",IsImage);
        Message.put("file",IsFile);
        db.collection("chats").add(Message);
        String NotifMessage = (IsImage) ? "Imagen" : chatMessage;
        db.collection("users").document(SenderID).get().addOnSuccessListener(documentSnapshot -> {
            Utilities.OfflineNotification(ReceivedUser.getUserInfo().get("UserID").toString(), documentSnapshot.getString("UserInfo.Name") + " " + documentSnapshot.getString("UserInfo.LastName"), NotifMessage);
            if(ReceivedUser.getFCMToken() != null){
                FCMSend.pushNotif(ChatActivity.this, ReceivedUser.getFCMToken(),documentSnapshot.getString("UserInfo.Name") + " " + documentSnapshot.getString("UserInfo.LastName"), NotifMessage);
            }
        });

        if(ConvoID != null){
            updateConvo(chatMessage, IsImage, IsFile);
            binding.ChatInputMessage.setText(null);
        }else{
            HashMap<String, Object> Convo = new HashMap<>();
            Convo.put("Sender ID", SenderID);
            DocumentReference documentReference = db.collection("users").document(SenderID);
            documentReference.get().addOnSuccessListener(documentSnapshot -> {
                Convo.put("Sender Name", documentSnapshot.getString("UserInfo.Name") + " " + documentSnapshot.getString("UserInfo.LastName"));
                Convo.put("Sender pfp", documentSnapshot.get("UserInfo.Profile Picture.url"));
                Convo.put("Receiver ID", ReceivedUser.getUserInfo().get("UserID"));
                Convo.put("Receiver Name", ReceivedUser.getUserInfo().get("Name") + " " + ReceivedUser.getUserInfo().get("LastName"));
                if(ReceivedUser.getUserInfo().get("Profile Picture") != null){
                    Convo.put("Receiver pfp", ((Map)ReceivedUser.getUserInfo().get("Profile Picture")).get("url"));
                }else{
                    Convo.put("Receiver pfp", "https://firebasestorage.googleapis.com/v0/b/proyectoceti-ccb53.appspot.com/o/profilephotocare.png?alt=media&token=c5b9d5a7-17d0-4340-ba8b-d531c7502543");
                }
                if(IsImage){
                    Convo.put("Last Message", "Imagen");
                }else if(IsFile) {
                    Convo.put("Last Message","Archivo");
                }
                else{
                    Convo.put("Last Message",chatMessage);
                }
                Convo.put("TimeStamp", new Date());
                addConvo(Convo);
                binding.ChatInputMessage.setText(null);
            });

        }
    }

    private void SendImage() {
        if(ActivityCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{
            Intent FileBrowse = new Intent();
            FileBrowse.setType("image/*");
            FileBrowse.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(FileBrowse,"IMAGE FILE SELECT"),1000);
        }
    }

    private void SendFile() {
        if(ActivityCompat.checkSelfPermission(ChatActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ChatActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else{
            Intent FileBrowse = new Intent();
            FileBrowse.setType("application/pdf");
            FileBrowse.setAction(FileBrowse.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(FileBrowse,"PDF FILE SELECT"),10);
        }
    }

    private void listenMessages(){
        db.collection("chats").whereEqualTo("Sender ID", SenderID).whereEqualTo("Receiver ID", ReceivedUser.getUserInfo().get("UserID")).addSnapshotListener(eventListener);
        db.collection("chats").whereEqualTo("Sender ID", ReceivedUser.getUserInfo().get("UserID")).whereEqualTo("Receiver ID", SenderID).addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
       if(error != null){
           return;
       }
       if(value!= null){
           int count = chatMessages.size();
           for(DocumentChange documentChange : value.getDocumentChanges()){
               if(documentChange.getType() == DocumentChange.Type.ADDED){
                   ChatMessageModel chatMessage = new ChatMessageModel();
                   chatMessage.setSenderID(documentChange.getDocument().getString("Sender ID"));
                   chatMessage.setReceiverID(documentChange.getDocument().getString("Receiver ID"));
                   chatMessage.setMessage(documentChange.getDocument().getString("Message text"));
                   chatMessage.setDateObject(documentChange.getDocument().getDate("TimeStamp"));
                   chatMessage.setImage(documentChange.getDocument().getBoolean("image"));
                   chatMessage.setFile(documentChange.getDocument().getBoolean("file"));
                   chatMessage.setDateTime(getReadableDateTime(chatMessage.getDateObject()));
                   chatMessages.add(chatMessage);
               }
           }
           Collections.sort(chatMessages, Comparator.comparing(ChatMessageModel::getDateObject));
           if(count==0){
               chatAdapter.notifyDataSetChanged();
           }else {
               chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
               binding.ChatRecyclerView.smoothScrollToPosition(chatMessages.size()-1);
           }
           binding.ChatRecyclerView.setVisibility(View.VISIBLE);
       }
       binding.ChatProgressBar.setVisibility(View.GONE);
       if(ConvoID == null){
           checkForConvo();
       }
    });

    private void addConvo(HashMap<String, Object> conversation){
        db.collection("conversations").add(conversation).addOnSuccessListener(documentReference -> ConvoID = documentReference.getId());
    }

    private void updateConvo(String message, boolean isImage, boolean isFile){
        DocumentReference documentReference= db.collection("conversations").document(ConvoID);
        if(isImage){
            documentReference.update("Last Message", "Imagen", "TimeStamp", new Date());
            return;
        }
        if(isFile){
            documentReference.update("Last Message", "Archivo", "TimeStamp", new Date());
            return;
        }
        documentReference.update("Last Message", message, "TimeStamp", new Date());
    }

    private void checkForConvo(){
        if(chatMessages.size() !=0){
            checkForConvoRemotely(SenderID,ReceivedUser.getUserInfo().get("UserID").toString());
            checkForConvoRemotely(ReceivedUser.getUserInfo().get("UserID").toString(), SenderID);
        }

    }

    private void checkForConvoRemotely(String senderID, String receiverID){
        db.collection("conversations").whereEqualTo("Sender ID", senderID).whereEqualTo("Receiver ID", receiverID).get().addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() >0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            ConvoID = documentSnapshot.getId();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1000){
                String fileID = UUID.randomUUID().toString();
                ContentResolver contentResolver = getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                StorageReference pfpref= storageReference.child("chat/"+ fileID +"."+mime.getExtensionFromMimeType(contentResolver.getType(data.getData())));
                pfpref.putFile(data.getData()).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete()) ;
                    Uri uri = uriTask.getResult();
                    sendMessage(uri.toString(),true, false);
                });
            }
            if(requestCode==10){
                String fileID = UUID.randomUUID().toString();
                StorageReference pfpref= storageReference.child("chat/"+ fileID +".pdf");
                pfpref.putFile(data.getData()).addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete()) ;
                    Uri uri = uriTask.getResult();
                    sendMessage(uri.toString(),false, true);
                });
            }
        }else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private final List<ChatMessageModel> chatMessages;
        private final String senderID;

        public static final int VIEW_TYPE_SENT = 1;
        public static final int VIEW_TYPE_RECEIVED = 2;

        public ChatAdapter(List<ChatMessageModel> chatMessages, String senderID) {
            this.chatMessages = chatMessages;
            this.senderID = senderID;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(viewType == VIEW_TYPE_SENT){
                return new SentMessageViewHolder(
                        ChatSentMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent, false
                        )
                );
            }else{
                return new ReceivedMessageViewHolder(
                        ChatReceivedMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent, false
                        )
                );
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(getItemViewType(position) == VIEW_TYPE_SENT){
                ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
            }else{
                ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return chatMessages.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(chatMessages.get(position).getSenderID().equals(senderID)){
                return VIEW_TYPE_SENT;
            }else{
                return VIEW_TYPE_RECEIVED;
            }
        }

        class SentMessageViewHolder extends RecyclerView.ViewHolder{
            private final ChatSentMessageBinding binding;
            SentMessageViewHolder(ChatSentMessageBinding chatSentMessageBinding){
                super(chatSentMessageBinding.getRoot());
                binding = chatSentMessageBinding;
            }

            void setData(ChatMessageModel chatMessage){
                binding.ChatTextDateTime.setText(chatMessage.getDateTime());
                if(chatMessage.isImage()){
                    binding.MessageImage.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet= new ConstraintSet();
                    constraintSet.clone(binding.constraint);
                    constraintSet.connect(R.id.ChatTextDateTime,constraintSet.TOP, R.id.MessageImage, constraintSet.BOTTOM);
                    constraintSet.connect(R.id.ChatTextDateTime,constraintSet.START, R.id.MessageImage, constraintSet.START);
                    constraintSet.applyTo(binding.constraint);
                    binding.ChatTextMessage.setVisibility(View.GONE);
                    Picasso.get().load(chatMessage.getMessage()).into(binding.MessageImage);
                    return;
                }
                if(chatMessage.isFile()){
                    binding.MessagePDF.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(binding.constraint);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.TOP, R.id.MessagePDF, constraintSet.BOTTOM);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.START, R.id.MessagePDF, constraintSet.START);
                    constraintSet.applyTo(binding.constraint);
                    binding.ChatTextMessage.setVisibility(View.GONE);
                    binding.downloadFile.setOnClickListener(view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setType("application/pdf");
                        intent.setData(Uri.parse(chatMessage.getMessage()));
                        startActivity(intent);
                    });
                    return;
                }
                binding.ChatTextMessage.setText(chatMessage.getMessage());
            }
        }

        class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
            private final ChatReceivedMessageBinding binding;
            ReceivedMessageViewHolder(ChatReceivedMessageBinding chatReceivedMessageBinding){
                super(chatReceivedMessageBinding.getRoot());
                binding=chatReceivedMessageBinding;
            }
            void setData(ChatMessageModel chatMessage){
                binding.ChatTextDateTime.setText(chatMessage.getDateTime());
                if(ReceivedUser.getUserType().equals("Caretaker")){
                    Picasso.get().load(((Map)ReceivedUser.getUserInfo().get("Profile Picture")).get("url").toString()).into(binding.ChatProfilePicture);
                }else{
                    binding.ChatProfilePicture.setImageDrawable(getDrawable(R.drawable.profilephotocare));
                }
                if(chatMessage.isImage()) {
                    binding.MessageImage.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(binding.constraint);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.TOP, R.id.MessageImage, constraintSet.BOTTOM);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.START, R.id.MessageImage, constraintSet.START);
                    constraintSet.connect(R.id.ChatProfilePicture,constraintSet.BOTTOM, R.id.MessageImage, constraintSet.BOTTOM);
                    constraintSet.applyTo(binding.constraint);
                    binding.ChatTextMessage.setVisibility(View.GONE);
                    Picasso.get().load(chatMessage.getMessage()).into(binding.MessageImage);
                    return;
                }
                if(chatMessage.isFile()){
                    binding.MessagePDF.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(binding.constraint);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.TOP, R.id.MessagePDF, constraintSet.BOTTOM);
                    constraintSet.connect(R.id.ChatTextDateTime, constraintSet.START, R.id.MessagePDF, constraintSet.START);
                    constraintSet.connect(R.id.ChatProfilePicture,constraintSet.BOTTOM, R.id.MessagePDF, constraintSet.BOTTOM);
                    constraintSet.applyTo(binding.constraint);
                    binding.ChatTextMessage.setVisibility(View.GONE);
                    binding.downloadFile.setOnClickListener(view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setType("application/pdf");
                        intent.setData(Uri.parse(chatMessage.getMessage()));
                        startActivity(intent);
                    });
                    return;
                }
                binding.ChatTextMessage.setText(chatMessage.getMessage());
            }
        }
    }
}

