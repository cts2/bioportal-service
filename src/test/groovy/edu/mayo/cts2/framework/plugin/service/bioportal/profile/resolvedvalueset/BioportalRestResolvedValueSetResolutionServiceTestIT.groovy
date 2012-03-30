package edu.mayo.cts2.framework.plugin.service.bioportal.profile.resolvedvalueset

import static org.junit.Assert.*

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult

import org.junit.Test
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller;
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.name.ResolvedValueSetReadId

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
class BioportalRestResolvedValueSetResolutionServiceTestIT {
	
	@Resource
	private BioportalRestResolvedValueSetResolutionService service
	
	@Resource
	private Cts2Marshaller marshaller
	
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
	
		def id = new ResolvedValueSetReadId("43070",ModelUtils.nameOrUriFromName("BRO"),null)

		def result = 
			service.getResolution(id,null,new Page(maxtoreturn:10))
			
		assertEquals 10, result.getEntries().size()
		
	}
	
	@Test
	public void testIsValidXml(){
		
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
	
		def id = new ResolvedValueSetReadId("43070",ModelUtils.nameOrUriFromName("BRO"),null)

		def result =
			service.getResolution(id,null,new Page())
			
		assertTrue 0 < result.getEntries().size()
		
		result.entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
		
		result.resolvedValueSetHeader.marshal(new StringWriter())
		
	}
}
