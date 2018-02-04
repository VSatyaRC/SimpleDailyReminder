package com.vsrc.ashareminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AlarmReceiver extends BroadcastReceiver {

    SharedPreferences preferences;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Starting sending..  ", Toast.LENGTH_SHORT).show();
        this.context = context;
        preferences = context.getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE);
        String reminder = intent.getStringExtra("reminder");
        Log.d("AlarmReceiver", "onReceive: " + reminder.toString());
        JSONObject reminderObject;
        try {
            reminderObject = new JSONObject(reminder);
            if (reminderObject.getString("name").matches("Delete")) {
                addMissedReminder(new JSONObject(), true);
            } else {
                saveReminder(reminder);
            }
        } catch (Exception e) {

        }
    }

    private void saveReminder(String reminder) {

        Log.d("Save Reminder", reminder);
        preferences.edit().putString(GlobalConstants.CURRENT_REMINDER, reminder).commit();
        preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "true").commit();
        startSendingNotifications(reminder);

    }

    private void startSendingNotifications(String reminder) {

        String title = "", text = "";
        JSONObject reminderObject = new JSONObject();
        try {
            reminderObject = new JSONObject(reminder);
            title = reminderObject.getString("name");
            text = reminderObject.getString("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int uniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        String channelId = "channel-01";
        Notification.Builder builder;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Reminder",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(context, channelId);

        }else {
            builder = new Notification.Builder(context);
        }


        Intent notificationIntent = new Intent(context, ReminderActivity.class);
        Log.d("Notification", text);
        notificationIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent = PendingIntent.getActivity(context, uniqueId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


                builder.setLargeIcon(getBitmap())
                .setSmallIcon(R.drawable.reminder)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(intent);
        notificationManager.notify(uniqueId, builder.build());
        startTimer(reminderObject, getWorkedReminders());
    }


    private int getWorkedReminders() {

        int workedReminder = 0;
        String reminders = preferences.getString(GlobalConstants.WORKED_REMINDERS, "");
        JSONArray remindersArray;
        try {
            remindersArray = new JSONArray(reminders);
            workedReminder = remindersArray.length();
        } catch (JSONException e) {
            Log.d("Worked Reminders", "No previous reminders");
        }
        return workedReminder;
    }

    private void startTimer(final JSONObject nextReminder, final int workedRemindersBefore) {

        Intent intent = new Intent("OkVisibility");
        intent.putExtra("visibility", "show");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        preferences.edit().putBoolean("showOk", true).commit();

        System.out.println("sending broadcast");

        new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                int workedRemindersAfter = getWorkedReminders();
                if (workedRemindersAfter == workedRemindersBefore) {
                    addMissedReminder(nextReminder, false);
                    Intent intent = new Intent("OkVisibility");
                    intent.putExtra("visibility", "hide");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                    Intent intentUpdateMissedFragment = new Intent("update");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentUpdateMissedFragment);

                    preferences.edit().putString(GlobalConstants.CURRENT_REMINDER, "").commit();
                    preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "false").commit();
                    preferences.edit().putBoolean("showOk", false).commit();

                }
            }
        }.start();
    }

    private void addMissedReminder(JSONObject nextReminder, boolean clearList) {

        if (clearList) {
            preferences.edit().putString(GlobalConstants.MISSED_REMINDERS, "").commit();
        } else {
            try {
                String reminders = preferences.getString(GlobalConstants.MISSED_REMINDERS, "");
                JSONArray remindersArray;
                try {
                    remindersArray = new JSONArray(reminders);
                    remindersArray.put(nextReminder);
                } catch (JSONException e) {
                    remindersArray = new JSONArray();
                    remindersArray.put(nextReminder);
                    Log.d("save missed reminder", "First missed reminder");
                }
                Log.d("Save Reminder", nextReminder.toString());

                preferences.edit().putString(GlobalConstants.MISSED_REMINDERS, remindersArray.toString()).commit();
                preferences.edit().putString(GlobalConstants.CURRENT_REMINDER, "").commit();
                preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "false").commit();
                sendSMS(nextReminder);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSMS(JSONObject nextReminder) {

        String name = "Name";
        try {
            name = nextReminder.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String message = "";
        try {
            String time = nextReminder.getJSONObject("time").getString("hour") + ":";// + nextReminder.getJSONObject("time").getString("minutes");

            int minutes = Integer.valueOf(nextReminder.getJSONObject("time").getString("minutes"));
            if (minutes < 10) {
                time = time + "0" + minutes;
            } else {
                time = time + minutes;
            }

            message = "Alarm missed at: " + time + ". " + name;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String mobileNumber = preferences.getString("contactNumber", "9985238771");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmap() {
        return new BitmapFactory().decodeResource(context.getResources(), R.drawable.mother);
    }

}
