package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestValueSetQueryServiceTestIT {
	
	@Resource
	private BioportalRestValueSetQueryService service

	@Test
	public void testCorrectlyCallBioportal(){
		
		def q = [
			getFilterComponent : { },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as ValueSetQuery
	
		def summaries = 
			service.getResourceSummaries(q,null,new Page())
	
		assertTrue summaries.getEntries().size > 0		
	}
	
	
	

}
