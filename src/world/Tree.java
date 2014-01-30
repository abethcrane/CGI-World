package ass2;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.jogamp.opengl.util.gl2.GLUT;

import ass2.Texture;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private Point myPos;
    private double height;
	private double base;
	private double top;
	private int slices;
	private int stacks;
	private double radius = 1;
	private Texture myTexture;
	Color puffColor;
	Color puffColor2;
	
    public Tree(Point p) {
    	// Randomly determines various features
        myPos = p;
        
        Random r = new Random();
        height = 2 + r.nextDouble()*2;
        base = 0.1;
        top = 0.05;
        slices = 3 + r.nextInt(10);
        stacks = 3 + r.nextInt(10);
        
        // Randomly generates a solid and a mesh colour
        puffColor = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble());
        puffColor2 = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble());
    }
    
    public double[] getPosition() {
        return myPos.doubleVector();
    } 
    
    public void drawTrunk(GL2 gl, Terrain t) {
    	
    	myTexture = (Texture) Game.myTextures.get("tree");
    	// Textures the trunk to look like Dr Seuss trees
    	gl.glBindTexture(gl.GL_TEXTURE_2D, myTexture.getTextureID());
        // Use the texture to modulate diffuse and ambient lighting
        gl.glTexEnvf(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_MODULATE);
    	
    	//Create a GLU quadric object to draw a cylinder
    	GLU glu = new GLU();
    	gl.glPushMatrix();
    		gl.glEnable(gl.GL_TEXTURE_2D);
    		gl.glColor3d(1, 1, 1);
	    	GLUquadric q = glu.gluNewQuadric();    	 
	    	glu.gluQuadricDrawStyle(q, glu.GLU_FILL);
	    	glu.gluQuadricTexture(q, true);
	    	gl.glTranslated(myPos.x, t.altitude(myPos.x, myPos.z), myPos.z);
	    	// Rotate 90 (auto draws with z up, so rotate to have y up)
	    	gl.glRotated(-90, 1, 0 , 0);
	    	glu.gluCylinder(q, base, top, height, slices, stacks);
	    	gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glPopMatrix();
    }
    
    public void drawPuffball(GL2 gl, Terrain t) {
    	// Draws a glut sphere
    	GLUT glut = new GLUT();
		gl.glPushMatrix();
			gl.glTranslated(myPos.x, t.altitude(myPos.x, myPos.z)+height, myPos.z);
			// First solid, then mesh
			gl.glColor3dv(puffColor.doubleVector(), 0);
			glut.glutSolidSphere(radius, 10, 10);
			gl.glColor3dv(puffColor2.doubleVector(), 0);
			glut.glutWireSphere(radius, 10, 10);
		gl.glPopMatrix();
    }
    
    public void draw(GL2 gl, Terrain t) {
    	drawTrunk(gl, t);
    	drawPuffball(gl, t);
    }
    
    
}
