package beth.weatherworld;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import beth.weatherworld.Game;
import com.jogamp.opengl.util.gl2.GLUT;

public class Sun extends Star {
	double angle, distance;
	
	// The color transitions for day and night
	float[] dayTimings = new float[]  {0.03f, 0.1f, 0.15f,  0.8f,   0.9f,   1.0f};
	Color[] dayColors = new Color[] {Helpers.red, Helpers.orange, Helpers.yellow, Helpers.yellow, Helpers.orange, Helpers.red};
  	float[] nightTimings = new float[]  {0.05f, 0.1f, 0.95f, 1.0f};
  	Color[] nightColors = new Color[] {Helpers.red, Helpers.black, Helpers.black, Helpers.red};
  	
  	Texture myTexture;

	public Sun () {
		super(new Point(0, 0, 0), 1, new Color(1, 0.5, 0), true);
		
		// Distance is easily changeable
		distance = 19;
		// Goes from West to East
		if (Game.day) {
			angle = 90;
		} else {
			angle = -90;
		}
		p.x = 0;
		
		slices = 15;
		stacks = 15;
		
	}
	
	public void update(Point sunlight) {
		// Gets the colour
		c = Helpers.getColor(dayTimings, dayColors, nightTimings, nightColors);
		
		// Gets the angle based on interpolating how far we are through the day/night.
		float fractionThrough;
        if (Game.day) {
        	fractionThrough = ((float)Game.dayLength - (float)Game.timeLeftInDay)/(float)Game.dayLength;
        	angle = (90 - 180*fractionThrough) % 360;
        } else {
        	fractionThrough = ((float)Game.dayLength - (float)Game.timeLeftInNight)/(float)Game.dayLength; 
        	angle = (-90 - 180*fractionThrough) % 360;
        }
        
        // Gets the position based on angle and distance
        p.z = Math.sin(Math.toRadians(angle)) * distance;
		p.y = Math.cos(Math.toRadians(angle)) * distance;
		
		if (!Game.dayNightMode) {
			p = sunlight;
			p.scalarMultiply(-30);
		}
	}

	public void draw(GL2 gl, Point s) {
		update(s);
		myTexture = (Texture) Game.myTextures.get("sun2");
		if (myTexture != null) {
	    	gl.glBindTexture(gl.GL_TEXTURE_2D, myTexture.getTextureID());
	        // use the texture to modulate diffuse and ambient lighting
	        gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
		}
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		
		gl.glColor3dv(c.doubleVector(), 0);
		
		// Uses a glu sphere not a glut sphere to allow for easier texturing
		GLU glu = new GLU();
		gl.glPushMatrix();
			gl.glEnable(gl.GL_TEXTURE_2D);
	    	GLUquadric q = glu.gluNewQuadric();    	 
	    	glu.gluQuadricDrawStyle(q, glu.GLU_FILL);
	    	glu.gluQuadricTexture(q, true);
	    	Point t = Terrain.center;
	    	gl.glTranslated(t.x, t.y, t.z);
	    	gl.glTranslated(p.x, p.y, p.z);
	    	glu.gluSphere(q, radius, slices, stacks);
	    	gl.glDisable(gl.GL_TEXTURE_2D);
		gl.glPopMatrix();

	}

	
}
