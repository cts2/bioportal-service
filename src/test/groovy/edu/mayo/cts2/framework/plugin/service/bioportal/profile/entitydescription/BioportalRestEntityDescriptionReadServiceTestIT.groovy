package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller;
import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestEntiyDescriptionReadServiceTestIT {
	
	@Resource
	private BioportalRestEntityDescriptionReadService service
	
	@Resource
	Cts2Marshaller marshaller

	@Test
	public void testGetEntityCallBioportal(){
		def name = new EntityDescriptionReadId(
			new ScopedEntityName(name:"29506000", namespace:"SNOMEDCT"), 
			ModelUtils.nameOrUriFromName("SNOMEDCT_2011_01_31_UMLS-RELA"))
		
		def ed = service.read(name, null)
		
		assertEquals "29506000", ed.getChoiceValue().getEntityID().getName()
		
	}
	
	@Test
	public void testGetEntityValidXml(){
		def name = new EntityDescriptionReadId(
			new ScopedEntityName(name:"29506000", namespace:"SNOMEDCT"),
			ModelUtils.nameOrUriFromName("SNOMEDCT_2011_01_31_UMLS-RELA"))
		
		def ed = service.read(name, null)
		
		marshaller.marshal(ed, new StreamResult(new StringWriter()))		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByUri(){
		def name = new EntityDescriptionReadId(
			"http://purl.bioontology.org/ontology/ICD10/D45",
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.read(name, null)
		
		assertEquals "D45", ed.getChoiceValue().getEntityID().getName()
		
	}

	@Test
	public void testGetEntityCallBioportalReadByUriGO(){
		def name = new EntityDescriptionReadId(
			"http://purl.obolibrary.org/obo/GO_0000003",
			ModelUtils.nameOrUriFromName("NIGO_44724_OBO"))
		
		def ed = service.read(name, null)
		
		assertEquals "0000003", ed.getChoiceValue().getEntityID().getName()
		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByUriNotFound(){
		def name = new EntityDescriptionReadId(
			"http://__INVALIDpurl.bioontology.org/ontology/__INVALID___",
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.read(name, null)
		
		assertNull ed
		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByUriWithDot(){
		def name = new EntityDescriptionReadId(
			"http://purl.bioontology.org/ontology/ICD10/G45.0",
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.read(name, null)
		
		assertEquals "G45.0", ed.getChoiceValue().getEntityID().getName()
		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByUriWithNoChildren(){
		def name = new EntityDescriptionReadId(
			"http://purl.bioontology.org/ontology/ICD10/G45.0",
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.read(name, null)
		
		assertEquals "G45.0", ed.getChoiceValue().getEntityID().getName()
		
		assertEquals "", ed.getChoiceValue().children
		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByUriWithChildren(){
		def name = new EntityDescriptionReadId(
			"http://purl.bioontology.org/ontology/ICD10/G40-G47.9",
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.read(name, null)
		
		assertEquals "G40-G47.9", ed.getChoiceValue().getEntityID().getName()
		
		assertNotNull ed.getChoiceValue().children
		
	}
	
	@Test
	public void testGetEntityCallBioportalReadByName(){
		def name = new EntityDescriptionReadId(
			new ScopedEntityName(name:"Disposition", namespace:"snap"),
			ModelUtils.nameOrUriFromName("BFO_1-1_OWL"))
		
		def ed = service.read(name, null)
		
		assertEquals "Disposition", ed.getChoiceValue().getEntityID().getName()
		assertEquals "snap", ed.getChoiceValue().getEntityID().getNamespace()
	}
	
	@Test
	public void testGetEntityCallBioportalReadByNameWithBadCodeSystemVesionName(){
		def name = new EntityDescriptionReadId(
			new ScopedEntityName(name:"Disposition", namespace:"snap"),
			ModelUtils.nameOrUriFromName("THIS_IS_INVALID"))
		
		def ed = service.read(name, null)
		
		assertNull ed
	}
	
}
