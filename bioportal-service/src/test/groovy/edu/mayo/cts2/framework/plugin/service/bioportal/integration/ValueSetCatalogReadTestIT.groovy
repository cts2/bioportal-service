package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryMsg
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntryMsg

class ValueSetCatalogReadTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get a value set by name"(){
		
		ValueSetCatalogEntryMsg result = 
			client.getCts2Resource(server + "valueset/SNOMEDCT-MAS", ValueSetCatalogEntryMsg.class);
			
		assertNotNull result
		
		assertEquals "SNOMEDCT-MAS", result.getValueSetCatalogEntry().getValueSetName()
	}
	
}
