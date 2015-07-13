package beth.weatherworld;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import org.json.JSONException;

import beth.weatherworld.Texture;
import beth.weatherworld.LevelIO;

/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements KeyListener, MouseMotionListener, GLEventListener {

    private Terrain myTerrain;
    private Sky mySky;
    
    private DroneCamera myDrone;
    private PersonCamera myPersonCam;
    private boolean usingDrone = true;
    private boolean usingPerson = false;
    
    private Person myPerson;
    
    private GL2 gl;
    private GLU glu = new GLU();
    
    private static final float FOV = 90;
    
    private int myWidth = 1920;
    private int myHeight = 1080;
    
    // Toggle whether we start in day or night mode
    public static boolean day = true;
    public static boolean dayNightMode = true;
    public static long dayLength = 24*60*60; // 24 seconds in a day, 24 seconds in a night, as opposed to 24 hours all up
    public static long dayStart;
    public static long nightStart;
    public static long timeLeftInDay;
    public static long timeLeftInNight;
    public static float fractionThroughDay;
    public static float fractionThroughNight;
    
    // Colour specifications for the background
    private float[] dayTimings = new float[] {0.03f, 0.06f, 0.1f, 0.3f, 0.8f, 0.95f, 0.98f, 1.0f};
    private Color[] dayColors = new Color[] {Helpers.orange, Helpers.pink, Helpers.deepBlue, Helpers.brightBlue, Helpers.brightBlue, Helpers.pink, Helpers.orange, Helpers.deepBlue};
    private float[] nightTimings = new float[] {0.1f, 0.3f, 0.6f, 0.8f, 0.95f, 1.0f};
    private Color[] nightColors = new Color[] {Helpers.deepBlue, Helpers.midnightBlue, Helpers.black, Helpers.midnightBlue, Helpers.deepBlue, Helpers.orange};    
    
    // Light specifications
    float[] dayAmbient = {0.3f, 0.3f, 0.3f, 1};
    float[] nightAmbient = {0.1f, 0.1f, 0.1f, 1}; // low ambient light
	float[] diffuse = {1,1,1,1}; // full diffuse colour
	float daySpecular[] = { 0.6f, 0.6f, 0.6f, 1.0f };
	float[] nightSpecular = {0.9f, 0.9f, 0.9f, 1}; 
	
	public static Map myTextures;
	
    public Game(Terrain t) {
    	super ("Weatherworld");
        myTerrain = t;
        // Makes a new sky, needs no parameters (sun and stars and cloud are randomly generated)
        mySky = new Sky(myTerrain);
        myTextures = new HashMap<String, Texture>();
    }
    
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, JSONException {
    	dayStart = System.currentTimeMillis();
    	nightStart = System.currentTimeMillis();
		
		Terrain terrain;
		
		if (args.length > 0) {
    		terrain = LevelIO.load(new File(args[0]));
		} else {
			terrain = LevelIO.generate();
			Date d = new Date(System.currentTimeMillis());
			File f = new File("levels/" + d.toString() + "-levelFile.json");
			LevelIO.save(terrain, f);
		}
		
        terrain.initialize();

        Game game = new Game(terrain);
        game.run();
    }
    
	@Override
	public void init(GLAutoDrawable arg0) {
        gl = arg0.getGL().getGL2();
        
        // Enable standard things - these are enabled and disabled in relevant places
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
    	gl.glEnable(gl.GL_POLYGON_OFFSET_FILL);
    	gl.glEnable(GL2.GL_NORMALIZE);

    	// Read the textures in and store them in a dictionary
		// TODO: What's the best way to reference these resources relatively?
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/tree.png", "png"), "tree");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/particle.bmp", "bmp"), "particle1");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/particle_mask.bmp", "bmp"), "particle2");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/sun1.png", "png"), "sun1");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/sun2.png", "png"), "sun2");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/road.png", "png"), "road");
    	addTexture(new Texture(GLProfile.getDefault(), gl, "/Users/beth/Dropbox/projects/weatherworld/src/main/resources/textures/grass.jpg", "jpg"), "grass");
	}
    
    /** 
     * Run the game.
     *
     */
    public void run() {
    	// Makes a standard glprofile and panel, setting this as the key and mousemotion listener
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        GLJPanel panel = new GLJPanel(glcapabilities);

        panel.addGLEventListener(this);
        panel.addKeyListener(this);
        panel.addMouseMotionListener(this);
        panel.setFocusable(true);
        panel.requestFocus();

        // Add an animator to call 'display' at 60fps        
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        // Starts at 800, 800 and visible
        getContentPane().add(panel, BorderLayout.CENTER);
		// TODO: Make this full screen
        setSize(1920, 1080);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);  
        
        // Sets the center point of the terrain and makes the camera and avatar
        Point c = new Point(myTerrain.center);
        myDrone = new DroneCamera(new Point(c.x, c.y + 10, c.z), c);
        myPersonCam = new PersonCamera(new Point(0, myTerrain.altitude(0,0), 0), new Point(c.x, myTerrain.altitude(c.x, c.z), c.z));
        myPerson = new Person(c.x, c.y, c.z);
    }

	@Override
	public void keyPressed(KeyEvent e) {
		// Toggles various settings to true (in order to handle key down registered as continuous key press).
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_D) {
			usingDrone = true;
			usingPerson = false;
		}
		if (key == KeyEvent.VK_P) {
			usingPerson = true;
			usingDrone = false;
		}
		if (key == KeyEvent.VK_W) {
			myDrone.zoomIn = true;
			myPersonCam.tiltUp = true;
		}
		if (key == KeyEvent.VK_S) {
			myDrone.zoomOut = true;
			myPersonCam.tiltDown = true;
		}
		if (key == KeyEvent.VK_UP) {
			myPersonCam.forward = true;
		}
		if (key == KeyEvent.VK_DOWN) {
			myPersonCam.backward = true;
		}
		if (key == KeyEvent.VK_LEFT) {
			myPersonCam.left = true;
		}
		if (key == KeyEvent.VK_RIGHT) {
			myPersonCam.right = true;
		}
		if (key == KeyEvent.VK_PERIOD) {
			myPersonCam.faster = true;
			myDrone.faster = true;
		}
		if (key == KeyEvent.VK_COMMA) {
			myPersonCam.slower = true;
			myDrone.slower = true;
		}
		// Toggles the texture of the terrain
		if (key == KeyEvent.VK_T) {
			myTerrain.texture = myTerrain.texture == true ? false : true;
		}	
		// Toggles day night mode on and off
		if (key == KeyEvent.VK_N) {
			dayNightMode = dayNightMode == true ? false : true;
		}	
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Toggles various settings to true (in order to handle key down registered as continuous key press).
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_W) {
			myDrone.zoomIn = false;
			myPersonCam.tiltUp = false;
		}
		if (key == KeyEvent.VK_S) {
			myDrone.zoomOut = false;
			myPersonCam.tiltDown = false;
		}
		if (key == KeyEvent.VK_UP) {
			myPersonCam.forward = false;
		}
		if (key == KeyEvent.VK_DOWN) {
			myPersonCam.backward = false;
		}
		if (key == KeyEvent.VK_LEFT) {
			myPersonCam.left = false;
		}
		if (key == KeyEvent.VK_RIGHT) {
			myPersonCam.right = false;
		}
		if (key == KeyEvent.VK_PERIOD) {
			myPersonCam.faster = false;
			myDrone.faster = false;
		}
		if (key == KeyEvent.VK_COMMA) {
			myPersonCam.slower = false;
			myDrone.slower = false;
		}		
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void display(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL2();

        // Update the clock
        Helpers.updateDayNight(gl);
        
        // Set background colour
        Color c = Helpers.brightBlue;
        if (dayNightMode) {
        	c = Helpers.getColor(dayTimings, dayColors, nightTimings, nightColors);	
        }
        gl.glClearColor((float)c.r, (float)c.g, (float)c.b, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
		// Update the camera positions - don't change the view yet though
		myPersonCam.update(myTerrain);
        myPerson.update(gl, myPersonCam, myTerrain);
		// TODO: Uncertain if this will give a good experience
		myDrone.update();
		
    	// If it's day we update the camera first before the global light
    	if (day) {
    		// Update the Cameras
            if (usingDrone) {
				myDrone.use(gl,glu);
            } else if (usingPerson) {
				myPersonCam.use(gl,glu);
            }

            //Update the lights
            setDayLights(gl);
        // If night we update the lighting first
    	} else {
    		// Update the lights
    		setNightLights(gl);

    		// Update the Cameras
            if (usingDrone) {
				myDrone.use(gl,glu);
            } else if (usingPerson) {
				myPersonCam.use(gl,glu);
            }
    	}
    	
    	// Draw the sky first (so clouds don't get in the way when we look at the terrain)
        mySky.draw(gl);
        // Draw the terrain
        myTerrain.draw(gl);
        
        if (usingPerson) {
        	myPerson.update(gl, myPersonCam, myTerrain);
        }

	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {
        gl = drawable.getGL().getGL2();
        
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        double aspect = 1.0 * width / height;
        
        myWidth = width;
        myHeight = height;
        glu.gluPerspective(FOV, aspect, 0.1, 100);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Updates the drone camera (controllable by mouse)	
		myDrone.mouseMove(e.getX(), e.getY(), myWidth, myHeight);
	}
	
    public void addTexture(Texture texture, String s) {
    	myTextures.put(s, texture);
    }
	
    public void background () {
    	//Attempt at making a gradient background. Failed :(
        gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		Point p1 = new Point (-1, -1, 0);
		Point p2 = new Point (-1, 1, 0);
		Point p3 = new Point (1, 1, 0);
		Point p4 = new Point (1, -1, 0);
		
		gl.glBegin(gl.GL_QUADS);
			gl.glColor3dv(Helpers.pink.doubleVector(), 0);
			gl.glVertex3dv(p1.doubleVector(), 0);
			gl.glColor3dv(Helpers.orange.doubleVector(), 0);
			gl.glVertex3dv(p2.doubleVector(), 0);
			gl.glColor3dv(Helpers.brightBlue.doubleVector(), 0);
			gl.glVertex3dv(p3.doubleVector(), 0);
			gl.glColor3dv(Helpers.yellow.doubleVector(), 0);
			gl.glVertex3dv(p4.doubleVector(), 0);
		gl.glEnd();
		gl.glMatrixMode(gl.GL_PROJECTION);
    }
    
    void setDayLights(GL2 gl) {
    	// We could use myTerrain.getSunlight() here instead if we wanted the map one
        Point sun = new Point(mySky.mySun.p.doubleVector());

		gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, dayAmbient, 0);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, diffuse, 0);
    	gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, sun.floatVector(), 0);
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, daySpecular, 0);
		gl.glMaterialfv(gl.GL_FRONT,gl.GL_SPECULAR, daySpecular,0);
		gl.glMateriali(gl.GL_FRONT, gl.GL_SHININESS, 56);
    }
    
    void setNightLights(GL2 gl) {
    	// Get the position of the camera
		float[] cp = myPersonCam.getPosition();
		gl.glLightfv(gl.GL_LIGHT1, gl.GL_POSITION, new float[] {cp[0], cp[1], cp[2], 1}, 0);
		// Get the vector between camera looking at and camera position
		float[] la = myPersonCam.getDirection();
		gl.glLightfv(gl.GL_LIGHT1,gl.GL_SPOT_DIRECTION, new float[] {la[0], la[1], la[2], 1} ,1);
		
	    gl.glLightf(gl.GL_LIGHT1,gl.GL_SPOT_CUTOFF,10f); // Very narrow
	    gl.glLightf(gl.GL_LIGHT1,gl.GL_SPOT_EXPONENT, 128f); // Very bright
	    
		gl.glLightfv(gl.GL_LIGHT1, gl.GL_QUADRATIC_ATTENUATION, new float[] {1f},0);
	    
		gl.glLightfv(gl.GL_LIGHT2, gl.GL_AMBIENT, nightAmbient, 0);
		gl.glLightfv(gl.GL_LIGHT2, gl.GL_SPECULAR, nightSpecular, 0);
		gl.glMaterialfv(gl.GL_FRONT,gl.GL_SPECULAR, nightSpecular,0);
		gl.glMateriali(gl.GL_FRONT, gl.GL_SHININESS, 56);
    }
}
