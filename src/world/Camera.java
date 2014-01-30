package ass2;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

// Base camera class for shared functionality between Person and Drone
public class Camera {
	Point lookAt = new Point();
	Point up = new Point(0, 1, 0);
	Point eye = new Point ();

	boolean faster = false, slower = false;
	
	double rotAmount = 0.01;

	public Camera (Point p) {
		eye = new Point(p);
	}
	
	public Camera (Point p, Point a) {
		eye = new Point(p);
		lookAt = new Point(a);
	}
	
	public float[] getPosition () {
		return eye.floatVector();
	}
	
	public float[] getView () {
		return lookAt.floatVector();
	}
	
	// The vector from eye to lookAt
	public float[] getDirection() {
		Point p = new Point(lookAt);
		p.minus(eye);
		return p.floatVector();
	}
	
}
