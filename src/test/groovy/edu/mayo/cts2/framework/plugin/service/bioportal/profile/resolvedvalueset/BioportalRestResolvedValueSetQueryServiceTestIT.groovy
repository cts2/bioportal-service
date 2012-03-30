package edu.mayo.cts2.framework.plugin.service.bioportal.profile.resolvedvalueset

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.core.xml.Cts2Marshaller;
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQuery

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestResolvedValueSetQueryServiceTestIT {
	
	@Resource
	private BioportalRestResolvedValueSetQueryService service
	
	@Resource
	Cts2Marshaller marshaller

	@Test
	public void testCorrectlyCallBioportal(){
		
		def q = [
			getFilterComponent : { },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as ResolvedValueSetQuery
	
		def summaries = 
			service.getResourceSummaries(q,null,new Page())
	
		assertTrue summaries.getEntries().size > 0		
	}
	
	@Test
	void TestGetResourceSummariesHaveHrefs(){
		def q = [
			getFilterComponent : { },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as ResolvedValueSetQuery
	
		def dir = service.getResourceSummaries(q,null,new Page())
		
		dir.entries.each {
			
			assertNotNull it.href
			
			assertTrue it.href, !it.href.contains("null")
			
		}
	}
	
	@Test
	void TestGetResourceSummariesValidXml(){
		def q = [
			getFilterComponent : { },
			getReadContext : { },
			getQuery : { },
			getRestrictions : { }
		] as ResolvedValueSetQuery
	
		def dir = service.getResourceSummaries(q,null,new Page())
		
		dir.entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
	}
	

}
