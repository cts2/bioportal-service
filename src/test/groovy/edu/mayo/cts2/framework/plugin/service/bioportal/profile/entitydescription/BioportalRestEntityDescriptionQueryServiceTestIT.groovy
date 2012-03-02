package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestEntityDescriptionQueryServiceTestIT {
	
	@Resource
	private BioportalRestEntityDescriptionQueryService service

	@Test
	public void testGetPageCorrectlyCallBioportal(){
		
			MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		def filter = new ResolvedFilter(
				matchAlgorithmReference:StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference(),
				matchValue:"thing",
				propertyReference: StandardModelAttributeReference.RESOURCE_NAME.propertyReference)
		
		def q = [
			getFilterComponent : { [filter] as Set },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as EntityDescriptionQuery

		def result = 
			service.getResourceSummaries(q, null, new Page(page:2,maxtoreturn:10))
			
		assertEquals 10, result.getEntries().size()
		
	}
}
