package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import static org.junit.Assert.*

import org.junit.After
import org.junit.Test

import edu.mayo.cts2.framework.core.config.ConfigConstants
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.FilterComponent
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import groovy.mock.interceptor.*

class BioportalRestServiceTest {
	
	def service = new BioportalRestService()

	@After
	void tearDown(){
		System.clearProperty(ConfigConstants.CTS2_CONFIG_DIRECTORY_ENV_VARIABLE)
	}

	@Test
	void testGetBioportalQueryStringForFilterDefinitions(){
		
		def uriAndName = new URIAndEntityName(
			name:BioportalRestService.DEFINITIONS_NAME,
			uri:BioportalRestService.DEFINITIONS_URI)
		
		def filter = new ResolvedFilter(
			propertyReference:uriAndName)
		
		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&includedefinitions=true", url
	}

	@Test
	void testGetBioportalQueryStringForFilterProperties(){
		
		def uriAndName = new URIAndEntityName(
			name:BioportalRestService.PROPERTIES_NAME,
			uri:BioportalRestService.PROPERTIES_URI)
		
		def filter = new ResolvedFilter(
			propertyReference:uriAndName)
		
		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&includeproperties=true", url
	}
}
