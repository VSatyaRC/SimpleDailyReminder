package com.vsrc.ashareminder;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllRemindersFragment extends Fragment {


    public AllRemindersFragment() {
        // Required empty public constructor
    }

    View view;
    ArrayList<Integer> sortedTimesList = new ArrayList<>();
    SharedPreferences preferences;
    ArrayList<CharSequence> allRemindersList = new ArrayList<>();
    ListView missedListView;
    JSONArray allRemindersArray;
    TextView messageText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_all_reminders, container, false);
        missedListView = view.findViewById(R.id.allRemindersListView);
        messageText = view.findViewById(R.id.messageText);
        preferences = getActivity().getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE);
        fetchReminders();
        adapter = new ArrayAdapter<>(getActivity(), R.layout.reminder_item_layout, R.id.missedItem, allRemindersList);

        if (allRemindersList.size() > 0) {
            missedListView.setAdapter(adapter);
            missedListView.setVisibility(View.VISIBLE);
            messageText.setVisibility(View.GONE);
        } else {
            messageText.setText("Great! \nYou haven't anything missed so far.");
            messageText.setVisibility(View.VISIBLE);
            missedListView.setVisibility(View.GONE);
        }

        missedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteReminder(i);
                return false;
            }
        });
        return view;
    }


    private void fetchReminders() {
        String reminders = preferences.getString(GlobalConstants.REMINDERS, "");
        Log.d("All Reminders", "onResume: " + reminders);
        ArrayList<Integer> allRemindersTimesList = new ArrayList<>();

        try {
            allRemindersArray = new JSONArray(reminders);
            String reminder = "";
            for (int i = 0; i < allRemindersArray.length(); i++) {
                int nextReminderTime = Integer.valueOf(comingUpTime(allRemindersArray.getJSONObject(i).getJSONObject("time")).replaceAll(":", ""));
                allRemindersTimesList.add(nextReminderTime);
            }
            Collections.sort(allRemindersTimesList);
            sortedTimesList.addAll(allRemindersTimesList);
            Collections.reverse(allRemindersTimesList);

            while (allRemindersTimesList.size() > 0) {
                int nextSmallReminderTime = allRemindersTimesList.get(allRemindersTimesList.size() - 1);

                for (int i = 0; i < allRemindersArray.length(); i++) {
                    String time = comingUpTime(allRemindersArray.getJSONObject(i).getJSONObject("time"));
                    int nextReminderTime = Integer.valueOf(time.replaceAll(":", ""));
                    if (nextSmallReminderTime == nextReminderTime) {
                        reminder = "<b>Name: </b>" + allRemindersArray.getJSONObject(i).getString("name")
                                + "<br><br><b>Description:</b><br>" + allRemindersArray.getJSONObject(i).getString("description")
                                + "<br><br><b>Time:</b> " + time;

                        allRemindersTimesList.remove(allRemindersTimesList.size() - 1);
                        allRemindersList.add(Html.fromHtml(reminder));
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("Missed Reminders", "Empty");
        }
    }

    ArrayAdapter<CharSequence> adapter;

    private void deleteReminder(int position) {

        int selectedReminderTime = sortedTimesList.get(position);

        for (int i = 0; i < allRemindersArray.length(); i++) {
            try {
                if (selectedReminderTime == Integer.valueOf(comingUpTime(allRemindersArray.getJSONObject(i).getJSONObject("time")).replaceAll(":", ""))) {
                    ((ReminderActivity) getActivity()).saveOrDeleteReminder(allRemindersArray.getJSONObject(i), true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        fetchReminders();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private String comingUpTime(JSONObject timeObject) {

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

        String comingUpTime;

        if (String.valueOf(comingUpMinutes).length() == 1) {
            comingUpTime = comingUpHours + ":0" + comingUpMinutes;
        } else {
            comingUpTime = comingUpHours + ":" + comingUpMinutes;
        }
        return comingUpTime;
    }
}
