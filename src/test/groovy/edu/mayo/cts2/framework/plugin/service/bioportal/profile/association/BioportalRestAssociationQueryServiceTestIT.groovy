package edu.mayo.cts2.framework.plugin.service.bioportal.profile.association

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test;
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller;
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions
import edu.mayo.cts2.framework.service.profile.association.AssociationQuery


@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestAssociationQueryServiceTestIT {
	
	@Resource
	private BioportalRestAssociationQueryService service
	
	@Resource
	private Cts2Marshaller marshaller
	
	@Test
	void testGetResourceSummariesWithCodeSystemVersionAndSourceNameRestriction(){
		def dir = service.getResourceSummaries(
			[
				getRestrictions:{ new  AssociationQueryServiceRestrictions(
					codeSystemVersion: ModelUtils.nameOrUriFromName("SNOMEDCT_2011_01_31_UMLS-RELA"),
					sourceEntity: ModelUtils.entityNameOrUriFromName(new ScopedEntityName(name:"29506000", namespace:"SNOMEDCT") ))},
				getFilterComponent:{[] as Set}
			] as AssociationQuery,null,new Page())
		
		assertNotNull dir
		assertTrue dir.getEntries().size() > 0
	}
	
	@Test
	void testGetResourceSummariesWithCodeSystemVersionAndSourceNameRestrictionValid(){
		def dir = service.getResourceSummaries(
			[
				getRestrictions:{ new  AssociationQueryServiceRestrictions(
					codeSystemVersion: ModelUtils.nameOrUriFromName("SNOMEDCT_2011_01_31_UMLS-RELA"),
					sourceEntity: ModelUtils.entityNameOrUriFromName(new ScopedEntityName(name:"29506000", namespace:"SNOMEDCT") ))},
				getFilterComponent:{[] as Set}
			] as AssociationQuery,null,new Page())
		
		assertNotNull dir
		assertTrue dir.getEntries().size() > 0
		
		dir.entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
	}
	
}
