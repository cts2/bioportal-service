package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Ignore
import org.junit.Test

import edu.mayo.cts2.framework.model.entity.EntityDescriptionMsg

class EntityDescriptionReadTestIT extends BaseServiceTestITBase {
	
	@Ignore
	@Test void TestReadEntityWithColon(){

		EntityDescriptionMsg result = 
			client.getCts2Resource(server + "codesystem/GO/version/2.0/entity/GO:0008158", EntityDescriptionMsg.class);
			
		assertNotNull result
	}
}
