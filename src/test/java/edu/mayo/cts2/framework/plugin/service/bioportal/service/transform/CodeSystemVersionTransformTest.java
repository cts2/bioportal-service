package edu.mayo.cts2.framework.plugin.service.bioportal.service.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.mayo.cts2.framework.core.client.Cts2RestClient;
import edu.mayo.cts2.framework.core.url.UrlConstructor;
import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.EntryDescription;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.SourceAndNotation;
import edu.mayo.cts2.framework.model.core.ValueSetReference;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.core.types.SetOperator;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valuesetdefinition.CompleteCodeSystemReference;
import edu.mayo.cts2.framework.model.valuesetdefinition.IteratableResolvedValueSet;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectory;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
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
		EasyMock.expect(idConverter.codeSystemNameToOntologyId("testCsName")).andReturn("1104").anyTimes();
		EasyMock.expect(idConverter.codeSystemVersionNameToOntologyVersionId("testCsName")).andReturn("44450").anyTimes();
		EasyMock.expect(idConverter.codeSystemVersionNameToVersion("testCsName")).andReturn("44450").anyTimes();
		
	
		UrlConstructor urlConstructor = EasyMock.createNiceMock(UrlConstructor.class);
		
		EasyMock.expect(idConverter.ontologyVersionIdToCodeSystemVersionName("1104", "44450")).andReturn("testCsName").anyTimes();
		
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

	public static void main(String... args) throws Exception{
		ResolvedValueSetDirectory dir = 
				Cts2RestClient.instance().getCts2Resource(
						"http://informatics.mayo.edu/cts2/services/sharp/resolvedvaluesets?maxtoreturn={max}", ResolvedValueSetDirectory.class, "100");
		
		
		for(ResolvedValueSetDirectoryEntry entry : dir.getEntry()){
			System.out.println(entry.getHref());
			IteratableResolvedValueSet vs = Cts2RestClient.instance().getCts2Resource(entry.getHref(), IteratableResolvedValueSet.class);
			ValueSetReference vsref = vs.getResolutionInfo().getResolutionOf().getValueSet();
			NameAndMeaningReference vsdref = vs.getResolutionInfo().getResolutionOf().getValueSetDefinition();
			
			ValueSetCatalogEntry vce = new ValueSetCatalogEntry();
			vce.setAbout(vsref.getUri());
			vce.setValueSetName(vsref.getContent());
			vce.setFormalName(vsref.getContent());
			EntryDescription ed = new EntryDescription();
			ed.setValue(ModelUtils.toTsAnyType(vsref.getContent()));
			vce.setResourceSynopsis(ed);
			
			/*
			ValueSetDefinition def = new ValueSetDefinition();
			def.setAbout(vsref.getUri());
			def.setDocumentURI(vsdref.getUri());
			def.addVersionTag(new VersionTagReference("CURRENT"));
			def.setDefinedValueSet(vsref);
			def.setSourceAndNotation(new SourceAndNotation());
			ValueSetDefinitionEntry e = new ValueSetDefinitionEntry();
			CompleteCodeSystemReference ccsr = new CompleteCodeSystemReference();
			ccsr.setCodeSystem(new CodeSystemReference("CKS"));
			e.setCompleteCodeSystem(ccsr);
			e.setEntryOrder(1L);
			e.setOperator(SetOperator.UNION);
			def.addEntry(e);
			*/
			
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			DelegatingMarshaller m = new DelegatingMarshaller();
			m.marshal(vce, sr);
			
			File f = new File("out/valuesets/" + vsref.getContent() + ".xml");
			f.createNewFile();
			FileUtils.writeStringToFile(f, sw.toString());
		}
	}
}
