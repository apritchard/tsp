package com.amp.tsp;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import org.apache.log4j.Logger;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.amp.tsp.app.App;

@RunWith(Suite.class)
@SuiteClasses({ CorrectnessTest.class, PerformanceTest.class })
public class TspTestSuite {
	
	private static final Logger logger = Logger.getLogger(TspTestSuite.class);

	@BeforeClass
	public static void setUp(){
		try{
			InputStream is = App.class.getResourceAsStream("/logging.properties");
			LogManager.getLogManager().readConfiguration(is);
		} catch (IOException ioe){
			logger.warn("Failed to find logger.properties");
		}	
	}
}
