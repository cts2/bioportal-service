package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntryMsg
import edu.mayo.cts2.framework.model.entity.EntityDirectory

class EntityDescriptionQueryTestIT extends BaseServiceTestITBase {
	
	@Test void TestQueryAllEntities(){
		
		EntityDirectory result = 
			client.getCts2Resource(server + "entities?matchvalue=cancer", EntityDirectory.class);
			
		assertNotNull result
	}
}
