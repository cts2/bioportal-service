package edu.mayo.cts2.framework.plugin.service.bioportal.integration;

import org.junit.Before
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException

import edu.mayo.cts2.framework.core.client.Cts2RestClient


class BaseServiceTestITBase {
	
	public Cts2RestClient client = new Cts2RestClient()
	
	public String server = "http://localhost:5150/webapp-rest/"
	
	//Wait until OSGi service installs itself and becomes available
	@Before
	void waitForService(){
			
		boolean wait = true;
		
		long startTime = System.currentTimeMillis()
		while(wait){
			if( (System.currentTimeMillis() - startTime) > (50*1000)){
				throw new RuntimeException("Timeout waiting for service to start")
			}
			try {
				client.getCts2Resource(server + "/codesystems", Object)
				} catch (HttpStatusCodeException e){
					if(e.getStatusCode().equals(HttpStatus.NOT_IMPLEMENTED)){
						Thread.currentThread().sleep(5000)
						continue
					} else {
						throw e
					}
				}
				
			wait = false;
		}
	}
}
