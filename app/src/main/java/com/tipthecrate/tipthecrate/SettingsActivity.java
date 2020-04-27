package com.tipthecrate.tipthecrate;

/*
 * File: SettingsActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 15th August 2017
 * Description: User preferences can be altered through this activity
 */

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import com.bumptech.glide.Glide;

public class SettingsActivity extends GameActivity
{
    // Text images
    ImageView musicText;
    ImageView clickText;
    ImageView gameSoundsText;

    // Switches
    private Switch musicSwitch;
    private Switch clickSwitch;
    private Switch gameSoundsSwitch;

    // Related to animation
    private ObjectAnimator musicAnimation;
    private ObjectAnimator clickAnimation;
    private ObjectAnimator gameSoundsAnimation;

    // Related to audio
    private int bassID;
    private int motifID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the associated XML file
        setContentView(R.layout.activity_settings);

        // Load the background image
        setImageBackground();

        // Link to the text images
        musicText = findViewById(R.id.musicText);
        clickText = findViewById(R.id.clickText);
        gameSoundsText = findViewById(R.id.gameSoundsText);

        // Load the text images
        Glide.with(this)
                .load(R.drawable.text_music)
                .into(musicText);
        Glide.with(this)
                .load(R.drawable.text_click_sound)
                .into(clickText);
        Glide.with(this)
                .load(R.drawable.text_game_sounds)
                .into(gameSoundsText);

        // Set the animation for the text
        setAnimation();

        // Link to the preferences for editing
        setSharedPreferences();
        setSharedPreferencesEditor();

        // Read the current settings
        setIsMusic();
        setIsClick();
        setIsGameSounds();

        // Link to the switches
        musicSwitch = findViewById(R.id.musicSwitch);
        clickSwitch = findViewById(R.id.clickSwitch);
        gameSoundsSwitch = findViewById(R.id.gameSoundsSwitch);

        // Set the switches based on the current preferences
        if(isMusic)
        {
            musicSwitch.setChecked(true);
        }
        if(isClick)
        {
            clickSwitch.setChecked(true);
        }
        else
        {
            // To allow click sound in the settings menu
            isClick = true;
        }
        if(isGameSounds)
        {
            gameSoundsSwitch.setChecked(true);
        }
        else
        {
            // To allow some sound effects in the settings menu
            isGameSounds = true;
        }

        // Set sounds
        setSoundPool();
        setClick();
        setFanfare();
        setTaunt();
        bassID = soundPool.load(this, R.raw.bass, 1);
        motifID = soundPool.load(this, R.raw.motif, 1);

        // Add functionality to the buttons
        // Music button
        musicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    musicCheck();
                }
                else
                {
                    musicUncheck();
                }
            }
        });

        // Click button
        clickSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    clickCheck();
                }
                else
                {
                    clickUncheck();
                }
            }
        });

        // Game sounds buttons
        // Click buttons
        gameSoundsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    gameSoundsCheck();
                }
                else
                {
                    gameSoundsUncheck();
                }
            }
        });
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

        // Begin the animation on the main button
        musicAnimation.start();
        clickAnimation.start();
        gameSoundsAnimation.start();
    }

    // Called when the activity is destroyed
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Clear the sound pool from the memory
        releaseSoundPool();
    }

    // Loads the background image
    private void setImageBackground()
    {
        ImageView imageBackground = findViewById(R.id.imageBackground);

        Glide.with(this)
                .load(R.drawable.background)
                .into(imageBackground);
    }

    // Set the animations for the text
    private void setAnimation()
    {
        // Music text animation
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        musicAnimation =
                ObjectAnimator.ofPropertyValuesHolder(musicText, pvhX, pvhY);
        musicAnimation.setRepeatCount(Animation.INFINITE);
        musicAnimation.setRepeatMode(ValueAnimator.REVERSE);
        musicAnimation.setDuration(1000);

        // Click text animation
        PropertyValuesHolder pvhnX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f);
        PropertyValuesHolder pvhnY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f);
        clickAnimation =
                ObjectAnimator.ofPropertyValuesHolder(clickText, pvhnX, pvhnY);
        clickAnimation.setRepeatCount(Animation.INFINITE);
        clickAnimation.setRepeatMode(ValueAnimator.REVERSE);
        clickAnimation.setDuration(1000);

        // Game sounds text animation
        gameSoundsAnimation =
                ObjectAnimator.ofPropertyValuesHolder(gameSoundsText, pvhX, pvhY);
        gameSoundsAnimation.setRepeatCount(Animation.INFINITE);
        gameSoundsAnimation.setRepeatMode(ValueAnimator.REVERSE);
        gameSoundsAnimation.setDuration(1000);
    }

    // Link to the game preferences for editing
    private void setSharedPreferencesEditor()
    {
        sharedPrefEditor = sharedPref.edit();
    }

    private void musicCheck()
    {
        soundPool.play(motifID, 1, 1, 0, 0, 1);
        sharedPrefEditor.putBoolean(musicKey, true);
        sharedPrefEditor.apply();
    }

    private void musicUncheck()
    {
        soundPool.play(bassID, 1, 1, 0, 0, 1);
        sharedPrefEditor.putBoolean(musicKey, false);
        sharedPrefEditor.apply();
    }

    private void clickCheck()
    {
        playClick();
        sharedPrefEditor.putBoolean(clickKey, true);
        sharedPrefEditor.apply();
    }

    private void clickUncheck()
    {
        sharedPrefEditor.putBoolean(clickKey, false);
        sharedPrefEditor.apply();
    }

    private void gameSoundsCheck()
    {
        playFanfare();
        sharedPrefEditor.putBoolean(gameSoundsKey, true);
        sharedPrefEditor.apply();
    }

    private void gameSoundsUncheck()
    {
        playTaunt();
        sharedPrefEditor.putBoolean(gameSoundsKey, false);
        sharedPrefEditor.apply();
    }
}