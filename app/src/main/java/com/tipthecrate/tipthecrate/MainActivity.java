package com.tipthecrate.tipthecrate;

/*
 * File: MainActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Used to open the activities of the application from activity_main.xml
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.games.Games;

public class MainActivity extends GameActivity
{
    // Error code for leader boards
    private static final int RC_SCORE = 1;

    // onCreate is always called first in an activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the associated XML file
        setContentView(R.layout.activity_main);

        // Create the GoogleAPIClient
        createGoogleApiClient();
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();

        // Add functionality to the buttons
        addButtonFunctionality();
    }

    // Called when focus returns to the activity
    @Override
    public void onRestart()
    {
        super.onRestart();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();
    }

    // Called when the activity is hidden
    @Override
    public void onStop()
    {
        super.onStop();

        // Disconnect from Google Play Services
        disconnectGooglePlay();
    }

    // Adds functionality to the buttons in activity_main.xml
    private void addButtonFunctionality()
    {
        // Add functionality to the button to start the continuous game
        findViewById(R.id.buttonContinuous).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startGameContinuousActivity();
            }
        });

        // Add functionality to the button to start the timed game
        findViewById(R.id.buttonTimed).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startGameTimedActivity();
            }
        });

        // Add functionality to the button to start the how to play activity
        findViewById(R.id.buttonHowToPlay).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startHowToPlayActivity();
            }
        });

        // Add functionality to the button to start the practice game
        findViewById(R.id.buttonPractice).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                startGamePracticeActivity();
            }
        });

        // Add functionality to the button to display scores
        findViewById(R.id.buttonScore).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                getScore();
            }
        });

        // Add functionality to the button to display achievements
        findViewById(R.id.buttonAchievement).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                getAchievement();
            }
        });
    }

    //  Starts a continuous game
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

    // Displays the leader board for the application
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
}