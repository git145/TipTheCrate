package com.tipthecrate.tipthecrate;

/*
 * File: GameTimedActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 19th July 2017
 * Description: Manages a timed game
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.games.Games;

public class GameTimedActivity extends GameActivity
{
    // Represents time
    private static int TIME_START = 20 /*seconds*/;
    private int timeRed = TIME_START / 4 /*seconds*/;
    private static int NOW = 0 /*milliseconds*/;

    // Represents that it is not a continuous game or a practice game
    private boolean isGameContinuous = IS_NOT_GAME_CONTINUOUS;
    private boolean isGamePractice = IS_NOT_GAME_PRACTICE;

    // The score
    private int score;
    private static final int SCORE_LAST_DISPLAYED = 99999;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // 'super' is like 'this' but is used in an overridden method to access the functions of the
        // parent class rather than the child class
        super.onCreate(savedInstanceState);

        // Display the associated XML file on a device
        setContentView(R.layout.activity_timed_game);

        // Reset the variables in GameActivity
        setVariables(isGameContinuous, isGamePractice);

        // Start the first level of the game
        startFirstLevelTimed();
    }

    // Called when the activity moves to the background
    @Override
    public void onPause()
    {
        super.onPause();

        // Stop the timer
        stopTimer();

        // Record the time at which the game was paused
        timePaused = SystemClock.elapsedRealtime();
    }

    // Called when focus returns to the activity
    @Override
    public void onRestart()
    {
        super.onRestart();

        // Restart the timer
        timerHandler.postDelayed(timerRunnable, ONE_SECOND - (timePaused - timeLastCall));

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();
    }

    // Called when the activity is destroyed
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Upload the score to the leader board if it is the user's best
        uploadTimedScore();

        // Disconnect from the Google Play Services
        disconnectGooglePlay();

        // Stop the timer
        stopTimer();
    }

    // Begins the first level of a continuous game
    private void startFirstLevelTimed()
    {
        // Reset the score
        score = 0;
        setScore();

        // Create the first level
        startLevelTimed();

        // Add functionality to the buttons on the board
        addListeners();
    }

    // Set the score displayed on the interface
    private void setScore()
    {
        ((TextView)findViewById(R.id.textScore)).setText(getResources().getString(R.string.score) +
                ": " + score);
    }

    // Begins a new level
    private void startLevelTimed()
    {
        // Begin a new level
        newLevel(isGameContinuous, isGamePractice);

        // Label the squares that the player can access
        setPlayerSquares(boardCurrent);

        // Update the board on the display
        paintBoard(boardCurrent);

        // Start the timer
        startTimer();
    }

    // Starts the timer
    private void startTimer()
    {
        // Reset the timer
        time = TIME_START;

        // Start the timer
        timerHandler.postDelayed(timerRunnable, NOW);
    }

    // Manages the timer
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            // If time has expired
            if(time == 0)
            {
                // Resolve the result of failure
                endTimedGame();
            }
            else
            {
                // If the time should turn red on the display
                if(time == timeRed)
                {
                    ((TextView)findViewById(R.id.textTime)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                }

                // Change the time displayed on the interface and decrement the timer
                ((TextView)findViewById(R.id.textTime)).setText(String.valueOf(time--));

                // Record the time
                timeLastCall = SystemClock.elapsedRealtime();

                // The method will run again in a second
                timerHandler.postDelayed(this, ONE_SECOND);
            }
        }
    } /* timerRunnable() */;

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
                                        // Update the score
                                        score += (time + 1);
                                        if(score <= SCORE_LAST_DISPLAYED)
                                            setScore();

                                        // Begin the next level
                                        wonTimedLevel();
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

    // Update variables before a new timed level
    private void wonTimedLevel()
    {
        // Stop the timer
        stopTimer();

        // Update progress towards achievements in the timed game
        checkAchievementsTimed();

        // Clear undoList and remove buttonUndo
        removeUndo();

        // Increase the level
        level++;

        // Determine the difficulty
        setDifficultyCurrent();

        // Begin a new level
        startLevelTimed();
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

        // Connect to the Google Play Services if not already connected
        connectGooglePlay();
    }

    // Stops the timer
    private void stopTimer()
    {
        timerHandler.removeCallbacks(timerRunnable);
    }

    // Update progress towards achievements in the timed game
    private void checkAchievementsTimed()
    {
        // If connected to the Google Play Services
        try
        {
            // Update progress towards 'Wood Star' achievement
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_wood_star));

            // Update progress towards 'I've Run Out of Crate Related Puns' achievement
            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_ive_run_out_of_crate_related_puns), RC_ACHIEVEMENTS);

            // Update progress towards achievements based on time
            if(time == 0)
                Games.Achievements.unlock(mGoogleApiClient,
                        getResources().getString(R.string.achievement_health_and_safety_nightmare));

            else if(time >= (TIME_START - TIME_ALL_TIME_CRATE))
                unlockAllTimeCrate();

            // Update achievements based on level reached
            if(level == FINAL_LEVEL_BEGINNER)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_stack_attack));

            else if(level == FINAL_LEVEL_INTERMEDIATE)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_wood_you_believe_it));

            else if(level == FINAL_LEVEL_ADVANCED)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_thinking_outside_the_box));

            else if(level == FINAL_LEVEL_ACHIEVEMENT)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_crate_balls_of_fire));
        }
        catch(Exception e)
        {
        }
    }

    // Resolves the result of failure
    private void endTimedGame()
    {
        // Change the time displayed on the interface
        ((TextView)findViewById(R.id.textTime)).setText(String.valueOf(time));

        // Perform common actions that result from failure
        resolveFailure();

        // Clear undoList and remove buttonUndo
        removeUndo();

        // Upload the score to the leader board if it is the user's best
        uploadTimedScore();

        // Add a restart button
        (buttonRestart = (Button)findViewById(R.id.buttonRestart)).setVisibility(View.VISIBLE);
        buttonRestart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Set the timer to white on the display
                ((TextView)findViewById(R.id.textTime)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

                // Begin a new timed game
                newTimedGame();
            }
        });
    }

    // Upload the score to the leader board if it is the user's best
    public void uploadTimedScore()
    {
        // Inform the user of their score
        showScore(score);

        // Upload the score to the leader board if it is the user's best
        if ((score > 0))
        {
            try
            {
                Games.Leaderboards.submitScoreImmediate(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_timed), score);
            }
            catch(Exception e)
            {
            }
        }
    }

    // Starts a timed game
    private void newTimedGame()
    {
        // Reset the game
        resetGame();

        // Begin a new timed game
        startFirstLevelTimed();
    }

    /*
    // Called when the user presses the pause button
    private void pauseGame()
    {
        // Stop the timer
        stopTimer();

        // Record the time at which the game was paused
        timePaused = SystemClock.elapsedRealtime();
    }

    // Called when the user presses the pause button a second time
    private void resumeGame()
    {
        // Restart the timer
        timerHandler.postDelayed(timerRunnable, ONE_SECOND - (timePaused - timeLastCall));
    }
    */
}