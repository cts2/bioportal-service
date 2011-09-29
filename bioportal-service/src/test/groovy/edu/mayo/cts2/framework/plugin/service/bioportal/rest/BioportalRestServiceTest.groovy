package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import static org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test

import edu.mayo.cts2.framework.core.config.ConfigConstants
import edu.mayo.cts2.framework.core.config.Cts2Config
import edu.mayo.cts2.framework.core.config.Cts2TestConfigFactory
import edu.mayo.cts2.framework.model.core.FilterComponent
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import groovy.mock.interceptor.*

class BioportalRestServiceTest {
	
	def service = new BioportalRestService()
	
	@Before
	void setUp(){
		String path = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
		System.setProperty(ConfigConstants.CTS2_CONFIG_DIRECTORY_ENV_VARIABLE, path)
		String context = UUID.randomUUID();
		def factory = new Cts2TestConfigFactory(context:context) 
		
		try {
			service.cts2Config = factory.getObject();
		} catch (e) {
			//
		}
	}
	
	@After
	void tearDown(){
		System.clearProperty(ConfigConstants.CTS2_CONFIG_DIRECTORY_ENV_VARIABLE)
	}

	@Test
	void testGetBioportalQueryStringForFilterDefinitions(){
		
		def uriAndName = new URIAndEntityName(
			name:BioportalRestService.DEFINITIONS_NAME,
			uri:BioportalRestService.DEFINITIONS_URI)
		
		def filter = new FilterComponent(
			referenceTarget:uriAndName)
		
		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&includedefinitions=true", url
	}

	@Test
	void testGetBioportalQueryStringForFilterProperties(){
		
		def uriAndName = new URIAndEntityName(
			name:BioportalRestService.PROPERTIES_NAME,
			uri:BioportalRestService.PROPERTIES_URI)
		
		def filter = new FilterComponent(
			referenceTarget:uriAndName)
		
		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&includeproperties=true", url
	}
}
