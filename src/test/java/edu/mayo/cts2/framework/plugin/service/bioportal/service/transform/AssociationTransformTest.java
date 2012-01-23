package edu.mayo.cts2.framework.plugin.service.bioportal.service.transform;

import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;

public class AssociationTransformTest {
	private AssociationTransform associationTransform;
	
	private String xml;
	
	@Before
	public void buildTransform() throws Exception{
		
		this.associationTransform= new AssociationTransform();		
		IdentityConverter idConverter = EasyMock.createMock(IdentityConverter.class);
		
		UrlConstructor urlConstructor = EasyMock.createNiceMock(UrlConstructor.class);
		
		EasyMock.expect(idConverter.ontologyIdToCodeSystemName("1104")).andReturn("testCsName").anyTimes();
		
		EasyMock.expect(idConverter.ontologyVersionIdToCodeSystemVersionName("1104", "44450")).andReturn("testCsVersionName").anyTimes();
		EasyMock.expect(idConverter.getCodeSystemAbout("csName", "http://purl.bioontology.org/ontology/")).andReturn("http://test.doc.uri").anyTimes();
		EasyMock.expect(idConverter.getCodeSystemAbout("ICD10", "http://purl.bioontology.org/ontology/")).andReturn("http://test.doc.uri").anyTimes();
		EasyMock.expect(idConverter.codeSystemVersionNameToVersion("ICD10")).andReturn("ICD10").anyTimes();
		EasyMock.replay(idConverter, urlConstructor);
		

		this.associationTransform.setIdentityConverter(idConverter);
		this.associationTransform.setUrlConstructor(urlConstructor);
		
		Resource resource = new ClassPathResource("bioportalXml/entityDescription.xml");
		
		StringWriter sw = new StringWriter();
		
		IOUtils.copy(resource.getInputStream(), sw);
		
		this.xml = sw.toString();
	}
	
	@Test
	public void transformSubjectOfAssociationsForEntity() throws Exception {
		DirectoryResult<AssociationDirectoryEntry> dr = this.associationTransform.transformSubjectOfAssociationsForEntity(
				xml, 
				"ICD10", 
				"ICD10");
		assertNotNull( dr.getEntries());
	}
}
