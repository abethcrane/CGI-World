package beth.weatherworld;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class Cloud {

	static Random r = new Random();
	List<Raindrop> raindrops;
	Color c = new Color(0.9, 1, 0.95);
	Point p; // center of the bunch
	Terrain myTerrain;
	private boolean raining = false;
	static double min = -40;
	static double max = 40;
	private List<Sphere> mySpheres;
	private int numSpheres;
	float alpha = 0.6f;
	
	// Randomly positions the cloud in the sky
	public Cloud (Terrain t) {
		this (t, createPoint());
	}

	public Cloud (Terrain t, Point p) {
		this.p = p;
		raindrops = new ArrayList<Raindrop>();
		mySpheres = new ArrayList<Sphere>();
		myTerrain = t;
		updateWeather();
		addSpheres();
		alpha = r.nextFloat();
	}
	
	// Randomly allocates raindrops to a sphere in the cloud so that they come from all over the cloud
	public void addDrops(int numDrops) {
		for (int i = 0; i < numDrops; i++) {
			// Get a random sphere
			// Pass its p in instead of p
			Point n = (mySpheres.get(r.nextInt(numSpheres))).p;
			raindrops.add(new Raindrop(n));
		}
	}
	
	// Clouds consist of multiple spheres, randomly adjused away from the center by a bit, so as to overlap
	public void addSpheres() {
		numSpheres = 3 + r.nextInt(5);
		for (int i = 0; i < numSpheres; i++) {
			Point n = new Point(p);
			n.x += r.nextDouble()*1.5 - .5;
			n.y += r.nextDouble()*1.5 - .5;
			n.z += r.nextDouble()*1.5 - .5;
			mySpheres.add(new Sphere(n, 1.5 * r.nextDouble(), c, true));
		}
	}
	
	public void update(GL2 gl) {
		updateWeather();
		
		// Whilst its raining we constantly add new raindrops
		if (raining) {
			addDrops(r.nextInt(20));
		}
		// Raindrops are drawn in sky
		draw(gl);
	}
	
	// Draws each sphere, then updates the raindrops
	public void draw(GL2 gl) {
		GLUT glut = new GLUT();
		gl.glPushMatrix();
			gl.glEnable (gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA,gl.GL_ONE);  //define blending factors
	    	for (Sphere sphere : mySpheres) {
	    		sphere.draw(gl);
	    	}
			gl.glDisable(gl.GL_BLEND);
		gl.glPopMatrix();
		updateRaindrops(gl);
	}
	
	// Updates the raindrops, removing any that have fallen too far
	public void updateRaindrops(GL2 gl) {
		List<Raindrop> tempList = new ArrayList<Raindrop>();
		
		for (Raindrop drop : raindrops) { 
			double travelLength = p.y - myTerrain.altitude(drop.p.x, drop.p.z) - 0.01;
			if (Math.abs(drop.p.y) < travelLength) {
				tempList.add(drop);
			}
		}			
		raindrops = tempList;
	}
	
	// Probability of it staying the same is high, but does change between raining and nt
	public void updateWeather() {
		if (!raining) {
			if (r.nextDouble() < 0.2) {
				raining = true;
			}
		} else {
			if (r.nextDouble() < 0.3) {
				raining = false;
			}
		}
		//Only clouds above 0 can rain
		if (p.y < 0) {
			raining = false;
		}
	}
	
	private static Point createPoint() {
		double x = 0;
		double y = 0;
		double z = 0;
		
		while (Math.sqrt((x*x) + (y*y) + (z*z)) < Math.abs(20)) {
			x = min + (max - min) * r.nextDouble();
			y = min + (max - min) * r.nextDouble();
			z = min + (max - min) * r.nextDouble();
		}
		
		return new Point(x,y,z);
	}
}
