package com.tipthecrate.tipthecrate;

/*
 * File: MainActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Used to open the activities of the application from activity_main.xml
 */

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.gms.games.Games;

public class MainActivity extends GameActivity
{
    // Error code for leader boards
    private static final int RC_SCORE = 1;

    // Relates to the result when the button is clicked
    private int buttonResult = 0;
    private static final int NUMBER_OF_RESULTS = 7;
    private static final int RESULT_CONTINUOUS = 0;
    private static final int RESULT_TIMED = 1;
    private static final int RESULT_HOW_TO_PLAY = 2;
    private static final int RESULT_PRACTICE = 3;
    private static final int RESULT_SCORE = 4;
    private static final int RESULT_ACHIEVEMENT = 5;
    private static final int RESULT_SETTINGS = 6;

    // Related to animation
    private ObjectAnimator scaleAnimation;

    // View objects
    private ImageView imageBackground;
    private ImageView buttonMain;
    private ImageView buttonPrevious;
    private ImageView buttonNext;
    private ImageView nameSpace;

    // Relating to audio
    MediaPlayer mainTheme;

    // onCreate is always called first in an activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // The main activity must attempt to set default preferences
        setSharedPreferences();
        setIsMusic();
        setIsClick();

        // Display the associated XML file
        setContentView(R.layout.activity_main);

        // Access the View widgets
        getViews();

        // Set the graphics
        setGraphics();

        // Load the music
        if(isMusic)
        {
            setMusic();
        }

        // Load the click sound
        if(isClick)
        {
            setClickSound();
        }

        // Create the GoogleAPIClient
        createGoogleApiClient();

        // Add functionality to the buttons
        addButtonFunctionality();
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();

        // Add functionality to the buttons
        setLink();

        // Begin the animation on the main button
        scaleAnimation.start();

        // Play the music
        if(isMusic)
        {
            mainTheme.start();
        }
    }

    // Called when focus returns to the activity
    @Override
    public void onRestart()
    {
        super.onRestart();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();

        // Restart the music
        // Play the music
        if(isMusic)
        {
            setMusic();
            mainTheme.start();
        }
    }

    // Called when the activity is hidden
    @Override
    public void onStop()
    {
        super.onStop();

        // Disconnect from Google Play Services
        disconnectGooglePlay();

        // Clear the audio from the memory
        releaseAudio();
    }

    // Get references to the View widgets
    private void getViews()
    {
        imageBackground = findViewById(R.id.imageBackground);
        buttonMain = findViewById(R.id.buttonMain);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        nameSpace = findViewById(R.id.nameSpace);
    }

    // Loads the graphics
    private void setGraphics()
    {
        // Load the background image
        Glide.with(this)
                .load(R.drawable.background)
                .into(imageBackground);

        // Load the arrow buttons
        Glide.with(this)
                .load(R.drawable.button_previous)
                .into(buttonPrevious);

        Glide.with(this)
                .load(R.drawable.button_next)
                .into(buttonNext);
    }

    // Set up the MediaPlay for the main theme
    private void setMusic()
    {
        mainTheme = MediaPlayer.create(this, R.raw.main_theme);
        mainTheme.setLooping(true);
    }

    // Set up the soundPool and add the click sound
    private void setClickSound()
    {
        setSoundPool();
        setClick();
    }

    // Adds functionality to the buttons in activity_main.xml
    private void addButtonFunctionality()
    {
        // Add functionality to the activity links
        setLink();

        // Set the animation for the main button
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        scaleAnimation =
                ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.buttonMain), pvhX, pvhY);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnimation.setDuration(1000);

        // Add the functionality to the next and previous buttons
        buttonPrevious.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setLinkPrevious();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setLinkNext();
            }
        });
    }

    // Set the activity links to link to the next activity
    private void setLinkNext()
    {
        // Play the click sound
        playClickOnMain();

        // Link to the next activity
        buttonResult = (++buttonResult) % NUMBER_OF_RESULTS;

        // Update the main button
        setLink();
    }

    // Set the activity links to link to the previous activity
    private void setLinkPrevious()
    {
        // Play the click sound
        playClickOnMain();

        // Link to the previous activity
        buttonResult = (buttonResult + (NUMBER_OF_RESULTS - 1)) % NUMBER_OF_RESULTS;

        // Update the main button
        setLink();
    }

    // Updates the functionality of the main button
    private void setLink()
    {
        switch(buttonResult)
        {
            case(RESULT_CONTINUOUS):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_continuous)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_continuous)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startGameContinuousActivity();
                    }
                });

                break;

            case(RESULT_TIMED):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_timed)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_timed)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startGameTimedActivity();
                    }
                });

                break;

            case(RESULT_HOW_TO_PLAY):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_how_to_play)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_how_to_play)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startHowToPlayActivity();
                    }
                });

                break;

            case(RESULT_PRACTICE):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_practice)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_practice)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startGamePracticeActivity();
                    }
                });

                break;

            case(RESULT_SCORE):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_scores)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_score)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        getScore();
                    }
                });

                break;

            case(RESULT_ACHIEVEMENT):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_achievements)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_achievement)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        getAchievement();
                    }
                });

                break;

            case(RESULT_SETTINGS):
                // Change the image and link for the name space
                Glide.with(this)
                        .load(R.drawable.text_settings)
                        .into(nameSpace);

                // Change the image and link on the main button
                Glide.with(this)
                        .load(R.drawable.button_settings)
                        .into(buttonMain);

                buttonMain.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        startSettingsActivity();
                    }
                });

                break;
        }
    }

    // Plays the click sound
    private void playClickOnMain()
    {
        if(isClick)
        {
            if (soundPool != null)
            {
                playClick();
            }

            else
            {
                setSoundPool();
                setClick();
                playClick();
            }
        }
    }

    // Starts a continuous game
    private void startGameContinuousActivity()
    {
        Intent intent = new Intent(this, GameContinuousActivity.class);
        startActivity(intent);
    }

    // Starts a timed game
    private void startGameTimedActivity()
    {
        Intent intent = new Intent(this, GameTimedActivity.class);
        startActivity(intent);
    }

    // Starts the how to play activity
    private void startHowToPlayActivity()
    {
        Intent intent = new Intent(this, HowToPlayActivity.class);
        startActivity(intent);
    }

    // Starts a practice game
    private void startGamePracticeActivity()
    {
        Intent intent = new Intent(this, GamePracticeActivity.class);
        startActivity(intent);
    }

    // Displays the leader boards for the application
    private void getScore()
    {
        try // Show leader boards
        {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
                    RC_ACHIEVEMENTS);
        }
        catch(Exception e) // Inform the user that the application is connecting to Google Play Services
        {
            connectGooglePlayMessage();
        }
    }

    // Displays the achievements of a user
    private void getAchievement()
    {
        try // Show achievements
        {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    RC_SCORE);
        }
        catch(Exception e) // Inform the user that the application is connecting to Google Play Services
        {
            connectGooglePlayMessage();
        }
    }

    // Displays the settings page game
    private void startSettingsActivity()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // Clears the audio from the memory
    private void releaseAudio()
    {
        if (mainTheme != null)
        {
            mainTheme.release();
            mainTheme = null;
        }

        releaseSoundPool();
    }
}