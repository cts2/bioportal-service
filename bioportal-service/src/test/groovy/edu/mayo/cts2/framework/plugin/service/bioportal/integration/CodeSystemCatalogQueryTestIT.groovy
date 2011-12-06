package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryDirectory

class CodeSystemCatalogQueryTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get code systems"(){
		
		CodeSystemCatalogEntryDirectory result = 
			client.getCts2Resource(server + "codesystems", CodeSystemCatalogEntryDirectory.class);
			
		assertNotNull result
		
		assertTrue result.getEntryCount() > 0
	}
}
