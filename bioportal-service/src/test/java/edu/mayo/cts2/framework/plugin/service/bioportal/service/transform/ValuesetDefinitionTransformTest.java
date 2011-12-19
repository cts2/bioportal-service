package edu.mayo.cts2.framework.plugin.service.bioportal.service.transform;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;

public class ValuesetDefinitionTransformTest {
	
	private ValueSetDefinitionTransform transform;
	
	private String xml;
	
	@Before
	public void buildTransform() throws Exception{
		Resource resource = new ClassPathResource("bioportalXml/valueSetDefinition.xml");
		
		StringWriter sw = new StringWriter();
		
		IOUtils.copy(resource.getInputStream(), sw);
		
		this.xml = sw.toString();
		
		this.transform = new ValueSetDefinitionTransform();
		
		IdentityConverter idConverter = EasyMock.createMock(IdentityConverter.class);
		
		BioportalRestService restService = EasyMock.createMock(BioportalRestService.class);
		
		EasyMock.expect(restService.getOntologyByOntologyVersionId("42157")).andReturn(this.xml).anyTimes();
		
		EasyMock.expect(idConverter.ontologyIdToValueSetName("1416")).andReturn("testVsName").anyTimes();
		EasyMock.expect(idConverter.ontologyVersionIdToValueSetDefinitionName("1416","42157")).andReturn("testVsName").anyTimes();

		EasyMock.expect(idConverter.getValueSetAbout("testVsName", "http://purl.bioontology.org/view/")).andReturn("http://test.about").anyTimes();
		EasyMock.expect(idConverter.getDocumentUri("42157")).andReturn("http://test.doc.uri").anyTimes();
		EasyMock.expect(idConverter.codeSystemNameToOntologyId("testVsName")).andReturn("1416").anyTimes();
		EasyMock.expect(idConverter.codeSystemVersionNameToOntologyVersionId("testVsName")).andReturn("42157").anyTimes();
		EasyMock.expect(idConverter.codeSystemVersionNameToVersion("testVsName")).andReturn("42157").anyTimes();
		
	
		UrlConstructor urlConstructor = EasyMock.createNiceMock(UrlConstructor.class);
		
		EasyMock.expect(idConverter.ontologyVersionIdToCodeSystemVersionName("1104", "42157")).andReturn("testVsName").anyTimes();
		
		EasyMock.replay(idConverter, urlConstructor, restService);
		
		this.transform.setIdentityConverter(idConverter);
		this.transform.setUrlConstructor(urlConstructor);
		this.transform.setBioportalRestService(restService);
	}
	
	@Test
	public void transformCodeSystemVersionUrnNotNull() throws Exception {
		List<ValueSetDefinitionDirectoryEntry> vsde = this.transform.transformVersionsOfResource(xml);
		assertEquals("testVsName", vsde.get(0).getResourceName());
		
	}
	


}
