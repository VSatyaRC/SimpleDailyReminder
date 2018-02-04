package com.vsrc.ashareminder;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MissedRemindersFragment extends Fragment {


    public MissedRemindersFragment() {
        // Required empty public constructor
    }

    View view;
    ListView missedListView;
    SharedPreferences preferences;
    TextView messageText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_missed_reminders, container, false);
        preferences = getActivity().getSharedPreferences(GlobalConstants.ALL_DATA, Context.MODE_PRIVATE);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("update"));
        return view;
    }

    private void displayMissedReminders() {
        missedListView = view.findViewById(R.id.missedListView);
        messageText = view.findViewById(R.id.messageText);
        String reminders = preferences.getString(GlobalConstants.MISSED_REMINDERS, "");
        Log.d("Missed Reminders", "onCreateView: " + reminders);
        ArrayList<CharSequence> missedList = new ArrayList<>();
        try {
            JSONArray missedRemindersArray = new JSONArray(reminders);
            String reminder = "";
            for (int i = 0; i < missedRemindersArray.length(); i++) {
                reminder = "<b>Name: </b>" + missedRemindersArray.getJSONObject(i).getString("name") + "<br><br><b>Description:</b><br>" +
                        missedRemindersArray.getJSONObject(i).getString("description") + "<br><br><b>Time:</b> " +
                        comingUpTime(missedRemindersArray.getJSONObject(i).getJSONObject("time"));
                missedList.add(Html.fromHtml(reminder));
            }
        } catch (JSONException e) {
            Log.d("Missed Reminders", "Empty");
        }
        if (missedList.size() > 0) {
            missedListView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.reminder_item_layout, R.id.missedItem, missedList));
            missedListView.setVisibility(View.VISIBLE);
            messageText.setVisibility(View.GONE);

        } else {
            messageText.setText("Great! \nYou haven't anything missed so far.");
            messageText.setVisibility(View.VISIBLE);
            missedListView.setVisibility(View.GONE);

        }
    }

    @Override
    public void onResume() {
        displayMissedReminders();
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayMissedReminders();
        }
    };

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
            comingUpTime = comingUpHours + " : 0" + comingUpMinutes;
        } else {
            comingUpTime = comingUpHours + " : " + comingUpMinutes;
        }
        return comingUpTime;
    }
}
