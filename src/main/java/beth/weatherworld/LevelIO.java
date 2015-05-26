package beth.weatherworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.Math;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import beth.weatherworld.Point;

/**
 * COMMENT: Comment LevelIO 
 *
 * @author malcolmr
 */
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
            }
        }
        return terrain;
    }
    
    public static Terrain generate() {
        Random rand = new Random();
        
        int width = rand.nextInt(10) + 3;
        int depth = rand.nextInt(10) + 3;
        Terrain terrain = new Terrain(width, depth);
        
        float dx = rand.nextInt();
        float dy = rand.nextInt();
        float dz = rand.nextInt();
        terrain.setSunlightDir(dx, dy, dz);
        
        Double prevAltitude = 0.0;
        for (int x = 0; x < width; x++) {
           for (int z = 0; z < depth; z++) {
               terrain.setGridAltitude(x, z, prevAltitude - 1 + rand.nextDouble() * 2);
           }
        }
       
        int numTrees = rand.nextInt(Math.max(1,width*depth/10));
        for (int i = 0; i < numTrees; i++) {
            double x = rand.nextDouble() * rand.nextInt(width);
            double z = rand.nextDouble() * rand.nextInt(depth);
            terrain.addTree(x, z);
        }
        
        boolean hasRoads = rand.nextBoolean();
        int numRoads = 1; // TODO: Make rand.nextInt(4);
        if (hasRoads) {
            for (int i = 0; i < numRoads; i++) {
                double w = rand.nextDouble();
                
                // TODO: How many splines can a road have?
                int splineLength = 4;
                Point[] spline = new Point[splineLength];
                
                for (int j = 0; j < splineLength; j++) {
                    double x = rand.nextDouble() * Math.min(width, depth);
                    double z = rand.nextDouble() * Math.min(width, depth);
                    spline[j] = new Point(x, 0, z);
                }
                
                terrain.addRoad(w, spline);
            }
        }
        return terrain;
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
