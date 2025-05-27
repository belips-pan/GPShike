package com.example.gpshike;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WaypointsEditFragment extends Fragment {

    private static final String WAYPOINTS_FILE = "waypoints_cache.txt";
    private List<String> waypointsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Runnable onSaveListener;

    public void setOnSaveListener(Runnable listener) {
        this.onSaveListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_waypoints, container, false);

        ListView listView = view.findViewById(R.id.listView);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnDeleteAll = view.findViewById(R.id.btnDeleteAll);

        loadWaypoints();

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, waypointsList);
        listView.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            saveWaypoints();
            if (onSaveListener != null) {
                onSaveListener.run();
            }
            getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            waypointsList.add((waypointsList.size() + 1) + ",0.0,0.0,0.0,NEWPT," +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            adapter.notifyDataSetChanged();
        });

        btnDeleteAll.setOnClickListener(v -> {
            waypointsList.clear();
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    private void loadWaypoints() {
        try {
            File waypointsFile = new File(requireContext().getExternalFilesDir(null), WAYPOINTS_FILE);
            if (waypointsFile.exists()) {
                waypointsList = Files.lines(waypointsFile.toPath(), StandardCharsets.UTF_8)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error loading waypoints", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveWaypoints() {
        try {
            File waypointsFile = new File(requireContext().getExternalFilesDir(null), WAYPOINTS_FILE);
            Files.write(waypointsFile.toPath(),
                    waypointsList,
                    StandardCharsets.UTF_8);
            Toast.makeText(requireContext(), "Waypoints saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error saving waypoints", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}