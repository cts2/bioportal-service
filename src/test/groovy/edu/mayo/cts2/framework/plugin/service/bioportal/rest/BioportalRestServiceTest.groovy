package edu.mayo.cts2.framework.plugin.service.bioportal.rest
import edu.mayo.cts2.framework.core.config.ConfigConstants
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.ComponentReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
class BioportalRestServiceTest {

    @Autowired
	BioportalRestService service;

	@After
	void tearDown(){
		System.clearProperty(ConfigConstants.CTS2_CONFIG_DIRECTORY_ENV_VARIABLE)
	}

	@Test
	void testGetBioportalQueryStringForFilterDefinitions(){
		
		def uriAndName = new ComponentReference(
				propertyReference:  new URIAndEntityName(
					name:BioportalRestService.DEFINITIONS_NAME,
					uri:BioportalRestService.DEFINITIONS_URI))
		
		def filter = new ResolvedFilter(
			componentReference:  uriAndName)
		
		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&require_definition=true", url
	}

	@Test
	void testGetBioportalQueryStringForFilterProperties(){
		
		def uriAndName = new ComponentReference(
				propertyReference: new URIAndEntityName(
					name:BioportalRestService.PROPERTIES_NAME,
					uri:BioportalRestService.PROPERTIES_URI))

		def filter = new ResolvedFilter(
			componentReference: uriAndName)

		def url = service.getBioportalQueryStringForFilter(filter);
		
		assertEquals "&include_properties=true", url
	}

    @Test
    void testGetAllEntitiesByAcronym(){

        def xml = service.getAllEntitiesByAcronym("GO", new Page());

        assertNotNull xml
    }
}
