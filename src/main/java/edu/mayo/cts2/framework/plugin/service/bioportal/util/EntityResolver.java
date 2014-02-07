package edu.mayo.cts2.framework.plugin.service.bioportal.util;

import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class EntityResolver {
	
	@Resource
	private BioportalRestService bioportalRestService;

	public String getEntityXml(ScopedEntityName name, String acronym){
		return this.getXmlForEntityName(
					name,
                acronym);
	}
	
	public String getEntityXml(String uri, String acronym){
		return this.doGetEntityXml(acronym, uri);
	}
	
	private String doGetEntityXml(String acronym, String entityId){
		String xml = null;
		try {
			xml = this.bioportalRestService
					.getEntityByAcronymAndEntityId(
                            acronym,
							entityId);
		} catch (HttpClientErrorException e) {
			if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
				//
			} else {
				throw e;
			}
		}
		
		return xml;
	}
	
	protected String getXmlForEntityName(ScopedEntityName name, String ontologyVersionId){
		List<String> tries = new ArrayList<String>();
		tries.add(name.getName());
		tries.add(StringUtils.replace(name.getName(), "_", ":"));
		tries.add(name.getNamespace() + ":" +name.getName());
		tries.add(name.getNamespace() + "#" +name.getName());
				
		for(String str : tries){
			String xml = this.doGetEntityXml(ontologyVersionId, str);
			if(xml != null){
				return xml;
			}
		}
		return null;
		/* If we need to do a search... hopefully this isn't needed.
		ResolvedFilter filter = new ResolvedFilter();
		filter.setMatchValue(name.getName());
		filter.setMatchAlgorithmReference(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
		
		String searchXml = this.bioportalRestService.searchEntitiesByOntologyId(
					ontologyId, 
					filter, 
					new Page());
			
		String uri = this.entityDescriptionTransform.getUriFromSearch(searchXml);
			
		String xml = this.bioportalRestService
			.getEntityByOntologyVersionIdAndEntityId(
					ontologyId,
					uri);
		
		return xml;
		*/
	}
}
