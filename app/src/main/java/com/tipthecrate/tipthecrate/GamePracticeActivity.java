package com.tipthecrate.tipthecrate;

/*
 * File: GamePracticeActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 17th July 2017
 * Description: Manages a practice game
 */

import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.games.Games;

public class GamePracticeActivity extends GameActivity
{
    // Difficulty order
    private static final int LEVEL_ORDER_INTERMEDIATE = 2;
    private static final int LEVEL_ORDER_ADVANCED = 3;
    private static final int LEVEL_ORDER_EXPERT = 4;

    // Represents that it is a practice game
    private boolean isGamePractice = IS_GAME_PRACTICE;

    // onCreate is always called first in an activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // 'super' is like 'this' but is used in an overridden method to access the functions of the
        // parent class rather than the child class
        super.onCreate(savedInstanceState);

        // Display the associated XML file on a device
        setContentView(R.layout.activity_practice_game);

        // Reset the variables in GameActivity
        setVariables(IS_NOT_GAME_CONTINUOUS, isGamePractice);

        // Create the first level
        startLevel(isGamePractice);

        // Add functionality to buttons
        addListeners();
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

        // Start a new timer
        startClock();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();
    }

    // Called when the activity moves to the background
    @Override
    public void onPause()
    {
        super.onPause();

        // Stop the timer
        stopClock();

        // Record the time at which the game was paused
        timePaused = SystemClock.elapsedRealtime();
    }

    // Called when focus returns to the activity
    @Override
    public void onRestart()
    {
        super.onRestart();

        // Restart the timer
        clockHandler.postDelayed(clockRunnable, ONE_SECOND - (timePaused - timeLastCall));

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();
    }

    // Called when the activity is destroyed
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Disconnect from the Google Play Services
        disconnectGooglePlay();

        // Stop the timer
        stopClock();
    }

    // Add functionality to the buttons on the board
    private void addListeners()
    {
        // For each button
        for(int square = 0; square < NUMBER_OF_SQUARES; square++)
        {
            // Access the button in the display file
            String buttonID = "button" + square;
            int resourceID = getResources().getIdentifier(buttonID, "id", getPackageName());
            Button button = (Button) findViewById(resourceID);

            // Add an ID to the button
            button.setTag(square);

            // Add functionality to the button
            button.setOnTouchListener(new View.OnTouchListener()
            {
                // User applies their finger to the button
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    // User touches the button
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        // Convert the tag of the button touched to an integer
                        int squareID = Integer.parseInt(v.getTag().toString());

                        // Access the square that was touched
                        SquareActivity currentSquare = boardCurrent.getSquare(squareID);

                        // If the square is useable (accessible by the player and has not been used previously)
                        if (currentSquare.getUp() && currentSquare.getPlayerHere())
                        {
                            // Record the position of the finger on the screen
                            int[] touchXY = getTouchXY(event);
                            xStart = touchXY[POSITION_X];
                            yStart = touchXY[POSITION_Y];
                        }
                    }

                    // User removes their finger from the button
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        // Convert the tag of the button touched to an integer
                        int squareID = Integer.parseInt(v.getTag().toString());

                        // Access the square that was touched
                        SquareActivity currentSquare = boardCurrent.getSquare(squareID);

                        // If the square is useable (accessible by the player and has not been used previously)
                        if (currentSquare.getUp() && currentSquare.getPlayerHere())
                        {
                            // Record the position of the finger on the screen
                            int[] touchXY = getTouchXY(event);

                            // Determine the cardinal direction in which the user moved their finger the most
                            int tipDirection = getTipDirection(xStart, touchXY[POSITION_X], yStart, touchXY[POSITION_Y]);

                            // Generate a new board if the user made a valid move
                            if ((tipDirection >= UP) && (tipDirection <= LEFT))
                            {
                                // Generate a new board if the move was valid
                                BoardActivity newBoard = tipCrate(tipDirection, squareID, currentSquare.getHeight());

                                // If a move was made
                                if (newBoard != boardCurrent)
                                {
                                    // If the move solved the level
                                    if(getSolution(newBoard))
                                    {
                                        // Begin the next level
                                        wonLevelPractice();
                                    }

                                    // If the move did not solve the level
                                    else
                                    {
                                        // Resolve the effect of making a non-solution move
                                        resolveMove(newBoard);
                                    }
                                }
                            }
                        }
                    }

                    // onTouch() must return a boolean
                    return true;
                }
            });
        }
    } // addListeners()

    // Updates variables before a new level
    private void wonLevelPractice()
    {
        // Stop the timer
        stopClock();

        // Update progress towards achievements
        checkAchievementsPractice();

        // Clear undoList and remove buttonUndo
        removeUndo();

        // Increase the level
        level++;

        // Determine the difficultyCurrent
        setPracticeDifficulty();

        // Begin a new level
        startLevel(isGamePractice);

        // Start a new timer
        startClock();
    }

    // Update progress towards achievements
    private void checkAchievementsPractice()
    {
        // If connected to the Google Play Services
        try
        {
            // Update progress towards 'Practice Makes Perfect' achievement
            Games.Achievements.increment(mGoogleApiClient,
                    getResources().getString(R.string.achievement_practice_makes_perfect), RC_ACHIEVEMENTS);

            // Unlocks 'All Time Crate' achievement if criteria are met
            unlockAllTimeCrateCriteria();
        }
        catch(Exception e)
        {
        }
    }

    // Change the difficultyCurrent based on the level reached
    private void setPracticeDifficulty()
    {
        // Expert
        if ((level % LEVEL_ORDER_EXPERT) == 0)
        {
            difficultyCurrent = EXPERT;
        }

        // Intermediate
        else if ((level % LEVEL_ORDER_ADVANCED) == 0)
        {
            difficultyCurrent = ADVANCED;
        }

        // Advanced
        else if ((level % LEVEL_ORDER_INTERMEDIATE) == 0)
        {
            difficultyCurrent = INTERMEDIATE;
        }

        // Beginner
        else
        {
            difficultyCurrent = BEGINNER;
        }
    } // setPracticeDifficulty();
}