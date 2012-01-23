package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;

class BioportalRestUtilsTest {
	
	@Test
	void testGetEnd(){
		
		def page = new Page( maxtoreturn:50, page:2)
		
		assertEquals BioportalRestUtils.getEnd(page), 150
	}
	
	@Test
	void testGetStart(){
		
		def page = new Page( maxtoreturn:50, page:2)
		
		assertEquals 100, BioportalRestUtils.getStart(page)
	}

}
