package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.service.command.restriction.CodeSystemVersionQueryServiceRestrictions
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context-non-webapp.xml")
public class BioportalRestCodeSystemVersionQueryServiceTestIT {
	
	@Resource
	private BioportalRestCodeSystemVersionQueryService service

	@Test
	public void testCorrectlyCallBioportal(){
		
		def q = [
			getFilterComponent : { },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as CodeSystemVersionQuery
	
		def summaries = 
			service.getResourceSummaries(q,null,new Page())
	
		assertTrue summaries.getEntries().size > 0		
	}
	
	
	

}
