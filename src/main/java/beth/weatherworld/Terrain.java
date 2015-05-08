package beth.weatherworld;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.lang.Object.*;

import com.jogamp.opengl.GL2;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLProfile;

import beth.weatherworld.Texture;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private Point mySunlight;
    static Point centre;
    Texture myTexture;
    public boolean texture = true;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        centre = new Point(width/2, 0, depth/2);
    }
    
    public Terrain(Dimension size) {   
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
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
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        double min = 0;
        double max = 0;
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
                if (myAltitude[i][j] < min) {
                	min = myAltitude[i][j];
                }
                if (myAltitude[i][j] > max) {
                	max = myAltitude[i][j];
                }
            }
        }
        
        centre.y = (max+min)/2;
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
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
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points

     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        double altitude = 0;
        
        //v1 = a -az + bz;
        //v2 = c -cz + dz;
        //v3 = v1 -v1x + v2x

        if (Math.floor(x) < 0 || Math.floor(z) < 0 || Math.ceil(x) >= mySize.height || Math.ceil(z) >= mySize.width) {
        	return altitude;
        }
        
        int topX = (int)Math.floor(x);
        int bottomX = (int) Math.ceil(x);
        int leftZ = (int)Math.floor(z);
        int rightZ = (int)Math.ceil(z);
        
        double zFraction = z - (double)leftZ;
        double xFraction = x - (double)topX;
        
        double p1 = myAltitude[topX][leftZ] - (myAltitude[topX][leftZ] * zFraction) + (myAltitude[topX][rightZ] * zFraction);
        double p2 = myAltitude[bottomX][leftZ] - (myAltitude[bottomX][leftZ] * zFraction) + (myAltitude[bottomX][rightZ] * zFraction);
        altitude = p1 - (p1 * xFraction) + (p2 * xFraction);
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
        Tree tree = new Tree(new Point(x, y, z));
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param x
     * @param z
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }

    public void draw(GL2 gl) {
        
        gl.glMatrixMode(gl.GL_MODELVIEW);
        
    	gl.glPushMatrix();

		gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
    	gl.glPolygonOffset(0,0);
		
		
		Point p1 = new Point();
		Point p2 = new Point();
		Point p3 = new Point();
		Point normal;
		
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

		gl.glBegin(GL2.GL_TRIANGLES);
		
			for (int x = 0; x < size().width - 1; x++) {
				for (int z = 0; z < size().height - 1; z++) {
	
					//Triangle 1 - top right, top left, bottom left,
					p1.reset(x, myAltitude[x][z+1], z+1);
					p2.reset(x, myAltitude[x][z], z);
					p3.reset(x+1, myAltitude[x+1][z], z);
					
					normal = Helpers.calculateNormal(new Point[] {p1, p2, p3});
					gl.glNormal3dv(normal.doubleVector(), 0);
					
					if (texture) {
						// Draws the first triangle 
							gl.glTexCoord2d(0, 0);
						gl.glVertex3dv(p1.doubleVector(), 0);
							gl.glTexCoord2d(1, 0);
						gl.glVertex3dv(p2.doubleVector(), 0);
							gl.glTexCoord2d(0, 1);
						gl.glVertex3dv(p3.doubleVector(), 0);
					} else {
						// Color is calculated based on x y and z (with an aim towards higher green values).
						Color c1 = new Color((z+1)/2, x, myAltitude[x][z+1]/2);
						Color c2 = new Color(z/2, x, myAltitude[x][z]/2);
						Color c3 = new Color(z/2, x+1, myAltitude[x+1][z]/2);
						
						// Draws the first triangle 
							gl.glColor3dv(c1.doubleVector(), 0);
						gl.glVertex3dv(p1.doubleVector(), 0);
							gl.glColor3dv(c2.doubleVector(), 0);
						gl.glVertex3dv(p2.doubleVector(), 0);
							gl.glColor3dv(c3.doubleVector(), 0);
						gl.glVertex3dv(p3.doubleVector(), 0);
					}
					//Triangle 2 -bottom left, bottom right, top right
					p1.reset(x+1, myAltitude[x+1][z], z);
					p2.reset(x+1, myAltitude[x+1][z+1], z+1);
					p3.reset(x, myAltitude[x][z+1], z+1);
					
					normal = Helpers.calculateNormal(new Point[] {p1, p2, p3});
					gl.glNormal3dv(normal.doubleVector(), 0);
					 
					if (texture) {
						gl.glTexCoord2d(1, 0);
						gl.glVertex3dv(p1.doubleVector(), 0);
							gl.glTexCoord2d(1, 1);
						gl.glVertex3dv(p2.doubleVector(), 0);
							gl.glTexCoord2d(0, 1);
						gl.glVertex3dv(p3.doubleVector(), 0);
					} else {
						// Colour is not reset here, to give the fun triangle colour effect
						gl.glVertex3dv(p1.doubleVector(), 0);
						gl.glVertex3dv(p2.doubleVector(), 0);
						gl.glVertex3dv(p3.doubleVector(), 0);
					}
					
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

        gl.glPopMatrix();
	
    }
}
