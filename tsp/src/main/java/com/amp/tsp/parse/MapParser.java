package com.amp.tsp.parse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amp.tsp.mapping.Sector;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * Utility class to contain methods involving parsing maps from yaml files.
 */
public class MapParser {
	private static final Logger logger = Logger.getLogger(MapParser.class.getName());
	
	/**
	 * Parse the provided file into a list of Sectors.  File should be in the following format:
	 * 
	 * SectorName
	 * 	 -{AdjacentSector, EdgeWeight}
	 *   -{AdjacentSector, EdgeWeight}
	 * ---
	 * NextSectorName
	 *   -{AdjacentSector, EdgeWeight}
	 *   
	 * @param file Yaml file to be read.
	 * @return Set of linked Sector objects
	 */
	public static Set<Sector> parseMapFile(URL mapUrl){
		
		List<YamlSector> yamlSectors = readYamlObjects(mapUrl, YamlSector.class);
		
		if(yamlSectors == null){
			logger.severe("No Sector objects found in file " + mapUrl.getPath());
			return null;
		}
		
		Map<String,Sector> sectors = new HashMap<>();
		
		//First pull out the list of sector names
		for(YamlSector s : yamlSectors){
			sectors.put(s.name, new Sector(s.name));
		}
		
		//Then link sectors to their neighbors
		for(YamlSector s : yamlSectors){
			for(Map.Entry<String, String> edge: s.edgeList.entrySet()){
				if(!sectors.containsKey(edge.getKey())){
					logger.warning("Unknown edge for " + s.name + " linking to " + edge.getKey());
					continue;
				}

				Sector s1 = sectors.get(s.name);
				Sector s2 = sectors.get(edge.getKey());
				Integer weight = Integer.valueOf(edge.getValue());
				s1.addEdge(s2, weight);
			}
		}
		
		return new HashSet<>(sectors.values());
	}
	
	/**
	 * Parse seed files into paths represented by Lists of Sectors.  Expects input
	 * in the following format:
	 * 
	 * {SectorName, SectorName, ...}
	 * ---
	 * {SectorName, SectorName, ...}
	 * ---
	 * {SectorName, SectorName, ...}
	 * 
	 * @param seedUrl Location of seed file.
	 * @param sectors Set of sectors from which to retrieve object references by their names
	 * 				  in the seed file.
	 * @return List of Sector lists representing the paths with which to seed a search.
	 */
	public static List<List<Sector>> parseSeedFile(URL seedUrl, Set<Sector> sectors){
		
		List<YamlPath> yamlPaths = readYamlObjects(seedUrl, YamlPath.class);
		
		if(yamlPaths == null){
			logger.warning("No seeds found in file " + seedUrl.getPath());
			return null;
		}
		
		//rebuild string/sector map for one time lookup
		Map<String, Sector> sectorByName = new HashMap<>();
		for(Sector s : sectors){
			sectorByName.put(s.getName(), s);
		}
		
		List<List<Sector>> paths = new ArrayList<>();
		
		for(YamlPath p : yamlPaths){
			List<Sector> path = new ArrayList<>();
			for(String name : p.path){
				if(!sectorByName.containsKey(name)){
					logger.warning("Invalid Seed " + name + " not found in map file; ignoring seed.");
				} else {
					path.add(sectorByName.get(name));
				}
			}
			paths.add(path);
		}
		return paths;
	}
	
	/**
	 * Parse all objects from the provided file into the specified class.
	 * @param fileUrl
	 * @param clazz
	 * @return List of objects of the specified type contained in the file.  Null if the file
	 * 			is invalid, inaccessible, or contains invalid object types. 
	 */
    private static <T> List<T> readYamlObjects(URL fileUrl, Class<T> clazz) {
        logger.info("Parsing " + fileUrl.toString() + " for " + clazz.getSimpleName() + " objects");
        
        List<T> ts = null;
        
        try {
        	Path p = Paths.get(fileUrl.toURI());
            YamlReader reader = new YamlReader(Files.newBufferedReader(p, Charset.forName("UTF-8")));
            ts = readYamlObjects(reader, clazz);
            
        } catch (URISyntaxException use) {
        	logger.log(Level.SEVERE, "Invalid file path provided for " + fileUrl.getPath(), use);
        } catch (YamlException ye){
        	logger.log(Level.SEVERE, "Invalid object interfered with parsing, halting.", ye);
        } catch (IOException ioe) {
			logger.log(Level.SEVERE, "Unable to access file " + fileUrl.getPath() + " with reader.", ioe);
		} 
        
        return ts;
    }
	
    /**
     * Attempt to parse all objects from the given YamlReader into the
     * specified class type.  Returns a list of that type of objects.
     * @param reader
     * @param clazz
     * @return
     * @throws YamlException If error parsing yaml file or file contains objects of other types.
     */
    private static <T> List<T> readYamlObjects(YamlReader reader, Class<T> clazz) throws YamlException{
        List<T> ts = new ArrayList<>();
        
        T t = reader.read(clazz);
        while(t != null){
        	ts.add(t);
        	t = reader.read(clazz);
        }
        return ts;
    }
}