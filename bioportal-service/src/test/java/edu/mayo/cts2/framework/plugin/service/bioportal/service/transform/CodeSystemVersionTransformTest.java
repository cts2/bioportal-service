package edu.mayo.cts2.framework.plugin.service.bioportal.service.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.CodeSystemVersionTransform;

public class CodeSystemVersionTransformTest {
	
	private CodeSystemVersionTransform transform;
	
	private String xml;
	
	@Before
	public void buildTransform() throws Exception{
		Resource resource = new ClassPathResource("bioportalXml/codeSystemVersion.xml");
		
		StringWriter sw = new StringWriter();
		
		IOUtils.copy(resource.getInputStream(), sw);
		
		this.xml = sw.toString();
		
		this.transform = new CodeSystemVersionTransform();
		
		IdentityConverter idConverter = EasyMock.createMock(IdentityConverter.class);
		
		BioportalRestService restService = EasyMock.createMock(BioportalRestService.class);
		
		EasyMock.expect(restService.getOntologyByOntologyVersionId("44450")).andReturn(this.xml).times(2);
		
		EasyMock.expect(idConverter.ontologyIdToCodeSystemName("1104")).andReturn("testCsName").once();
		EasyMock.expect(idConverter.getCodeSystemAbout("testCsName", "http://purl.bioontology.org/ontology/")).andReturn("http://test.about").anyTimes();
		EasyMock.expect(idConverter.getDocumentUri("44450")).andReturn("http://test.doc.uri").anyTimes();
		
		UrlConstructor urlConstructor = EasyMock.createNiceMock(UrlConstructor.class);
		
		EasyMock.expect(idConverter.ontologyVersionIdToCodeSystemVersionName("1104", "44450")).andReturn("testCsVersionName").once();
		
		EasyMock.replay(idConverter, urlConstructor, restService);
		
		this.transform.setIdentityConverter(idConverter);
		this.transform.setUrlConstructor(urlConstructor);
		this.transform.setBioportalRestService(restService);
	}
	
	@Test
	public void transformCodeSystemVersionUrnNotNull() throws Exception {
		CodeSystemVersionCatalogEntry csv = this.transform.transformResourceVersion(this.xml);
		
		assertNotNull(csv.getAbout());
	}
	
	@Test
	public void transformCodeSystemVersionOfficialResourceVersionId() throws Exception {
		CodeSystemVersionCatalogEntry csv = this.transform.transformResourceVersion(this.xml);
		
		assertEquals("3.2.1", csv.getOfficialResourceVersionId());
	}
	
	@Test
	public void transformCodeSystemVersionResourceSynopsis() throws Exception {
		CodeSystemVersionCatalogEntry csv = this.transform.transformResourceVersion(this.xml);
		
		assertEquals("A controlled terminology of resources, which is used to improve the" +
				"sensitivity and specificity of web searches.", 
				
				csv.getResourceSynopsis().getValue().getContent());
	}
	
	@Test
	public void transformCodeSystemVersionFormalName() throws Exception {
		CodeSystemVersionCatalogEntry csv = this.transform.transformResourceVersion(this.xml);
		
		assertEquals("Biomedical Resource Ontology", csv.getFormalName());
	}
	
	@Test
	public void transformCodeSystemVersionSourceDocumentSyntax() throws Exception {
		CodeSystemVersionCatalogEntry csv = this.transform.transformResourceVersion(this.xml);
		
		assertEquals("OWL-FULL", csv.getSourceAndNotation().getSourceDocumentSyntax().getContent());
	}

}
