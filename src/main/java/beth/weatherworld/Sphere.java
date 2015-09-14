package beth.weatherworld;

import java.util.Random;

import com.jogamp.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class Sphere {
	// Standard class for customising spheres

	private static Random r = new Random();
	protected double radius;
	protected int slices;
	protected int stacks;
	private boolean solid;
	protected Point p;
	protected Color c;
	protected float alpha = 1f;
	private GLUT glut;
	
	public Sphere (Point location) {
		this (location, r.nextDouble(), new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()), r.nextBoolean());
	}
	
	public Sphere (Point location, double radius) {
		this (location, radius, new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()), r.nextBoolean());
	}
	
	public Sphere (Point location, double radius, Color colors, boolean solidity) {
		glut = new GLUT();
		
		p = location;
		this.radius = radius;
		c = colors;
		
		solid = solidity;
		
		slices = 5 + r.nextInt(30);
		stacks = 5 + r.nextInt(30);
	}
	
	public void draw(GL2 gl) {
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
	}
}
