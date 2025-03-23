package com.evchargers.westyorkshire.utils;

import android.content.Context;
import com.evchargers.westyorkshire.model.Chargepoint;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.evchargers.westyorkshire.R;

public class CSVParser {
    public static List<Chargepoint> parseChargepoints(Context context) {
        List<Chargepoint> chargepoints = new ArrayList<>();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.sample_national_chargepoints);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length >= 9) {
                    String id = data[0];
                    String name = data[3]; // town
                    String county = data[4];
                    String chargerType = data[8];
                    double latitude = Double.parseDouble(data[1]);
                    double longitude = Double.parseDouble(data[2]);
                    String status = data[6];

                    Chargepoint chargepoint = new Chargepoint(id, name, county, chargerType,
                            latitude, longitude, status);
                    chargepoints.add(chargepoint);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chargepoints;
    }

}