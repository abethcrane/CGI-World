package beth.weatherworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import beth.weatherworld.Point;


public class LevelIO {

    /**
     * Load a terrain object from a JSON file
     *
     * @param mapFile
     * @return
     * @throws FileNotFoundException
     */
    public static Terrain load(File mapFile) throws FileNotFoundException, JSONException {
        Reader in = new FileReader(mapFile);
        JSONTokener jtk = new JSONTokener(in);
        JSONObject jsonTerrain = new JSONObject(jtk);

        int width = jsonTerrain.getInt("width");
        int depth = jsonTerrain.getInt("depth");
        Terrain terrain = new Terrain(width, depth);

        JSONArray jsonSun = jsonTerrain.getJSONArray("sunlight");
        float dx = (float)jsonSun.getDouble(0);
        float dy = (float)jsonSun.getDouble(1);
        float dz = (float)jsonSun.getDouble(2);
        terrain.setSunlightDir(dx, dy, dz);

        JSONArray jsonAltitude = jsonTerrain.getJSONArray("altitude");
        for (int i = 0; i < jsonAltitude.length(); i++) {
            int x = i % width;
            int z = i / width;

            double h = jsonAltitude.getDouble(i);
            terrain.setGridAltitude(x, z, h);
        }

        if (jsonTerrain.has("trees")) {
            JSONArray jsonTrees = jsonTerrain.getJSONArray("trees");
            for (int i = 0; i < jsonTrees.length(); i++) {
                JSONObject jsonTree = jsonTrees.getJSONObject(i);
                double x = jsonTree.getDouble("x");
                double z = jsonTree.getDouble("z");
                terrain.addTree(x, z);
            }
        }

        if (jsonTerrain.has("roads")) {
            JSONArray jsonRoads = jsonTerrain.getJSONArray("roads");
            for (int i = 0; i < jsonRoads.length(); i++) {
                JSONObject jsonRoad = jsonRoads.getJSONObject(i);
                double w = jsonRoad.getDouble("width");

                JSONArray jsonSpline = jsonRoad.getJSONArray("spline");
                Point[] spline = new Point[jsonSpline.length()];

                for (int j = 0; j < jsonSpline.length(); j += 2) {
                    spline[j] = new Point(jsonSpline.getDouble(j), 0, jsonSpline.getDouble(j+1));
                }
                terrain.addRoad(w, spline);

                flattenRoad(terrain, spline);
            }
        }

        return terrain;
    }

    public static Terrain generate() {
        Random rand = new Random();

        int width = 5 + rand.nextInt(10) + rand.nextInt(20);
        int depth = 5 + rand.nextInt(10) + rand.nextInt(20);
        Terrain terrain = new Terrain(width, depth);

        float dx = rand.nextInt();
        float dy = rand.nextInt();
        float dz = rand.nextInt();
        terrain.setSunlightDir(dx, dy, dz);

        // TODO: Set hills and then lerp out around them. Given them a height and maybe a width?

        Double prevAltitude = 3.0;
        for (int x = 0; x < width; x++) {
           for (int z = 0; z < depth; z++) {
               // Combine squares around you to get an average
               prevAltitude = (terrain.getGridAltitude(x-1, z) +
                               terrain.getGridAltitude(x, z-1) +
                               terrain.getGridAltitude(x-1, z-1) +
                               terrain.getGridAltitude(x-1, z+1)) / 4;
              // Negative change, 0 change or positive change
              prevAltitude += (rand.nextInt(3)-1) * (rand.nextDouble()) / 2;
              terrain.setGridAltitude(x, z, Math.min(0,prevAltitude));
           }
        }

        int numTrees = 3 + rand.nextInt(Math.max(1,width*depth/10));
        for (int i = 0; i < numTrees; i++)
        {
            // In an attempt to space the trees out, we increment the starting point for our randomness

            double x = i*rand.nextDouble() + rand.nextInt(width) + 1;
            double z = i*rand.nextDouble() + rand.nextInt(depth) + 1;

            x = Math.min(x, width);
            z = Math.min(z, depth);

            terrain.addTree(x, z);
        }

        // TODO: add something in where any trees on any of the grid cells around the roads are also 'flattened'

        boolean hasRoads = true;//rand.nextBoolean();
        int numRoads = 1; // TODO: Make rand.nextInt(4);
        if (hasRoads) {
            for (int i = 0; i < numRoads; i++) {
                double w = 0.1 + rand.nextDouble() * 0.8;

                int splineLength = 4;
                Point[] spline = new Point[splineLength];

                for (int j = 0; j < splineLength; j++) {
                    double x = rand.nextDouble() * Math.min(width, depth);
                    double z = rand.nextDouble() * Math.min(width, depth);
                    spline[j] = new Point(x, 0, z);
                }

                terrain.addRoad(w, spline);

                flattenRoad(terrain, spline);
            }
        }

        // TODO: calculate the grid cells that my road intersects
        // and then, in each chunk, make it be the same height all across the road. 

        return terrain;
    }

