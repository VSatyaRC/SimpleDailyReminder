package com.vsrc.ashareminder;


import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {


    public DashboardFragment() {
        // Required empty public constructor
    }

    View view;

    EditText newReminderName, contactName, contactNumber, newReminderText;
    String newName, newText, newTime;
    TextView reminderName, newReminderTime, changeContact, crntContact, addReminderFeedback, reminderDescription, reminderTime, later, addReminder;
    ImageView okay;
    JSONObject timePicked;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        addReminder = view.findViewById(R.id.addReminder);
        timePicked = new JSONObject();


        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("OkVisibility"));


        preferences = getActivity().getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE);

        newReminderName = view.findViewById(R.id.reminderName);

        contactName = view.findViewById(R.id.contactName);
        contactNumber = view.findViewById(R.id.mobileNumber);
        changeContact = view.findViewById(R.id.changeContact);
        crntContact = view.findViewById(R.id.contactPerson);

        newReminderText = view.findViewById(R.id.reminderText);
        newReminderTime = view.findViewById(R.id.timePicker);

        reminderName = view.findViewById(R.id.reminderTitle);
        reminderDescription = view.findViewById(R.id.reminderDescription);
        reminderTime = view.findViewById(R.id.reminderTime);
        addReminderFeedback = view.findViewById(R.id.addReminderFeedback);
        okay = view.findViewById(R.id.done);

        contactName.setText(preferences.getString("contactName", "Rishi"));
        crntContact.setText("Sending SMS to " + preferences.getString("contactName", "Rishi"));

        contactNumber.setText(preferences.getString("contactNumber", "9985238771"));

        newReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimePicker();
            }
        });

        addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reminder = validateReminder();

                if (!reminder.matches("false")) {
                    try {
                        ((ReminderActivity) getActivity()).saveOrDeleteReminder(new JSONObject(reminder), false);
                        newReminderName.setText("");
                        newReminderTime.setText("");
                        newReminderText.setText("");
                        addReminderFeedback.setText("New reminder added successfully.");
                        addReminderFeedback.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));

                        ((ReminderActivity) getActivity()).getNextReminder();
                        displayReminder(((ReminderActivity) getActivity()).nextReminder);

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Sorry! Error Saving.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        changeContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = contactName.getText().toString().trim(), number = contactNumber.getText().toString().trim();
                if (name.length() <= 0) {
                    Toast.makeText(getActivity(), "Invalid Name", Toast.LENGTH_SHORT).show();
                } else if (number.length() < 10) {
                    Toast.makeText(getActivity(), "Invalid Number", Toast.LENGTH_SHORT).show();
                } else {
                    ((ReminderActivity) getActivity()).saveContactDetails(name, number);
                    crntContact.setText("Sending SMS to " + name);
                }
            }
        });

        final JSONObject nextReminder = ((ReminderActivity) getActivity()).nextReminder;
        displayReminder(nextReminder);

