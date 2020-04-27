package com.tipthecrate.tipthecrate;

/*
 * File: GameActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Contains the rules of the game
 */

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener
{
    // The board has thirty-six squares
    public static final int NUMBER_OF_SQUARES = 36;

    // The height of different types of pillar
    public static final int PILLAR_HEIGHT_ONE = 1;
    public static final int PILLAR_HEIGHT_TWO = 2;
    public static final int PILLAR_HEIGHT_THREE = 3;
    public static final int PILLAR_HEIGHT_FOUR = 4;

    // Difficulty levels
    public static final int BEGINNER = 0;
    public static final int INTERMEDIATE = 1;
    public static final int ADVANCED = 2;
    public static final int EXPERT = 3;

    // Directions
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    // The difficulty level bands
    public static final int FINAL_LEVEL_BEGINNER = 10;
    public static final int FINAL_LEVEL_INTERMEDIATE = 20;
    public static final int FINAL_LEVEL_ADVANCED = 30;
    public static final int FINAL_LEVEL_ACHIEVEMENT = 100;

    // The position of levels in the file by difficulty
    public static final int FILE_BEGINNER_FIRST = 0;
    public static final int FILE_BEGINNER_LAST = 99;
    public static final int FILE_INTERMEDIATE_FIRST = 100;
    public static final int FILE_INTERMEDIATE_LAST = 179;
    public static final int FILE_ADVANCED_FIRST = 180;
    public static final int FILE_ADVANCED_LAST = 299;
    public static final int FILE_EXPERT_FIRST = 300;
    public static final int FILE_EXPERT_LAST = 379;

    // Variables to represent sprites, and colors in colors.xml
    // Some of these values could be defined locally but have been defined here to improve performance
    private int colorSquareEmpty;
    private int colorCrateTop;
    private int colorCrateSide;
    private int colorPlayerTop;
    private int colorPlayerSide;
    private int colorGoal;
    private static final int YELLOW = 0;
    public static final int RED = 1;

    // Variables that change as the game progresses
    public int level = 1;
    private static final int LAST_DISPLAYED_LEVEL = 99999;
    public int difficultyCurrent;
    protected BoardActivity boardCurrent;

    // Represents time
    public int time;
    public static int ONE_SECOND = 1000 /*milliseconds*/;
    public static final int TIME_ALL_TIME_CRATE = 3; // The maximum number of seconds to unlock 'all time crate' achievement
    public long timeLastCall = 0; // The time when a clock was last called
    public long timePaused = 0; // The time when the game moves to the background

    // The TextView widgets
    public TextView textLevel;
    public TextView textDifficulty;

    // The position of a players finger when they touch the screen
    // Defined here to ensure that they are assigned
    public int xStart = 0;
    public int yStart = 0;

    // Represents whether a continuous game is being played
    public static final boolean IS_GAME_CONTINUOUS = true;
    public static final boolean IS_NOT_GAME_CONTINUOUS = false;

    // Represents whether a practice game is being played
    public static final boolean IS_GAME_PRACTICE = true;
    public static final boolean IS_NOT_GAME_PRACTICE = false;

    // Assign data for an undo function
    public LinkedList<BoardActivity> undoList; // Assign data for a list to contain previous boards in a level
    public ImageView buttonUndo;
    public boolean isFirstMove = true; // Represent whether it is the first turn in a level

    // Assign data for a restartButton
    public ImageView buttonRestart;

    // The position of X and Y in an array
    public static final int POSITION_X = 0;
    public static final int POSITION_Y = 1;

    // Used to manage Google Play Services features
    protected GoogleApiClient mGoogleApiClient;

    // Define request codes for error reporting
    public static final int RC_ACHIEVEMENTS = 2;

    // Relates to a timer
    public Handler clockHandler;
    public Runnable clockRunnable;

    // Audio
    public SoundPool soundPool;

    // Relating to preferences
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor sharedPrefEditor;
    public static final String musicKey = "pref_music";
    public static final String clickKey = "pref_click";
    public static final String gameSoundsKey = "pref_game_sounds";
    public Boolean isMusic;
    public Boolean isClick;
    public Boolean isGameSounds;
    private int clickID;
    private int fanfareID;
    private int tauntID;

    // Reset the variables
    public void setVariables(boolean isGameContinuous)
    {
        // Create the GoogleApiClient
        createGoogleApiClient();

        // Get sprites
        setDrawables();

        // Set the first difficulty
        difficultyCurrent = BEGINNER;

        // If it is not a continuous game
        if(!isGameContinuous)
        {
            // Instantiate the undoList
            undoList = new LinkedList<>();
        }

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
    }

    // Used to access the drawables
    private void setDrawables()
    {
        // Add the images to activity_game.xml
        setImages();

        // Get the crate sprites
        colorSquareEmpty = R.drawable.crate_empty;
        colorCrateTop = R.drawable.crate_top_gray;
        colorCrateSide = R.drawable.crate_side_gray;
        colorPlayerTop = R.drawable.crate_top_brown;
        colorPlayerSide = R.drawable.crate_side_brown;
        colorGoal = R.drawable.crate_top_yellow;
    }

    // Adds the images to activity_game.xml
    private void setImages()
    {
        // Background
        ImageView imageBackground = findViewById(R.id.imageBackground);

        Glide.with(this)
                .load(R.drawable.background)
                .into(imageBackground);
    }

    // Link to the game preferences
    public void setSharedPreferences()
    {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    // Recognise whether the user wants to hear the click sound
    public void setIsClick()
    {
        isClick = sharedPref.getBoolean(clickKey, true);
    }

    // Recognise whether the user wants to hear the game sounds
    public void setIsGameSounds()
    {
        isGameSounds = sharedPref.getBoolean(gameSoundsKey, true);
    }

    // Create the sound pool
    public void setSoundPool()
    {
        // The sounds are part of a game and are triggered by user actions
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // Up to one sound will be played at a time
        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(1).build();
    }

    // Add the click sound to the sound pool
    public void setClick()
    {
        if (isClick)
        {
            clickID = soundPool.load(this, R.raw.click, 1);
        }
    }

    // Adds the sound for when the user wins a level to the sound pool
    public void setFanfare()
    {
        if (isGameSounds)
        {
            fanfareID = soundPool.load(this, R.raw.fanfare, 1);
        }
    }

    // Create a completable new level
    public void newLevel(boolean isContinuousGame, boolean isGamePractice)
    {
        // Change the level displayed
        if(!isGamePractice && (level <= LAST_DISPLAYED_LEVEL))
            textLevel.setText(getString(R.string.level, level));

        // Create a Random object
        Random random = new Random();

        int lineStop;

        switch(difficultyCurrent)
        {
            case BEGINNER:
                textDifficulty.setText(getResources().getString(R.string.difficulty_first));
                lineStop = FILE_BEGINNER_FIRST + random.nextInt((FILE_BEGINNER_LAST - FILE_BEGINNER_FIRST) + 1);
                break;

            case INTERMEDIATE:
                textDifficulty.setText(getResources().getString(R.string.difficulty_second));
                lineStop = FILE_INTERMEDIATE_FIRST + random.nextInt((FILE_INTERMEDIATE_LAST - FILE_INTERMEDIATE_FIRST) + 1);
                break;

            case ADVANCED:
                textDifficulty.setText(getResources().getString(R.string.difficulty_third));
                lineStop = FILE_ADVANCED_FIRST + random.nextInt((FILE_ADVANCED_LAST - FILE_ADVANCED_FIRST) + 1);
                break;

            case EXPERT:
                textDifficulty.setText(getResources().getString(R.string.difficulty_fourth));
                lineStop = FILE_EXPERT_FIRST + random.nextInt((FILE_EXPERT_LAST - FILE_EXPERT_FIRST) + 1);
                break;

            // Ensure that a level is always set
            default:
                textDifficulty.setText(getResources().getString(R.string.difficulty_first));
                lineStop = FILE_BEGINNER_FIRST + random.nextInt((FILE_BEGINNER_LAST - FILE_BEGINNER_FIRST) + 1);
                break;
        }

        try
        {
        // Open levels.txt for reading
        InputStream readLevels = this.getResources().openRawResource(R.raw.levels);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(readLevels));

            int line = 0;
            String level;

            while ((level = bufferedReader.readLine()) != null)
            {
                if (line == lineStop)
                {
                    break;
                }

                line++;
            }

            boardCurrent = new BoardActivity(new BoardActivity(level, lineStop), isContinuousGame);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } // newLevel()

    // Create a game state based on the pillar of crates tipped
    public BoardActivity createGameState(BoardActivity boardToCopy, int squareTippedID, int pillarHeight,
                                         int tipDirection, boolean isContinuousGame)
    {
        // Create a copy of the board
        BoardActivity newBoard = new BoardActivity(boardToCopy, squareTippedID , tipDirection, isContinuousGame);

        // Add the new board to the list of successors in a continuous game
        if(isContinuousGame)
            boardToCopy.addSuccessor(newBoard);

        // Tip the pillar of crates
        switch(tipDirection)
        {
            // Tip up
            case UP:
                // Populate the squares above
                for(int tipSquare = 1 ; tipSquare <= pillarHeight ; tipSquare++)
                {
                    newBoard.getSquare(squareTippedID - (tipSquare * 6)).setAttributes
                            (PILLAR_HEIGHT_ONE, true, false);
                }

                // Move the player to the end of the tipped pillar
                newBoard.setPlayerSquareID(squareTippedID - (6 * pillarHeight));
                break;

            // Tip right
            case RIGHT:
                // Populate the squares to the right
                for(int tipSquare = 1 ; tipSquare <= pillarHeight ; tipSquare++)
                {
                    newBoard.getSquare(squareTippedID + tipSquare).setAttributes
                            (PILLAR_HEIGHT_ONE, true, false);
                }

                // Move the player to the end of the tipped pillar
                newBoard.setPlayerSquareID(squareTippedID + pillarHeight);
                break;

            // Tip down
            case DOWN:
                // Populate the squares below
                for(int tipSquare = 1 ; tipSquare <= pillarHeight ; tipSquare++)
                {
                    newBoard.getSquare(squareTippedID + (tipSquare * 6)).setAttributes
                            (PILLAR_HEIGHT_ONE, true, false);
                }

                // Move the player to the end of the tipped pillar
                newBoard.setPlayerSquareID(squareTippedID + (6 * pillarHeight));
                break;

            // Tip left
            case LEFT:
                // Populate the squares to the left
                for(int tipSquare = 1 ; tipSquare <= pillarHeight ; tipSquare++)
                {
                    newBoard.getSquare(squareTippedID - tipSquare).setAttributes
                            (PILLAR_HEIGHT_ONE, true, false);
                }

                // Move the player to the end of the tipped pillar
                newBoard.setPlayerSquareID(squareTippedID - pillarHeight);
                break;
        }

        // Return the new board
        return newBoard;
    } // createGameState();

    // Label the squares that can be reached by the player
    public void setPlayerSquares(BoardActivity boardToCheck)
    {
        // Get the array of squares from boardToCheck
        SquareActivity[] squares = boardToCheck.getSquaresArray();

        // Initially assume that no squares contain the player
        for (SquareActivity boardSquare : squares)
        {
            // If player could previously access the square
            if(boardSquare.getPlayerHere())

                // Tell the square that the player can't access it
                boardSquare.setPlayerHere(false);
        }

        // Create a list of unchecked squares and add the square where the player is
        LinkedList<SquareActivity> uncheckedSquares = new LinkedList<>();
        uncheckedSquares.addLast(boardToCheck.getSquare(boardToCheck.getPlayerSquareID()));

        // Create a list of checked squares
        LinkedList<SquareActivity> checkedSquares = new LinkedList<>();

        // While the list of unchecked squares is not empty
        while(uncheckedSquares.size() > 0)
        {
            // Get the head of the list of unchecked squares
            SquareActivity checkNext = uncheckedSquares.remove(0);

            // Get the ID of the square being checked
            int checkNextID = checkNext.getID();

            // If the square is not in the top row of the board
            if(checkNextID >= 6)
            {
                // Access the square above
                SquareActivity squareReference = boardToCheck.getSquare(checkNextID - 6);

                // Add the square to the unchecked list if it contains a crate and hasn't been checked
                addPlayerSquareToList(squareReference, uncheckedSquares);
            }

            // If the square is not in the right column of the board
            if(((checkNextID + 1) % 6) != 0)
            {
                // Access the square to the right
                SquareActivity squareReference = boardToCheck.getSquare(checkNextID + 1);

                // Add the square to the unchecked list if it contains a crate and hasn't been checked
                addPlayerSquareToList(squareReference, uncheckedSquares);
            }

            // If the square is not in the bottom row of the board
            if(!(checkNextID >= 30))
            {
                // Access the square below
                SquareActivity squareReference = boardToCheck.getSquare(checkNextID + 6);

                // Add the square to the unchecked list if it contains a crate and hasn't been checked
                addPlayerSquareToList(squareReference, uncheckedSquares);
            }

            // If the square is not in the left column of the board
            if((checkNextID % 6) != 0)
            {
                // Access the square to the left
                SquareActivity squareReference = boardToCheck.getSquare(checkNextID - 1);

                // Add the square to the unchecked list if it contains a crate and hasn't been checked
                addPlayerSquareToList(squareReference, uncheckedSquares);
            }

            // Tell the square that it has been checked
            checkNext.setChecked(true);

            // Add the square to the list of checked squares
            checkedSquares.addLast(checkNext);
        }

        // Define that the player can access the squares and change their colour
        for (SquareActivity boardSquare : checkedSquares)
        {
            // Tell the square that the player can access it
            boardSquare.setPlayerHere(true);

            // Tell the square that it hasn't been checked
            boardSquare.setChecked(false);
        }
    } // setPlayerSquares()

    // Used to add a square that a player can reach to a list
    private void addPlayerSquareToList(SquareActivity square, LinkedList<SquareActivity> linkedList)
    {
        // If the square is occupied and hasn't been checked already
        if (square.getOccupied() && !(square.getChecked()))
        {
            // Add the given square to the end of the given list
            linkedList.addLast(square);
        }
    }

    // Determine whether the board is in a solution state
    // A player can access the goal crate in the solution state
    public boolean getSolution(BoardActivity boardToCheck)
    {
        return boardToCheck.getSquare(boardToCheck.getGoalSquareID()).getPlayerHere();
    }

    // Changes the color and labels on the board to match the square objects
    public void paintBoard(BoardActivity boardToPaint)
    {
        // An integer to represent the colour of a square
        int squareColor;

        // A string to represent the height of a pillar
        String heightString;

        // For each square
        for(int square = 0; square < NUMBER_OF_SQUARES; square++)
        {
            // Access the square in the display file
            String buttonID = "button" + square;
            int resourceID = getResources().getIdentifier(buttonID, "id", getPackageName());
            Button button = findViewById(resourceID);

            // Access the square in the board
            SquareActivity boardSquare = boardToPaint.getSquare(square);

            // If it is a goal square
            if(boardSquare.getID() == boardToPaint.getGoalSquareID())
            {
                squareColor = colorGoal;
                heightString = "";
            }

            // If it is an empty square
            else if(!boardSquare.getOccupied())
            {
                squareColor = colorSquareEmpty;
                heightString = "";
            }

            // If it is an unused player square
            else if(boardSquare.getPlayerHere() && boardSquare.getUp())
            {
                squareColor = colorPlayerTop;
                heightString = boardSquare.getHeightString();
            }

            // If it is a used player square
            else if(boardSquare.getPlayerHere() && !boardSquare.getUp())
            {
                squareColor = colorPlayerSide;
                heightString = boardSquare.getHeightString();
            }

            // If it is an unused non-player and non-goal square
            else if(!boardSquare.getPlayerHere() && boardSquare.getUp())
            {
                squareColor = colorCrateTop;
                heightString = boardSquare.getHeightString();
            }

            // If it is a used non-player and non-goal square
            else
            {
                squareColor = colorCrateSide;
                heightString = boardSquare.getHeightString();
            }

            // Change the colour of the square
            button.setBackgroundResource(squareColor);

            // Label the square with the height
            button.setText(heightString);
        }
    } // paintBoard()

    // Returns the cardinal direction in which the user moved their finger on the screen
    public int getTipDirection(int x1, int x2, int y1, int y2)
    {
        // The cardinal direction in which the user moved their finger
        int tipDirection;

        // Calculate the distance the user moved their finger in x and y
        int x = Math.abs(x2 - x1);
        int y = Math.abs(y2 - y1);

        // User flicked up
        if((y > x) && (y2 < y1))
            tipDirection = UP;

        // User flicked right
        else if((x > y) && (x2 > x1))
            tipDirection = RIGHT;

        // User flicked down
        else if((y > x) && (y2 > y1))
            tipDirection = DOWN;

        // User flicked left
        else if((x > y) && (x2 < x1))
            tipDirection = LEFT;

        // Any other direction
        else
            tipDirection = -1;

        return tipDirection;
    } // getTipDirection()

    // Change the difficulty based on the level reached
    public void setDifficultyCurrent()
    {
        // Beginner
        if (level <= FINAL_LEVEL_BEGINNER)
        {
            difficultyCurrent = BEGINNER;
        }

        // Intermediate
        else if ((level > FINAL_LEVEL_BEGINNER) && (level <= FINAL_LEVEL_INTERMEDIATE))
        {
            difficultyCurrent = INTERMEDIATE;
        }

        // Advanced
        else if ((level > FINAL_LEVEL_INTERMEDIATE) &&  (level <= FINAL_LEVEL_ADVANCED))
        {
            difficultyCurrent = ADVANCED;
        }

        // Expert
        else
        {
            difficultyCurrent = EXPERT;
        }
    } // setDifficultyCurrent()

    // Starts a non-continuous level
    public void startLevel(boolean isGamePractice)
    {
        // Begin a new level
        newLevel(IS_NOT_GAME_CONTINUOUS, isGamePractice);

        // Label the player's squares
        setPlayerSquares(boardCurrent);

        // Update the board on the display
        paintBoard(boardCurrent);
    }

    // Removes the undo button
    public void removeButtonUndo()
    {
        isFirstMove = true; // Recognise that it is the first turn of a new level
        buttonUndo.setOnClickListener(new View.OnClickListener() // Remove the listener from buttonUndo ...
        {
            public void onClick(View v)
            {
                // ... by giving it empty functionality
            }
        });
        buttonUndo.setVisibility(View.GONE); // Hide buttonUndo
    }

    // Ensures that only valid moves are made in a non-continuous version of the game
    public BoardActivity tipCrate(int tipDirection, int squareID, int squareHeight)
    {
        // boardCurrent will be replaced if a new board is created
        BoardActivity newBoard = boardCurrent;

        switch(tipDirection)
        {
            // The finger of the user moved up
            case UP:

                // Valid tip up with pillar of height two
                if((squareHeight == PILLAR_HEIGHT_TWO) && (squareID >= 12))
                {
                    if (!(boardCurrent.getSquare(squareID - 6).getOccupied() || boardCurrent.getSquare(squareID - 12).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_TWO, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip up with pillar of height three
                else if ((squareHeight == PILLAR_HEIGHT_THREE) && (squareID >= 18))
                {
                    if (!(boardCurrent.getSquare(squareID - 6).getOccupied() || boardCurrent.getSquare(squareID - 12).getOccupied() ||
                            boardCurrent.getSquare(squareID - 18).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_THREE, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip up with pillar of height four
                else if ((squareHeight == PILLAR_HEIGHT_FOUR) && (squareID >= 24))
                {
                    if (!(boardCurrent.getSquare(squareID - 6).getOccupied() || boardCurrent.getSquare(squareID - 12).getOccupied() ||
                            boardCurrent.getSquare(squareID - 18).getOccupied() || boardCurrent.getSquare(squareID - 24).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_FOUR, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Leave the loop
                break;

            // The finger of the user moved right
            case RIGHT:

                // Valid tip right with pillar of height two
                if((squareHeight == PILLAR_HEIGHT_TWO) && !((((squareID + 1) % 6) == 0) || (((squareID + 2) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID + 1).getOccupied() || boardCurrent.getSquare(squareID + 2).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_TWO, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip right with pillar of height three
                else if ((squareHeight == PILLAR_HEIGHT_THREE) && (((squareID % 6) == 0) || (((squareID - 1) % 6) == 0) ||
                            (((squareID - 2) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID + 1).getOccupied() || boardCurrent.getSquare(squareID + 2).getOccupied() ||
                            boardCurrent.getSquare(squareID + 3).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_THREE, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip right with pillar of height four
                else if ((squareHeight == PILLAR_HEIGHT_FOUR) && (((squareID % 6) == 0) || (((squareID - 1) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID + 1).getOccupied() || boardCurrent.getSquare(squareID + 2).getOccupied() ||
                            boardCurrent.getSquare(squareID + 3).getOccupied() || boardCurrent.getSquare(squareID + 4).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_FOUR, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Leave the loop
                break;

            // The finger of the user moved down
            case DOWN:

                // Valid tip down with pillar of height two
                if((squareHeight == PILLAR_HEIGHT_TWO) && (squareID < 24))
                {
                    if (!(boardCurrent.getSquare(squareID + 6).getOccupied() || boardCurrent.getSquare(squareID + 12).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_TWO, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }

                }

                // Valid tip down with pillar of height three
                else if ((squareHeight == PILLAR_HEIGHT_THREE) && (squareID < 18))
                {
                    if (!(boardCurrent.getSquare(squareID + 6).getOccupied() || boardCurrent.getSquare(squareID + 12).getOccupied() ||
                            boardCurrent.getSquare(squareID + 18).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_THREE, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip down with pillar of height four
                else if ((squareHeight == PILLAR_HEIGHT_FOUR) && (squareID < 12))
                {
                    if (!(boardCurrent.getSquare(squareID + 6).getOccupied() || boardCurrent.getSquare(squareID + 12).getOccupied() ||
                            boardCurrent.getSquare(squareID + 18).getOccupied() || boardCurrent.getSquare(squareID + 24).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_FOUR, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Leave the loop
                break;

            // The finger of the user moved left
            case LEFT:

                // Valid tip left with pillar of height two
                if ((squareHeight == PILLAR_HEIGHT_TWO) && !(((squareID % 6) == 0) || (((squareID - 1) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID - 1).getOccupied() || boardCurrent.getSquare(squareID - 2).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_TWO, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip left with pillar of height three
                else if ((squareHeight == PILLAR_HEIGHT_THREE) && ((((squareID + 1) % 6) == 0) || (((squareID + 2) % 6) == 0) ||
                            (((squareID + 3) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID - 1).getOccupied() || boardCurrent.getSquare(squareID - 2).getOccupied() ||
                            boardCurrent.getSquare(squareID - 3).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_THREE, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Valid tip left with pillar of height four
                else if ((squareHeight == PILLAR_HEIGHT_FOUR) && ((((squareID + 1) % 6) == 0) || (((squareID + 2) % 6) == 0)))
                {
                    if (!(boardCurrent.getSquare(squareID - 1).getOccupied() || boardCurrent.getSquare(squareID - 2).getOccupied() ||
                            boardCurrent.getSquare(squareID - 3).getOccupied() || boardCurrent.getSquare(squareID - 4).getOccupied()))
                    {
                        newBoard = createGameState(boardCurrent, squareID, PILLAR_HEIGHT_FOUR, tipDirection, IS_NOT_GAME_CONTINUOUS);
                    }
                }

                // Leave the loop
                break;
        }

        // Label the squares in the new board if a move was made
        if (newBoard != boardCurrent)
        {
            setPlayerSquares(newBoard);
        }

        return newBoard;
    } // tipCrate()

    // Resolves a move in a non-continuous version of the game
    public void resolveMove(BoardActivity newBoard)
    {
        // Add the current board to the end of the list of previous boards in this level
        undoList.addLast(boardCurrent);

        // Make the new board the current board
        boardCurrent = newBoard;

        // Update the display
        paintBoard(boardCurrent);

        playClick();

        // If it is the first turn of this level
        if(isFirstMove)
        {
            // Add the undo button to the display
            addButtonUndo();

            // Recognise that it is no longer the first turn of this level
            isFirstMove = false;
        }
    } // resolveMove()

    // Displays the undo button and add its functionality
    public void addButtonUndo()
    {
        setUndoButtonImage();
        buttonUndo.setVisibility(View.VISIBLE);
        buttonUndo.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Set the current board to be the previous board
                boardCurrent = undoList.removeLast();

                // Update the display to reflect the previous board
                paintBoard(boardCurrent);

                playClick();

                // Remove the button if undoList is empty
                if (undoList.size() < 1)
                {
                    removeButtonUndo(); // Remove buttonUndo
                }
            }
        });
    }

    // Get the position of a players finger
    public int[] getTouchXY(MotionEvent event)
    {
        int[] touchXY = {(int) event.getRawX(), (int) event.getRawY()};
        return touchXY;
    }

    // Can be used to change the color of the board
    public void setBoardColor(int givenColor)
    {
        // Get the board layout
        ConstraintLayout boardDisplay = findViewById(R.id.boardOuter);

        // Get the color to use for the board
        // The default is yellow
        int boardColor = R.color.yellow;

        // Change the color on request
        if (givenColor == RED)
            boardColor = R.color.red;

        // Change the colour of the board
        boardDisplay.setBackgroundResource(boardColor);
    }

    // Resets a game
    public void resetGame()
    {
        // Reset the color of the board
        setBoardColor(YELLOW);

        // Remove the restart button
        removeButtonRestart();

        // Reset the level and difficultyCurrent
        level = 1;
        difficultyCurrent = BEGINNER;
    }

    // Remove the restart button
    public void removeButtonRestart()
    {
        // Set a new listener that does not do anything
        buttonRestart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            }
        });

        // Hide the restart button
        buttonRestart.setVisibility(View.GONE);
    }

    // Create the GoogleApiClient
    public void createGoogleApiClient()
    {
        // Create the GoogleApiClient with access to Games if the service is available
        if(checkGooglePlayServices())
        {
            // Attempt to create the GoogleApiClient
            try
            {
                // Instantiate the GoogleApiClient
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        .build();
            }

            // Inform the user of an error
            catch(Exception e)
            {
                Toast toast = Toast.makeText(this, getText(R.string.google_created_not), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else
        {
            Toast toast = Toast.makeText(this, getText(R.string.google_unavailable), Toast.LENGTH_SHORT);
            toast.show();
        }
    } // createGoogleApiClient()

    // Reports errors if the Google Play Services are unavailable
    public boolean checkGooglePlayServices()
    {
        boolean isGooglePlayAvailable = false;

        // Get whether the Google Play Service is available
        GoogleApiAvailability googleAPIAvailability = GoogleApiAvailability.getInstance();
        int status = googleAPIAvailability.isGooglePlayServicesAvailable(this);

        // If the Google Play Service is available
        if (status == ConnectionResult.SUCCESS)
        {
            isGooglePlayAvailable = true;
        }

        // If the Google Play Service is not available
        else
        {
            // If the user can correct the error
            if (googleAPIAvailability.isUserResolvableError(status))
            {
                // Provide details
                googleAPIAvailability.getErrorDialog(this, status, 0).show();
            }

            // Instruct the user to update Google Play Services to attempt to solve any other error
            else
            {
                Toast toast = Toast.makeText(this, getText(R.string.google_update), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        return isGooglePlayAvailable;
    } // checkGooglePlayServices()

    // Connect to the Google Play Services
    public void connectGooglePlay()
    {
        try
        {
            if(!mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.connect();
            }
        }
        catch(Exception e)
        {
            Toast toast = Toast.makeText(this, getText(R.string.google_connect_not), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // Inform the user that the application is connecting to Google Play Services
    public void connectGooglePlayMessage()
    {
        Toast toast = Toast.makeText(this, getText(R.string.google_connecting), Toast.LENGTH_SHORT);
        toast.show();
    }

    // Disconnect from the Google Play Services
    public void disconnectGooglePlay()
    {
        try
        {
            if (mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.disconnect();
            }
        }
        catch(Exception e)
        {
            Toast toast = Toast.makeText(this, getText(R.string.google_disconnect_not), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // Common actions that result from failure
    public void resolveFailure()
    {
        // Paint the board red to indicate failure
        setBoardColor(RED);

        if(isGameSounds)
        {
            playTaunt();
        }
        else if(isClick)
        {
            playClick();
        }

        // Remove listeners from the board
        removeListeners();
    }

    // Removes listeners from the board
    private void removeListeners()
    {
        // For each button
        for(int square = 0; square < NUMBER_OF_SQUARES; square++)
        {
            // Access the button in the display file
            String buttonID = "button" + square;
            int resourceID = getResources().getIdentifier(buttonID, "id", getPackageName());
            Button button = findViewById(resourceID);

            // Set a new listener that does not do anything
            button.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    return true;
                }
            });
        }
    }

    // Clear undoList and remove buttonUndo
    public void removeUndo()
    {
        undoList.clear(); // Clear undoList
        removeButtonUndo(); // Remove buttonUndo
    }

    // Inform the user that the score was uploaded to the Google Play Services
    public void showScore(int score)
    {
        Toast toast = Toast.makeText(this, getString(R.string.scored, score), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void setClock()
    {
        // Manages a timer
        clockHandler = new Handler();
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                // Decrement the time
                time++;

                // Record the time
                timeLastCall = SystemClock.elapsedRealtime();

                // The method will run again in a second
                clockHandler.postDelayed(clockRunnable, ONE_SECOND);
            }
        } /* clockRunnable() */;
    }

    // Starts a timer
    public void startClock()
    {
        // Reset the timer
        time = 0;

        //Start the timer
        clockHandler.postDelayed(clockRunnable, ONE_SECOND);
    }

    // Unlocks 'All Time Crate' achievement if criteria are met
    public void unlockAllTimeCrateCriteria()
    {
        if (time < TIME_ALL_TIME_CRATE)
        {
            unlockAllTimeCrate();
        }
    }

    // Update progress towards 'All Time Crate' achievement
    public void unlockAllTimeCrate()
    {
        Games.Achievements.unlock(mGoogleApiClient,
                getResources().getString(R.string.achievement_all_time_crate));
    }

    // Stops a timer
    public void stopClock()
    {
        clockHandler.removeCallbacks(clockRunnable);
    }

    // Loads the image for the undo button
    public void setUndoButtonImage()
    {
        Glide.with(this)
                .load(R.drawable.button_undo)
                .into(buttonUndo);
    }

    // Loads the image for the restart button
    public void setRestartButtonImage()
    {
        Glide.with(this)
                .load(R.drawable.button_restart)
                .into(buttonRestart);
    }

    // Highlights that the user has completed a level
    public void congratulate()
    {
        if(isGameSounds)
        {
            playFanfare();
        }
        else if(isClick)
        {
            playClick();
        }

        Toast toast = Toast.makeText(this, getText(R.string.congratulations), Toast.LENGTH_SHORT);
        toast.show();
    }

    // Adds the sound for when the user loses a level to the sound pool
    public void setTaunt()
    {
        if (isGameSounds)
        {
            tauntID = soundPool.load(this, R.raw.taunt, 1);
        }
    }

    // Plays the click sound
    public void playClick()
    {
        if (isClick)
        {
            soundPool.play(clickID, 1, 1, 0, 0, 1);
        }
    }

    // Play the sound for when the user wins a level
    public void playFanfare()
    {
        if (isGameSounds)
        {
            soundPool.play(fanfareID, 1, 1, 0, 0, 1);
        }
    }

    // Play the sound for when the user loses a level
    public void playTaunt()
    {
        if (isGameSounds)
        {
            soundPool.play(tauntID, 1, 1, 0, 0, 1);
        }
    }

    // Recognise whether the user wants to hear music
    public void setIsMusic()
    {
        isMusic = sharedPref.getBoolean(musicKey, true);
    }

    // Clears the sound pool from the memory
    public void releaseSoundPool()
    {
        if (soundPool != null)
        {
            soundPool.release();
            soundPool = null;
        }
    }

    /* Used to manage connection to Google Play Services */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**/
}