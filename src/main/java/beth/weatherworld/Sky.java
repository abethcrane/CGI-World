package beth.weatherworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.GL2;

// Contains the sun, stars, clouds and rain
public class Sky {
	
    private List<Star> myStars;
    private List<Cloud> myClouds;
    public Sun mySun;
    private int numStars;
    private int numClouds;
    private Terrain myTerrain;
    
	public Sky (Terrain t) {
		mySun = new Sun();
		// References terrain to find altitudes
		myTerrain = t;
        myStars = new ArrayList<Star>();
        myClouds = new ArrayList<Cloud>();
        addStars();
        addClouds();
	}
	
	// Adds some randomly positioned stars/planets
	public void addStars() {
    	Random r = new Random();
    	int min = 30;
    	int max = 100;
    	numStars = min + r.nextInt(max-min);
    	for (int i = 0; i < numStars; i++) {
    		myStars.add(new Star());
    	}
    }

	// Adds some randomly positioned clouds.
    public void addClouds() {
    	Random r = new Random();
    	int min = 5;
    	int max = 50;
    	numClouds = min + r.nextInt(max-min);
    	for (int i = 0; i < numClouds; i++) {
    		myClouds.add(new Cloud(myTerrain));
    	}
    }

	public void draw (GL2 gl) {
        mySun.draw(gl, myTerrain.getSunlight());
		
        if (Game.day) {
        	int i = 0;
        	double numCloudsToDraw = (Game.fractionThroughDay > 0.5 ? (1 - Game.fractionThroughDay) * 2*numClouds : Game.fractionThroughDay * 2*numClouds);
        	// num to draw = the fraction of the way you are between 0 and halfway or halfway and the end
        	for (Cloud cloud : myClouds) {
        		if (i < numCloudsToDraw) {
        			cloud.update(gl);
            		for (Raindrop drop : cloud.raindrops) { 
            			drop.update(gl);
            		}
            		i++;
        		}
	        }
        	
        } else {
        	int i = 0;
        	double numStarsToDraw = (Game.fractionThroughNight > 0.5 ? (1 - Game.fractionThroughNight) * 2*numStars : Game.fractionThroughNight * 2*numStars);
        	// num to draw = the fraction of the way you are between 0 and halfway or halfway and the end
        	for (Star star : myStars) {
        		if (i < numStarsToDraw) {
        			star.draw(gl);
            		i++;
        		}
	        }
        }
	}
}
