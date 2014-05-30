package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion
import edu.mayo.cts2.framework.model.util.ModelUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.annotation.Resource

import static org.junit.Assert.assertNotNull

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestCodeSystemVersionReadServiceTestIT {
	
	@Resource
	private BioportalRestCodeSystemVersionReadService service

	@Test
	public void testCodingSystemVersionReadForNameAndVersion(){
	
		def csv = 
			service.getCodeSystemByVersionId(ModelUtils.nameOrUriFromName("NIGO"), "v1", null)
	
		assertNotNull csv	
	}
}