//        if (((ReminderActivity) getActivity()).startTimer) {
//            startTimer(nextReminder);
//            okay.setVisibility(View.VISIBLE);
//            ((ReminderActivity) getActivity()).startTimer = false;
//        }

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveWorkedReminders(nextReminder);
                ((ReminderActivity) getActivity()).getNextReminder();
                displayReminder(((ReminderActivity) getActivity()).nextReminder);
                okay.setVisibility(View.GONE);
            }
        });
        return view;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String visibility = intent.getStringExtra("visibility");
            if (visibility.matches("show")) {
                okay.setVisibility(View.VISIBLE);
                preferences.edit().putBoolean("showOk", true).commit();
            } else {
                okay.setVisibility(View.GONE);
                ((ReminderActivity) getActivity()).getNextReminder();
                preferences.edit().putBoolean("showOk", false).commit();
                displayReminder(((ReminderActivity) getActivity()).nextReminder);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        if (preferences.getBoolean("showOk", false)) {
            okay.setVisibility(View.VISIBLE);
        } else {
            okay.setVisibility(View.GONE);

        }
        super.onResume();
    }

    private void saveWorkedReminders(JSONObject nextReminder) {

        try {
            try {
                okay.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String reminders = preferences.getString(GlobalConstants.WORKED_REMINDERS, "");
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
            preferences.edit().putString(GlobalConstants.WORKED_REMINDERS, remindersArray.toString()).commit();
            preferences.edit().putString(GlobalConstants.CURRENT_REMINDER, "").commit();
            preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "false").commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayReminder(JSONObject reminder) {
        try {
            reminderName.setText(reminder.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String time = "Time: " + reminder.getJSONObject("time").getString("hour") + ":";
            int minutes = Integer.valueOf(reminder.getJSONObject("time").getString("minutes"));

            if (minutes < 10) {
                time = time + "0" + minutes;
            } else {
                time = time + minutes;
            }
            reminderTime.setText(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            reminderDescription.setText(reminder.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String validateReminder() {

        newName = newReminderName.getText().toString().trim();
        newTime = newReminderTime.getText().toString().trim();
        newText = newReminderText.getText().toString().trim();

        if (newName.length() < 4) {
            Toast.makeText(getActivity(), "Enter a valid name.", Toast.LENGTH_SHORT).show();
            return "false";
        } else if (newTime.length() < 4) {
            Toast.makeText(getActivity(), "Pick a valid time.", Toast.LENGTH_SHORT).show();
            return "false";
        } else if (newText.length() < 4) {
            Toast.makeText(getActivity(), "Enter a valid description.", Toast.LENGTH_SHORT).show();
            return "false";
        } else {
            JSONObject reminder = new JSONObject();
            try {
                reminder.put("name", newName);
                reminder.put("time", timePicked);
                reminder.put("description", newText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return reminder.toString();
        }
    }


    public void startTimePicker() {

        Calendar calender = Calendar.getInstance();
        int hour = calender.get(Calendar.HOUR_OF_DAY);
        int minute = calender.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                JSONObject timeObject = new JSONObject();

                try {
                    timeObject.put("hour", selectedHour);
                    timeObject.put("minutes", selectedMinute);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Time Picker", "onTimeSet: " + timeObject.toString());
                timePicked = timeObject;
                if (selectedMinute < 10) {
                    newTime = selectedHour + " : 0" + selectedMinute;
                } else {
                    newTime = selectedHour + " : " + selectedMinute;
                }
                newReminderTime.setText(newTime);
                hideKeyBoard();

            }
        }, hour, minute, true);
        mTimePicker.show();
    }

    //    private void startTimer(final JSONObject nextReminder) {
//
//        new CountDownTimer(300000, 1000) {
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            public void onFinish() {
//                try {
//                    if (!reminderWorked) {
//                        try {
//                            okay.setVisibility(View.GONE);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        String reminders = preferences.getString(GlobalConstants.MISSED_REMINDERS, "");
//                        JSONArray remindersArray;
//                        try {
//                            remindersArray = new JSONArray(reminders);
//                            remindersArray.put(nextReminder);
//                        } catch (JSONException e) {
//                            remindersArray = new JSONArray();
//                            remindersArray.put(nextReminder);
//                            Log.d("save missed reminder", "First missed reminder");
//                        }
//                        Log.d("Save Reminder", nextReminder.toString());
//                        preferences.edit().putString(GlobalConstants.MISSED_REMINDERS, remindersArray.toString()).commit();
//                        preferences.edit().putString(GlobalConstants.CURRENT_REMINDER, "").commit();
//                        preferences.edit().putString(GlobalConstants.NOTIFICATIONS, "false").commit();
//
//
//                        String name = nextReminder.getString("name");
//                        String message = "";
//                        try {
//                            String time = nextReminder.getJSONObject("time").getString("hour") + ":";// + nextReminder.getJSONObject("time").getString("minutes");
//
//                            int minutes = Integer.valueOf(nextReminder.getJSONObject("time").getString("minutes"));
//                            if (minutes < 10) {
//                                time = time + "0" + minutes;
//                            } else {
//                                time = time + minutes;
//                            }
//
//                            message = "Alarm missed at: " + time + ". " + name;
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        SmsManager smsManager = SmsManager.getDefault();
//                        smsManager.sendTextMessage("9985238771", null, message, null, null);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
    public void hideKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
