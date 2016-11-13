package com.piro.bezier;

import com.badlogic.gdx.math.Vector2;


public class BezierHistory
{

    Vector2 startPoint = new Vector2();
    Vector2 lastPoint = new Vector2();
    Vector2 lastKnot = new Vector2();

    public BezierHistory()
    {
    }
    
    public void setStartPoint(float x, float y)
    {
        startPoint.set(x, y);
    }
    
    public void setLastPoint(float x, float y)
    {
        lastPoint.set(x, y);
    }
    
    public void setLastKnot(float x, float y)
    {
        lastKnot.set(x, y);
    }
}
