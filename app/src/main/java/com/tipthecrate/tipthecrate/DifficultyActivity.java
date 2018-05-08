package com.tipthecrate.tipthecrate;

/*
 * File: DifficultyActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 19th July 2017
 * Description: Describes a difficulty level
 */

import android.support.v7.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity
{
    // The minimum and maximum number of pillars on the board and number of moves to complete the board
    private int pillarsMinimum;
    private int pillarsMaximum;
    private int movesMinimum;
    private int movesMaximum;
    private String name;

    // Default constructor
    public DifficultyActivity()
    {
    }

    // Used to construct a DifficultyActivity with the minimum and maximum number of pillars on the
    // board and number of moves to complete the board
    public DifficultyActivity(int newPillarsMinimum, int newPillarsMaximum, int newMovesMinimum, int newMovesMaximum, String newDescription)
    {
        pillarsMinimum = newPillarsMinimum;
        pillarsMaximum = newPillarsMaximum;
        movesMinimum = newMovesMinimum;
        movesMaximum = newMovesMaximum;
        name = newDescription;
    }

    // Returns the minimum number of pillars
    public int getPillarsMinimum() {
        return pillarsMinimum;
    }

    // Returns the maximum number of pillars
    public int getPillarsMaximum() {
        return pillarsMaximum;
    }

    // Returns the minimum number of moves
    public int getMovesMinimum() {
        return movesMinimum;
    }

    // Returns the maximum number of moves
    public int getMovesMaximum() {
        return movesMaximum;
    }

    // Returns the string representing the difficulty
    public String toString()
    {
        return name;
    }
}