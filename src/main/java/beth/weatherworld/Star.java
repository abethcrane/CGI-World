package beth.weatherworld;

import java.util.Random;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

// A specific type of sphere, drawn within a random area
// If values aren't defined then they are randomly generated
public class Star extends Sphere {

	static double min = -40;
	static double max = 40;
	static Random r = new Random();

	boolean solid = false;

	public Star () {
		this (createPoint(), r.nextDouble(), new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
	}

	public Star (Point location) {
		this (location, r.nextDouble(), new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
	}

	public Star (Point location, double radius) {
		this (location, radius, new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
	}

	public Star (Point location, double radius, Color colors) {
		this (location, radius, colors, r.nextBoolean());
	}

	public Star (Point location, double radius, Color colors, boolean solid) {
		super (location, radius, colors, solid);

		if (!solid) {
			c.reset(1, 1, 1);
		}
	}

	public void draw (GL2 gl) {
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glPushMatrix();
			gl.glTranslated(p.x, p.y, p.z);
			if (solid) {
				gl.glColor4d(c.r, c.g, c.b, alpha);
				glut.glutSolidSphere(radius, slices, stacks);
			} else {
				gl.glColor4d(c.r, c.g, c.b, alpha);
				glut.glutWireSphere(radius, slices, stacks);
			}
		gl.glPopMatrix();
		gl.glEnable(GL2.GL_LIGHTING);
	}

	private static Point createPoint() {
		double x = 0;
		double y = 0;
		double z = 0;

		while (Math.sqrt((x*x) + (y*y) + (z*z)) < 20) {
			x = min + (max - min) * r.nextDouble();
			y = min + (max - min) * r.nextDouble();
			z = min + (max - min) * r.nextDouble();
		}

		return new Point(x,y,z);
	}

}
