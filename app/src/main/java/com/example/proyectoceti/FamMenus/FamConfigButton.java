package com.example.proyectoceti.FamMenus;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityFamConfigButtonBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

public class FamConfigButton extends AppCompatActivity {

    private Handler bluetoothIn;
    private final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private ConnectedThread MyConnectionBT;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;

    private ActivityFamConfigButtonBinding binding;
    private FirebaseFirestore db;
    private String FamUserId;
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter adapter;

    private String PatientID;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamConfigButtonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FamUserId = mAuth.getUid();

        Query query = db.collection("patients").whereEqualTo("FamUserID", FamUserId);
        FirestoreRecyclerOptions<PatientsModel> options = new FirestoreRecyclerOptions.Builder<PatientsModel>()
                .setQuery(query, PatientsModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<PatientsModel, FamPatientsViewHolder>(options) {
            @NonNull
            @Override
            public FamPatientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fam_patient_item_single, parent, false);
                return new FamPatientsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FamPatientsViewHolder holder, int position, @NonNull PatientsModel model) {
                holder.FamPatName.setText(model.getPatientInfo().get("Name").toString() + " " + model.getPatientInfo().get("LastName"));
                holder.FamPatCareType.setText(model.getPatientInfo().get("CareType").toString());
                String url = ((Map) model.getPatientInfo().get("Photo")).get("url").toString();
                Picasso.get().load(url).into(holder.FamPatPhoto);
                holder.itemView.setOnClickListener(view -> {
                    Toast.makeText(FamConfigButton.this, "Paciente seleccionado: " + model.getPatientInfo().get("Name").toString(), Toast.LENGTH_SHORT).show();
                    PatientID = model.getPatientID();
                });
            }
        };
        binding.FamPatientList.setHasFixedSize(false);
        binding.FamPatientList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(FamConfigButton.this, LinearLayoutManager.VERTICAL, false));
        binding.FamPatientList.setAdapter(adapter);
        binding.BTSendSettings.setOnClickListener(view -> {
            String SSID = binding.ETssid.getEditText().getText().toString();
            String Pass = binding.ETpassword.getEditText().getText().toString();
            if (TextUtils.isEmpty(PatientID) || TextUtils.isEmpty(Pass) || TextUtils.isEmpty(SSID)) {
                Toast.makeText(this, "Favor de rellenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            MyConnectionBT.write(SSID + "/" + Pass + "/" + PatientID);
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBTState();
        Intent intent = getIntent();
        address = intent.getStringExtra("device_address");
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            btSocket.connect();
        } catch (IOException e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
        MyConnectionBT = new ConnectedThread(btSocket);
        MyConnectionBT.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        Log.d(TAG, "createBluetoothSocket: waossss");
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void CheckBTState() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta, soporten", Toast.LENGTH_SHORT).show();
            return;
        }
        if (btAdapter.isEnabled()) {
            Log.d(TAG, "CheckBTState: pues sí soporto, besos");
            return;
        }
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FamConfigButton.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        startActivityForResult(enableBT, 1);
    }

    private static class FamPatientsViewHolder extends RecyclerView.ViewHolder {

        private final TextView FamPatName;
        private final TextView FamPatCareType;
        private final ImageView FamPatPhoto;

        public FamPatientsViewHolder(@NonNull View itemView) {
            super(itemView);
            FamPatName = itemView.findViewById(R.id.FamPatSingleName);
            FamPatCareType = itemView.findViewById(R.id.FamPatSingleCareType);
            FamPatPhoto = itemView.findViewById(R.id.FamPatSinglePhoto);
        }
    }

    private class ConnectedThread extends Thread
    {
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            OutputStream tmpOut = null;
            try
            {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, "ConnectedThread: " + e.getMessage());
            }
            mmOutStream = tmpOut;
        }

        //Envio de trama
        public void write(String input)
        {
            try {
                Toast.makeText(FamConfigButton.this, "Configuración enviada exitosamente, se recomienda probar el botón para verificar su funcionamiento.", Toast.LENGTH_SHORT).show();
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}