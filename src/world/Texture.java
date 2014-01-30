package ass2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Week 07 example Texture class
 * @author malcolmr
 */

public class Texture {
	
	public static final boolean TEXTURE_MIPMAP_EANBLED = true; 
	int[] textureID = new int[1];
	
	public Texture(GLProfile glp, GL2 gl, String filename, String fileType) {
		// Load the texture data from file to raw data in memory.
		// Src: http://www.land-of-kain.de/docs/jogl/

		TextureData data = null;
		try {
			File file = new File(filename);
			data = TextureIO.newTextureData(glp, file, false, fileType);
		} catch (IOException exc) {
            exc.printStackTrace();
            System.exit(1);
        }

		// Create the OpenGL texture object.
		System.out.println(textureID[0]);
		gl.glGenTextures(1, textureID, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, getTextureID());

		// Build the texture from data.
		if (TEXTURE_MIPMAP_EANBLED) {
			// Set texture parameters to enable automatic mipmap generation and bilinear filtering.
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);
	        
	        // Now build the texture (and generate mipmaps).
	        GLU glu = new GLUgl2();
	        glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D,
	        		              data.getInternalFormat(),
	        		              data.getWidth(),
	        		              data.getHeight(),
	        		              data.getPixelFormat(),
	        		              data.getPixelType(),
	        		              data.getBuffer());
		} else {
			// Set texture parameters to enable bilinear filtering.
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
	        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
	        
	        // Build texture initialised with image data.
	        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0,
	        				data.getInternalFormat(),
	        				data.getWidth(),
	        				data.getHeight(),
	        				0,
	        				data.getPixelFormat(),
	        				data.getPixelType(),
	        				data.getBuffer());
		}
        
	}
	
	public Texture(GL2 gl, String filename, String fileType) {
		this(GLProfile.getDefault(), gl, filename, fileType);
	}
	
	public int getTextureID() {
		return textureID[0];
	}
	
	public void release(GL2 gl) {
		if (textureID[0] > 0) {
			gl.glDeleteTextures(1, textureID, 0);
		}
	}
}
