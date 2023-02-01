package com.example.proyectoceti.CareMenus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.ClassesAndModels.MedsModel;
import com.example.proyectoceti.ClassesAndModels.PatientsModel;
import com.example.proyectoceti.GeneralMenus.AddEvents;
import com.example.proyectoceti.GeneralMenus.ViewEvent;
import com.example.proyectoceti.GeneralMenus.ViewMed;
import com.example.proyectoceti.Misc.Utilities;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FragmentCarePatientViewGeneralSummaryBinding;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CarePatientViewGeneralSummary extends Fragment {

    private FirebaseFirestore db;
    private FragmentCarePatientViewGeneralSummaryBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private PatientsModel model;
    private String dateString;

    public CarePatientViewGeneralSummary() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCarePatientViewGeneralSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ((CarePatientView)getActivity()).Patientmodel;
        db = FirebaseFirestore.getInstance();
        Date today = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        dateString = dateFormat.format(today);
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM - yyyy", Locale.forLanguageTag("ES"));
        binding.CarePatViewSumCalendar.setLocale(TimeZone.getDefault(),Locale.forLanguageTag("ES"));
        binding.Month.setText(dateFormatMonth.format(binding.CarePatViewSumCalendar.getFirstDayOfCurrentMonth()));
        db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("type", "event").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                Long EventDate = snapshot.getTimestamp("timeStamp").getSeconds() * 1000;
                Event event = new Event(Color.parseColor("#8047B9"), EventDate, snapshot.getString("name"));
                binding.CarePatViewSumCalendar.addEvent(event);
            }
        });

        binding.CarePatViewSumCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                dateString = dateFormat.format(dateClicked);
                Query NewQuery = db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("date", dateString).orderBy("timeStamp", Query.Direction.ASCENDING);
                FirestoreRecyclerOptions<EventModel> NewOptions = new FirestoreRecyclerOptions.Builder<EventModel>()
                        .setQuery(NewQuery, EventModel.class)
                        .setLifecycleOwner(CarePatientViewGeneralSummary.this)
                        .build();
                adapter = NewAdapter(NewOptions);
                binding.CarePatViewEventList.setAdapter(adapter);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                binding.Month.setText(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });

        Query query = db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("date", dateString).orderBy("timeStamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<EventModel> options = new FirestoreRecyclerOptions.Builder<EventModel>()
                .setQuery(query, EventModel.class)
                .build();
        adapter = NewAdapter(options);
        binding.CarePatViewEventList.setHasFixedSize(false);
        binding.CarePatViewEventList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.CarePatViewEventList.setAdapter(adapter);

        binding.AddEventBT.setOnClickListener(view -> {
            Intent addevents = new Intent(getActivity(), AddEvents.class);
            addevents.putExtra("PatientModel", model);
            addevents.putExtra("Today",dateString);
            startActivity(addevents);
        });
    }

    private static class CarePatientsEventsViewHolder extends RecyclerView.ViewHolder {

        private final TextView PatientEventName;
        private final TextView PatientEventDate;

        public CarePatientsEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            PatientEventName = itemView.findViewById(R.id.PatEventName);
            PatientEventDate = itemView.findViewById(R.id.PatEventDate);
        }
    }

    private FirestoreRecyclerAdapter NewAdapter(FirestoreRecyclerOptions<EventModel> options){
        FirestoreRecyclerAdapter newAdapt;
        newAdapt = new FirestoreRecyclerAdapter<EventModel, CarePatientsEventsViewHolder>(options) {
            @NonNull
            @Override
            public CarePatientsEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_event_item_single, parent, false);
                return new CarePatientsEventsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CarePatientsEventsViewHolder holder, int position, @NonNull EventModel model) {
                holder.PatientEventName.setText(model.getName());
                holder.PatientEventDate.setText(model.getTime());
                holder.itemView.setOnClickListener(view -> {
                    if(model.getType().equals("event")){
                        Intent CareSingleEvent = new Intent(getActivity(), ViewEvent.class);
                        CareSingleEvent.putExtra("EventModel", model);
                        startActivity(CareSingleEvent);
                    }else if (model.getType().equals("medication")){
                        Intent MedEvent = new Intent(getActivity(), ViewMed.class);
                        db.collection("medications").document(model.getExtraID()).get().addOnSuccessListener(documentSnapshot -> {
                            if(documentSnapshot.exists()){
                                MedsModel med = documentSnapshot.toObject(MedsModel.class);
                                MedEvent.putExtra("MedModel", med);
                                startActivity(MedEvent);
                            }
                            else{
                                Toast.makeText(getContext(), "Medicamento ha sido eliminado.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        };
        return newAdapt;
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