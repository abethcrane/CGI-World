package ass2;

import java.util.Random;

import javax.media.opengl.GL2;

// Constructed after reading http://www.swiftless.com/tutorials/opengl/particles.html
public class Raindrop {
	
	Random r = new Random();
	Point p, origin;
	Color c;
	double xMov, zMov;
	double deceleration;
	double angle, scale;
	Texture myTexture, myTexture2;
	double stopHeight;
	Terrain myTerrain;
	
	public Raindrop (Point o) {
		//Sets the start point, and assigns random values to the x and z shift and the deceleration.
		origin = new Point(o);
		p = new Point (0, 0, 0);
		xMov = 0.02*r.nextDouble() - 0.01;
		zMov = 0.02*r.nextDouble() - 0.01;
		c = new Color(0.2, 0.2, 0.4);
		angle = 0;
		deceleration = 0.01 + 0.5*r.nextDouble();
	}

	public void update(GL2 gl) {
		// Updates the position based on x and z shift and deceleration.
		p.y -= deceleration;
		p.x += xMov;
		p.z += zMov;
		// Randomly adjusts the angle
		angle += -0.1 + 0.2*r.nextDouble();
		
		draw(gl);
	}
	
	public void draw (GL2 gl) {
		myTexture = (Texture) Game.myTextures.get("particle1");
		myTexture2 = (Texture) Game.myTextures.get("particle2");
	
		gl.glPushMatrix();
		
			gl.glDisable (gl.GL_DEPTH_TEST);
			gl.glEnable (gl.GL_BLEND);
			// Translate them to their start position, and then to their current position (its relative to their start)
			gl.glTranslated(origin.x, origin.y, origin.z);
			gl.glTranslated(p.x, p.y, p.z);		
			// Rotate them to their new angle
			gl.glRotated(angle, 0, 0, 1);

			gl.glColor3dv(c.doubleVector(), 0);

			gl.glBlendFunc (gl.GL_DST_COLOR, gl.GL_ZERO);
			gl.glBindTexture (gl.GL_TEXTURE_2D, myTexture.getTextureID());
			// Draw the droplets
			gl.glBegin (gl.GL_QUADS);
				gl.glTexCoord2d (0, 0);
				gl.glVertex3f (-0.25f, -0.25f, 0);
				gl.glTexCoord2d (1, 0);
				gl.glVertex3f (0.25f, -0.25f, 0);
				gl.glTexCoord2d (1, 1);
				gl.glVertex3f (0.25f, 0.25f, 0);
				gl.glTexCoord2d (0, 1);
				gl.glVertex3f (-0.25f, 0.25f, 0);
			gl.glEnd();
			
			// Now make them opaque
			gl.glBlendFunc (gl.GL_ONE, gl.GL_ONE);
			gl.glBindTexture (gl.GL_TEXTURE_2D, myTexture2.getTextureID());
			
			gl.glBegin (gl.GL_QUADS);
				gl.glTexCoord2d (0, 0);
				gl.glVertex3f (-0.25f, -0.25f, 0);
				gl.glTexCoord2d (1, 0);
				gl.glVertex3f (0.25f, -0.25f, 0);
				gl.glTexCoord2d (1, 1);
				gl.glVertex3f (0.25f, 0.25f, 0);
				gl.glTexCoord2d (0, 1);
				gl.glVertex3f (-0.25f, 0.25f, 0);
			gl.glEnd();

			gl.glDisable(gl.GL_BLEND);
			gl.glEnable(gl.GL_DEPTH_TEST);
		gl.glPopMatrix();
	}

}
