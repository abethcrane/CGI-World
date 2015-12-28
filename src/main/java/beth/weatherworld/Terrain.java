package beth.weatherworld;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.Math;
import java.lang.Object.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import beth.weatherworld.Helpers;
import beth.weatherworld.Texture;
import beth.weatherworld.Triangle.TriangleFace;

public class Terrain {

    private int myWidth;
    private int myDepth;
    private int baseHeight;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private Point mySunlight;
    static Point center;
    Texture myTexture;
    public boolean texture = true;

    private List<Triangle> trianglesToDraw;
    private Point[][][] faceNormals;
    private Point[][] vertexNormals;
    private static Point bottomVertexNormals = new Point(0, -1, 0);
    private static Point xMinVertexNormals = new Point(-1, 0, 0);
    private static Point xMaxVertexNormals = new Point(1, 0, 0);
    private static Point zMinVertexNormals = new Point(0, 0, -1);
    private static Point zMaxVertexNormals = new Point(0, 0, 1);

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        myWidth = width;
        myDepth = depth;
        baseHeight = -2;
        myAltitude = new double[width+1][depth+1];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        trianglesToDraw = new ArrayList<Triangle>();

        center = new Point(width/2, 0, depth/2);
        faceNormals = new Point[width+1][depth+1][2];
        vertexNormals = new Point[width+2][depth+2];
    }

    public void initialize() {
        setCenter();
        setEdgeAltitudes();
        calculateTriangles();
    }

    public int width() {
        return myWidth;
    }

    public int depth() {
        return myDepth;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public Point getSunlight() {
        return new Point(mySunlight);
    }

    public int baseHeight() {
        return baseHeight;
    }

    /**
     * Set the sunlight direction.
     *
     * Note: the sun should be treated as a directional light, without a position
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight = new Point(dx, dy, dz);
    }

    /**
     * Resize the terrain, copying any old altitudes.
     *
     * @param width
     * @param depth
     */
    public void setSize(int width, int depth) {
        myWidth = width;
        myDepth = depth;
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width+1][depth+1];

        for (int x = 0; x <= width && x < oldAlt.length; x++) {
            for (int z = 0; z <= depth && z < oldAlt[x].length; z++) {
                myAltitude[x][z] = oldAlt[x][z];
            }
        }

        setCenter();
    }

    /**
     * Get the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        if (x < 0 || z < 0 || x >= myWidth || z >= myDepth) {
            return baseHeight;
        }
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = Math.max(baseHeight, h);
    }

    // The edges of the grids need altitudes so that we can figure out the edge vertex normals
    private void setEdgeAltitudes() {
        for (int x = 0; x <= myWidth; x++) {
            myAltitude[x][myDepth] = baseHeight;
        }
        for (int z = 0; z <= myDepth; z++) {
            myAltitude[myWidth][z] = baseHeight;
        }
    }

    /**
     * Get the altitude at an arbitrary point.
     * Non-integer points should be interpolated from neighbouring grid points
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        double altitude = baseHeight;

        //v1 = a -az + bz;
        //v2 = c -cz + dz;
        //v3 = v1 -v1x + v2x

        if (Math.floor(x) < 0 || Math.floor(z) < 0 || Math.ceil(x) >= myWidth || Math.ceil(z) >= myDepth) {
            return altitude;
        }

        try {
            int leftX = (int)Math.floor(x);
            int rightX = (int) Math.ceil(x);
            int farZ = (int)Math.floor(z);
            int nearZ = (int)Math.ceil(z);

            double zFraction = z - (double)farZ;
            double xFraction = x - (double)leftX;

            double p1 = myAltitude[leftX][farZ] - (myAltitude[leftX][farZ] * zFraction) + (myAltitude[leftX][nearZ] * zFraction);
            double p2 = myAltitude[rightX][farZ] - (myAltitude[leftX][farZ] * zFraction) + (myAltitude[rightX][nearZ] * zFraction);
            altitude = p1 - (p1 * xFraction) + (p2 * xFraction);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array out of bounds - x: " + x + " out of " + myWidth + ", z: " + z + " out of " + myDepth);
        }

        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point.
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     *
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(new Point(x, altitude(x,z), z));
        myTrees.add(tree);
    }

    /**
     * Add a road.
     *
     * @param width
     * @param spline
     */
    public void addRoad(double width, Point[] spline) {
        Road road = new Road(width, spline);
        myRoads.add(road);
    }

    private void setCenter() {
        center = new Point(myWidth/2, altitude(myWidth/2, myDepth/2), myDepth/2);
    }

    // Once the mesh has been generated, calculate the triangles so that on the draw call we do not have to do this.
    // Triangles are drawn counterclockwise so that the normals face the right way
    private void calculateTriangles() {
        calculateTopTriangles();
        calculateBottomTriangles();
        calculateZTriangles();
        calculateXTriangles();
    }

    private void calculateTopTriangles() {
        // Save the triangles for the lumpy top terrain
        for (int x = 0; x < myWidth; x++) {
            for (int z = 0; z < myDepth; z++) {
                // All grid squares are split into 2 triangles
                Point bottomLeft = new Point(x, myAltitude[x][z+1], z+1);
                Point topLeft = new Point(x, myAltitude[x][z], z);
                Point topRight = new Point(x+1, myAltitude[x+1][z], z);
                Point bottomRight = new Point(x+1, myAltitude[x+1][z+1], z+1);

                // Triangle 1 - top left, bottom left, bottom right
                trianglesToDraw.add(new Triangle(topLeft, bottomLeft, bottomRight, true, TriangleFace.TOP));
                // Triangle 2 - bottom right, top right, top left
                trianglesToDraw.add(new Triangle(bottomRight, topRight, topLeft, false, TriangleFace.TOP));

                faceNormals[x][z][0] = Helpers.calculateNormal(new Point[] {topLeft, bottomLeft, bottomRight});
                faceNormals[x][z][1] = Helpers.calculateNormal(new Point[] {bottomRight, topRight, topLeft});
            }
        }

        // Each vertex is the normalized sum of all face normals it is a vertex on.
        // For most vertices they are on 8 triangles (there) are 4 squares they lie between, and 2 triangles to a square.

        // Has to be + 1 because we do a -1 in there. But we restrict to be < myDepth
        for (int x = 0; x < myWidth + 1; x++) {
            for (int z = 0; z < myDepth + 1; z++) {
                // TODO: Check array bounds max as well as min
                Point normal = new Point();
                // Top Left
                if (x > 0 && z > 0) {
                    normal.plus(faceNormals[x-1][z-1][0]);
                    normal.plus(faceNormals[x-1][z-1][1]);
                }
                // Top Right
                if (x > 0 && z < myDepth) {
                    normal.plus(faceNormals[x-1][z][0]);
                    normal.plus(faceNormals[x-1][z][1]);
                }
                // Bottom Left
                if (x < myWidth && z > 0) {
                    normal.plus(faceNormals[x][z-1][0]);
                    normal.plus(faceNormals[x][z-1][1]);
                }
                // Bottom Right
                if (x < myWidth && z < myDepth) {
                    normal.plus(faceNormals[x][z][0]);
                    normal.plus(faceNormals[x][z][1]);
                }

                // TODO: Normalize???
                vertexNormals[x][z] = normal;
            }
        }
    }

    private void calculateBottomTriangles() {
    // Save triangles for the flat bottom of the island
        for (int x = 0; x < myWidth; x++) {
            for (int z = 0; z < myDepth; z++) {
                Point bottomLeft = new Point(x, baseHeight, z+1);
                Point topLeft = new Point(x, baseHeight, z);
                Point topRight = new Point(x+1, baseHeight, z);
                Point bottomRight = new Point(x+1, baseHeight, z+1);

                // Clockwise so the normals point out the bottom
                // Triangle 1
                trianglesToDraw.add(new Triangle(bottomRight, bottomLeft, topLeft, true, TriangleFace.BOTTOM));
                // Triangle 2
                trianglesToDraw.add(new Triangle(topLeft, topRight, bottomRight, false, TriangleFace.BOTTOM));
            }
        }
    }

    private void calculateXTriangles() {
        // TODO: Make this less hackish - better size squares etc
        // Draw the 2 island sides that are parallel with the x axis: x = 0 and x = width, with z = 0 -> depth
        int[] xSides = {0, myWidth};
        for (int x : xSides) {
           for (int z = 0; z < myDepth; z++) {
                // When looking at it from the side, with 0,0 to your left
                Point bottomLeft = new Point(x, baseHeight, z);
                Point topLeft = new Point(x, myAltitude[x][z], z);
                Point topRight = new Point(x, myAltitude[x][z+1], z+1);
                Point bottomRight = new Point(x, baseHeight, z+1);

                TriangleFace faceType;
                if (x == 0) {
                    faceType = TriangleFace.XMIN;
                } else {
                    faceType = TriangleFace.XMAX;
                }

                trianglesToDraw.add(new Triangle(topLeft, bottomLeft, bottomRight, true, faceType));
                trianglesToDraw.add(new Triangle(bottomRight, topRight, topLeft, false, faceType));
            }
        }
    }

    private void calculateZTriangles() {
        // Draw the 2 island sides that are parallel with the z axis: z = 0 and z = depth, with x = 0 -> width
        int[] zSides = {0, myDepth};
        for (int z : zSides) {
            for (int x = 0; x < myWidth; x++) {
                // When looking at it from the bottom, with 0, 0 to your left
                Point bottomLeft = new Point(x, baseHeight, z);
                Point topLeft = new Point(x, myAltitude[x][z], z);
                Point topRight = new Point(x+1, myAltitude[x+1][z], z);
                Point bottomRight = new Point(x+1, baseHeight, z);

                TriangleFace faceType;
                if (z == 0) {
                    faceType = TriangleFace.ZMIN;
                    trianglesToDraw.add(new Triangle(bottomRight, bottomLeft, topLeft, true, faceType));
                    trianglesToDraw.add(new Triangle(topLeft, topRight, bottomRight, false, faceType));
                } else {
                    faceType = TriangleFace.ZMAX;
                    trianglesToDraw.add(new Triangle(topLeft, bottomLeft, bottomRight, true, faceType));
                    trianglesToDraw.add(new Triangle(bottomRight, topRight, topLeft, false, faceType));
                }
            }
        }
    }

    private Point getNormal (Triangle t, Point position) {
        Point normal;
        switch(t.faceType) {
            case TOP:
            normal = vertexNormals[(int)position.x][(int)position.z];
            break;
            case BOTTOM:
            normal = bottomVertexNormals;
            break;
            case XMIN:
            normal = xMinVertexNormals;
            break;
            case XMAX:
            normal = xMaxVertexNormals;
            break;
            case ZMIN:
            normal = zMinVertexNormals;
            break;
            case ZMAX:
            normal = zMaxVertexNormals;
            break;
            default:
            System.out.println("eep something went wrong with triangle face type");
            normal = new Point (0, 1, 0);
        }
        return normal;
    }

    public void draw(GL2 gl) {

        gl.glMatrixMode(gl.GL_MODELVIEW);
        gl.glEnable(gl.GL_CULL_FACE);
        gl.glCullFace(gl.GL_BACK);

        gl.glPushMatrix();

        gl.glPolygonMode(gl.GL_FRONT, gl.GL_FILL);
        gl.glPolygonOffset(0,0);

        if (texture) {
            gl.glEnable(gl.GL_TEXTURE_2D);
            myTexture = (Texture) Game.myTextures.get("grass");
            if (myTexture != null) {
                gl.glBindTexture(gl.GL_TEXTURE_2D, myTexture.getTextureID());
                    // use the texture to modulate diffuse and ambient lighting
                gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
            }

            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
        }

        gl.glMaterialfv(gl.GL_FRONT, gl.GL_AMBIENT, Helpers.dayAmbient, 0);
        gl.glMaterialfv(gl.GL_FRONT, gl.GL_DIFFUSE, Helpers.diffuse, 0);
        gl.glMaterialf(gl.GL_FRONT, gl.GL_SHININESS, 10f);

        // Draw each of our pre-calculated triangles
        gl.glBegin(GL2.GL_TRIANGLES);
        for (Triangle triangle : trianglesToDraw) {
            for (int i = 0; i < 3; i++) {
                Point position = triangle.points[i];
                Point normal = getNormal(triangle, position);
                gl.glNormal3dv(normal.doubleVector(), 0);
                if (texture) {
                    gl.glTexCoord2d(triangle.textureCoords[i].x, triangle.textureCoords[i].y);
                } else {
                    gl.glColor3dv(triangle.colors[i].doubleVector(), 0);
                }
                gl.glVertex3dv(position.doubleVector(), 0);
            }
        }
        gl.glEnd();

        if (texture) {
            gl.glDisable(gl.GL_TEXTURE_2D);
        }

        for (Tree tree : myTrees) {
            tree.draw(gl, this);
        }

        for (Road road : myRoads) {
            road.draw(gl, this);
        }

        gl.glDisable(gl.GL_CULL_FACE);
        gl.glPopMatrix();

    }
}
