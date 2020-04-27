package com.tipthecrate.tipthecrate;

/*
 * File: HowToPlayActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Used to display and navigate between the pages of the how to play activity
 */

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class HowToPlayActivity extends GameActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get whether sounds are turned on or off in preferences
        setSharedPreferences();
        setIsClick();
        setIsGameSounds();
        if (isClick || isGameSounds)
        {
            setSoundPool();

            if (isClick)
            {
                setClick();
            }

            if (isGameSounds)
            {
                setFanfare();
            }
        }

        // Display the the first how to play screen
        setPageZero();
    }

    // Called when the activity is destroyed
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Clear the sound pool from the memory
        releaseSoundPool();
    }

    // Displays the first page of HowToPlayActivity
    private void setPageZero()
    {
        // Display the the first how to play screen
        setContentView(R.layout.activity_how_to_play_0);

        // Load images
        setImageBackground();

        ImageView imageView0 = findViewById(R.id.imageView0);
        ImageView imageView1 = findViewById(R.id.imageView1);

        Glide.with(this)
                .load(R.drawable.crate_top_brown_hr)
                .into(imageView0);

        Glide.with(this)
                .load(R.drawable.how_to_play_image_0)
                .into(imageView1);

        // Access the button to move to the next page
        ImageView buttonNext = findViewById(R.id.buttonNext);

        // Change the image on the button
        Glide.with(this)
                .load(R.drawable.button_next)
                .into(buttonNext);

        // Create the link to the next page
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();
                setPageOne();
            }
        });
    }

    // Displays the first page of HowToPlayActivity
    private void setPageOne()
    {
        // Display the the first how to play screen
        setContentView(R.layout.activity_how_to_play_1);

        // Load the images
        setImageBackground();

        ImageView imageView0 = findViewById(R.id.imageView0);
        ImageView imageView1 = findViewById(R.id.imageView1);

        Glide.with(this)
                .load(R.drawable.crate_top_brown_hr)
                .into(imageView0);

        Glide.with(this)
                .load(R.drawable.crate_top_yellow_hr)
                .into(imageView1);

        // Access the buttons
        ImageView buttonPrevious = findViewById(R.id.buttonPrevious);
        ImageView buttonNext = findViewById(R.id.buttonNext);

        // Change the image on the button to move to the previous page
        Glide.with(this)
                .load(R.drawable.button_previous)
                .into(buttonPrevious);

        // Create the link to the previous page
        buttonPrevious.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();
                setPageZero();
            }
        });

        // Change the image on the button to move to the next page
        Glide.with(this)
                .load(R.drawable.button_next)
                .into(buttonNext);

        // Create the link to the next page
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();
                setPageTwo();
            }
        });
    }

    // Displays the first page of HowToPlayActivity
    private void setPageTwo()
    {
        // Display the the first how to play screen
        setContentView(R.layout.activity_how_to_play_2);

        // Load the images
        setImageBackground();

        ImageView imageView0 = findViewById(R.id.imageView0);
        ImageView imageView1 = findViewById(R.id.imageView1);

        Glide.with(this)
                .load(R.drawable.how_to_play_image_1)
                .into(imageView0);

        Glide.with(this)
                .load(R.drawable.how_to_play_image_2)
                .into(imageView1);

        // Access the buttons
        ImageView buttonPrevious = findViewById(R.id.buttonPrevious);
        ImageView buttonNext = findViewById(R.id.buttonNext);

        // Change the image on the button to move to the previous page
        Glide.with(this)
                .load(R.drawable.button_previous)
                .into(buttonPrevious);

        // Create the link to the previous page
        buttonPrevious.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();
                setPageOne();
            }
        });

        // Change the image on the button to move to the next page
        Glide.with(this)
                .load(R.drawable.button_next)
                .into(buttonNext);

        // Create the link to the next page
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!isGameSounds)
                {
                    playClick();
                }
                setPageThree();
            }
        });
    }

    private void setPageThree()
    {
        // Display the the first how to play screen
        setContentView(R.layout.activity_how_to_play_3);

        // Load the background image
        setImageBackground();

        // Access the button to move to the previous page
        ImageView buttonPrevious = findViewById(R.id.buttonPrevious);

        // Change the image on the button
        Glide.with(this)
                .load(R.drawable.button_previous)
                .into(buttonPrevious);

        // Play the fanfare
        playFanfare();

        // Create the link to the previous page
        buttonPrevious.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();
                setPageTwo();
            }
        });
    }

    // Loads the background image
    private void setImageBackground()
    {
        ImageView imageBackground = findViewById(R.id.imageBackground);

        Glide.with(this)
                .load(R.drawable.background)
                .into(imageBackground);
    }
}