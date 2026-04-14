package com.example.ecogrocer.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.ecogrocer.R;
import java.util.Calendar;

public class FestivalHelper {

    public enum Festival {
        NAVRATRI("Navratri", "Navratri & Ganpati Specials", R.color.festive_navratri_start, R.color.festive_navratri_end),
        GANPATI("Ganpati", "Navratri & Ganpati Specials", R.color.festive_ganpati_start, R.color.festive_ganpati_end),
        DIWALI("Diwali", "Diwali Essentials Delivered in 10 Minutes", R.color.festive_diwali_start, R.color.festive_diwali_end),
        HOLI("Holi", "Holi Celebration Bundles", R.color.festive_holi_start, R.color.festive_holi_end),
        SUMMER("Summer Specials", "Beat the Heat with Summer Bundles", R.color.festive_summer_start, R.color.festive_summer_end),
        MONSOON("Monsoon Munchies", "Rainy Day Cravings Delivered", R.color.festive_monsoon_start, R.color.festive_monsoon_end),
        NONE("", "", 0, 0);

        public final String subcategory;
        public final String bannerText;
        public final int startColorRes;
        public final int endColorRes;

        Festival(String subcategory, String bannerText, int startColorRes, int endColorRes) {
            this.subcategory = subcategory;
            this.bannerText = bannerText;
            this.startColorRes = startColorRes;
            this.endColorRes = endColorRes;
        }
    }

    public static Festival getCurrentFestival() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH); // 0-indexed

        if (month == Calendar.AUGUST) {
            return Festival.GANPATI;
        } else if (month == Calendar.SEPTEMBER || month == Calendar.OCTOBER) {
            return Festival.NAVRATRI;
        } else if (month == Calendar.NOVEMBER) {
            return Festival.DIWALI;
        } else if (month == Calendar.MARCH) {
            return Festival.HOLI;
        } else if (month == Calendar.APRIL || month == Calendar.MAY) {
            return Festival.SUMMER;
        } else if (month == Calendar.JULY) {
            return Festival.MONSOON;
        }
        return Festival.NONE;
    }

    public static void applyFestivalTheme(Context context, Festival festival, View bannerView) {
        if (festival == Festival.NONE) return;

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        ContextCompat.getColor(context, festival.startColorRes),
                        ContextCompat.getColor(context, festival.endColorRes)
                });
        gd.setCornerRadius(0f);
        bannerView.setBackground(gd);
    }
}
