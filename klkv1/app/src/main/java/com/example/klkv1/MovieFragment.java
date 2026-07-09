package com.example.klkv1;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MovieFragment extends Fragment {

    private RecyclerView recyclerView;
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

        Context context = requireContext();


        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);

        // RecyclerView
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(context)
        );


        movies = new ArrayList<>();


        adapter = new MovieAdapter(movies);
        recyclerView.setAdapter(adapter);


        LinearLayout buttonsLayout = new LinearLayout(context);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.CENTER);


        btnAdd = new Button(context);
        btnAdd.setText("Dodaj");


        btnRecord = new Button(context);
        btnRecord.setText("Snimi");


        btnRecord.setEnabled(false);


        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        buttonsLayout.addView(btnAdd, buttonParams);
        buttonsLayout.addView(btnRecord, buttonParams);


        root.addView(
                recyclerView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );


        root.addView(
                buttonsLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );


        btnAdd.setOnClickListener(v -> showAddMovieDialog());

        return root;
    }

    private void showAddMovieDialog() {

        Context context = requireContext();


        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);


        EditText etName = new EditText(context);
        etName.setHint("Naziv filma");


        EditText etRating = new EditText(context);
        etRating.setHint("Ocena");


        etRating.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );


        CheckBox cbWatched = new CheckBox(context);
        cbWatched.setText("Odgledano");


        layout.addView(etName);
        layout.addView(etRating);
        layout.addView(cbWatched);


        AlertDialog dialog =
                new AlertDialog.Builder(context)
                        .setTitle("Dodaj novi film")
                        .setView(layout)
                        .setPositiveButton("Potvrdi", null)
                        .setNegativeButton("Odustani", null)
                        .create();

        dialog.setOnShowListener(d -> {

            Button btnConfirm =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            Button btnCancel =
                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    );


            btnCancel.setOnClickListener(v ->
                    dialog.dismiss()
            );


            btnConfirm.setOnClickListener(v -> {

                String name =
                        etName.getText()
                                .toString()
                                .trim();

                String ratingText =
                        etRating.getText()
                                .toString()
                                .trim();

                if (name.isEmpty()) {
                    etName.setError(
                            "Unesite naziv filma"
                    );
                    return;
                }


                if (ratingText.isEmpty()) {
                    etRating.setError(
                            "Unesite ocenu"
                    );
                    return;
                }

                int rating;

                try {
                    rating = Integer.parseInt(
                            ratingText
                    );
                } catch (NumberFormatException e) {
                    etRating.setError(
                            "Ocena mora biti broj"
                    );
                    return;
                }

                boolean watched =
                        cbWatched.isChecked();


                Movie movie = new Movie(
                        name,
                        rating,
                        watched
                );


                movies.add(movie);


                adapter.notifyItemInserted(
                        movies.size() - 1
                );


                recyclerView.scrollToPosition(
                        movies.size() - 1
                );

                Intent intent = new Intent(
                        "com.example.klkv1.MOVIE_ADDED"
                );

                intent.putExtra(
                        "name",
                        name
                );

                intent.putExtra(
                        "rating",
                        rating
                );


                intent.setPackage(
                        requireContext().getPackageName()
                );

                requireContext().sendBroadcast(intent);

                Toast.makeText(
                        context,
                        "Film dodat",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            });
        });

        dialog.show();
    }
}