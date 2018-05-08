package com.tipthecrate.tipthecrate;

/*
 * File: HowToPlayActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Used to display and navigate between the pages of the how to play activity
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class HowToPlayActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the the first how to play screen
        setContentView(R.layout.activity_how_to_play_0);
    }

    // Displays the first page of HowToPlayActivity
    public void getPageZero(View view)
    {
        setContentView(R.layout.activity_how_to_play_0);
    }

    // Displays the second page of HowToPlayActivity
    public void getPageOne(View view)
    {
        setContentView(R.layout.activity_how_to_play_1);
    }

    // Displays the third page of HowToPlayActivity
    public void getPageTwo(View view)
    {
        setContentView(R.layout.activity_how_to_play_2);
    }

    // Displays the fourth page of HowToPlayActivity
    public void getPageThree(View view)
    {
        setContentView(R.layout.activity_how_to_play_3);
    }
}