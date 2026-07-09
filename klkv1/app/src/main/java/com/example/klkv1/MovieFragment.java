package com.example.klkv1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MovieFragment extends Fragment {

    private RecyclerView recyclerMovies;
    private Button btnAdd;
    private Button btnRecord;

    private ArrayList<Movie> movies;
    private MovieAdapter adapter;

    public Button getBtnRecord() {
        return btnRecord;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        recyclerMovies = view.findViewById(R.id.recyclerMovies);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnRecord = view.findViewById(R.id.btnRecord);

        movies = new ArrayList<>();
        adapter = new MovieAdapter(movies);

        recyclerMovies.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMovies.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddMovieDialog());

        return view;
    }

    private void showAddMovieDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_movie, null);

        EditText etMovieName = dialogView.findViewById(R.id.etMovieName);
        EditText etMovieRating = dialogView.findViewById(R.id.etMovieRating);
        CheckBox cbWatched = dialogView.findViewById(R.id.cbWatched);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Dodaj film")
                .setView(dialogView)
                .setPositiveButton("Potvrdi", null)
                .setNegativeButton("Odustani", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnConfirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            btnCancel.setOnClickListener(v -> dialog.dismiss());

            btnConfirm.setOnClickListener(v -> {
                String name = etMovieName.getText().toString().trim();
                String ratingText = etMovieRating.getText().toString().trim();

                if (name.isEmpty()) {
                    etMovieName.setError("Unesite naziv filma");
                    return;
                }

                if (ratingText.isEmpty()) {
                    etMovieRating.setError("Unesite ocenu");
                    return;
                }

                int rating = Integer.parseInt(ratingText);
                boolean watched = cbWatched.isChecked();

                Movie movie = new Movie(name, rating, watched);
                movies.add(movie);
                adapter.notifyItemInserted(movies.size() - 1);

                Intent intent = new Intent("com.example.klkv1.MOVIE_ADDED");
                intent.putExtra("name", name);
                intent.putExtra("rating", rating);
                intent.setPackage(requireContext().getPackageName());
                requireContext().sendBroadcast(intent);

                dialog.dismiss();
            });
        });

        dialog.show();
    }
}