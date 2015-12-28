package beth.weatherworld;

import com.jogamp.opengl.GL2;

// Very simple avatar. Currently just a sphere rotated and transitioned to where the camera is looking.
public class Person extends Sphere {

	public Person () {
		this(0, 1, 0);
	}
	
	public Person(double x, double y, double z) {
		super(new Point(x, y  + 0.076, z), 0.15);	
	}	
	
	public void update(GL2 gl, PersonCamera cam, Terrain t) {
		p.reset(cam.getView()[0], t.altitude(cam.getView()[0], cam.getView()[2]) + 0.075, cam.getView()[2]);
		draw(gl);
	}	
}
