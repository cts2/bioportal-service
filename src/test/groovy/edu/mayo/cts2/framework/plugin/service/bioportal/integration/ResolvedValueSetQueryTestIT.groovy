package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import static org.junit.Assert.*

import org.junit.Test

import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectory

class ResolvedValueSetQueryTestIT extends BaseServiceTestITBase {
	
	@Test 
	void TestRestrictResolvedValueSetsToValueSet(){
		
		ResolvedValueSetDirectory result = 
			client.getCts2Resource(server + "resolvedvaluesets?valueset={valueSet}", 
				ResolvedValueSetDirectory.class, "SNOMEDCT-MAS");
			
		assertNotNull result
		
		assertTrue result.entry.size() > 0
		
		result.entry.each {
			assertEquals "SNOMEDCT-MAS", it.resolvedHeader.resolutionOf.valueSet.content
		}

	}
	
	@Test
	void TestRestrictResolvedValueSetsAll(){
		
		ResolvedValueSetDirectory result =
			client.getCts2Resource(server + "resolvedvaluesets",
				ResolvedValueSetDirectory.class);
			
		assertNotNull result
		
		assertTrue result.entry.size() > 0

	}
	
}
