package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryMsg

class CodeSystemCatalogReadTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get a code system catalog entry by name"(){
		
		CodeSystemCatalogEntryMsg result = 
			client.getCts2Resource(server + "codesystem/LNC", CodeSystemCatalogEntryMsg.class);
			
		assertNotNull result
		
		assertEquals "LNC", result.getCodeSystemCatalogEntry().getCodeSystemName()
	}
	
	@Test void "Test get a code system catalog entry by uri"(){
		
		CodeSystemCatalogEntryMsg result =
			client.getCts2Resource(server + "codesystembyuri?uri=http://purl.bioontology.org/ontology/CL", CodeSystemCatalogEntryMsg.class);
			
		assertNotNull result
		
		assertEquals "CL", result.getCodeSystemCatalogEntry().getCodeSystemName()
	}
}
