package com.example.proyectoceti.CareMenus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.ContactModel;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCarePatientViewEmergencyBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

public class CarePatientViewEmergency extends Fragment {

    private FragmentCarePatientViewEmergencyBinding binding;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private PatientsModel model;

    public CarePatientViewEmergency() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarePatientViewEmergencyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = ((CarePatientView)getActivity()).Patientmodel;
        db = FirebaseFirestore.getInstance();

        setHospitalInfo();
        setInsuranceInfo();
        getContacts();
    }

    private void setHospitalInfo() {
        if(model.getHospital()==null){
            binding.HospitalInfoName.setText("No se ha establecido un hospital");
            binding.HospitalInfoNumber.setText("");
            binding.HospitalInfoAddy.setText("");
            return;
        }
        binding.HospitalInfoName.setText(model.getHospital().get("Name").toString());
        binding.HospitalInfoNumber.setText(model.getHospital().get("Number").toString());
        binding.HospitalInfoAddy.setText(((Map<String, Object>)model.getHospital().get("Location")).get("address").toString());

        binding.CallButton.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CALL_PHONE},10);
                return;
            }
            Intent call = new Intent(Intent.ACTION_CALL);
            try {
                call.setData(Uri.parse("tel: " + model.getHospital().get("Number")));
                startActivity(call);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.DirectionsButton.setOnClickListener(view -> {
            double lat = (double) ((Map<String, Object>) model.getHospital().get("Location")).get("latitude");
            double lng = (double) ((Map<String, Object>) model.getHospital().get("Location")).get("longitude");
            String Addy = ((Map<String, Object>) model.getHospital().get("Location")).get("address").toString();
            Uri gmmIntentUri = Uri.parse("geo: " + (lat) + " " + (lng) + "?q=" + Addy);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }

    private void setInsuranceInfo(){
        if(model.getInsurance()==null){
            binding.InsuranceInfoCompany.setText("No se ha introducido información de seguro");
            binding.InsuranceInfoEmp.setText(" ");
            binding.InsuranceInfoNumber.setText(" ");
            binding.InsuranceInfoExpiration.setText(" ");
            return;
        }
        binding.InsuranceInfoCompany.setText(model.getInsurance().get("Company").toString());
        binding.InsuranceInfoEmp.setText(model.getInsurance().get("Employer").toString());
        binding.InsuranceInfoNumber.setText(model.getInsurance().get("Policy").toString());
        binding.InsuranceInfoExpiration.setText(model.getInsurance().get("Date").toString());
    }

    private void getContacts() {
        Query query = db.collection("contacts").whereEqualTo("patientID", model.getPatientID());
        FirestoreRecyclerOptions<ContactModel> options = new FirestoreRecyclerOptions.Builder<ContactModel>()
                .setQuery(query, ContactModel.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<ContactModel, CarePatContactsViewHolder>(options) {
            @NonNull
            @Override
            public CarePatContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.care_patient_single_contact, parent, false);
                return new CarePatContactsViewHolder(view);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onBindViewHolder(@NonNull CarePatContactsViewHolder holder, int position, @NonNull ContactModel model) {
                holder.ContactName.setText(model.getName());
                holder.ContactCall.setOnClickListener(view1 -> {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CALL_PHONE},10);
                        return;
                    }
                    Intent call = new Intent(Intent.ACTION_CALL);
                    try {
                        call.setData(Uri.parse("tel: " + Encrypter.Decrypt(model.getCellphoneNumber())));
                        startActivity(call);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        };
        binding.CarePatientContactList.setHasFixedSize(false);
        binding.CarePatientContactList.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.CarePatientContactList.setAdapter(adapter);
    }

    private static class CarePatContactsViewHolder extends RecyclerView.ViewHolder {

        private final TextView ContactName;
        private final ImageButton ContactCall;

        public CarePatContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            ContactName = itemView.findViewById(R.id.ContactName);
            ContactCall = itemView.findViewById(R.id.ContactCallButton);
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

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
    }
}