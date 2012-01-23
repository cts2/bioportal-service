package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntryDirectory

class ValueSetCatalogQueryTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get value sets"(){
		
			
		ValueSetCatalogEntryDirectory result = 
			client.getCts2Resource(server + "valuesets", ValueSetCatalogEntryDirectory.class);
			
		assertNotNull result

		assertTrue result.getEntryCount() > 0
	}
}
