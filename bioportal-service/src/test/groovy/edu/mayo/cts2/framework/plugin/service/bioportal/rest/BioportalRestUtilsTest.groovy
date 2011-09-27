package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.service.command.Page

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
