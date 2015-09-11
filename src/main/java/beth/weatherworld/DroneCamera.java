package beth.weatherworld;

// Constructed after reading http://homepages.ius.edu/RWISMAN/b481/html/notes/FlyAround.htm
public class DroneCamera extends Camera{
	
	double theta = 0, phi = 0;
	double distance = 10;
	boolean zoomIn, zoomOut;
	
	public DroneCamera (Point p) {
		super(p);
	}
	
	public DroneCamera (Point p, Point a) {
		super(p,a);
	}
	
	public void mouseMove(int x, int y, int winWidth, int winHeight) {
		theta = (360.0/winHeight)*y*3.0;
		phi = (360.0/winWidth)*x*3.0;
	}
	
	public void update () {
		// Updates rotAmount for faster/slower movements
		if (faster) {
			rotAmount += 0.1;
		}
		if (slower) {
			rotAmount -= 0.1;
			rotAmount = rotAmount < 0 ? 0 : rotAmount;
		}
		
		// Zooms in or out based on keystrokes
		if (zoomIn) {
			distance -= rotAmount;
		}
		if (zoomOut) {
			distance += rotAmount;
		}
		
		// Restrict the angles to 0-360
		theta %= 360;
		phi %= 360;
		
		// Spherical to Cartesian conversion
		eye.x = lookAt.x + distance * Math.sin(Math.toRadians(theta)) * Math.sin(Math.toRadians(phi));
		eye.y = lookAt.y + distance * Math.cos(Math.toRadians(theta));
		eye.z = lookAt.z + distance * Math.sin(Math.toRadians(theta)) * Math.cos(Math.toRadians(phi));
	
		// Finds another point on this line along the sphere
		double dt = 1.0;
		Point eyeTemp = new Point();
		eyeTemp.x = lookAt.x + distance * Math.sin(Math.toRadians(theta)-dt) * Math.sin(Math.toRadians(phi));
		eyeTemp.y = lookAt.y + distance * Math.cos(Math.toRadians(theta)-dt);
		eyeTemp.z = lookAt.z + distance * Math.sin(Math.toRadians(theta)-dt) * Math.cos(Math.toRadians(phi));
	
		// Finds the gap between these points to find the new up vector (given we spin all around)
		up = new Point(eyeTemp);
		up.minus(eye);
	}
	
}