    // TODO: I stole this from roads, let's not keep it here
    /**
     * Calculate the position of a point on a cubic bezier curve
     * http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Cubic_B.C3.A9zier_curves
     */
    private static void flattenRoad(Terrain terrain, Point[] spline)
    {
        double tInt = 0.01;
        int numPositions = (int)(1.0/tInt);
        for (double t = 0; t < 1; t += tInt) {
            Point currentPoint = new Point();

            // Work out where the point is
            Point temp = new Point(spline[0]);
            temp.scalarMultiply(Math.pow((1-t), 3));
            currentPoint.plus(temp);

            temp = new Point(spline[1]);
            temp.scalarMultiply(Math.pow((1-t), 2) * 3 * t);
            currentPoint.plus(temp);

            temp = new Point(spline[2]);
            temp.scalarMultiply(Math.pow(t, 2) * 3 * (1-t));
            currentPoint.plus(temp);

            temp = new Point(spline[3]);
            temp.scalarMultiply(Math.pow(t, 3));
            currentPoint.plus(temp);

            // Now flatten/raise the cells around the road to be whatever height the midpoint is
            double altitude = terrain.altitude(currentPoint.x, currentPoint.z);
            int floorX = (int)Math.floor(currentPoint.x);
            int floorZ = (int)Math.floor(currentPoint.z);

            int[] dx = {-1, 0, 1};
            int[] dz = {-1, 0, 1};

            for (int x : dx)
            {
                for (int z : dz)
                {
                    if (floorX + x >= 0 && floorX + x < terrain.width() && floorZ + z >= 0 && floorZ + z < terrain.depth())
                    {
                        terrain.setGridAltitude(floorX + x, floorZ + z, altitude);
                        RemoveTreesInGridCell(terrain, new Point(floorX + x, 0, floorZ + z));
                    }
                }
            }
        }
    }

    private static void RemoveTreesInGridCell(Terrain terrain, Point p)
    {
        List<Tree> treesToRemove = new ArrayList<Tree>();
        for (Tree t : terrain.trees())
        {
            int floorX = (int)Math.floor(t.getPosition()[0]);
            int floorZ = (int)Math.floor(t.getPosition()[1]);

            if (floorX == p.x && floorZ == p.z)
            {
                //System.out.println("Removing tree at: " + t.getPosition()[0] + ", " + t.getPosition()[1]);
                treesToRemove.add(t);
            }
            else
            {
                //System.out.println("Tree at " + t.getPosition()[0] + ", " + t.getPosition()[1] + " is okay!");
            }
        }

        for (Tree t : treesToRemove)
        {
            terrain.removeTree(t);
        }
    }

    /**
     * Write Terrain to a JSON file
     *
     * @param file
     * @throws IOException
     */
    public static void save(Terrain terrain, File file) throws IOException, JSONException {
        JSONObject json = new JSONObject();

        int width = terrain.width();
        int depth = terrain.depth();
        json.put("width", width);
        json.put("depth", depth);

        JSONArray jsonSun = new JSONArray();
        float[] sunlight = terrain.getSunlight().floatVector();
        jsonSun.put(sunlight[0]);
        jsonSun.put(sunlight[1]);
        jsonSun.put(sunlight[2]);
        json.put("sunlight", jsonSun);

        JSONArray altitude = new JSONArray();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < depth; j++) {
                altitude.put(terrain.getGridAltitude(i, j));
            }
        }
        json.put("altitude", altitude);

        JSONArray trees = new JSONArray();
        for (Tree t : terrain.trees()) {
            JSONObject j = new JSONObject();
            double[] position = t.getPosition();
            j.put("x", position[0]);
            j.put("z", position[2]);
            trees.put(j);
        }
        json.put("trees", trees);

        JSONArray roads = new JSONArray();
        for (Road r : terrain.roads()) {
            JSONObject j = new JSONObject();
            j.put("width", r.width());

            JSONArray spline = new JSONArray();
            int numPoints = r.numPoints();
            Point[] points = r.points();

            for (int i = 0; i < numPoints; i++) {
                spline.put(points[i].x);
                spline.put(points[i].z);
            }

            j.put("spline", spline);
            roads.put(j);
        }
        json.put("roads", roads);

        FileWriter out = new FileWriter(file);
        json.write(out);
        out.close();

    }

    /**
     * For testing.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JSONException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        LevelIO.save(terrain, new File(args[1]));
    }

}
