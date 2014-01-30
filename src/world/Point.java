package ass2;

// Basic point/vector class
public class Point {

	public double x, y, z;
	
	public Point () {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y; 
		this.z = z;
	}
	
	public Point(double[] d) {
		x = d[0];
		y = d[1];
		z = d[2];
	}

	public Point (float[] f) {
		x = f[0];
		y = f[1];
		z = f[2];
	}
	
	public Point(Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	public double[] doubleVector() {
		return new double[] {x, y, z};
	}
	
	public float[] floatVector() {
		return new float[] {(float)x, (float)y, (float)z};
	}
	
	
	public void reset (double x, double y, double z) {
		this.x = x;
		this.y = y; 
		this.z = z;
	}
	
	public void reset (double[] d) {
		x = d[0];
		y = d[1];
		z = d[2];
	}
	
	public void reset (float[] f) {
		x = f[0];
		y = f[1];
		z = f[2];
	}
	
	public void minus (Point p) {
		x -= p.x;
		y -= p.y;
		z -= p.z;
	}
	
	public void plus (Point p) {
		x += p.x;
		y += p.y;
		z += p.z;
	}
	
	public void multiply (Point p) {
		x *= p.x;
		y *= p.y;
		z *= p.z;
	}
	
	public void divide (Point p) {
		x /= p.x;
		y /= p.y;
		z /= p.z;
	}
	
	public void negate() {
		x *= -1;
		y *= -1;
		z *= -1;
	}
	
	public void print() {
		System.out.println("x: "+ x + " y: " + y + " z: " +z);
	}
	
	public void scalarMultiply(double d) {
		x *= d;
		y *= d;
		z *= d;		
	}
	
	public double magnitude() {
		return Math.sqrt(x*x +  y*y + z*z);
	}
	
	
}
