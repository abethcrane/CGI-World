package beth.weatherworld;

// Color class for easy passing in and referencing of colors
public class Color {

	public float r, g, b;
	
	public Color(double r, double g, double b) {
		this.r = (float)r;
		this.g = (float)g; 
		this.b = (float)b;
	}
	
	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g; 
		this.b = b;
	}
	
	public Color(Color c) {
		r = c.r;
		g = c.g;
		b = c.b;
	}
	
	public double[] doubleVector() {
		return new double[] {r, g, b};
	}
	
	public float[] floatVector() {
		return new float[] {r, g, b};
	}
	
	
	public void reset (float r, float g, float b) {
		this.r = r;
		this.g = g; 
		this.b = b;
	}
	
	public void reset (double[] d) {
		r = (float)d[0];
		g = (float)d[1];
		b = (float)d[2];
	}
	
	public void reset (float[] f) {
		r = f[0];
		g = f[1];
		b = f[2];
	}
	
	public void minus (Color c) {
		r = r - c.r;
		g = g - c.g;
		b = b - c.b;
	}
	
	public void plus (Color c) {
		r = r + c.r;
		g = g + c.g;
		b = b + c.b;
	}
	
	public void multiply (Color c) {
		r = r * c.r;
		g = g * c.g;
		b = b * c.b;
	}
	
	public void divide (Color c) {
		r = r / c.r;
		g = g / c.g;
		b = b / c.b;
	}
	
	public void scalarMultiply (float f) {
		r = r * f;
		g = g * f;
		b = b * f;
	}
	
	public void scalarPlus (float f) {
		r = r + f;
		g = g + f;
		b = b + f;
	}
	
	public void scalarMinus (float f) {
		r = r - f;
		g = g - f;
		b = b - f;
	}
	
}
