package beth.weatherworld;

import beth.weatherworld.Point;

public class TriangleStrip
{
    Point vertex;
    Point vertexNormal;
    Point texCoord;

    public TriangleStrip(Point v, Point normal, Point coord)
    {
        vertex = v;
        vertexNormal = normal;
        texCoord = coord;
    }
}
