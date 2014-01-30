package ass2;

import java.util.Random;

import javax.media.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

// A specific type of sphere, drawn within a random area
// If values aren't defined then they are randomly generated
public class Star extends Sphere{

	static double min = -40;
	static double max = 40;
	static Random r = new Random();
	
	boolean solid = false;

	public Star () {
		this (new Point(min + (max - min) * r.nextDouble(), min + (max - min) * r.nextDouble(), min + (max - min) * r.nextDouble()),
			  r.nextDouble(),
			  new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
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

	
}
