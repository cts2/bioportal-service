package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valuesetdefinition

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
class BioportalRestValueSetDefinitionResolutionServiceTestIT {
	
	@Resource
	private BioportalRestValueSetDefinitionResolutionService service
	
	@Resource
	private Cts2Marshaller marshaller
	
	@Test
	public void testResolve(){
		
		def id = new ValueSetDefinitionReadId("ECGT_1-0_OWL",ModelUtils.nameOrUriFromName("ECGT"))

		def result = 
			service.resolveDefinition(id,null,null,null,null,null,new Page())
			
		assertNotNull result
	}
	
	
}
