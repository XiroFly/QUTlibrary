package com.example.libaray;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UseSettingsActivity extends AppCompatActivity {

    private Spinner timeSlotStart1, timeSlotEnd1, timeSlotStart2, timeSlotEnd2, timeSlotStart3, timeSlotEnd3;
    private Spinner floorSpinner, areaSpinner, seatSpinner;
    private CheckBox checkBox1, checkBox2, checkBox3;
    private static final String FILENAME = "usesettings.json";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usesettings);

        initializeViews();
        initializeSpinners();
        loadSavedPreferences();

        findViewById(R.id.save_button).setOnClickListener(v -> {
            try {
                savePreferences();
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Toast.makeText(this, "Error saving settings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        timeSlotStart1 = findViewById(R.id.time_slot_start_1);
        timeSlotEnd1 = findViewById(R.id.time_slot_end_1);
        timeSlotStart2 = findViewById(R.id.time_slot_start_2);
        timeSlotEnd2 = findViewById(R.id.time_slot_end_2);
        timeSlotStart3 = findViewById(R.id.time_slot_start_3);
        timeSlotEnd3 = findViewById(R.id.time_slot_end_3);
        floorSpinner = findViewById(R.id.floor_spinner);
        areaSpinner = findViewById(R.id.area_spinner);
        seatSpinner = findViewById(R.id.seat_spinner);
        checkBox1 = findViewById(R.id.select_checkbox_1);
        checkBox2 = findViewById(R.id.select_checkbox_2);
        checkBox3 = findViewById(R.id.select_checkbox_3);
    }

    private void initializeSpinners() {
        populateTimeSpinners(timeSlotStart1, timeSlotEnd1, "07:00", "12:00");
        populateTimeSpinners(timeSlotStart2, timeSlotEnd2, "13:30", "18:00");
        populateTimeSpinners(timeSlotStart3, timeSlotEnd3, "19:00", "22:00");

        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"3F", "4F", "5F", "6F"});
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(floorAdapter);

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"A", "B", "C"});
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaAdapter);

        List<String> seats = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            seats.add(String.format("%03d", i));
        }
        ArrayAdapter<String> seatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seats);
        seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatSpinner.setAdapter(seatAdapter);
    }

    private void populateTimeSpinners(Spinner startSpinner, Spinner endSpinner, String startTime, String endTime) {
        List<String> times = generateTimeSlots(startTime, endTime, 10);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSpinner.setAdapter(adapter);
        endSpinner.setAdapter(adapter);
    }

    private List<String> generateTimeSlots(String startTime, String endTime, int interval) {
        List<String> times = new ArrayList<>();
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMinute = Integer.parseInt(startTime.split(":")[1]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);
        int endMinute = Integer.parseInt(endTime.split(":")[1]);

        while (startHour < endHour || (startHour == endHour && startMinute <= endMinute)) {
            times.add(String.format("%02d:%02d", startHour, startMinute));
            startMinute += interval;
            if (startMinute >= 60) {
                startMinute -= 60;
                startHour++;
            }
        }
        return times;
    }

    private void loadSavedPreferences() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            JSONArray selections = new JSONArray(stringBuilder.toString());
            applySelections(selections);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applySelections(JSONArray selections) throws JSONException {
        for (int i = 0; i < selections.length(); i++) {
            JSONObject selection = selections.getJSONObject(i);
            switch (i) {
                case 0:
                    setSpinnerSelection(timeSlotStart1, selection.getString("start"));
                    setSpinnerSelection(timeSlotEnd1, selection.getString("end"));
                    break;
                case 1:
                    setSpinnerSelection(timeSlotStart2, selection.getString("start"));
                    setSpinnerSelection(timeSlotEnd2, selection.getString("end"));
                    break;
                case 2:
                    setSpinnerSelection(timeSlotStart3, selection.getString("start"));
                    setSpinnerSelection(timeSlotEnd3, selection.getString("end"));
                    break;
            }
            setSpinnerSelection(floorSpinner, selection.getString("floor"));
            setSpinnerSelection(areaSpinner, selection.getString("area"));
            setSpinnerSelection(seatSpinner, selection.getString("seat"));

            // Apply CheckBox selections
            checkBox1.setChecked(selection.getBoolean("checkbox1"));
            checkBox2.setChecked(selection.getBoolean("checkbox2"));
            checkBox3.setChecked(selection.getBoolean("checkbox3"));
        }
    }

    private void savePreferences() throws JSONException {
        JSONArray selections = new JSONArray();
        selections.put(createSelection(timeSlotStart1, timeSlotEnd1, checkBox1));
        selections.put(createSelection(timeSlotStart2, timeSlotEnd2, checkBox2));
        selections.put(createSelection(timeSlotStart3, timeSlotEnd3, checkBox3));

        try (FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE)) {
            fos.write(selections.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject createSelection(Spinner startSpinner, Spinner endSpinner, CheckBox radioButton) throws JSONException {
        JSONObject selection = new JSONObject();
        selection.put("start", startSpinner.getSelectedItem().toString());
        selection.put("end", endSpinner.getSelectedItem().toString());
        selection.put("floor", floorSpinner.getSelectedItem().toString());
        selection.put("area", areaSpinner.getSelectedItem().toString());
        selection.put("seat", seatSpinner.getSelectedItem().toString());
        selection.put("checkbox1", checkBox1.isChecked());
        selection.put("checkbox2", checkBox2.isChecked());
        selection.put("checkbox3", checkBox3.isChecked());
        return selection;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

}
