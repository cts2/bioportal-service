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
import edu.mayo.cts2.framework.model.core.ModelAttributeReference
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType
import edu.mayo.cts2.framework.service.constant.ExternalCts2Constants
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context-non-webapp.xml")
public class BioportalRestEntityDescriptionQueryServiceTestIT {
	
	@Resource
	private BioportalRestEntityDescriptionQueryService service

	@Test
	public void testGetPageCorrectlyCallBioportal(){
			MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		def filter = new ResolvedFilter(
				matchAlgorithmReference:StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference(),
				matchValue:"thing",
				modelAttributeReference: new ModelAttributeReference(content:ExternalCts2Constants.MA_RESOURCE_NAME_NAME),
				referenceType:TargetReferenceType.ATTRIBUTE)
		
		def q = [
			getFilterComponent : { [filter] as Set },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as EntityDescriptionQuery

		def result = 
			service.getAllEntityDescriptions(q, null, new Page(page:2,maxtoreturn:10))
			
		assertEquals 10, result.getEntries().size()
		
	}
}
