package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription.BioportalRestEntityDescriptionQueryService;

class BioportalRestEntityDescriptionQueryServiceTest {
	
	def service = new BioportalRestEntityDescriptionQueryService()
	
	@Test
	void testGetPage(){
		Page page = service.getPageForStartAndMax(0,10)
		
		assertEquals page.maxtoreturn, 10
		assertEquals page.page, 0
	}
	
	@Test
	void testGetPageOverMax(){
		Page page = service.getPageForStartAndMax(200,10)
		
		assertEquals page.maxtoreturn, 10
		assertEquals page.page, 20
	}
	
	@Test
	void testGetPageEquals(){
		Page page = service.getPageForStartAndMax(10,10)
		
		assertEquals page.maxtoreturn, 10
		assertEquals page.page, 1
	}
	
	@Test
	void testGetPageOneUnder(){
		Page page = service.getPageForStartAndMax(9,10)
		
		assertEquals page.maxtoreturn, 10
		assertEquals page.page, 0
	}
	
	@Test
	void testGetPageOptimized(){
		Page page = service.getPageForStartAndMax(6,2)
		
		assertEquals page.maxtoreturn, 2
		assertEquals page.page, 3
	}
	
	@Test
	void testGetPageOneOver(){
		Page page = service.getPageForStartAndMax(11,10)
		
		assertEquals page.maxtoreturn, 11
		assertEquals page.page, 1
	}
	
	@Test
	void testCalculateTransformStartUnder(){
		Page page = new Page(page:0,maxtoreturn:27)
		def start = service.calculateTransformStart(page, 7)
		
		assertEquals 7, start
	}
	
	@Test
	void testCalculateTransformStartOverWithOnePage(){
		Page page = new Page(page:1,maxtoreturn:30)
		def start = service.calculateTransformStart(page, 37)
		
		assertEquals 7, start
	}
	
	@Test
	void testCalculateTransformStartOverWithTwoPages(){
		Page page = new Page(page:10,maxtoreturn:30)
		def start = service.calculateTransformStart(page, 320)
		
		assertEquals 20, start
	}
	
	@Test
	void testPageRoundTripOver(){
		Page page = new Page(page:20,maxtoreturn:10)
		
		def start = page.getStart()
		def max = page.maxtoreturn
		
		def roundTripPage = service.getPageForStartAndMax(start,max)
		
		assertEquals page.page, roundTripPage.page
		assertEquals page.maxtoreturn, roundTripPage.maxtoreturn
	}
	
	@Test
	void testPageRoundTripUnder(){
		Page page = new Page(page:10,maxtoreturn:20)
		
		def start = page.getStart()
		def max = page.maxtoreturn
		
		def roundTripPage = service.getPageForStartAndMax(start,max)
		
		assertEquals page.page, roundTripPage.page
		assertEquals page.maxtoreturn, roundTripPage.maxtoreturn
	}
	
	@Test
	void testGetPageOneOverTransformStart(){
		Page page = service.getPageForStartAndMax(11,10)
		
		assertEquals page.maxtoreturn, 11
		assertEquals page.page, 1
		
		def transformStart = service.calculateTransformStart(page, 11)
		assertEquals 0, transformStart
	}
	
	@Test
	void testGetPageOneOverTransformEnd(){
		Page page = service.getPageForStartAndMax(11,10)
		
		assertEquals page.maxtoreturn, 11
		assertEquals page.page, 1
		
		def transformEnd = service.calculateTransformEnd(page, 11, 10)
		assertEquals 10, transformEnd
	}
}
