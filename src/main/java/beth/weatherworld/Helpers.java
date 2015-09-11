package beth.weatherworld;

import com.jogamp.opengl.GL2;

public class Helpers {

	public static Color orange =  new Color(1, 0.33f, 0.33f);
	public static Color red =  new Color (1, 0, 0);
	public static Color pink = new Color (1, 0.5f, 0.5f);
	public static Color yellow =  new Color (1, 1, 0);
	public static Color brightBlue = new Color (0.3f, 0.6f, 0.9f);
	public static Color deepBlue = new Color (0, 0, 1);
	public static Color midnightBlue = new Color (0.1f, 0.1f, 0.3f);
	public static Color black =  new Color (0, 0, 0);

	public static Color getColor(float[] dayTimings, Color[] dayColors, float[] nightTimings, Color[] nightColors) {	
		// Takes in two colour arrays and 2 float arrays, and then depending on time returns the appropriate colour value
		// Used by sun and background to rotate through colours nicely

		float fractionThrough;
		float[] timings;
		Color[] colors;
		
		Color color = new Color (0, 0, 0);
		
        if (Game.day) {
        	fractionThrough = Game.fractionThroughDay;
        	timings = dayTimings;
        	colors = dayColors;
        } else {
        	fractionThrough = Game.fractionThroughNight; 
          	timings = nightTimings;
          	colors = nightColors;
        }
        // Go through each segment of the day (e.g. the first 15%, or 50% to 70%)
    	boolean found = false;
    	for (int i = 0; i < timings.length && !found; i++) {
    		if (fractionThrough < timings[i]) {
    			//i and i-1 because .25 is <.7, but between .25 and .7
    			if (i > 0) {
    				// Go between the colours attached to that segment to transition smoothly
    				float lerp = (fractionThrough-timings[i-1])/(timings[i]-timings[i-1]);
        				color.r = colors[i-1].r - (colors[i-1].r*lerp) + (colors[i].r*lerp);
        				color.g = colors[i-1].g - (colors[i-1].g*lerp) + (colors[i].g*lerp);
        				color.b = colors[i-1].b - (colors[i-1].b*lerp) + (colors[i].b*lerp);
    			} else {
    				color.r = colors[i].r;
    				color.g = colors[i].g;
    				color.b = colors[i].b;
    			}
    			found = true;
    		}
    	}
		return color;
	}

    public static Point calculateNormal(Point[] points) {
    	
    	// Cross product method - simplest for a triangle
    	Point u = new Point(points[1]);
    	u.minus(points[0]);
        
        Point v = new Point(points[2]);
    	v.minus(points[0]);

    	Point normal = new Point (u.y * v.z - u.z * v.y,
    						      u.z * v.x - u.x * v.z,
    							  u.x * v.y - u.y * v.x);
    	/*
    	// Newell's method
    	Point normal = new Point();
    	Point current;
    	Point next;
    	for (int i = 0; i < points.length; i++) {
    		current = points[i];
    		next = points[(i+1)%points.length];
    		normal.x += (current.y - next.y)*(current.y + next.y);
    		normal.y += (current.z - next.z)*(current.x + next.x);
    		normal.z += (current.x - next.x)*(current.y + next.y);
    	}*/

    	// We have a normalize function, but turning on GL_NORMALIZE does this for us
    	return normal;
    }
    
    public static double[] normalize (double[] v) {
    	double sum = 0;
    	for (int i = 0; i < v.length; i++) {
    		sum += v[i]*v[i];
    	}
    	
    	double magnitude = Math.sqrt(sum); 

    	for (int i = 0; i < v.length; i++) {
    		v[i] /= magnitude;
    		
    	}    	
    	return v;
    }

    public static void updateDayNight(GL2 gl) {
    	
    	// The time left is calculated based on the time diff between our start time and now.
    	// Appropriate lights are then enabled and disabled.
        if (Game.day) {
        	Game.timeLeftInDay = Game.dayLength - (System.currentTimeMillis() - Game.dayStart);
        	Game.fractionThroughDay = ((float)Game.dayLength - (float)Game.timeLeftInDay)/(float)Game.dayLength;
        	if (Game.timeLeftInDay <= 0) {
        		Game.day = false;
            	Game.nightStart = System.currentTimeMillis();
            }
            gl.glDisable(GL2.GL_LIGHT1);
            gl.glDisable(GL2.GL_LIGHT2);
            gl.glEnable(GL2.GL_LIGHT0);
        } else {
        	Game.timeLeftInNight = Game.dayLength - (System.currentTimeMillis() - Game.nightStart);
        	Game.fractionThroughNight = ((float)Game.dayLength - (float)Game.timeLeftInNight)/(float)Game.dayLength;
        	if (Game.timeLeftInNight <= 0) {
        		Game.day =  true;
        		Game.dayStart = System.currentTimeMillis();
            }
            gl.glDisable(GL2.GL_LIGHT0);
        	gl.glEnable(GL2.GL_LIGHT2);
    		gl.glEnable(GL2.GL_LIGHT1);
        }
        
    	if (!Game.dayNightMode){
    		Game.fractionThroughDay = 0.5f;
    		Game.timeLeftInDay = (long) (-1 * (Game.dayLength * Game.fractionThroughDay - Game.dayLength));
    	}
    }
}
