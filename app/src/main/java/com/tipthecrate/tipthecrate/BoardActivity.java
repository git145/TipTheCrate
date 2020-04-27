package com.tipthecrate.tipthecrate;

/*
 * File: BoardActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Describes a game board
 */

import android.support.v7.app.AppCompatActivity;
import java.util.Arrays;
import java.util.LinkedList;

public class BoardActivity extends AppCompatActivity
{
    // An ID to represent the order of the board in the .txt file, for debugging
    private int boardID = -1;

    // WIN_STATE = 0. MOVE_STATE = 1. LOSE_STATE = 2.
    private int stateType = -1;

    // The board has thirty-six squares
    private static final int NUM_SQUARES = 36;

    // An array representing the squares on the board
    private SquareActivity[] squares;

    // The move that was made to reach this state
    private int predecessorTipDirection = -1;
    private int predecessorCrateTippedID = -1;

    // These values can be derived but appear here to improve performance
    private int playerSquareID = -1; // The position of the player on the board
    private int goalSquareID = -1; // The ID of the square containing the goal crate

    // Used in the computation tree
    BoardActivity predecessor;
    LinkedList<BoardActivity> successors;

    // Default constructor
    public BoardActivity()
    {
    }

    // Construct a board based on a string of values
    public BoardActivity(String s, int iD)
    {
        // Tokenize the string
        LinkedList<String> values = new LinkedList<>(Arrays.asList(s.split(",")));

        // Instantiate the array of squares
        squares = new SquareActivity[NUM_SQUARES];

        // Instantiate the individual squares
        for(int square = 0 ; square < NUM_SQUARES ; square++)
        {
            squares[square] = new SquareActivity(square);

            // Convert the height to an integer
            int height = Integer.parseInt(values.get(square));

            // If the square contains a pillar of crates
            if(height > 0)
            {
                // Set the height of a square, that it is occupied and that it is up
                squares[square].setAttributes(height, true, true);
            }
        }

        // Set other statistics of the board
        playerSquareID = Integer.parseInt(values.get(38));
        goalSquareID = Integer.parseInt(values.get(39));
        boardID = iD;
    } // BoardActivity()

    // Create a board based on another board
    public BoardActivity(BoardActivity boardToCopy, boolean isContinuousGame)
    {
        // Activities common to all constructors
        setup(isContinuousGame);

        // Copy the board
        copyBoard(boardToCopy);

        // Copy other useful values
        boardID = boardToCopy.getBoardID();
        playerSquareID = boardToCopy.getPlayerSquareID();
    }

    // Create a board based on another board
    public BoardActivity(BoardActivity boardToCopy, int crateTipped, int directionOfTip, boolean isContinuousGame)
    {
        // Activities common to all constructors
        setup(isContinuousGame);

        // Copy the board
        copyBoard(boardToCopy);

        // Set properties of the board in a continuous game
        if(isContinuousGame)
        {
            predecessor = boardToCopy;
            predecessorCrateTippedID = crateTipped;
            predecessorTipDirection = directionOfTip;
        }

        // Make the starting square empty
        squares[crateTipped].setAttributes
                (0, false, false);
    }

    // Activities common to all constructors
    private void setup(boolean isContinuousGame)
    {
        // Instantiate the array of squares
        squares = new SquareActivity[NUM_SQUARES];

        // If the user is playing the continuous game
        if(isContinuousGame)
        {
            // Instantiate the list of successorStates
            successors = new LinkedList<>();
        }
    }

    // Copy a given board
    public void copyBoard(BoardActivity boardToCopy)
    {
        // Instantiate the individual squares
        for(int square = 0 ; square < NUM_SQUARES ; square++)
        {
            // Access the square from the given board
            SquareActivity copySquare = boardToCopy.getSquare(square);

            // Instantiate the square
            squares[square] = new SquareActivity(square, copySquare.getHeight(), copySquare.getOccupied(), copySquare.getUp());
        }

        // Copy the ID of the goal square
        goalSquareID = boardToCopy.goalSquareID;
    }

    // Returns a square from the board
    public SquareActivity getSquare(int square)
    {
        return squares[square];
    }

    // Returns the array of squares
    public SquareActivity[] getSquaresArray()
    {
        return squares;
    }

    // Set the position of the player
    public void setPlayerSquareID(int playerPosition)
    {
        playerSquareID = playerPosition;
    }

    // Returns the position of the player
    public int getPlayerSquareID()
    {
        return playerSquareID;
    }

    // Returns the position of the goal crate on the board
    public int getGoalSquareID()
    {
        return goalSquareID;
    }

    // Returns the ID of the board
    public int getBoardID()
    {
        return boardID;
    }

    // Add a successor to the LinkedList of successors
    public void addSuccessor(BoardActivity successorToAdd)
    {
        successors.addLast(successorToAdd);
    }

    // Returns whether the board is in a move state, win state or failure state
    public int getStateType() {
        return stateType;
    }

    // Set whether the board is in a move state, win state or failure state
    public void setStateType(int newStateType) {
        stateType = newStateType;
    }

    // Returns the LinkedList of successors
    public LinkedList<BoardActivity> getSuccessors() { return successors; }

    // Returns the direction in which a crate was tipped to reach this board
    public int getPredecessorTipDirection() {
        return predecessorTipDirection;
    }

    // Returns the ID of the crate that was tipped to reach this board
    public int getPredecessorCrateTippedID() { return predecessorCrateTippedID; }
}