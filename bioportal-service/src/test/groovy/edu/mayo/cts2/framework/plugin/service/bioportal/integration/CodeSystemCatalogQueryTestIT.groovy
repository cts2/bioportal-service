package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryMsg

class CodeSystemCatalogQueryTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get a code system catalog entry by name"(){
		
		CodeSystemCatalogEntryMsg result = 
			client.getCts2Resource(server + "codesystem/LNC", CodeSystemCatalogEntryMsg.class);
			
		assertNotNull result
		
		assertEquals "LNC", result.getCodeSystemCatalogEntry().getCodeSystemName()
	}
}
