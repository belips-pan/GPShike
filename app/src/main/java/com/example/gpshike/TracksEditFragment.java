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

public class TracksEditFragment extends Fragment {

    private static final String TRACKS_FILE = "tracks_cache.txt";
    private List<String> tracksList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Runnable onSaveListener;

    public void setOnSaveListener(Runnable listener) {
        this.onSaveListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_tracks, container, false);

        ListView listView = view.findViewById(R.id.listView);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnDeleteAll = view.findViewById(R.id.btnDeleteAll);

        loadTracks();

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, tracksList);
        listView.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            saveTracks();
            if (onSaveListener != null) {
                onSaveListener.run();
            }
            getParentFragmentManager().popBackStack();
        });

        btnAdd.setOnClickListener(v -> {
            tracksList.add((tracksList.size() + 1) + ",0.0,0.0,0.0,NEWTR," +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            adapter.notifyDataSetChanged();
        });

        btnDeleteAll.setOnClickListener(v -> {
            tracksList.clear();
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    private void loadTracks() {
        try {
            File tracksFile = new File(requireContext().getExternalFilesDir(null), TRACKS_FILE);
            if (tracksFile.exists()) {
                tracksList = Files.lines(tracksFile.toPath(), StandardCharsets.UTF_8)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error loading tracks", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveTracks() {
        try {
            File tracksFile = new File(requireContext().getExternalFilesDir(null), TRACKS_FILE);
            Files.write(tracksFile.toPath(),
                    tracksList,
                    StandardCharsets.UTF_8);
            Toast.makeText(requireContext(), "Tracks saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error saving tracks", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}