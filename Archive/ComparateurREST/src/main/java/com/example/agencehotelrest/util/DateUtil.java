package com.example.agencehotelrest.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static Date stringToDate(String dateStr) {
        // Spécifie le format de date attendu
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;

        try {
            // Convertit la chaîne de date en objet Date
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            // Gérer l'erreur si nécessaire
        }

        return date;
    }

}