package com.example.myapplication.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antitheft.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<NotificationItem> notifications = loadNotificationsFromPrefs(); // Implement this method to load notifications
        NotificationAdapter adapter = new NotificationAdapter(notifications);
        binding.recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerNotifications.setAdapter(adapter);

        binding.btnClearNotifications.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "Clearing Notifications", Toast.LENGTH_SHORT).show();
            clearAllNotifications(); // Clears SharedPreferences and refreshes the RecyclerView
            adapter.notifications.clear();
            adapter.notifyDataSetChanged();
        });

        return root;
    }

    private List<NotificationItem> loadNotificationsFromPrefs() {
        List<NotificationItem> notifications = new ArrayList<>();
        SharedPreferences prefs = getActivity().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().endsWith("_title")) {
                String title = (String) entry.getValue();
                String messageKey = entry.getKey().replace("_title", "_message");
                String message = prefs.getString(messageKey, "");
                String timestampKey = entry.getKey().replace("_title", "_timestamp");
                String timestamp = prefs.getString(timestampKey, "");

                notifications.add(new NotificationItem(title, message, timestamp));
            }
        }

        // Sort the notifications list based on timestamp
        Collections.sort(notifications);

        return notifications;
    }


    private void clearAllNotifications() {
        // Clear SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Clear the RecyclerView and notify the adapter
        RecyclerView recyclerView = binding.recyclerNotifications;
        NotificationAdapter adapter = (NotificationAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifications.clear(); // Assuming your adapter exposes the notifications list
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}