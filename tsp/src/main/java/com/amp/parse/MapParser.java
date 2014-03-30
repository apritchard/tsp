package com.amp.parse;

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

import com.amp.app.Sector;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * Utility class to contain methods involving parsing map files.
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
			for(Map.Entry<String, Integer> edge: s.edgeList.entrySet()){
				if(!sectors.containsKey(edge.getKey())){
					logger.warning("Unknown edge for " + s.name + " linking to " + edge.getKey());
					continue;
				}
				//edge.key = name, edge.value = weight
				sectors.get(s).addLink(sectors.get(edge.getKey()), edge.getValue());
			}
		}
		
		return new HashSet<>(sectors.values());
		
	}
	
	/**
	 * Parse seed files into paths represented by Lists of strings.  Expects input
	 * in the following format:
	 * 
	 * {SectorName, SectorName, ...}
	 * ---
	 * {SectorName, SectorName, ...}
	 * ---
	 * {SectorName, SectorName, ...}
	 * 
	 * @param seedUrl
	 * @return
	 */
	public static List<List<String>> parseSeedFile(URL seedUrl) {
		
		List<YamlPath> seeds = readYamlObjects(seedUrl, YamlPath.class); 
				
		List<List<String>> paths = new ArrayList<>();
		for(YamlPath seed : seeds){
			paths.add(seed.path);
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