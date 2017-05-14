package com.piro.bezier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import eu.mihosoft.vrl.v3d.Vector3d;

public class BezierPath 
{
    
    static final Matcher matchPoint = Pattern.compile("\\s*(\\d+)[^\\d]+(\\d+)\\s*").matcher("");

    BezierListProducer path;
    
    /** Creates a new instance of Animate */
    public BezierPath()
    {
    }
    /** Creates a new instance of Animate */
    public BezierPath(String path)
    {
    	parsePathString(path); 
    }
    public void parsePathString(String d) {

        this.path = new BezierListProducer();

        parsePathList(d);
    }
    
    protected void parsePathList(String list)
    {
    	//Oh come on... well i have no idea what this regx is going to parse for, good luck
        final Matcher matchPathCmd = Pattern.compile("([MmLlHhVvAaQqTtCcSsZz])|([-+]?((\\d*\\.\\d+)|(\\d+))([eE][-+]?\\d+)?)").matcher(list);

        //Tokenize
        LinkedList<String> tokens = new LinkedList<String>();
        while (matchPathCmd.find())
        {
            tokens.addLast(matchPathCmd.group());
        }

        char curCmd = 'Z';
        while (tokens.size() != 0)
        {
            String curToken = tokens.removeFirst();
            char initChar = curToken.charAt(0);
            if ((initChar >= 'A' && initChar <= 'Z') || (initChar >= 'a' && initChar <= 'z'))
            {
                curCmd = initChar;
            } else
            {
                tokens.addFirst(curToken);
            }

            switch (curCmd)
            {
                case 'M':
                    path.movetoAbs(nextFloat(tokens), nextFloat(tokens));
                    curCmd = 'L';
                    break;
                case 'm':
                	path.movetoRel(nextFloat(tokens), nextFloat(tokens));
                    curCmd = 'l';
                    break;
                case 'L':
                    path.linetoAbs(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'l':
                	path.linetoRel(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'H':
                    path.linetoHorizontalAbs(nextFloat(tokens));
                    break;
                case 'h':
                	path.linetoHorizontalRel(nextFloat(tokens));
                    break;
                case 'V':
                    path.linetoVerticalAbs(nextFloat(tokens));
                    break;
                case 'v':
                	path.linetoVerticalAbs(nextFloat(tokens));
                    break;
                case 'A':
                case 'a':
                    break;
                case 'Q':
                    path.curvetoQuadraticAbs(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'q':
                	path.curvetoQuadraticAbs(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'T':
                    path.curvetoQuadraticSmoothAbs(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 't':
                	path.curvetoQuadraticSmoothRel(nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'C':
                    path.curvetoCubicAbs(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'c':
                	path.curvetoCubicRel(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'S':
                    path.curvetoCubicSmoothAbs(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 's':
                	path.curvetoCubicSmoothRel(nextFloat(tokens), nextFloat(tokens),
                        nextFloat(tokens), nextFloat(tokens));
                    break;
                case 'Z':
                case 'z':
                    path.closePath();
                    break;
                case '/':
                    //comment line
                    break;
                default:
                    throw new RuntimeException("Invalid path element");
            }
        }
    }
    
    static protected float nextFloat(LinkedList<String> l)
    {
        String s = l.removeFirst();
        return Float.parseFloat(s);
    }
    
    /**
     * Evaluates this animation element for the passed interpolation time.  Interp
     * must be on [0..1].
     */
    public Vector3d eval(float interp)
    {
    	Vector3d point=new Vector3d(0, 0);// = new Vector3d();
  
        
        double curLength = path.curveLength * interp;
        for (Iterator<Bezier> it = path.bezierSegs.iterator(); it.hasNext();)
        {
            Bezier bez = it.next();
            
            double bezLength = bez.getLength();
            if (curLength < bezLength)
            {
                double param = curLength / bezLength;
                point =bez.eval(param);
                break;
            }
            
            curLength -= bezLength;
        }
        
        return point;
    }

}
