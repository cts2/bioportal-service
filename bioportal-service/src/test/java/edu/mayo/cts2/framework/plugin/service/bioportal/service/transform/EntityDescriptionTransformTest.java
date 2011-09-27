package edu.mayo.cts2.framework.plugin.service.bioportal.service.transform;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;

public class EntityDescriptionTransformTest {
	
	private EntityDescriptionTransform transform;
	
	private String xml;
	
	@Before
	public void buildTransform() throws Exception{
		this.transform = new EntityDescriptionTransform();
		
		IdentityConverter idConverter = EasyMock.createMock(IdentityConverter.class);
		
		UrlConstructor urlConstructor = EasyMock.createNiceMock(UrlConstructor.class);
		
		EasyMock.expect(idConverter.ontologyIdToCodeSystemName("1104")).andReturn("testCsName").anyTimes();
		
		EasyMock.expect(idConverter.ontologyVersionIdToCodeSystemVersionName("1104", "44450")).andReturn("testCsVersionName").anyTimes();
		EasyMock.expect(idConverter.getCodeSystemAbout("csName", "http://purl.bioontology.org/ontology/")).andReturn("http://test.doc.uri").anyTimes();
		EasyMock.expect(idConverter.getCodeSystemAbout("ICD10", "http://purl.bioontology.org/ontology/")).andReturn("http://test.doc.uri").anyTimes();
		
		
		EasyMock.replay(idConverter, urlConstructor);
		
		this.transform.setIdentityConverter(idConverter);
		this.transform.setUrlConstructor(urlConstructor);
		
		Resource resource = new ClassPathResource("bioportalXml/entityDescription.xml");
		
		StringWriter sw = new StringWriter();
		
		IOUtils.copy(resource.getInputStream(), sw);
		
		this.xml = sw.toString();
	}
	
	@Test
	public void transformEntityDescriptionAbout() throws Exception {
		NamedEntityDescription ed = this.transform.transformEntityDescription(xml, "csName", "csVersionName");
		
		assertEquals("http://purl.bioontology.org/ontology/ICD10/O80-O84.9", ed.getAbout());
	}
	
	@Test
	public void transformEntityDescriptionEntityID() throws Exception {
		NamedEntityDescription ed = this.transform.transformEntityDescription(xml, "ICD10", "ICD10");
		
		assertEquals("O80-O84.9", ed.getEntityID().getName());
		
		assertEquals("ICD10", ed.getEntityID().getNamespace());
	}
	
	@Test
	public void transformEntityDescriptionDesignationContent() throws Exception {
		NamedEntityDescription ed = this.transform.transformEntityDescription(xml, "ICD10", "ICD10");
		
		assertEquals("Delivery", ed.getDesignation(0).getValue().getContent());
	}
	
}
