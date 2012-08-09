package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valuesetdefinition

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.util.ModelUtils


@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestValueSetDefinitionReadServiceTestIT {
	
	@Resource
	private BioportalRestValueSetDefinitionReadService service
	
	@Resource
	Cts2Marshaller marshaller

	@Test
	public void testReadByTag(){
	
		def vsd = 
			service.readByTag(ModelUtils.
				nameOrUriFromName("HOM-ICD9"),
				new VersionTagReference("CURRENT"),
				null)
	
		assertNotNull vsd	
	}
	
	@Test
	public void "TestReadByTagNCIt-Activity"(){
	
		def vsd =
			service.readByTag(ModelUtils.
				nameOrUriFromName("NCIt-Activity"),
				new VersionTagReference("CURRENT"),
				null)
	
		assertNotNull vsd
	}

}
