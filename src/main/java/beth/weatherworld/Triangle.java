package beth.weatherworld;

// TODO: Do I need these?
import beth.weatherworld.Color;
import beth.weatherworld.Coordinate;
import beth.weatherworld.Point;

// Stores 3 points to draw into a triangle, to save having to generate these each update/draw
public class Triangle {

	public Point[] points;
	public Color[] colors;
	public Coordinate[] textureCoords;
	
	public Triangle (Point p1, Point p2, Point p3, boolean bottomLeft) {
		points = new Point[3];
		points[0] = p1;
		points[1] = p2;
		points[2] = p3;

		// Color is calculated based on x y and z (with an aim towards higher green values).
		colors = new Color[3];
		colors[0] = new Color(p1.z/2, p1.x/2, p1.y/2);
		colors[1] = new Color(p2.z/2, p2.x/2, p2.y/2);
		colors[2] = new Color(p3.z/2, p3.x/2, p3.y/2);
		
		// We have to set the texture co-ords appropriately, depending on whether this triangle is top-left or bottom-right of the square (squares are split into 2 triangles)
		textureCoords = new Coordinate[3];
		if (bottomLeft) { // top left, bottom left, bottom right
			textureCoords[0] = new Coordinate(0,1);
			textureCoords[1] = new Coordinate(0,0);
			textureCoords[2] = new Coordinate(1,0);
		} else { // bottom right, top right, top left
			textureCoords[0] = new Coordinate(1,0);
			textureCoords[1] = new Coordinate(1,1);
			textureCoords[2] = new Coordinate(0,1);
		}
	}
}
