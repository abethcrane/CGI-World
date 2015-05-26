package beth.weatherworld;

import com.jogamp.opengl.GL2;
import java.lang.Math.*;

import beth.weatherworld.Point;

public class Road {

    private Point[] myPoints;
    private int numPoints;
    private double myWidth;
    private static final double tInt = 0.01;
    private Texture myTexture;
    Point[] bezierCurvePositions;
    int numPositions;
    
    /**
     * Create a new road with the specified spline
     *
     * @param width
     * @param spline
     */
    public Road(double width, Point[] spline) {
        myWidth = width;
        
        numPositions = (int)(1.0/tInt);
        
        numPoints = spline.length;
        myPoints = new Point[numPoints];
        for (int i = 0; i < numPoints; i++) {
            myPoints[i] = spline[i];
        }
        
        bezierCurvePositions = new Point[numPositions];
        calculateBezierCurvePositions();
    }

    /**
     * The width of the road.
     *
     * @return
     */
    public double width() {
        return myWidth;
    }
    
    public int numPoints() {
        return numPoints;
    }

    /**
     * Get the specified control point.
     *
     * @return
     */
    public Point[] points() {
        return myPoints; // TODO: Do I need to return a copy instead
    }
    
     /**
     * Calculate the position of a point on a cubic bezier curve
     * http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B.C3.A9zier_curves
     */
    private void calculateBezierCurvePositions() {
        double t = 0;
        for (int positionNum = 0; positionNum < numPositions && t < 1; positionNum++) {
            bezierCurvePositions[positionNum] = new Point();
            
            Point temp = new Point(myPoints[0]);
            temp.scalarMultiply(Math.pow((1-t), 3));
            bezierCurvePositions[positionNum].plus(temp);
            
            temp = new Point(myPoints[1]);
            temp.scalarMultiply(Math.pow((1-t), 2) * 3 * t);
            bezierCurvePositions[positionNum].plus(temp);
            
            temp = new Point(myPoints[2]);
            temp.scalarMultiply(Math.pow(t, 2) * 3 * (1-t));
            bezierCurvePositions[positionNum].plus(temp);
            
            temp = new Point(myPoints[3]);
            temp.scalarMultiply(Math.pow(t, 3));
            bezierCurvePositions[positionNum].plus(temp);
            
            t += tInt;
        }
    }
    
    /**
     * Calculate the position of a point on a cubic bezier curve
     * http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B.C3.A9zier_curves
     * @param t
     * @return
     */
     
    public void draw (GL2 gl, Terrain map) {
    	
    	gl.glColor3d(1, 1, 1);
        gl.glPushMatrix();
        
        gl.glEnable(gl.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(-1, -1);
        gl.glEnable(gl.GL_CULL_FACE);
        gl.glCullFace(gl.GL_BACK);
        
        gl.glEnable(gl.GL_TEXTURE_2D);
        
		myTexture = (Texture) Game.myTextures.get("road");
		if (myTexture != null) {
	    	gl.glBindTexture(gl.GL_TEXTURE_2D, myTexture.getTextureID());
	        // use the texture to modulate diffuse and ambient lighting
	        gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
		}
		
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
		
        gl.glBegin(gl.GL_TRIANGLE_STRIP);
        
            double t = 0;
	        for (int index = 0; index < numPositions && t < 1 - tInt; index++) {
                Point center = bezierCurvePositions[index];
	            center.y = map.altitude(center.x, center.z);
                
                Point next = bezierCurvePositions[index + 1];
                next.y = map.altitude(next.x, next.z);
	    		// Rotated is the vector between the current center and the next center, rotated 90 degrees
	    		Point rotated = new Point(-next.z + center.z, 0, next.x - center.x);
	    		// It's then scaled to be width/2 long
	    		rotated.scalarMultiply((myWidth/2)/rotated.magnitude());
	            
	    		// Left rotates one way
	            Point left = new Point(center);
	            left.minus(rotated);            
	            
	            // Right rotates the other
	            Point right = new Point(center);
	            right.plus(rotated);

	            // Sets the normal to be the up vector (roads are flat)
	            gl.glNormal3d(0, 1, 0);
	            
	            // Sets the texture co-ords and draws the vertices
	            gl.glTexCoord2d(t, 0);
	            gl.glVertex3dv(left.doubleVector(), 0);
	            gl.glTexCoord2d(t, 1);
	            gl.glVertex3dv(right.doubleVector(), 0);
                
                t += tInt;
	        }
	        
        gl.glEnd();
        gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glDisable(gl.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(gl.GL_CULL_FACE);
        gl.glPopMatrix();
    }


}