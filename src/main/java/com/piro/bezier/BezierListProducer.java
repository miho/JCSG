/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.piro.bezier;

import java.util.ArrayList;

/**
 * A handler class that generates an array of shorts and an array floats from
 * parsing path data.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id: PathArrayProducer.java 475685 2006-11-16 11:16:05Z cam $
 */
public class BezierListProducer implements PathHandler {

	final ArrayList<Bezier> bezierSegs = new ArrayList<Bezier>();
	float[] coords = new float[6];
	float curveLength = 0f;
	BezierHistory hist = new BezierHistory();

    @Override
    public void startPath() throws ParseException {
    	curveLength = 0f;
    	bezierSegs.clear();
    }

    @Override
    public void movetoRel(float x, float y) throws ParseException {
    	float offx = hist.lastPoint.x;
        float offy = hist.lastPoint.y;

        movetoAbs(offx + x, offy + y);;
    }

    @Override
    public void movetoAbs(float x, float y) throws ParseException {
    	hist.setLastPoint(x, y);
    }

    @Override
    public void closePath() throws ParseException {
        //command(SVGPathSeg.PATHSEG_CLOSEPATH);
    }

    @Override
    public void linetoRel(float x, float y) throws ParseException {
    	float offx = hist.lastPoint.x;
        float offy = hist.lastPoint.y;

        linetoAbs(offx + x, offy + y);
    }

    @Override
    public void linetoAbs(float x, float y) throws ParseException {
    	
    	coords[0] = x;
    	coords[1] = y;
    	
    	Bezier b = new Bezier(hist.lastPoint.x, hist.lastPoint.y, coords, 1);
    	bezierSegs.add(b);
    	curveLength += b.getLength();
    	
        hist.setLastPoint(x, y);
        hist.setLastKnot(x, y);

    }

    @Override
    public void linetoHorizontalRel(float x) throws ParseException {
    	linetoAbs(x + hist.lastPoint.x, hist.lastPoint.y);
    }

    @Override
    public void linetoHorizontalAbs(float x) throws ParseException {
    	linetoAbs(x, hist.lastPoint.y);
    }

    @Override
    public void linetoVerticalRel(float y) throws ParseException {
    	linetoAbs(hist.lastPoint.x, y + hist.lastPoint.y);
    }

    @Override
    public void linetoVerticalAbs(float y) throws ParseException {
    	linetoAbs(hist.lastPoint.x, y);
    }

    @Override
    public void curvetoCubicRel(float x1, float y1, 
                                float x2, float y2, 
                                float x, float y) throws ParseException {
    	float offx = hist.lastPoint.x;
        float offy = hist.lastPoint.y;
        
        curvetoCubicAbs(x1 + offx, y1 + offy,
                x2 + offx, y2 + offy, 
                x + offx, y + offy);
    }

    @Override
    public void curvetoCubicAbs(float x1, float y1, 
                                float x2, float y2, 
                                float x, float y) throws ParseException {
        
    	coords[0] = x1;
    	coords[1] = y1;
    	coords[2] = x2;
    	coords[3] = y2;
    	coords[4] = x;
    	coords[5] = y;
        
    	Bezier b = new Bezier(hist.lastPoint.x, hist.lastPoint.y, coords, 3);
    	bezierSegs.add(b);
    	curveLength += b.getLength();
        hist.setLastPoint(x, y);
        hist.setLastKnot(x2, y2);
    }

    @Override
    public void curvetoCubicSmoothRel(float x2, float y2, 
                                      float x, float y) throws ParseException {
    	float offx = hist.lastPoint.x;
        float offy = hist.lastPoint.y;

        curvetoCubicSmoothAbs(x2 + offx, y2 + offy, x + offx, y + offy);
    }

    @Override
    public void curvetoCubicSmoothAbs(float x2, float y2, 
                                      float x, float y) throws ParseException {

        float oldKx = hist.lastKnot.x;
        float oldKy = hist.lastKnot.y;
        float oldX = hist.lastPoint.x;
        float oldY = hist.lastPoint.y;
        //Calc knot as reflection of old knot
        float k1x = oldX * 2f - oldKx;
        float k1y = oldY * 2f - oldKy;
        
        coords[0] = k1x;
        coords[1] = k1y;
    	coords[2] = x2;
    	coords[3] = y2;
    	coords[4] = x;
    	coords[5] = y; 
    	
    	Bezier b = new Bezier(hist.lastPoint.x, hist.lastPoint.y, coords, 3);
    	bezierSegs.add(b);
    	curveLength += b.getLength();
        hist.setLastPoint(x, y);
        hist.setLastKnot(x2, y2);
    }

    @Override
    public void curvetoQuadraticRel(float x1, float y1, 
                                    float x, float y) throws ParseException {
    	 float offx = hist.lastPoint.x;
         float offy = hist.lastPoint.y;

         curvetoQuadraticAbs(x1 + offx, y1 + offy, x + offx, y + offy);
    }

    @Override
    public void curvetoQuadraticAbs(float x1, float y1, 
                                    float x, float y) throws ParseException {

    	coords[0] = x1;
    	coords[1] = y1;
    	coords[2] = x;
    	coords[3] = y;
    	
    	Bezier b = new Bezier(hist.lastPoint.x, hist.lastPoint.y, coords, 2);
    	bezierSegs.add(b);
    	curveLength += b.getLength();
    	
    	hist.setLastPoint(x, y);
        hist.setLastKnot(x1, y1);
    }

    @Override
    public void curvetoQuadraticSmoothRel(float x, float y) {
	    float offx = hist.lastPoint.x;
	    float offy = hist.lastPoint.y;
	
	    curvetoQuadraticSmoothAbs(x + offx, y + offy);
    }

    @Override
    public void curvetoQuadraticSmoothAbs(float x, float y)
        throws ParseException {
    	
    	curvetoQuadraticAbs(hist.lastKnot.x, hist.lastKnot.y, x, y);
    }

    @Override
    public void arcRel(float rx, float ry, 
                       float xAxisRotation, 
                       boolean largeArcFlag, boolean sweepFlag, 
                       float x, float y) throws ParseException {
        
    }

    @Override
    public void arcAbs(float rx, float ry, 
                       float xAxisRotation, 
                       boolean largeArcFlag, boolean sweepFlag, 
                       float x, float y) throws ParseException {

    }

    @Override
    public void endPath() throws ParseException {
    	hist.setLastPoint(hist.startPoint.x, hist.startPoint.y);
        hist.setLastKnot(hist.startPoint.x, hist.startPoint.y);
    }
}
