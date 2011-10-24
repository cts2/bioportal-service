package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntryMsg

class CodeSystemVersionCatalogReadTestIT extends BaseServiceTestITBase {
	
	@Test void TestGetCodeSystemVersion(){
		
		CodeSystemVersionCatalogEntryMsg result = 
			client.getCts2Resource(server + "codesystem/LNC/version/226", CodeSystemVersionCatalogEntryMsg.class);
			
		assertNotNull result
		
		assertEquals "226", result.getCodeSystemVersionCatalogEntry().getOfficialResourceVersionId()
	}
}
