package beth.weatherworld;

public class PersonCamera extends Camera {
	
	boolean forward = false, backward = false, left = false, right = false;
	boolean tiltUp = false, tiltDown = false;
	double angle = 0;
	double stepAmount = 0.01;
	double rotAmount = 0.7;
	double panY = 0;
	
	public PersonCamera (Point p) {
		super(p);
	}
	public PersonCamera (Point p, Point a) {
		super(p,a);
	}
	
	public void update (Terrain t) {
		// Updates the stepAmount to allow for faster/slower movement
		if (faster) {
			stepAmount += 0.01;
		}
		if (slower) {
			stepAmount -= 0.01;
			stepAmount = stepAmount < 0.01 ? 0.01 : stepAmount;
		}
		
		// Updates the position based on key strokes
		if (forward) {
			eye.x += Math.cos(Math.toRadians(angle % 360)) * stepAmount;
			eye.z += Math.sin(Math.toRadians(angle % 360)) * stepAmount;
		}
		if (backward) {
			eye.x -= Math.cos(Math.toRadians(angle % 360)) * stepAmount;
			eye.z -= Math.sin(Math.toRadians(angle % 360)) * stepAmount;
		}
		
		// Updates the angle based on key strokes
		if (left) {
			angle -= rotAmount;
		}
		if (right) {
			angle += rotAmount;
		}
		
		// Tiles up or down based on key strokes
		if (tiltUp) {
			panY += 0.01;
		}
		if (tiltDown) {
			panY -= 0.01;
		}
		
		// Sets the eye altitude
		eye.y = t.altitude(eye.x, eye.z) + 1;
		
		// Sets the look at based on position and angle
		lookAt.y = eye.y + panY;
		lookAt.x = eye.x + Math.cos(Math.toRadians(angle % 360));
		lookAt.z = eye.z + Math.sin(Math.toRadians(angle % 360));
	}
}
