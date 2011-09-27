package edu.mayo.cts2.framework.plugin.service.bioportal.transform

import static org.junit.Assert.*

import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*

import org.junit.Test
import org.w3c.dom.*

import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;

class AssociationTransformTest {
	
	def urlConstructor = [
		createEntityUrl: {cs, csv, sub -> "http://entity/uri" },
		createCodeSystemUrl: {codesystemname -> "http://test/webapp/codesystem/"+codesystemname }
	] as UrlConstructor

	def idConverter = [
		getCodeSystemAbout: {cs, defNs -> "http://about" }
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
}
