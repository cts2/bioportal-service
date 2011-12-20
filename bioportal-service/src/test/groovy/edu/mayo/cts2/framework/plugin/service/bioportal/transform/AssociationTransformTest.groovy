package edu.mayo.cts2.framework.plugin.service.bioportal.transform

import static org.junit.Assert.*

import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*

import org.apache.commons.io.IOUtils
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import org.w3c.dom.*

import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter

class AssociationTransformTest {
	
	def urlConstructor = [
		createEntityUrl: {cs, csv, sub -> "http://entity/uri" },
		createCodeSystemUrl: {codesystemname -> "http://test/webapp/codesystem/"+codesystemname }
	] as UrlConstructor

	def idConverter = [
		getCodeSystemAbout: {cs, defNs -> "http://about" },
		codeSystemVersionNameToVersion: {csvn -> csvn }
	] as IdentityConverter

	@Test
	void testSourceOfCount(){
		AssociationTransform transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def associations = transform.
			transformAssociationForRelationships(xml, "testcs", "testcsv");
	
		assertEquals 7, associations.size
	}
	
	@Test
	void testSubjectHref(){
		AssociationTransform transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def associations = transform.
			transformAssociationForRelationships(xml, "testcs", "testcsv");
		
		associations.each {  
			assertNotNull it.subject.href
			assertEquals it.subject.href, "http://entity/uri"
		}
	}
	
	@Test
	void testSubjectAbout(){
		AssociationTransform transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def associations = transform.
			transformAssociationForRelationships(xml, "testcs", "testcsv");
	
		assertEquals associations.find {
			it.subject.name == "O80-O84.9"
		}.subject.uri, "http://purl.bioontology.org/ontology/ICD10/O80-O84.9"
	}
	
	@Test
	void testTargetAbout(){
		AssociationTransform transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def associations = transform.
			transformAssociationForRelationships(xml, "testcs", "testcsv");
	
		assertEquals associations.find {
			it.subject.name == "O80-O84.9" && it.predicate.name == "SubClass"
		}.target.entity.uri, "http://purl.bioontology.org/ontology/ICD10/O84"
	}
	
	@Test
	void testPredicateName(){
		AssociationTransform transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def associations = transform.
			transformAssociationForRelationships(xml, "testcs", "testcsv");
	
		assertTrue associations.findAll {
			it.predicate.name == "SubClass"
		}.size > 0
	}
	
	
	@Test
	void testTransformURIAndEntityNameForRelationships(){
		def transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		def resource = new ClassPathResource("xml/entity.xml")
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(resource.getInputStream(), writer, "UTF-8");
		String xml = writer.toString();
		
		def result = transform.transformURIAndEntityNameForRelationships(xml, "csname", "csvname", "SuperClass")
		
		assertEquals 1, result.length
		assertEquals "O00-O99.9", result[0].getName();
		assertEquals "http://purl.bioontology.org/ontology/ICD10/O00-O99.9", result[0].getUri();
		assertNotNull result[0].getHref()
	}
	
	@Test
	void testTransformEntitiesForRelationship(){
		def transform = new AssociationTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		def resource = new ClassPathResource("xml/entity.xml")
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(resource.getInputStream(), writer, "UTF-8");
		String xml = writer.toString();
		
		def result = transform.transformEntitiesForRelationship(xml, "csname", "csvname", "SubClass")
		
		assertEquals 5, result.size()
		
		result.each {
			assertNotNull it.getHref()	
			assertNotNull it.getAbout()
		}
	}
}
