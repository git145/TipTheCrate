package com.tipthecrate.tipthecrate;

/*
 * File: GameContinuousActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Manages a continuous game
 */

import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.games.Games;
import java.util.LinkedList;

public class GameContinuousActivity extends GameActivity
{
    // Integers describing the states
    private static final int WIN_STATE = 0;
    private static final int MOVE_STATE = 1;
    private static final int FAIL_STATE = 2;

    // Recognise that the game is a continuous game
    private boolean isGameContinuous = IS_GAME_CONTINUOUS;

    // onCreate is always called first in an activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // 'super' is like 'this' but is used in an overridden method to access the functions of the
        // parent class rather than the child class
        super.onCreate(savedInstanceState);

        // Display the associated XML file on a device
        setContentView(R.layout.activity_game);

        // Reset variables
        setVariables(IS_GAME_CONTINUOUS);
        setVariablesGameContinuous();

        // Set the clock
        setClock();

        // Start the first level of the game
        startFirstLevelContinuous();
    }

    // onStart is called when the activity is presented to the user
    @Override
    protected void onStart()
    {
        super.onStart();

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

        // Upload the score to the leader board if it is the user's best
        uploadContinuousScore();

        // Disconnect from the Google Play Services
        disconnectGooglePlay();

        // Stop the timer
        stopClock();

        // Clear the sound pool from the memory
        releaseSoundPool();
    }

    // Sets variables before a continuous game
    private void setVariablesGameContinuous()
    {
        textLevel = findViewById(R.id.text0);
        textDifficulty = findViewById(R.id.text1);

        buttonRestart = findViewById(R.id.buttonRight);
        setRestartButtonImage();

        // Set the taunt sound
        if (isGameSounds)
        {
            setTaunt();
        }
    }

    // Begins the first level of a continuous game
    private void startFirstLevelContinuous()
    {
        // Begin a new continuous level
        startLevelContinuous();

        // Add functionality to buttons
        addListeners();
    }

    // Begins a new level
    private void startLevelContinuous()
    {
        // Begin a new level
        newLevel(isGameContinuous, IS_NOT_GAME_PRACTICE);

        // Create a computation tree of game states
        setLevelTree(boardCurrent);

        // Update the board on the display
        paintBoard(boardCurrent);

        // Start a new timer
        startClock();
    }

    // Creates the states reachable from the initial state and labels them with their type
    private void setLevelTree(BoardActivity initialBoard)
    {
        // A list representing the states
        LinkedList<BoardActivity> gameTree = new LinkedList<>();

        // A list of boards to check for valid moves
        LinkedList<BoardActivity> toCheck = new LinkedList<>();

        // Add the initial board to the list of boards to check
        toCheck.addLast(initialBoard);

        // Create a tree of states the player can reach
        // Add the predecessor and successors to each state
        while (toCheck.size() > 0)
        {
            // Create a variable to represent the board being investigated
            // Remove the head from toCheck
            BoardActivity current = toCheck.removeFirst();

            // Label the squares that the player can access
            setPlayerSquares(current);

            // Generate the boards that result from each available move in the current board
            for (int squareToCheck = 0; squareToCheck < NUMBER_OF_SQUARES; squareToCheck++)
            {
                // Access the square
                SquareActivity currentSquare = current.getSquare(squareToCheck);

                // If the square is useable (accessible by the player and has not been used previously)
                if (currentSquare.getUp() && currentSquare.getPlayerHere())
                {
                    // If the pillar is two crates high
                    if (currentSquare.getHeight() == PILLAR_HEIGHT_TWO)
                    {
                        // If there are two empty squares above
                        if (squareToCheck >= 12)
                            if (!(current.getSquare(squareToCheck - 6).getOccupied() || current.getSquare(squareToCheck - 12).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_TWO, UP, isGameContinuous));

                        // If there are two empty squares to the right
                        if (!((((squareToCheck + 1) % 6) == 0) || (((squareToCheck + 2) % 6) == 0)))
                            if (!(current.getSquare(squareToCheck + 1).getOccupied() || current.getSquare(squareToCheck + 2).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_TWO, RIGHT, isGameContinuous));

                        // If there are two empty squares below
                        if (squareToCheck < 24)
                            if (!(current.getSquare(squareToCheck + 6).getOccupied() || current.getSquare(squareToCheck + 12).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_TWO, DOWN, isGameContinuous));

                        // If there are two empty squares to the left
                        if (!(((squareToCheck % 6) == 0) || (((squareToCheck - 1) % 6) == 0)))
                            if (!(current.getSquare(squareToCheck - 1).getOccupied() || current.getSquare(squareToCheck - 2).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_TWO, LEFT, isGameContinuous));
                    }

                    // If the pillar is three crates high
                    else if (currentSquare.getHeight() == PILLAR_HEIGHT_THREE)
                    {
                        // If there are three empty squares above
                        if (squareToCheck >= 18)
                            if (!(current.getSquare(squareToCheck - 6).getOccupied() || current.getSquare(squareToCheck - 12).getOccupied() ||
                                    current.getSquare(squareToCheck - 18).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_THREE, UP, isGameContinuous));

                        // If there are three empty squares to the right
                        if (((squareToCheck % 6) == 0) || (((squareToCheck - 1) % 6) == 0) || (((squareToCheck - 2) % 6) == 0))
                            if (!(current.getSquare(squareToCheck + 1).getOccupied() || current.getSquare(squareToCheck + 2).getOccupied() ||
                                    current.getSquare(squareToCheck + 3).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_THREE, RIGHT, isGameContinuous));

                        // If there are three empty squares below
                        if (squareToCheck < 18)
                            if (!(current.getSquare(squareToCheck + 6).getOccupied() || current.getSquare(squareToCheck + 12).getOccupied() ||
                                    current.getSquare(squareToCheck + 18).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_THREE, DOWN, isGameContinuous));

                        // If there are three empty squares to the left
                        if ((((squareToCheck + 1) % 6) == 0) || (((squareToCheck + 2) % 6) == 0) || (((squareToCheck + 3) % 6) == 0))
                            if (!(current.getSquare(squareToCheck - 1).getOccupied() || current.getSquare(squareToCheck - 2).getOccupied() ||
                                    current.getSquare(squareToCheck - 3).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_THREE, LEFT, isGameContinuous));
                    }

                    // If the pillar is four crates high
                    else if (currentSquare.getHeight() == PILLAR_HEIGHT_FOUR)
                    {
                        // If there are four empty squares above
                        if (squareToCheck >= 24)
                            if (!(current.getSquare(squareToCheck - 6).getOccupied() || current.getSquare(squareToCheck - 12).getOccupied() ||
                                    current.getSquare(squareToCheck - 18).getOccupied() || current.getSquare(squareToCheck - 24).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_FOUR, UP, isGameContinuous));

                        // If there are four empty squares to the right
                        if (((squareToCheck % 6) == 0) || (((squareToCheck - 1) % 6) == 0))
                            if (!(current.getSquare(squareToCheck + 1).getOccupied() || current.getSquare(squareToCheck + 2).getOccupied() ||
                                    current.getSquare(squareToCheck + 3).getOccupied() || current.getSquare(squareToCheck + 4).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_FOUR, RIGHT, isGameContinuous));

                        // If there are four empty squares below
                        if (squareToCheck < 12)
                            if (!(current.getSquare(squareToCheck + 6).getOccupied() || current.getSquare(squareToCheck + 12).getOccupied() ||
                                    current.getSquare(squareToCheck + 18).getOccupied() || current.getSquare(squareToCheck + 24).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_FOUR, DOWN, isGameContinuous));

                        // If there are four empty squares to the left
                        if ((((squareToCheck + 1) % 6) == 0) || (((squareToCheck + 2) % 6) == 0))
                            if (!(current.getSquare(squareToCheck - 1).getOccupied() || current.getSquare(squareToCheck - 2).getOccupied() ||
                                    current.getSquare(squareToCheck - 3).getOccupied() || current.getSquare(squareToCheck - 4).getOccupied()))
                                toCheck.addLast(createGameState(current, squareToCheck, PILLAR_HEIGHT_FOUR, LEFT, isGameContinuous));
                    }
                }
            }

            // Add the tree just checked to the list of gameTrees
            gameTree.addLast(current);
        }

        // Create a list of states to label
        LinkedList<BoardActivity> statesToLabel = new LinkedList<>();
        for(BoardActivity boardToAdd : gameTree)
        {
            statesToLabel.addLast(boardToAdd);
        }

        // A list to remember boards to remove from statesToLabel after iterating through it
        LinkedList<Integer> toRemove =  new LinkedList<>();

        // Label solution states and remove from statesToLabel
        int statesToLabelLength = statesToLabel.size(); // Get the number of states not yet labelled
        for(int boardToCheck = 0 ; boardToCheck < statesToLabelLength ; boardToCheck++)
        {
            // Get the state
            BoardActivity stateToCheck = statesToLabel.get(boardToCheck);

            // If the state is a win state
            if(getSolution(stateToCheck))
            {
                // Label the state as a win state
                stateToCheck.setStateType(WIN_STATE);

                // Add to list to remove from the list of states still to label
                toRemove.addLast(boardToCheck);
            }
        }
        removeFromList(statesToLabel, toRemove); // Remove labelled states from statesToLabel

        // Label states where player can make a move, until no change, and remove from statesToLabel
        while(true)
        {
            // An integer representing the number of states changed on this iteration
            int statesChanged = 0;

            // Get the number of states not yet labelled
            statesToLabelLength = statesToLabel.size();

            // For all boards yet to be labelled
            for (int boardToCheck = 0 ; boardToCheck < statesToLabelLength ; boardToCheck++)
            {
                // Get the state
                BoardActivity stateToCheck = statesToLabel.get(boardToCheck);

                // List the successors of boardToCheck
                LinkedList<BoardActivity> successors = stateToCheck.getSuccessors();

                // For each successor
                for (BoardActivity successorToCheck : successors)
                {
                    // Get whether the state is a win state, move state or failure state
                    int stateType = successorToCheck.getStateType();

                    // If boardToCheck has a successor that is a win state or a move state
                    if(stateType == WIN_STATE || stateType == MOVE_STATE)
                    {
                        // Label boardToCheck as a move state
                        stateToCheck.setStateType(MOVE_STATE);

                        // Add to toRemove
                        toRemove.addLast(boardToCheck);

                        // Set that another state was changed in this iteration
                        statesChanged++;

                        // There is no need to check the state of remaining successors
                        break;
                    }
                }
            }

            // Leave the while loop if no states were changed on this iteration
            if (statesChanged < 1)
                break;

            // Remove labelled states from states still to label
            removeFromList(statesToLabel, toRemove);
        }

        // Label unlabelled states as failure states
        for (BoardActivity boardToLabel : statesToLabel)
        {
            boardToLabel.setStateType(FAIL_STATE);
        }
    } // setLevelTree()

    // Remove boards in given positions from a list
    private void removeFromList(LinkedList<BoardActivity> removeFrom, LinkedList<Integer> positionsToRemove)
    {
        while (true)
        {
            // Remove the last Integer from positionsToRemove and convert it to an int
            int toDelete = positionsToRemove.removeLast();

            // Remove the value
            removeFrom.remove(toDelete);

            // Stop when the list of positions is empty
            if (positionsToRemove.size() <= 0)
                break;
        }
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
            Button button = findViewById(resourceID);

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
                        // Record the position of the finger on the screen
                        int[] touchXY = getTouchXY(event);
                        xStart = touchXY[POSITION_X];
                        yStart = touchXY[POSITION_Y];
                    }

                    // User removes their finger from the button
                    if (event.getAction() == android.view.MotionEvent.ACTION_UP)
                    {
                        // Record the position of the finger on the screen
                        int[] touchXY = getTouchXY(event);

                        // Determine the cardinal direction in which the user moved their finger the most
                        int tipDirection = getTipDirection(xStart, touchXY[POSITION_X], yStart, touchXY[POSITION_Y]);

                        // If the user made a valid move
                        if((tipDirection >= UP) && (tipDirection <= LEFT))
                            makeContinuousMove(Integer.parseInt(v.getTag().toString()), tipDirection);
                    }

                    // onTouch() must return a boolean
                    return true;
                }
            });
        }
    } // addListeners()

    // Resolves a valid move
    private void makeContinuousMove(int buttonID, int direction)
    {
        // Get the list of successors from the current board
        LinkedList<BoardActivity> successors = boardCurrent.getSuccessors();

        // For each successor
        for(BoardActivity boardToCheck : successors)
        {
            // If a successor matches the move
            if((boardToCheck.getPredecessorCrateTippedID() == buttonID) && (boardToCheck.getPredecessorTipDirection() == direction))
            {
                // If the move results in a win state
                if(boardToCheck.getStateType() == WIN_STATE)
                {
                    // Proceed to the next level
                    wonContinuousLevel();

                    // Leave the loop
                    break;
                }

                // If the move results in a move state
                else if(boardToCheck.getStateType() == MOVE_STATE)
                {
                    // Update the board ready for the next turn
                    boardCurrent = boardToCheck;
                    paintBoard(boardCurrent);
                    playClick();

                    // Leave the loop
                    break;
                }

                // If the move results in a failure state
                else
                {
                    // Resolves the result of failure
                    endContinuousGame(boardToCheck);

                    // Leave the loop
                    break;
                }
            }
        }
    } // makeContinuousMove()

    // Update variables before a new continuous level
    private void wonContinuousLevel()
    {
        // Stop the clock
        stopClock();

        congratulate();

        // Update progress towards achievements
        checkAchievementsContinuous();

        // Increase the level
        level++;

        // Determine the difficulty
        setDifficultyCurrent();

        // Begin a new level
        startLevelContinuous();
    }

    // Update progress towards achievements
    private void checkAchievementsContinuous()
    {
        // If connected to the Google Play Services
        try
        {
            // Update progress towards 'Little Tipper' achievement
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_little_tipper));

            // Update progress towards 'Big Tipper' achievement
            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_big_tipper), RC_ACHIEVEMENTS);

            // Update achievements based on level reached
            if (level == FINAL_LEVEL_BEGINNER)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_leave_my_crates_alone));

            else if (level == FINAL_LEVEL_INTERMEDIATE)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_jack_in_the_box));

            else if (level == FINAL_LEVEL_ADVANCED)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_boxing_clever));

            else if (level == FINAL_LEVEL_ACHIEVEMENT)
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_youre_crate));

            // Unlocks 'All Time Crate' achievement if criteria are met
            unlockAllTimeCrateCriteria();
        }
        catch(Exception e)
        {
        }
    }

    // Resolves the result of failure
    private void endContinuousGame(BoardActivity boardToCheck)
    {
        // Stop the clock
        stopClock();

        // Update the board
        boardCurrent = boardToCheck;
        paintBoard(boardCurrent);

        // Perform common actions that result from failure
        resolveFailure();

        // Upload the score to the leader board if it is the user's best
        uploadContinuousScore();

        // Show the restart button
        buttonRestart.setVisibility(View.VISIBLE);
        buttonRestart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playClick();

                // Begin a new continuous game
                newContinuousGame();
            }
        });
    }

    // Upload the score to the leader board if it is the user's best
    private void uploadContinuousScore()
    {
        // Inform the user that the score was uploaded to the Google Play Services
        showScore(level - 1);

        // Upload the score to the leader board if it is the user's best
        if ((level > 1))
        {
                try
                {
                    Games.Leaderboards.submitScoreImmediate(mGoogleApiClient,
                            getResources().getString(R.string.leaderboard_continuous), (level - 1));
                }
                catch(Exception e)
                {
                }
        }
    }

    // Start a new continuous game
    private void newContinuousGame()
    {
        // Reset the game
        resetGame();

        // Begin a new continuous game
        startFirstLevelContinuous();
    }
}