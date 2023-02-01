package com.example.proyectoceti.Misc;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.NotificationModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utilities {

    public static String Round5Minutes(int hour, int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        int mod = minutes % 5;
        calendar.add(Calendar.MINUTE, mod < 3 ? -mod : (5-mod));
        return String.format(Locale.getDefault(), "%02d:%02d",calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE));
    }

    public static void OfflineNotification(String ReceiverID, String Title, String Body){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        NotificationModel NewNotif = new NotificationModel(ReceiverID, Title, Body, new Date());
        db.collection("notifications").add(NewNotif);
    }

    public static class WrapContentLinearLayoutManager extends LinearLayoutManager {

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
