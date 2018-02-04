package com.vsrc.ashareminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ReminderActivity extends AppCompatActivity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SharedPreferences preferences;
    ViewPager mPager;
    ScreenSlidePagerAdapter mPagerAdapter;
    JSONObject nextReminder;
//    boolean startTimer = false;


    JSONObject editReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        preferences = getSharedPreferences(GlobalConstants.ALL_DATA, MODE_PRIVATE);
        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        String reminders = preferences.getString(GlobalConstants.REMINDERS, "");
        JSONArray remindersArray;
        try {
            remindersArray = new JSONArray(reminders);
            if (remindersArray.length() <= 0) {
                resetReminders();
            }
        } catch (Exception e) {
            resetReminders();
        }

        mPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mPager);
        enableBootReceiver();
    }


    public void resetReminders(View view) {
        resetReminders();
    }

    public void resetReminders() {

        ArrayList<Integer> hoursList = new ArrayList<>();
        ArrayList<Integer> minutesList = new ArrayList<>();
        ArrayList<String> namesList = new ArrayList<>();
        ArrayList<String> descriptionsList = new ArrayList<>();

//        hoursList.add(5);
//        minutesList.add(30);
//        namesList.add("Thyroid Medicine");
//        descriptionsList.add("Good morning. Time to take your medicine.");

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

        preferences.edit().putString(GlobalConstants.REMINDERS, "").commit();


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

        Toast.makeText(this, "All reminders reset.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String notifications = preferences.getString(GlobalConstants.NOTIFICATIONS, "false");
            if (notifications.matches("true")) {
                String reminder = preferences.getString(GlobalConstants.CURRENT_REMINDER, "");
                preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "false").commit();
                nextReminder = new JSONObject(reminder);
                editReminder = new JSONObject(reminder);
                //  startTimer = true;
                Log.d("New Notification", "Fetching next reminder");

            } else {
                getNextReminder();
                Log.d("No notifications", "Fetching next reminder");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }


    private int comingUpTime(JSONObject timeObject) {

        int comingUpHours = 0;
        try {
            comingUpHours = timeObject.getInt("hour");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int comingUpMinutes = 0;
        try {
            comingUpMinutes = timeObject.getInt("minutes");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int comingUpTime;

        if (String.valueOf(comingUpMinutes).length() == 1) {
            comingUpTime = Integer.valueOf("" + comingUpHours + "0" + comingUpMinutes);
        } else {
            comingUpTime = Integer.valueOf("" + comingUpHours + comingUpMinutes);
        }
        return comingUpTime;
    }

    public void saveContactDetails(String name , String number){
        preferences.edit().putString("contactName", name).commit();
        preferences.edit().putString("contactNumber", number).commit();
    }

    public void getNextReminder() {

        nextReminder = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        int crntTime = Integer.valueOf(sdf.format(new Date()));

        ArrayList<Integer> nextRemindersList = new ArrayList<>();
        ArrayList<Integer> allRemindersList = new ArrayList<>();

        String reminders = getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE).getString(GlobalConstants.REMINDERS, "");
        Log.d("Saved Reminders", "getNextReminder: " + reminders.toString());
        try {
            JSONArray remindersArray = new JSONArray(reminders);
            for (int i = 0; i < remindersArray.length(); i++) {
                int comingUpTime = comingUpTime(remindersArray.getJSONObject(i).getJSONObject("time"));
                allRemindersList.add(comingUpTime);
                if (comingUpTime > crntTime) {
                    nextRemindersList.add(comingUpTime);
                }
            }

            Collections.sort(nextRemindersList);
            Collections.sort(allRemindersList);

            if (nextRemindersList.size() > 0) {
                for (int i = 0; i < remindersArray.length(); i++) {
                    int comingUpTime = comingUpTime(remindersArray.getJSONObject(i).getJSONObject("time"));
                    if (nextRemindersList.get(0) == comingUpTime) {
                        nextReminder = remindersArray.getJSONObject(i);
                    }
                }
            } else {
                for (int i = 0; i < remindersArray.length(); i++) {
                    int comingUpTime = comingUpTime(remindersArray.getJSONObject(i).getJSONObject("time"));
                    if (allRemindersList.get(0) == comingUpTime) {
                        nextReminder = remindersArray.getJSONObject(i);
                    }
                }
            }

            Log.d("displayNextReminder", nextReminder.toString());
            Log.d("All Reminders", allRemindersList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("reminder", reminder.toString());

        alarmIntent = PendingIntent.getBroadcast(this, id, intent, 0);

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
        Toast.makeText(this, "Deleted Successfully.", Toast.LENGTH_SHORT).show();
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

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new DashboardFragment();

                case 1:
                    return new MissedRemindersFragment();

                case 2:
                    return new AllRemindersFragment();

                default:
                    return new DashboardFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Reminder";

                case 1:
                    return "Missed";

                case 2:
                    return "All Reminders";

                default:
                    return "Reminder";
            }
        }
    }

    private void enableBootReceiver() {
        ComponentName receiver = new ComponentName(this, SimpleBootReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


}
