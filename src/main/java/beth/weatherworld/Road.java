package beth.weatherworld;

import com.jogamp.opengl.GL2;
import java.lang.Math.*;
import java.util.List;
import java.util.ArrayList;

import beth.weatherworld.Point;
import beth.weatherworld.TriangleStrip;

public class Road {

    private Point[] bezierPoints;
    private int numPoints;
    private double myWidth;
    private static final double tInt = 0.01;
    private Texture myTexture;
    Point[] bezierCurvePositions;
    int numPositions;

    private List<TriangleStrip> roadTriangleStrips;
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
        bezierPoints = new Point[numPoints];
        for (int i = 0; i < numPoints; i++) {
            bezierPoints[i] = spline[i];
        }

        bezierCurvePositions = new Point[numPositions];
        calculateBezierCurvePositions();

        roadTriangleStrips = new ArrayList<TriangleStrip>();
    }

    public void initialize(Terrain t)
    {
        calculateRoadTriangleStrips(t);
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
        return bezierPoints; // TODO: Do I need to return a copy instead
    }

     /**
     * Calculate the position of a point on a cubic bezier curve
     * http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B.C3.A9zier_curves
     */
    private void calculateBezierCurvePositions() {
        double t = 0;
        for (int positionNum = 0; positionNum < numPositions && t < 1; positionNum++) {
            bezierCurvePositions[positionNum] = new Point();

            Point temp = new Point(bezierPoints[0]);
            temp.scalarMultiply(Math.pow((1-t), 3));
            bezierCurvePositions[positionNum].plus(temp);

            temp = new Point(bezierPoints[1]);
            temp.scalarMultiply(Math.pow((1-t), 2) * 3 * t);
            bezierCurvePositions[positionNum].plus(temp);

            temp = new Point(bezierPoints[2]);
            temp.scalarMultiply(Math.pow(t, 2) * 3 * (1-t));
            bezierCurvePositions[positionNum].plus(temp);

            temp = new Point(bezierPoints[3]);
            temp.scalarMultiply(Math.pow(t, 3));
            bezierCurvePositions[positionNum].plus(temp);

            t += tInt;
        }
    }


    private void calculateRoadTriangleStrips(Terrain map) {
        // for each line-between-2-points-in-time on our road
        // we have to split the line up at each grid square that we come to
        // to match the terrain
        // so a line from 0, 1 -> 1, 0 (whole numbers for simplicity)
        /*
        0 |  x
        1 |x
        2 |
        3 |
           - - - -
           0 1 2 3
        */
        // with a width of say .5, is going to go across the cells:
        // 0,0 0,1 0,2 1,0 1,1 2,1
        // So each cell it's in we split it into the 2 correct triangles
        // Dividing it along the line:
        /*
         -----
        |\    |
        | \   |
        |  \  |
        |   \ |
         -----
        */
        // something like
        // https://gamedev.stackexchange.com/questions/20103/finding-which-tiles-are-intersected-by-a-line-without-looping-through-all-of-th
        // http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html
        // https://stackoverflow.com/questions/35771458/square-grid-rotated-rect-intersection-calculate-all-intersection-cells


        double t = 0;
        for (int index = 0; index < numPositions && t < 1 - tInt; index++)
        {
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
            Point leftNormal = CalculateNormalForPoint(left, map);
            roadTriangleStrips.add(new TriangleStrip(left, leftNormal, new Point(t, 0, 0)));

            // Right rotates the other
            Point right = new Point(center);
            right.plus(rotated);
            Point rightNormal = CalculateNormalForPoint(right, map);
            roadTriangleStrips.add(new TriangleStrip(right, rightNormal, new Point(t, 1, 1)));

            t += tInt;
        }
    }

    private Point CalculateNormalForPoint(Point p, Terrain map)
    {
        // Find out which cell it's in - that's easy! just floor the x and z
        int flooredX = (int)Math.floor(p.x);
        int flooredZ = (int)Math.floor(p.z);

        // Get just the position relative to the cell
        Point pointPositionInCell = new Point(p.x - flooredX, 0, p.z - flooredZ);

        // Find out which triangle of the cell it's in - turns out this is pretty easy for our case
        // https://math.stackexchange.com/questions/274712/calculate-on-which-side-of-a-straight-line-is-a-given-point-located
        // Because our line starts at 0,0 and goes to 1,1, we're actually just figuring out whether the point's x is > its z
        int triangleSide = pointPositionInCell.z > pointPositionInCell.x ? 1 : 0;

        // Sets the texture co-ords and draws the vertices
        return map.faceNormals[flooredX][flooredZ][triangleSide];
    }

    /**
     * Draw the road on the map
     * @param gl
     * @param map
     * @return
     */

    public void draw (GL2 gl) {

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

            // Terrain is drawn with 2 triangles per grid square.
            // Every half grid square that a road covers should have a triangle.
            // We should cache a map of the island and indicate which triangles are roads.
            // We should generate a list of 100(e.g.) time points and then we should

            // okay so we have the list of 100, I see.
            // I still think we should stop the triangles at grid square corners maybe. That's probably good. If a triangle goes across a grid square, make it 2??.
            // Also roads are not flat, stupid! Get the normal from the triangle gosh.

            for (TriangleStrip t : roadTriangleStrips)
            {
                // Sets the normal
                gl.glNormal3d(t.vertexNormal.x, t.vertexNormal.y, t.vertexNormal.z);

                // Sets the texture co-ords and draws the vertices
                gl.glTexCoord2d(t.texCoord.x, t.texCoord.y);
                gl.glVertex3dv(t.vertex.doubleVector(), 0);
            }

        gl.glEnd();
        gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glDisable(gl.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(gl.GL_CULL_FACE);
        gl.glPopMatrix();
    }


}