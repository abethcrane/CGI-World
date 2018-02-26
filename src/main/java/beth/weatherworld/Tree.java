package beth.weatherworld;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.util.Random;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;

import beth.weatherworld.Texture;

public class Tree
{
	private Point myPos;

	private double startingHeight = 0.1f;
    private double startingBase = 0.01;
	private double startingTop = 0.005;
	private double startingRadius = 0.01;
	private double currentHeight;
	private double currentBase;
	private double currentTop;
	private double currentRadius;
	private double maxHeight;
	private double maxTrunkBase;
	private double maxTrunkTop;
	private double maxPuffRadius;
	private double growthSpeed;

	private Texture myTexture;
	private Color puffColor;
	private Color puffColor2;

	public Tree(Point p)
	{
    	// Randomly determines various features
        myPos = p;

        Random r = new Random();
        maxHeight = 0.5 + r.nextInt(4)*r.nextDouble() + r.nextDouble();
        maxTrunkBase = maxHeight / (5 + r.nextInt(5));
		maxTrunkTop = maxTrunkBase;
		maxPuffRadius = maxTrunkTop*(1.3 + r.nextDouble()/2);
		
		currentBase = startingBase;
		currentHeight = startingHeight;
		currentTop = startingTop;
		currentRadius = startingRadius;

		growthSpeed = r.nextDouble() / 10;

        // Randomly generates a solid and a mesh colour
        puffColor = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble());
        puffColor2 = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble());
    }

	public double[] getPosition()
	{
        return myPos.doubleVector();
    }

	public void drawTrunk(GL2 gl, Terrain t)
	{

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
			
			int slices = Math.max((int)Math.floor(currentHeight / 0.01), 3);
			int stacks = Math.max((int)Math.floor(currentRadius / 0.01), 3);

			currentBase = Math.min(maxTrunkBase, currentBase + growthSpeed/2);
			currentHeight = Math.min(maxHeight, currentHeight + growthSpeed);
			currentTop = Math.min(maxTrunkTop, currentTop + growthSpeed/2);

			glu.gluCylinder(q, currentBase, currentTop, currentHeight, slices, stacks);
	    	gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glPopMatrix();
    }

	public void drawPuffball(GL2 gl, Terrain t)
	{
    	// Draws a glut sphere
    	GLUT glut = new GLUT();
		gl.glPushMatrix();
		    currentRadius = Math.min(maxPuffRadius, currentRadius + growthSpeed);

			gl.glTranslated(myPos.x, t.altitude(myPos.x, myPos.z)+currentHeight+currentRadius/2, myPos.z);
			// First solid, then mesh
			gl.glColor3dv(puffColor.doubleVector(), 0);
		   // gl.glMaterialf(gl.GL_FRONT, gl.GL_SHININESS, 10.0f);

			glut.glutSolidSphere(currentRadius, 10, 10);
			gl.glColor3dv(puffColor2.doubleVector(), 0);
			glut.glutWireSphere(currentRadius, 10, 10);
            //gl.glMaterialf(gl.GL_FRONT, gl.GL_SHININESS, 0f);
		gl.glPopMatrix();
    }

	public void draw(GL2 gl, Terrain t)
	{
    	drawTrunk(gl, t);
    	drawPuffball(gl, t);
    }
}
