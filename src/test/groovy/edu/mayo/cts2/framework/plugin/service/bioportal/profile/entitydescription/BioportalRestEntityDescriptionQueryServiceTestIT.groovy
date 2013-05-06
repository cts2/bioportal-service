package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions.HierarchyRestriction
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions.HierarchyRestriction.HierarchyType
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestEntityDescriptionQueryServiceTestIT {
	
	@Resource
	private BioportalRestEntityDescriptionQueryService service
	
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

		def result = 
			service.getResourceSummaries(q, null, new Page(page:2,maxtoreturn:10))
			
		assertEquals 10, result.getEntries().size()
		
	}
	
	@Test
	public void testSearchForEntitiesNamespaceValidate(){
		
		MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		def filter = new ResolvedFilter(
				matchAlgorithmReference:StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference(),
				matchValue:"Vertebro",
				propertyReference: StandardModelAttributeReference.RESOURCE_NAME.propertyReference)
		
		def q = [
			getFilterComponent : { [filter] as Set },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as EntityDescriptionQuery

		def result =
			service.getResourceSummaries(q, null, new Page())
			
		result.entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
		
	}
	
	@Test
	public void testSearchForEntitiesResourceSynopsis(){
		
		MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		def filter = new ResolvedFilter(
				matchAlgorithmReference:StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference(),
				matchValue:"Vertebro",
				propertyReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.propertyReference)
		
		def q = [
			getFilterComponent : { [filter] as Set },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as EntityDescriptionQuery

		def result =
			service.getResourceSummaries(q, null, new Page())
			
		result.entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
		
	}
	
	@Test
	public void testGetChildren(){
		
		def sen = new ScopedEntityName(name:"G40-G47.9", namespace:"ICD10")
		def e = new EntityNameOrURI(entityName: sen)
		
		def restrictions = new EntityDescriptionQueryServiceRestrictions()
		restrictions.hierarchyRestriction = 
			new HierarchyRestriction(
				hierarchyType: HierarchyType.CHILDREN,
				entity: e)
		restrictions.codeSystemVersion = ModelUtils.nameOrUriFromName("ICD10_1998_RRF")
				
		def q = [
			getFilterComponent : {  },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { restrictions }
		] as EntityDescriptionQuery

		def ed = service.getResourceSummaries(q, null, new Page())
		
		assertTrue ed.entries.size > 0

	}
	
	@Test
	public void testEntityPagination(){
		
		def restrictions = new EntityDescriptionQueryServiceRestrictions()
		restrictions.codeSystemVersion = ModelUtils.nameOrUriFromName("ICD10_1998_RRF")
				
		def q = [
			getFilterComponent : {  },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { restrictions }
		] as EntityDescriptionQuery

		def ed = service.getResourceSummaries(q, null, new Page(page:10))
		
		assertTrue ed.entries.size > 0

	}
	
	@Test
	public void testEntityPaginationSnomedCT(){
		
		def restrictions = new EntityDescriptionQueryServiceRestrictions()
		restrictions.codeSystemVersion = ModelUtils.nameOrUriFromName("SNOMEDCT_2011_07_31_UMLS-RELA")

		def q = [
			getFilterComponent : {  },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { restrictions }
		] as EntityDescriptionQuery

		def ed = service.getResourceSummaries(q, null, new Page(page:6000))
		
		assertTrue ed.entries.size > 0
		println(ed.entries.size)
	}

}
