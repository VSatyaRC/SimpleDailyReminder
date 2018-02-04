package com.vsrc.ashareminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class SimpleBootReceiver extends BroadcastReceiver {

    AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SharedPreferences preferences;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.context = context;
            alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            preferences = context.getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE);
            preferences.edit().putString(GlobalConstants.REMINDERS, "").commit();

            Log.d("Buddy Reminder", "onReceive: CRM Alarm Triggered");
            resetReminders();

        }
    }


    public void resetReminders() {

        ArrayList<Integer> hoursList = new ArrayList<>();
        ArrayList<Integer> minutesList = new ArrayList<>();
        ArrayList<String> namesList = new ArrayList<>();
        ArrayList<String> descriptionsList = new ArrayList<>();

        hoursList.add(6);
        minutesList.add(30);
        namesList.add("Hot Water");
        descriptionsList.add("Good morning.\nTake Medicine\nTime to drink hot water.");

        hoursList.add(7);
        minutesList.add(00);
        namesList.add("Milk & Dry Fruits");
        descriptionsList.add("Drink milk and have some dry fruits.");

        hoursList.add(7);
        minutesList.add(45);
        namesList.add("Breakfast");
        descriptionsList.add("Whats your breakfast today?");

        hoursList.add(8);
        minutesList.add(30);
        namesList.add("Water");
        descriptionsList.add("Drink plenty of water and keep yourself hydrated.");

        hoursList.add(9);
        minutesList.add(30);
        namesList.add("Fruits");
        descriptionsList.add("Took any fruits today? Have some.");

        hoursList.add(10);
        minutesList.add(30);
        namesList.add("Water");
        descriptionsList.add("Drink plenty of water and keep yourself hydrated.");

        hoursList.add(11);
        minutesList.add(30);
        namesList.add("Juice");
        descriptionsList.add("Time for snacks fruits or juice? ");

        hoursList.add(12);
        minutesList.add(45);
        namesList.add("Lunch");
        descriptionsList.add("Time for lunch.");

        hoursList.add(13);
        minutesList.add(15);
        namesList.add("Medicine");
        descriptionsList.add("Iron is good for you. Take the medicine");

        hoursList.add(14);
        minutesList.add(30);
        namesList.add("Water");
        descriptionsList.add("Drink plenty of water and keep yourself hydrated.");

        hoursList.add(17);
        minutesList.add(00);
        namesList.add("Snacks");
        descriptionsList.add("Time for a small snack. ");

        hoursList.add(18);
        minutesList.add(30);
        namesList.add("Water");
        descriptionsList.add("Drink plenty of water and keep yourself hydrated.");

        hoursList.add(19);
        minutesList.add(30);
        namesList.add("Dinner");
        descriptionsList.add("Time for dinner.");

        hoursList.add(19);
        minutesList.add(30);
        namesList.add("Medicine");
        descriptionsList.add("Time for medicine(Ecosprin).");

        hoursList.add(21);
        minutesList.add(00);
        namesList.add("Badam");
        descriptionsList.add("Soak your badam please.");

        hoursList.add(23);
        minutesList.add(55);
        namesList.add("Delete");
        descriptionsList.add("Clearing all missed alarms.");

        JSONObject timeObject;
        JSONObject reminder;


        for (int i = 0; i < namesList.size(); i++) {
            timeObject = new JSONObject();
            reminder = new JSONObject();
            try {
                timeObject.put("hour", hoursList.get(i));
                timeObject.put("minutes", minutesList.get(i));
                reminder.put("name", namesList.get(i));
                reminder.put("time", timeObject);
                reminder.put("description", descriptionsList.get(i));
                saveOrDeleteReminder(reminder, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(context, "All reminders reset.", Toast.LENGTH_SHORT).show();
    }

    public void saveOrDeleteReminder(JSONObject reminder, boolean delete) {

        String reminders = preferences.getString(GlobalConstants.REMINDERS, "");
        if (delete) {
            deleteReminder(reminders, reminder);
        } else {
            saveReminder(reminders, reminder);
        }
    }

    private void deleteReminder(String reminders, JSONObject reminder) {

        Log.d("Delete Reminder", reminder.toString());
        JSONArray remindersArray;
        try {
            remindersArray = new JSONArray(reminders);
            for (int i = 0; i < remindersArray.length(); i++) {
                if (reminder.get("id") == remindersArray.getJSONObject(i).get("id")) {
                    remindersArray.remove(i);
                }
            }
            preferences.edit().putString(GlobalConstants.REMINDERS, remindersArray.toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setReminder(reminder, true);
        Toast.makeText(context, "Deleted Successfully.", Toast.LENGTH_SHORT).show();
    }

    private void saveReminder(String reminders, JSONObject reminder) {

        Log.d("Save Reminder", reminder.toString());

        int reminderCount = 0;
        JSONArray remindersArray;

        try {
            remindersArray = new JSONArray(reminders);
            reminderCount = remindersArray.length() + 1;
            reminder.put("id", reminderCount);
            remindersArray.put(reminder);
        } catch (JSONException e) {
            remindersArray = new JSONArray();
            try {
                reminderCount++;
                reminder.put("id", reminderCount);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            remindersArray.put(reminder);
            Log.d("saveNewReminder", "First Reminder");
        }
        preferences.edit().putString(GlobalConstants.REMINDERS, remindersArray.toString()).commit();
        setReminder(reminder, false);

    }


    public void setReminder(JSONObject reminder, boolean cancelReminder) {

        JSONObject timeObject = new JSONObject();
        try {
            timeObject = reminder.getJSONObject("time");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int hour = 6;
        try {
            hour = timeObject.getInt("hour");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int minute = 30;
        try {
            minute = timeObject.getInt("minutes");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int id = 0;
        try {
            id = reminder.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("reminder", reminder.toString());

        alarmIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        }

        if (cancelReminder) {
            alarmMgr.cancel(alarmIntent);
            Log.d("Cancel Reminder", "Alarm Cancelled" + reminder.toString());
        } else {
//            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY, alarmIntent);

            int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmMgr.setExactAndAllowWhileIdle(ALARM_TYPE, calendar.getTimeInMillis(), alarmIntent);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                alarmMgr.setExact(ALARM_TYPE, calendar.getTimeInMillis(), alarmIntent);
            else
                alarmMgr.set(ALARM_TYPE, calendar.getTimeInMillis(), alarmIntent);
        }
    }


}
