package com.example.proyectoceti.GeneralMenus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectoceti.ClassesAndModels.EventModel;
import com.example.proyectoceti.R;
import com.google.android.material.button.MaterialButton;

public class ViewEvent extends AppCompatActivity {
    TextView EventName, EventDescription, DateTime;
    MaterialButton EditEvent;
    EventModel eventModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        Intent getIntent = getIntent();
        eventModel = getIntent.getParcelableExtra("EventModel");

        EditEvent = findViewById(R.id.EditEvent);
        EventName = findViewById(R.id.ETAddEventName);
        EventDescription = findViewById(R.id.ETAddEventDes);
        DateTime = findViewById(R.id.ETAddEventDate);

        EventName.setText(eventModel.getName());
        EventDescription.setText(eventModel.getDescription());
        DateTime.setText(eventModel.getDate());

        EditEvent.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEvent.this, AddEvents.class);
            intent.putExtra("EventModel", eventModel);
            startActivity(intent);
        });
    }
}