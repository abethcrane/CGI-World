package ass2;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

/**
 * COMMENT: Comment Road
 *
 * @author malcolmr
 */
public class Road {

    private List<Double> myPoints;
    private double myWidth;
    private static final double tInt = 0.01;
    private Texture myTexture;

    /**
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     *
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);
    }

    /**
     * Get the number of segments in the curve
     *
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     *
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }

    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     *
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;

        i *= 6;

        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);

        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;

        return p;
    }

    /**
     * Calculate the Bezier coefficients
     *
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {

        switch(i) {

        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;

        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }

        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

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
        
	        Point center, next, rotated, normal;
	        for (double t = 0; t < 1-tInt; t += tInt) {
	            center = new Point(point(t)[0], map.altitude(point(t)[0], point(t)[1]), point(t)[1]);
	    		next = new Point(point(t+tInt)[0], map.altitude(point(t+tInt)[0], point(t+tInt)[1]), point(t+tInt)[1]);
	    		// Rotated is the vector between the current center and the next center, rotated 90 degrees
	    		rotated = new Point(-next.z + center.z, 0, next.x - center.x);
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
	        }
	        
        gl.glEnd();
        gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glDisable(gl.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(gl.GL_CULL_FACE);
        gl.glPopMatrix();
    }


}