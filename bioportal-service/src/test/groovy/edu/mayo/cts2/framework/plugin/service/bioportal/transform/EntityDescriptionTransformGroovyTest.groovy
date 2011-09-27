package edu.mayo.cts2.framework.plugin.service.bioportal.transform

import static org.junit.Assert.*

import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*

import org.junit.Test
import org.springframework.core.io.UrlResource
import org.w3c.dom.*

import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.core.xml.DelgatingMarshaller
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;

class EntityDescriptionTransformGroovyTest {
	
	def urlConstructor = [
		createEntityUrl: {cs, csv, sub -> "http://entity/uri" },
		createCodeSystemUrl: {codesystemname -> "http://test/webapp/codesystem/"+codesystemname }
	] as UrlConstructor

	def idConverter = [
		getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
	] as IdentityConverter


	@Test
	void "Validate Entity XML"(){
		
		def xmlFileName =
			"src/test/resources/bioportalXml/entityDescription.xml"
		
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			xmlFileName).text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
			
		def xsd = new UrlResource("http://informatics.mayo.edu/svn/trunk/cts2/spec/psm/rest/schema/Entity.xsd")
	
		def marshaller = new DelgatingMarshaller()
		
		marshaller.marshal(entity, new StreamResult(new StringWriter()))

	}
	
	void doValidateXml(String xmlfileName){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			xmlfileName).text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
			
		def xsd = new UrlResource("http://informatics.mayo.edu/svn/trunk/cts2/spec/psm/rest/schema/Entity.xsd")
	
		def marshaller = new DelgatingMarshaller()
		
		marshaller.marshal(entity, new StreamResult(new StringWriter()))
	}

	@Test
	void testGetPropertyCount(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
	
		assertEquals 3, entity.propertyCount
	}
	
	@Test
	void testPropertyNames(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
	
		assertNotNull entity.property.find{
			it.predicate.name == "TUI"
		}
	}
	
	@Test
	void testPropertyValueCount(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
	
		assertEquals 1, entity.property.find{
			it.predicate.name == "TUI"
		}.valueCount
	}
	
	@Test
	void testPropertyValueText(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entityDescription.xml").text
	
		def entity = transform.
			transformEntityDescription(xml, "testcs", "testcsv");
	
		assertEquals "T061", entity.property.find{
			it.predicate.name == "TUI"
		}.value[0].literal.value.content
	}
	
	@Test
	void testTransformEntityDirectoryCount(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		
		def idConverter = [
			ontologyIdToCodeSystemName : { a -> "test" },
			ontologyVersionIdToCodeSystemVersionName : { a,b -> "test" },
			codeSystemVersionNameToOntologyVersionId : {a,b -> "test" },
			getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
		] as IdentityConverter
		
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entitySearch.xml").text
	
		def entityDirectory = transform.
			transformEntityDirectoryFromSearch(0, 24, xml);
	
		assertEquals 25, entityDirectory.entries.size
	}
	
	@Test
	void testTransformEntityDirectoryWithLimit(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		
		def idConverter = [
			ontologyIdToCodeSystemName : { a -> "test" },
			ontologyVersionIdToCodeSystemVersionName : { a,b -> "test" },
			codeSystemVersionNameToOntologyVersionId : {a,b -> "test" },
			getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
		] as IdentityConverter
		
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entitySearch.xml").text
	
		def entityDirectory = transform.
			transformEntityDirectoryFromSearch(0, 2, xml);
	
		assertEquals 3, entityDirectory.entries.size
	}
	
	@Test
	void testTransformEntityDirectoryWithLimitAtEnd(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		
		def idConverter = [
			ontologyIdToCodeSystemName : { a -> "test" },
			ontologyVersionIdToCodeSystemVersionName : { a,b -> "test" },
			codeSystemVersionNameToOntologyVersionId : {a,b -> "test" },
			getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
		] as IdentityConverter
		
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entitySearch.xml").text
	
		def entityDirectory = transform.
			transformEntityDirectoryFromSearch(22, 24, xml);
	
		assertEquals 3, entityDirectory.entries.size
	}
	
	@Test
	void testTransformEntityDirectoryEntryAbout(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		
		def idConverter = [
			ontologyIdToCodeSystemName : { a -> "test" },
			ontologyVersionIdToCodeSystemVersionName : { a,b -> "test" },
			codeSystemVersionNameToOntologyVersionId : {a,b -> "test" },
			getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
		] as IdentityConverter
		
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entitySearch.xml").text
	
		def entityDirectory = transform.
			transformEntityDirectoryFromSearch(0, 24, xml);
	
		assertNotNull entityDirectory.entries.find {
			 it.about == "http://purl.bioontology.org/ontology/NCIM/C0417893" }
	}
	
	@Test
	void testTransformEntityDirectoryEntryName(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		
		def idConverter = [
			ontologyIdToCodeSystemName : { a -> "test" },
			ontologyVersionIdToCodeSystemVersionName : { a,b -> "test" },
			codeSystemVersionNameToOntologyVersionId : {a,b -> "test" },
			getCodeSystemAbout: {cs, ns -> "http://test.doc.uri" }
		] as IdentityConverter
		
		transform.setIdentityConverter(idConverter)
	
		def xml = new File(
			"src/test/resources/bioportalXml/entitySearch.xml").text
	
		def entityDirectory = transform.
			transformEntityDirectoryFromSearch(0, 24, xml);
	
		assertEquals "C0417893", entityDirectory.entries.find {
			 it.about == "http://purl.bioontology.org/ontology/NCIM/C0417893" }.name.name
	}
	
	@Test
	void testCreateKnownEntityDescriptionDesignation(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		def description = transform.createKnownEntityDescription("csName", "csVersionName", "label")
		
		assertEquals description.designation, "label"
	}
	
	@Test
	void testCreateKnownEntityDescriptionCodeSystemVersionNotNull(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		def description = transform.createKnownEntityDescription("csName", "csVersionName", "label")
		
		assertNotNull description.describingCodeSystemVersion
	}
	
	@Test
	void testCreateCodeSystemVersionReferenceCodeSystemReferenceNotNull(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		CodeSystemVersionReference versionRef = transform.buildCodeSystemVersionReference("csName", "csVersionName")
		
		assertNotNull versionRef.getCodeSystem()
	}
	
	@Test
	void testCreateCodeSystemVersionReferenceCodeSystemReferenceName(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		CodeSystemVersionReference versionRef = transform.buildCodeSystemVersionReference("csName", "csVersionName")
		
		assertEquals "csName", versionRef.codeSystem.content
	}
	
	@Test
	void testCreateCodeSystemVersionReferenceCodeSystemVersionReferenceName(){
		EntityDescriptionTransform transform = new EntityDescriptionTransform()
		transform.setUrlConstructor(urlConstructor)
		transform.setIdentityConverter(idConverter)
		
		CodeSystemVersionReference versionRef = transform.buildCodeSystemVersionReference("csName", "csVersionName")
		
		assertEquals "csVersionName", versionRef.version.content
	}
}
