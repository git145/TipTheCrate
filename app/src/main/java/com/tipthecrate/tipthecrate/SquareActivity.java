package com.tipthecrate.tipthecrate;

/*
 * File: SquareActivity.java
 * Author: Richard Kneale
 * Student ID: 200790336
 * Date created: 3rd July 2017
 * Description: Describes a square
 */

import android.support.v7.app.AppCompatActivity;

public class SquareActivity extends AppCompatActivity
{
    // Defines whether the square is empty or not
    private boolean isOccupied = false;

    // A square that is up can be tipped over if all conditions are met
    // A square that is not up (down) can be stood up if all conditions are met
    private boolean isUp = false;

    // Is this square in a part of the board that the player can access
    private boolean isPlayerHere = false;

    // Used to determine if the square has been checked in search algorithms
    private boolean isChecked = false;

    // Defines the position of the square on the board
    private int iD;

    // The height of a pillar of crates in this square
    private static final String EMPTY_SQUARE_STRING = "";
    private int height = 0;
    private String heightString = EMPTY_SQUARE_STRING;

    // Default constructor
    public SquareActivity()
    {
    }

    // Used to construct a square with given ID
    public SquareActivity(int squareID)
    {
        iD = squareID;
    }

    // Used to construct a square given an ID, height, whether it is occupied and whether it is up
    public SquareActivity(int squareID, int squareHeight, boolean squareOccupied, boolean squareUp)
    {
        iD = squareID;
        setHeight(squareHeight);
        isOccupied = squareOccupied;
        isUp = squareUp;
    }

    // Return an integer representation of the ID of the square
    public int getID()
    {
        return iD;
    }

    // Return a string representation of the ID of the square
    public String toString()
    {
        return String.valueOf(iD);
    }

    // Set the height of a square, whether it is occupied and whether it is up
    public void setAttributes(int pillarHeight, boolean occupied, boolean up)
    {
        setHeight(pillarHeight);
        isOccupied = occupied;
        isUp = up;
    }

    // Returns whether the player can access the square
    public boolean getPlayerHere()
    {
        return isPlayerHere;
    }

    // Define whether the player can access it
    public void setPlayerHere(boolean playerHere)
    {
        isPlayerHere = playerHere;
    }

    // Returns whether the square has been used in a traversal algorithm
    public boolean getChecked()
    {
        return isChecked;
    }

    // Define whether the square has been used in a traversal algorithm
    public void setChecked(boolean checked)
    {
        isChecked = checked;
    }

    // Returns whether the square is occupied (not empty) or not occupied (empty)
    public boolean getOccupied()
    {
        return isOccupied;
    }

    // Returns the height of the square
    public int getHeight()
    {
        return height;
    }

    // Returns a string representation of the height of the square
    public String getHeightString()
    {
        return heightString;
    }

    // Sets the integer and string representation of the height
    public void setHeight(int pillarHeight)
    {
        height = pillarHeight;

        if(height > 1)
        {
            heightString = String.valueOf(height);
        }
        else
        {
            heightString = EMPTY_SQUARE_STRING;
        }
    }

    // Returns whether the pillar of crates is up
    public boolean getUp()
    {
        return isUp;
    }

    // Define whether the pillar of crates is up
    public void setUp(boolean up) { isUp = up; }
}