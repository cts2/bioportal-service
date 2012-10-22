package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestCodeSystemVersionReadServiceTestIT {
	
	@Resource
	private BioportalRestCodeSystemVersionReadService service

	@Test
	public void testCorrectlyCallBioportal(){
	
		def csv = 
			service.getCodeSystemByVersionId(ModelUtils.nameOrUriFromName("NIGO"), "v1", null)
	
		assertNotNull csv	
	}

}
