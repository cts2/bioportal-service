package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.entity.EntityDescriptionMsg

class EntityDescriptionReadTestIT extends BaseServiceTestITBase {
	
	@Test void "Test get a code system catalog entry by name"(){
		
		EntityDescriptionMsg result = 
			client.getCts2Resource(server + "codesystem/LNC/version/226/entity/55284-4", EntityDescriptionMsg.class);
			
		assertNotNull result
		
		assertEquals "55284-4", result.getEntityDescription().getChoiceValue().getEntityID().getName()
	}
}
