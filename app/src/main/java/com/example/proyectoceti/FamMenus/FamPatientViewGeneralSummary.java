package com.example.proyectoceti.FamMenus;

import android.content.Intent;
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
import com.example.proyectoceti.databinding.FragmentFamPatientViewGeneralSummaryBinding;
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

public class FamPatientViewGeneralSummary extends Fragment {

    private FirebaseFirestore db;
    private FragmentFamPatientViewGeneralSummaryBinding binding;
    private FirestoreRecyclerAdapter adapter;
    private PatientsModel model;
    private String dateString;

    public FamPatientViewGeneralSummary() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFamPatientViewGeneralSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = ((FamPatientView)getActivity()).Patientmodel;
        db = FirebaseFirestore.getInstance();
        Date today = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy");
        dateString = dateFormat.format(today);
        SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM - yyyy", Locale.forLanguageTag("ES"));
        binding.FamPatViewSumCalendar.setLocale(TimeZone.getDefault(),Locale.forLanguageTag("ES"));
        binding.Month.setText(dateFormatMonth.format(binding.FamPatViewSumCalendar.getFirstDayOfCurrentMonth()));

        db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("type", "event").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
            for (DocumentSnapshot snapshot: snapshotList){
                Long EventDate = snapshot.getTimestamp("timeStamp").getSeconds() * 1000;
                Event event = new Event(com.google.android.libraries.places.R.color.quantum_googblueA200, EventDate, snapshot.getString("name"));
                binding.FamPatViewSumCalendar.addEvent(event);
            }
        });

        Query query = db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("date", dateString).orderBy("timeStamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<EventModel> options = new FirestoreRecyclerOptions.Builder<EventModel>()
                .setQuery(query, EventModel.class)
                .build();
        adapter = NewAdapter(options);
        binding.FamPatViewEventList.setHasFixedSize(false);
        binding.FamPatViewEventList.setLayoutManager(new Utilities.WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        binding.FamPatViewEventList.setAdapter(adapter);

        binding.FamPatViewSumCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                dateString = dateFormat.format(dateClicked);
                Query NewQuery = db.collection("events").whereEqualTo("patientID", model.getPatientID()).whereEqualTo("date", dateString).orderBy("timeStamp", Query.Direction.ASCENDING);
                FirestoreRecyclerOptions<EventModel> NewOptions = new FirestoreRecyclerOptions.Builder<EventModel>()
                        .setQuery(NewQuery, EventModel.class)
                        .setLifecycleOwner(FamPatientViewGeneralSummary.this)
                        .build();
                adapter = NewAdapter(NewOptions);
                binding.FamPatViewEventList.setAdapter(adapter);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                binding.Month.setText(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });

        binding.AddEventBT.setOnClickListener(view -> {
            Intent addevents = new Intent(getActivity(), AddEvents.class);
            addevents.putExtra("PatientModel", model);
            addevents.putExtra("Today",dateString);
            startActivity(addevents);
        });
    }

    private static class FamPatientsEventsViewHolder extends RecyclerView.ViewHolder {

        private final TextView PatientEventName;
        private final TextView PatientEventDate;

        public FamPatientsEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            PatientEventName = itemView.findViewById(R.id.PatEventName);
            PatientEventDate = itemView.findViewById(R.id.PatEventDate);
        }
    }

    private FirestoreRecyclerAdapter NewAdapter(FirestoreRecyclerOptions<EventModel> options){
        FirestoreRecyclerAdapter newAdapt;
        newAdapt = new FirestoreRecyclerAdapter<EventModel, FamPatientsEventsViewHolder>(options) {
            @NonNull
            @Override
            public FamPatientsEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_event_item_single, parent, false);
                return new FamPatientsEventsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FamPatientsEventsViewHolder holder, int position, @NonNull EventModel model) {
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