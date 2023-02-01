package com.example.proyectoceti.Misc;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validations {

    public static boolean IsInMexico(Context context, double lat, double lon){
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0)
        {
            return addresses.get(0).getCountryName().equals("Mexico");
        }
        return false;
    }

    public static boolean AgeValidation(String Birthday) {
        try {
            Date BirthDate = new SimpleDateFormat("dd/MM/yyyy").parse(Birthday);
            Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.YEAR, -18);
            if (BirthDate.before(c2.getTime())) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z]).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean AfterToday(String dateIn){
        try {
            Date dateConverted = new SimpleDateFormat("dd/MM/yyyy").parse(dateIn);
            Date currentDate = new Date();
            return !dateConverted.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
