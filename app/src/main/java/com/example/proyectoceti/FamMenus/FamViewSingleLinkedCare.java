package com.example.proyectoceti.FamMenus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.DayAndTime;
import com.example.proyectoceti.ClassesAndModels.LinkedUsersModel;
import com.example.proyectoceti.ClassesAndModels.RatingModel;
import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.Misc.FCMSend;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.databinding.ActivityFamViewSingleLinkedCareBinding;
import com.example.proyectoceti.databinding.LinkedCareShiftRowBinding;
import com.example.proyectoceti.databinding.RatingDialogBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.Order;
import com.paypal.checkout.order.Payee;
import com.paypal.checkout.order.PurchaseUnit;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FamViewSingleLinkedCare extends AppCompatActivity {
    ActivityFamViewSingleLinkedCareBinding binding;
    LinkedUsersModel linkedUserModel;
    private UserModel user;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamViewSingleLinkedCareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        Intent receive = getIntent();
        linkedUserModel = receive.getParcelableExtra("LinkedUserModel");
        user = receive.getParcelableExtra("UserModel");

        binding.FamViewSingleCarename.setText(linkedUserModel.getName());
        binding.FamViewSingleCareAccSalary.setText(String.valueOf(linkedUserModel.getAccumulatedSalary()));
        Picasso.get().load(linkedUserModel.getProfilePicture().url).into(binding.FamViewSingleCarePhoto);
        binding.FamViewSingleCareSalary.setText(String.valueOf(linkedUserModel.getSalary()));
        binding.FamViewSingleCareShiftsWorked.setText(String.valueOf(linkedUserModel.getHoursWorked()/60));
        List<DayAndTime> shifts = linkedUserModel.getShifts();
        if(shifts!=null) for (DayAndTime shift: shifts) {
            addView(shift);
        }

        binding.FamViewSingleCareUnlink.setOnClickListener(view -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(FamViewSingleLinkedCare.this);
            builder.setMessage("¿Está segur@ que desvincular este cuidador?")
                    .setPositiveButton("Si", (dialogInterface, i) -> unlink())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        });

        binding.FamViewSingleCareRate.setOnClickListener(view -> {
            if(linkedUserModel.isCareRated()){
                Toast.makeText(this, "No puede calificar al mismo usuario dos veces", Toast.LENGTH_SHORT).show();
                return;
            }
            if(linkedUserModel.getShiftsWorked()<1){
                Toast.makeText(this, "No puede calificar a un usuario que no ha trabajado un turno", Toast.LENGTH_SHORT).show();
                return;
            }
            RateCare(false);
        });

        binding.FamViewSingleCareEdit.setOnClickListener(view -> {
            Intent intent = new Intent(FamViewSingleLinkedCare.this, FamEditLinkedCare.class);
            intent.putExtra("LinkedUserModel", linkedUserModel);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String mail = user.getUserInfo().get("Mail").toString();
        String AccSalary = String.valueOf(linkedUserModel.getAccumulatedSalary());
        binding.payPalButton.setup(
                createOrderActions -> {
                    ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                    purchaseUnits.add(
                            new PurchaseUnit.Builder()
                                    .amount(
                                            new Amount.Builder()
                                                    .currencyCode(CurrencyCode.MXN)
                                                    .value(AccSalary)
                                                    .build()
                                    ).payee(
                                            new Payee(
                                                    mail, null
                                            )
                                    )
                                    .build()
                    );
                    Order order = new Order(
                            OrderIntent.CAPTURE,
                            new AppContext.Builder()
                                    .userAction(UserAction.PAY_NOW)
                                    .build(),
                            purchaseUnits
                    );
                    createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                },
                approval -> approval.getOrderActions().capture(result -> {
                    Toast.makeText(FamViewSingleLinkedCare.this, "Pago mandado exitosamente", Toast.LENGTH_SHORT).show();
                    PaymentDone(linkedUserModel,user);
                })
        );
    }


    private void addView(DayAndTime shift) {
        LinkedCareShiftRowBinding RowBinding = LinkedCareShiftRowBinding.inflate(getLayoutInflater());
        final View frequencyList = RowBinding.getRoot();
        RowBinding.LinkedCareDay.setText(shift.getDay());
        RowBinding.LinkedCareHours.setText(shift.getStartTime() + " " + shift.getEndTime());
        binding.LayoutLinkedCareShifts.addView(frequencyList);
    }

    private void PaymentDone(LinkedUsersModel model, UserModel user){
        db.collection("linkedusers").whereEqualTo("careUserID", model.getCareUserID()).whereEqualTo("famUserID",model.getFamUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                snapshot.getReference().update("accumulatedSalary", 0, "hoursWorked", 0);
            }
            if(user.getFCMToken()!=null){
                FCMSend.pushNotif(FamViewSingleLinkedCare.this,user.getFCMToken(), "Envio de paga","Un cuidador te ha mandado tu paga por paypal, revisa tu cuenta de paypal.");
            }
            Utilities.OfflineNotification(user.getUserInfo().get("UserID").toString(),"Envio de paga","Un cuidador te ha mandado tu paga por paypal, revisa tu cuenta de paypal.");
        });
    }

    private void unlink(){
        RateCare(true);
        db.collection("users").document(linkedUserModel.getCareUserID()).update("VinculatedFam", FieldValue.arrayRemove(linkedUserModel.getFamUserID()));
        db.collection("users").document(linkedUserModel.getFamUserID()).update("VinculatedCare", FieldValue.arrayRemove(linkedUserModel.getCareUserID()));
        db.collection("linkedusers").whereEqualTo("careUserID", linkedUserModel.getCareUserID()).whereEqualTo("famUserID", linkedUserModel.getFamUserID())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        snapshot.getReference().delete();
                    }
                });
        db.collection("patients").whereEqualTo("FamUserID", linkedUserModel.getFamUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                snapshot.getReference().update("CareUserIDs", FieldValue.arrayRemove(linkedUserModel.getCareUserID()));
            }
        });
        Intent intent = new Intent(FamViewSingleLinkedCare.this, FamMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void RateCare(boolean updater){
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RatingDialogBinding dialogBinding = RatingDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialogBinding.RatingDialogButton.setOnClickListener(view -> {
            Double Rating = (double) dialogBinding.RatingDialogRB.getRating();
            DocumentReference UserDocument = db.collection("users").document(linkedUserModel.getCareUserID());
            UserDocument.get().addOnSuccessListener(documentSnapshot -> {
                Double CurrentRating = documentSnapshot.getDouble("Rating");
                Double RatingQuantity = documentSnapshot.getDouble("RatingQuantity");
                Double NewRating = (CurrentRating * RatingQuantity + Rating)/ (RatingQuantity+1);
                UserDocument.update("Rating", NewRating, "RatingQuantity", RatingQuantity + 1);
            });
            db.collection("linkedusers").whereEqualTo("famUserID", linkedUserModel.getFamUserID()).whereEqualTo("careUserID", linkedUserModel.getCareUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot: snapshotList){
                    snapshot.getReference().update("careRated", true);
                }
            });
            RatingModel newRatingModel = new RatingModel(dialogBinding.RatingCritique.getEditText().getText().toString(), linkedUserModel.getCareUserID(), linkedUserModel.getFamUserID(), Rating);
            if(updater){
                db.collection("ratings").whereEqualTo("receiverID", linkedUserModel.getCareUserID()).whereEqualTo("senderID", linkedUserModel.getFamUserID()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot: snapshotList){
                        snapshot.getReference().set(newRatingModel);
                    }
                });
            }else{
                db.collection("ratings").add(newRatingModel);
                linkedUserModel.setCareRated(true);
            }
            dialog.dismiss();
        });
    }
}