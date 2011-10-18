package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionName

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context-non-webapp.xml")
public class BioportalRestEntityDescriptionReadServiceTestIT {
	
	@Resource
	private BioportalRestEntityDescriptionReadService service

	@Test
	public void testGetEntityCallBioportal(){
		def name = new EntityDescriptionName(
			new ScopedEntityName(name:"29506000", namespace:"SNOMEDCT"), 
			"SNOMEDCT_2011_01_31_UMLS-RELA")
		
		def ed = service.read(name)
		
		assertEquals "29506000", ed.getChoiceValue().getEntityID().getName()
		
	}
}
