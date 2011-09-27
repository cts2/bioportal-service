package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.core.FilterComponent
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription.BioportalRestEntityDescriptionQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService
import edu.mayo.cts2.framework.service.command.Page

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/bioportal-test-context.xml")
class BioportalRestEntityDescriptionQueryServiceTestIT {
	
	@Resource
	BioportalRestEntityDescriptionQueryService service

	@Test
	@Ignore
	void testGetPageCorrectlyCallBioportal(){
		def bioportalRestService = [
			searchEntitiesOfLatestOntologyVersions: {filter, page ->
			
			assertEquals 2, page.page
			assertEquals 10, page.maxtoreturn
			
			throw new Exception("break")
			
			}]  as BioportalRestService
		MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		FilterComponent filter = new FilterComponent(matchAlgorithm: matchAlgorithm, matchValue: "test")
		
		service.setBioportalRestService(bioportalRestService)
		
		try{
			service.getAllEntityDescriptions(filter, new Page(page:2,maxtoreturn:10))
		} catch(e){
			assertEquals "break", e.getMessage()
			return
		}
		fail
	}
	
	@Test
	@Ignore
	void testGetPageCorrectlyCallBioportalOver(){
		def bioportalRestService = [
			searchEntitiesOfLatestOntologyVersions: {filter, page ->
			
			assertEquals 10, page.page
			assertEquals 2, page.maxtoreturn
			
			throw new Exception("break")
			
			}]  as BioportalRestService
		MatchAlgorithmReference matchAlgorithm = new MatchAlgorithmReference(content:"contains")
		
		FilterComponent filter = new FilterComponent(matchAlgorithm: matchAlgorithm, matchValue: "test")
		
		service.setBioportalRestService(bioportalRestService)
		
		try{
			service.getAllEntityDescriptions(filter, new Page(page:10,maxtoreturn:2))
		} catch(e){
			assertEquals "break", e.getMessage()
			return
		}
		fail
	}
	

}
