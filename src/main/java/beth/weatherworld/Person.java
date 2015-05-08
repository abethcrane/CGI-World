package beth.weatherworld;

import com.jogamp.opengl.GL2;

// Very simple avatar. Currently just a rectangle rotated and transitioned to where the camera is looking.
public class Person extends Sphere {

	public Person () {
		super(new Point(), 0.15);
	}
	
	public void update(GL2 gl, PersonCamera cam, Terrain t) {
		p.reset(cam.getView()[0], t.altitude(cam.getView()[0], cam.getView()[2]), cam.getView()[2]);
		draw(gl);
	}
	
	/*public void draw (GL2 gl, PersonCamera cam, Terrain t) {
		
		gl.glPushMatrix();
			gl.glTranslated(cam.getView()[0], t.altitude(cam.getView()[0], cam.getView()[2]), cam.getView()[2]);
			gl.glRotated(-cam.angle, 0, 1, 0);
			
			Point p1 = new Point (0, 0, -0.125);
			Point p2 = new Point (0, 0, 0.125);
			Point p3 = new Point (0, 0.25, 0.125);
			Point p4 = new Point (0, 0.25, -0.125);
			
			gl.glBegin(gl.GL_QUADS);
				gl.glVertex3dv(p1.doubleVector(), 0);
				gl.glVertex3dv(p2.doubleVector(), 0);
				gl.glVertex3dv(p3.doubleVector(), 0);
				gl.glVertex3dv(p4.doubleVector(), 0);
			gl.glEnd();
			
		gl.glPopMatrix();
	}*/
	
}
