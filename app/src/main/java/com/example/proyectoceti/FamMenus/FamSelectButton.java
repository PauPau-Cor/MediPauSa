package com.example.proyectoceti.FamMenus;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.ActivityFamSelectButtonBinding;

import java.util.Set;

public class FamSelectButton extends AppCompatActivity {

    private ActivityFamSelectButtonBinding binding;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFamSelectButtonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CheckBTState();
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.linked_devices);
        binding.DeviceList.setAdapter(mPairedDevicesArrayAdapter);
        binding.DeviceList.setOnItemClickListener(mDeviceClickListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FamSelectButton.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            finishAffinity();

            Intent intent = new Intent(FamSelectButton.this, FamConfigButton.class);
            intent.putExtra("device_address", address);
            startActivity(intent);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void CheckBTState() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta, soporten", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mBtAdapter.isEnabled()) {
            Log.d(TAG, "CheckBTState: pues sí soporto, besos");
            return;
        }
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FamSelectButton.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
        startActivityForResult(enableBT, 1);
    }
}