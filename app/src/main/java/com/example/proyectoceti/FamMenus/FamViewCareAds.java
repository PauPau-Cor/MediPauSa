package com.example.proyectoceti.FamMenus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoceti.ClassesAndModels.UserModel;
import com.example.proyectoceti.GeneralMenus.ChatActivity;
import com.example.proyectoceti.Misc.Encrypter;
import com.example.proyectoceti.R;
import com.example.proyectoceti.databinding.FamViewCareAdSingleBinding;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamViewCareAds extends AppCompatActivity {

    private FirebaseFirestore db;

    private RecyclerView FamAdsList;
    private ProgressBar FamAdPB;
    private TextView FilterTV, FilterIndicator;
    private LinearLayout NoResults;
    private FusedLocationProviderClient fusedLocationClient;
    private GeoLocation FamLocation;

    private List<UserModel> AdsList;
    private AdsAdapter adsAdapter;

    private MaterialButtonToggleGroup FilterButtons;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fam_view_care_ads);

        db = FirebaseFirestore.getInstance();

        FamAdsList = findViewById(R.id.FamViewAdsList);
        FamAdPB = findViewById(R.id.FamAdsPB);
        FilterButtons =findViewById(R.id.FamViewAdsFilters);
        FilterTV = findViewById(R.id.FilterTv);
        FilterIndicator = findViewById(R.id.FilterIndicator);
        NoResults = findViewById(R.id.NoResults);

        FamAdPB.setVisibility(View.VISIBLE);
        if(ContextCompat.checkSelfPermission(FamViewCareAds.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(FamViewCareAds.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FamViewCareAds.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            FamLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                            AdapterConfig();
                        }
                        if(location==null){
                            Toast.makeText(FamViewCareAds.this, "Hubo un error consiguiendo su ubicacion, vuelvalo a intentar", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        FilterButtons.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            switch(checkedId){
                case R.id.FamViewAdsRatings:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved"), true);
                        FilterIndicator.setText("Reseñas");
                    }
                    break;
                case R.id.FamViewAdsRatingsQuan:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("RatingQuantity", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved"), true);
                        FilterIndicator.setText("Cantidad de reseñas");
                    }
                    break;
                case R.id.FamViewAdsMaleBT:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("UserInfo.Sex", "Masculino"), true);
                        FilterIndicator.setText("Genero masculino");
                    }
                break;
                case R.id.FamViewAdsFemaleBT:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("UserInfo.Sex", "Femenino"), true);
                        FilterIndicator.setText("Genero femenino");
                    }
                break;
                case R.id.FamViewAdsNonBinaryBT:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("UserInfo.Sex", "No binario"), true);
                        FilterIndicator.setText("Genero no binario");
                    }
                break;
                case R.id.FamViewAdsNoExp:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("Expertise", "Sin experiencia"), true);
                        FilterIndicator.setText("Sin experiencia");
                    }
                    break;
                case R.id.FamViewAdsStudent:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("Expertise", "Estudiante"), true);
                        FilterIndicator.setText("Estudiante");
                    }
                    break;
                case R.id.FamViewAdsDoctor:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved").whereEqualTo("Expertise", "Enfermer@"), true);
                        FilterIndicator.setText("Enfermer@");
                    }
                    break;
                case R.id.FamViewAdsMoneyAsc:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("UserInfo.Salary", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved"), true);
                        FilterIndicator.setText("Salario descendiente");
                    }
                    break;
                case R.id.FamViewAdsMoneyDesc:
                    if(isChecked){
                        GetAds(db.collection("users").orderBy("UserInfo.Salary", Query.Direction.ASCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved"), true);
                        FilterIndicator.setText("Salario ascendente");
                    }
                    break;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void AdapterConfig(){
        GetAds(db.collection("users").orderBy("Rating", Query.Direction.DESCENDING).whereEqualTo("Advertised", true).whereEqualTo("Approved","approved"), false);
        adsAdapter = new AdsAdapter(AdsList);
        FamAdsList.setAdapter(adsAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void GetAds(@NonNull Query query, boolean updater){
        AdsList= new ArrayList<>();
        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && !task.getResult().isEmpty()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double lat = 0, lng = 0;
                    try {
                        lat = Double.valueOf(Encrypter.Decrypt(document.getString("UserInfo.Location.latitude")));
                        lng = Double.valueOf(Encrypter.Decrypt(document.getString("UserInfo.Location.longitude")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GeoLocation docLocation = new GeoLocation(lat, lng);
                    double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, FamLocation);
                    if (distanceInM <= 25000) {
                        UserModel SingleAd = new UserModel();
                        SingleAd.setRating((document.getDouble("Rating")).floatValue());
                        SingleAd.setRatingQuantity(document.getDouble("RatingQuantity").intValue());
                        SingleAd.setUserInfo((HashMap<String, Object>)document.get("UserInfo"));
                        SingleAd.setUserType(document.getString("UserType"));
                        SingleAd.setFCMToken(document.getString("FCM Token"));
                        SingleAd.setExpertise(document.getString("Expertise"));
                        AdsList.add(SingleAd);
                    }
                }
                if(AdsList.isEmpty()){
                    FamAdsList.setVisibility(View.GONE);
                    NoResults.setVisibility(View.VISIBLE);
                }else{
                    NoResults.setVisibility(View.GONE);
                    FamAdsList.setVisibility(View.VISIBLE);
                    if(updater) adsAdapter = new AdsAdapter(AdsList);
                    FamAdsList.setAdapter(adsAdapter);
                }
            }
            if(AdsList.isEmpty()){
                FamAdsList.setVisibility(View.GONE);
                NoResults.setVisibility(View.VISIBLE);
            }
            FamAdPB.setVisibility(View.GONE);
        });
    }

    private class AdsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private final List<UserModel> AdsList;

        public AdsAdapter(List<UserModel> adsList) {
            this.AdsList = adsList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FilterButtons.setVisibility(View.VISIBLE);
            FilterIndicator.setVisibility(View.VISIBLE);
            FilterTV.setVisibility(View.VISIBLE);
            return new SingleAdViewHolder(
                    FamViewCareAdSingleBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((SingleAdViewHolder) holder).setData(AdsList.get(position));
        }

        @Override
        public int getItemCount() {
            return AdsList.size();
        }

        class SingleAdViewHolder extends RecyclerView.ViewHolder{
            private final FamViewCareAdSingleBinding binding;
            SingleAdViewHolder(@NonNull FamViewCareAdSingleBinding famViewCareAdSingleBinding){
                super(famViewCareAdSingleBinding.getRoot());
                binding = famViewCareAdSingleBinding;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            void setData(@NonNull UserModel model){
                String lat = "", lng = "";
                try {
                    lat = Encrypter.Decrypt(((Map)model.getUserInfo().get("Location")).get("latitude").toString());
                    lng = Encrypter.Decrypt(((Map)model.getUserInfo().get("Location")).get("longitude").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GeoLocation docLocation = new GeoLocation(Double.valueOf(lat), Double.valueOf(lng));
                double distanceInKM = GeoFireUtils.getDistanceBetween(docLocation, FamLocation)/1000;
                binding.FamCareAdName.setText(model.getUserInfo().get("Name").toString() + " " + model.getUserInfo().get("LastName"));
                binding.FamCareAdExpertise.setText(model.getExpertise());
                binding.FamCareAdDistance.setText(String.format("%.2f", distanceInKM) + "Km");
                binding.FamCareAdSalary.setText("$" + model.getUserInfo().get("Salary").toString());
                String url = ((Map)model.getUserInfo().get("Profile Picture")).get("url").toString();
                Picasso.get().load(url).into(binding.FamCareAdPhoto);
                binding.FamCareAdRating.setRating(model.getRating());
                binding.FamCareAdRatingQuantity.setText("(" + model.getRatingQuantity() + ")");
                binding.BTFamCareAdChat.setOnClickListener(view -> {
                    Intent intent = new Intent(FamViewCareAds.this, ChatActivity.class);
                    intent.putExtra("UserModel", model);
                    intent.putExtra("UserType", "fam");
                    startActivity(intent);
                });
            }
        }
    }
}