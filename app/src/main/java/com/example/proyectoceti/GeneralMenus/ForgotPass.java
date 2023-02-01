package com.example.proyectoceti.GeneralMenus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.Code;
import com.example.proyectoceti.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class ForgotPass extends AppCompatActivity {

    EditText FrgtPassMail, FrgtPassCode;
    Button BtFrgtPassSend, BTFrgtPassCode;
    Code code;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        FrgtPassMail = findViewById(R.id.ForgtPasMail);
        FrgtPassCode = findViewById(R.id.ForgtPasCode);
        BtFrgtPassSend = findViewById(R.id.BTFrgtPasSend);
        BTFrgtPassCode = findViewById(R.id.BTFrgtPasCode);

        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();

        BtFrgtPassSend.setOnClickListener(view -> CheckEmailExists());
        BTFrgtPassCode.setOnClickListener(view -> CheckCode());
    }

    protected void CheckEmailExists(){
        mAuth.fetchSignInMethodsForEmail(FrgtPassMail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.getResult().getSignInMethods().isEmpty()){
                    FrgtPassMail.setError("No tenemos ninguna cuenta con este correo");
                    FrgtPassMail.requestFocus();
                }else{
                    Random rnd = new Random();
                    int number = rnd.nextInt(999999);
                    code = new Code(String.format(Locale.getDefault(), "%06d", number), Calendar.getInstance().getTime(), false);
                    SendEmail();
                }
            }
        });
    }

    protected void SendEmail(){
        try {
            String SenderEmail = "medipausa@gmail.com";
            String ReceiverEmail = FrgtPassMail.getText().toString();
            String PassSenderEmail = "pxsgqrolczgygody";
            String Host= "smtp.gmail.com";

            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", Host);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SenderEmail, PassSenderEmail);
                }
            });
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(ReceiverEmail));
            mimeMessage.setSubject("Cambio de contraseña");
            mimeMessage.setText("Hola!\n" + code.code);
            Thread thread = new Thread(() -> {
                try {
                    Transport.send(mimeMessage);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        Query query = db.collection("users").whereEqualTo("UserInfo.Mail", FrgtPassMail.getText().toString());
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                String UserID = snapshot.getString("UserInfo.UserID");
                DocumentReference DocRef  = db.collection("users").document(UserID);
                Map<String, Object> PassCode=new HashMap<>();
                PassCode.put("PassCode", code);
                DocRef.set(PassCode, SetOptions.merge()).addOnSuccessListener(unused -> Toast.makeText(ForgotPass.this, "Codigo creado", Toast.LENGTH_SHORT).show());
            }
        });
    }



    protected void CheckCode(){
        if(TextUtils.isEmpty(FrgtPassCode.getText().toString())){
            FrgtPassCode.setError("Favor de introducir un codigo");
            FrgtPassCode.requestFocus();
        }else if(FrgtPassCode.getText().length()!=6){
            FrgtPassCode.setError("Codigo debe tener 6 digitos");
            FrgtPassCode.requestFocus();
        }else{
            Query query = db.collection("users").whereEqualTo("PassCode.code", FrgtPassCode.getText().toString());
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                if(snapshotList.isEmpty()){
                    Toast.makeText(this, "Codigo no encontrado", Toast.LENGTH_SHORT).show();
                }
                for (DocumentSnapshot snapshot: snapshotList){
                    if(!snapshot.getBoolean("PassCode.used")){
                        if(!daysBetween(snapshot.getDate("PassCode.date"))){
                            String UserID = snapshot.getString("UserInfo.UserID");
                            DocumentReference DocRef  = db.collection("users").document(UserID);
                            DocRef.update("PassCode.used", true);
                            Intent ResetPass = new Intent(ForgotPass.this, PassReset.class);
                            ResetPass.putExtra("UserID", UserID);
                            startActivity(ResetPass);
                        }else{
                            Toast.makeText(ForgotPass.this, "Este codigo ha caducado", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ForgotPass.this, "Este codigo ya ha sido usado", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    public boolean daysBetween(Date date) {
        Calendar first = Calendar.getInstance();
        first.setTime(date);
        Calendar second = Calendar.getInstance();
        long diffInMillis = second.getTimeInMillis() - first.getTimeInMillis();
        if(TimeUnit.MILLISECONDS.toHours(diffInMillis)>=1){
            return true;
        }else{
            return false;
        }
    }
}