package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.service.command.restriction.CodeSystemVersionQueryServiceRestrictions

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context-non-webapp.xml")
public class BioportalRestCodeSystemVersionQueryServiceTestIT {
	
	@Resource
	private BioportalRestCodeSystemVersionQueryService service

	@Test
	public void testCorrectlyCallBioportal(){
		def summaries = 
			service.getResourceSummaries(null,null,new CodeSystemVersionQueryServiceRestrictions(),new Page())
	
		assertTrue summaries.getEntries().size > 0		
	}
	
	
	

}
