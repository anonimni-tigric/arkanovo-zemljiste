package com.example.kolokvijum2.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.kolokvijum2.model.Country;

import java.util.List;

@Dao
public interface CountryDao {

    @Insert
    void insertAll(List<Country> countries);

    @Query("SELECT COUNT(*) FROM countries")
    int getCount();
}