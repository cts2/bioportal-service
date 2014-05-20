/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import edu.mayo.cts2.framework.core.plugin.PluginConfigManager;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.exception.ExceptionFactory;
import edu.mayo.cts2.framework.service.constant.ExternalCts2Constants;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;

/**
 * The Class BioportalRestService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class BioportalRestService extends BaseCacheObservable 
	implements InitializingBean, DisposableBean {
	
	public static final String BIOPORTAL_CACHE_NAME = "bioportal-cache";
	
	public static final String BIOPORTAL_CONFIG_NAMESPACE = "bioportal-service";
	
	public static final String CACHE_CONFIG_PROP = "cache";

	private static final String API_KEY_PROP = "apiKey";
	
	private static Log log = LogFactory.getLog(BioportalRestService.class);

	@Resource
	private PluginConfigManager pluginConfigManager;
	
	private Map<String,String> cache;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	private DB db;
	
	private String apiKey;
	
	private static final String API_KEY_PARAM = "apikey";
	
	/* every hour default */
	private static final int DEFAULT_CACHE_UPDATE_PERIOD = 60;
	
	/* in minutes */
	private int cacheUpdatePeriod = DEFAULT_CACHE_UPDATE_PERIOD;
	
	private static final long ONE_MINUTE = 60000L;
	
	private static int DEFAULT_MEM_CACHE_SIZE = 25;
	
	@SuppressWarnings("unchecked")
	private Map<String,String> memCache = new LRUMap(DEFAULT_MEM_CACHE_SIZE);

	private String cachePath;
	
	private boolean propertiesSet = false;
	
	public static final String PROPERTIES_NAME = "properties";
	public static final String PROPERTIES_URI = ExternalCts2Constants.buildModelAttributeUri(PROPERTIES_NAME);
	public static final ComponentReference PROPERTIES = new ComponentReference();
	static {
		PROPERTIES.setPropertyReference(new URIAndEntityName());
		PROPERTIES.getPropertyReference().setName(PROPERTIES_NAME);
		PROPERTIES.getPropertyReference().setUri(PROPERTIES_URI);
	};
	
	public static final String DEFINITIONS_NAME = "definitions";
	public static final String DEFINITIONS_URI = ExternalCts2Constants.buildModelAttributeUri(DEFINITIONS_NAME);
	public static final ComponentReference DEFINITIONS = new ComponentReference();
	static {
		DEFINITIONS.setPropertyReference(new URIAndEntityName());
		DEFINITIONS.getPropertyReference().setName(DEFINITIONS_NAME);
		DEFINITIONS.getPropertyReference().setUri(DEFINITIONS_URI);
	};
	
	@Override
	public void destroy() throws Exception {
		log.info("Shutting down... writing cache to file.");
		this.writeCache();
		this.db.close();
	}

	protected Map<String, String> createCache(File file) {
		this.db = DBMaker.newFileDB(file).
				closeOnJvmShutdown().
				make();
		
		return db.getHashMap(BIOPORTAL_CACHE_NAME);
	}

    public String callUrl(String url){
        return this.doCallBioportal(url);
    }

	public String getLatestOntologySubmissions(boolean includeViews){
		String url = buildGetLatestOntologySubmissionsUrl(includeViews);

		String xml = this.doCallBioportal(url);

		return xml;
	}
	
	/**
	 * Builds the get latest ontology versions url.
	 *
	 * @return the string
	 */
	private String buildGetLatestOntologySubmissionsUrl(boolean includeViews){
		String url = "http://data.bioontology.org/submissions?include_views=" + (includeViews ? "true" : "false");
		
		return url;
	}
	
	/**
	 * Gets the entity by ontology version id and entity id.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @param entityId the entity id
	 * @return the entity by ontology version id and entity id
	 */
	public String getEntityByAcronymAndEntityId(String acronym, String entityId){
		String url = "http://data.bioontology.org/ontologies/" + acronym + "/classes/" + entityId;

		String xml = this.doCallBioportalMemCache(url);

		return xml;
	}

	/**
	 * Gets the entity by uri.
	 *
	 * @param uri the uri
	 * @return the entity by uri
	 */
	public String getEntityByUri(String uri) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Gets the latest ontology version by ontology id.
	 *
	 * @param acronym the ontology id
	 * @return the latest ontology version by ontology id
	 */
	public String getLatestOntologySubmissionByAcronym(String acronym){
		String url = this.buildGetLatestOntologySubmissionByAcronymUrl(acronym);

		String xml = this.doCallBioportal(url);

		return xml;
	}
	
	/**
	 * Builds the get latest ontology version by ontology id url.
	 *
	 * @param acronym the ontology id
	 * @return the string
	 */
	private String buildGetLatestOntologySubmissionByAcronymUrl(String acronym){
		String url = "http://data.bioontology.org/ontologies/" + acronym + "/latest_submission";
		
		return url;
	}

	/**
	 * Gets the ontology by ontology version id.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @return the ontology by ontology version id
	 */
	public String getOntologySubmissionByAcronymAndSubmissionId(String acronym, String submissionId){
		String url = "http://data.bioontology.org/ontologies/" + acronym + "/submissions/" + submissionId;

		String xml = this.doCallBioportal(url);

		return xml;
	}

    public String getOntologyByAcronym(String acronym){
        String url = "http://data.bioontology.org/ontologies/" + acronym;

        String xml = this.doCallBioportal(url);

        return xml;
    }

	public String getOntologySubmissionsByAcronym(String acronym){
		String url = buildGetOntologySubmissionsByAcronymUrl(acronym);

		String xml = this.doCallBioportal(url);

		return xml;
	}

	/**
	 * Gets the view versions by ontology id.
	 *
	 * @param acronym the ontology id
	 * @return the view versions by ontology id
	 */
	public String getViewVersionsByAcronym(String acronym){
		String url = buildGetViewVersionsByAcronymUrl(acronym);

		String xml = this.doCallBioportal(url);

		return xml;
	}
	
	/**
	 * Builds the get ontology versions by ontology id url.
	 *
	 * @param acronym the ontology id
	 * @return the string
	 */
	private String buildGetOntologySubmissionsByAcronymUrl(String acronym){
		String url = "http://data.bioontology.org/ontologies/" + acronym + "/submissions";
		
		return url;
	}
	
	/**
	 * Builds the get view versions by ontology id url.
	 *
	 * @param acronym the ontology id
	 * @return the string
	 */
	private String buildGetViewVersionsByAcronymUrl(String acronym){
		//TODO: How do we filter out views?
		String url = "http://data.bioontology.org/ontologies/" + acronym;
		
		return url;
	}

	/**
	 * Gets the all entities by ontology version id.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @param page the page
	 * @return the all entities by ontology version id
	 */
	public String getAllEntitiesByAcronym(String acronym, Page page){
		String 
			url = "http://data.bioontology.org/ontologies/" + acronym + "/classes" +
			"?page=" + Integer.toString(page.getPage() + 1) +
			"&pagesize=" + Integer.toString(page.getMaxToReturn());

		String xml = this.doCallBioportal(url);

		return xml;
	}
	
	/**
	 * Search entities by ontology id.
	 *
	 * @param acronym the ontology id
	 * @param filter the filter
	 * @param page the page
	 * @return the string
	 */
	public String searchEntitiesByAcronym(String acronym, ResolvedFilter filter, Page page){
		return this.doSearchEntities(Arrays.asList(acronym), filter, page);
	}
	
	/**
	 * Search entities by ontology ids.
	 *
	 * @param acronyms the ontology ids
	 * @param filter the filter
	 * @param page the page
	 * @return the string
	 */
	public String searchEntitiesByAcronyms(Collection<String> acronyms, ResolvedFilter filter, Page page){
		return this.doSearchEntities(acronyms, filter, page);
	}
	
	/**
	 * Search entities of latest ontology versions.
	 *
	 * @param filter the filter
	 * @param page the page
	 * @return the string
	 */
	public String searchEntitiesOfLatestOntologySubmissions(ResolvedFilter filter, Page page){
		return this.doSearchEntities(null, filter, page);
	}

	/**
	 * Do search entities.
	 *
	 * @param acronyms the ontology ids
	 * @param filter the filter
	 * @param page the page
	 * @return the string
	 */
	protected String doSearchEntities(Collection<String> acronyms, ResolvedFilter filter, Page page){
		String url = "http://data.bioontology.org/search?q=" + filter.getMatchValue() +
			"&page=" + Integer.toString(page.getPage() + 1) +
			"&pagesize=" + Integer.toString(page.getMaxToReturn());
		
		StringBuffer sb = new StringBuffer();
		sb.append(url);
	
		if(CollectionUtils.isNotEmpty(acronyms)){
			Iterator<String> itr = acronyms.iterator();
			sb.append("&ontologies=" + itr.next());
			
			while(itr.hasNext()){
				sb.append("," + itr.next());
			}
		}
		
		String algorithm = filter.getMatchAlgorithmReference().getContent();
		
		if(algorithm.equals(StandardMatchAlgorithmReference.EXACT_MATCH.
			getMatchAlgorithmReference().getContent())){
			sb.append("&exact_match=true");
		}

		sb.append(
				this.getBioportalQueryStringForFilter(filter));
		
		String xml = this.doCallBioportalMemCache(sb.toString());

		return xml;
	}
	
	/**
	 * Gets the bioportal query string for filter.
	 *
	 * @param filter the filter
	 * @return the bioportal query string for filter
	 * 
	 */
	protected String getBioportalQueryStringForFilter(ResolvedFilter filter) {
		StringBuffer sb = new StringBuffer();

		URIAndEntityName target = filter.getComponentReference().getPropertyReference();
		
		if(StringUtils.equals(target.getName(),DEFINITIONS_NAME)){
			sb.append("&require_definition=true");
		} else if(StringUtils.equals(target.getName(),PROPERTIES_NAME)){
			sb.append("&include_properties=true");
		}
			
		return sb.toString();
	}
	
	/**
	 * Append api key.
	 *
	 * @param url the url
	 * @return the string
	 */
	private String appendApiKey(String url){
		String fullUrl = url + ( url.contains("?") ? "&" : "?") + API_KEY_PARAM + "=" + this.apiKey;
		
		return fullUrl;
	}

	/**
	 * Do call bioportal.
	 *
	 * @param url the url
	 * @return the string
	 */
	protected String doCallBioportal(String url){
		String fullUrl = this.appendApiKey(url);

        if(!this.cache.containsKey(fullUrl)){

            String xml = this.callBioportal(fullUrl);

            if(StringUtils.isBlank(xml)){
                return null;
            }

            this.cache.put(fullUrl, xml);

            this.writeCache();

            return xml;
        } else {
            return new String(this.cache.get(fullUrl));
        }
	}
	
	/**
	 * Purge cache.
	 *
	 * @param url the url
	 */
	protected void purgeCache(String url){
		String fullUrl = this.appendApiKey(url);
		
		synchronized(cache){
		
			this.cache.remove(fullUrl);
			
			try {
				this.writeCache();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Do call bioportal mem cache.
	 *
	 * @param url the url
	 * @return the string
	 */
	protected String doCallBioportalMemCache(String url){
		String fullUrl = url + ( url.contains("?") ? "&" : "?") + API_KEY_PARAM + "=" + this.apiKey;
		
		if(! this.memCache.containsKey(fullUrl)){
	
			String xml = this.callBioportal(fullUrl);
			
			this.memCache.put(fullUrl, xml);
		}
		
		return new String(this.memCache.get(fullUrl));
	}
	
	/**
	 * Call bioportal.
	 *
	 * @param url the url
	 * @return the string
	 */
	protected String callBioportal(String url){
		log.info("Calling Bioportal REST: " + url);
		HttpHeaders headers = new HttpHeaders();
		headers.set( "Accept", "application/xml" );
		
		ResponseEntity<String> response;
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
			response = this.restTemplate.exchange(
                    uri,
					HttpMethod.GET, 
					new HttpEntity<Void>(headers), 
					String.class);
		} catch (HttpStatusCodeException e) {
            if(e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
                throw new PrivateOntologyException();
            } else if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
				throw e;
			} else {
				log.error("Error calling BioPortal REST Service", e);
				throw ExceptionFactory.createUnknownException("Error calling NCBO BioPortal: " + e.getMessage());
			}
		}
		
		return response.getBody();
	}
	
	protected void setApiKey(){
		//check for environment variable
		if(StringUtils.isNotBlank(this.apiKey)){
			log.info("Using APIKEY from pre-set Property.");
			
			return;
		}
		
		String apiKeyEnvVar = System.getProperty(API_KEY_PROP);
		if(StringUtils.isNotBlank(apiKeyEnvVar)){
			log.info("Using APIKEY from System Property.");
			this.apiKey = apiKeyEnvVar;
			
			return;
		}

		log.warn("No Bioportal API Key Set! Please sent one via the System Variable: " 
				+ API_KEY_PROP + " or in the Bioportal config file.");
	}
	
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
	public void afterPropertiesSet() throws IOException {
    	this.propertiesSet = true;
    	
    	this.setApiKey();
    	
    	if(StringUtils.isBlank(this.cachePath)){
    		this.cachePath = 
				this.pluginConfigManager.getPluginWorkDirectory(BIOPORTAL_CONFIG_NAMESPACE).
					getPath() + File.separator + "cache";
    	}

		File file = getCacheFile();
		
		if(! file.exists()){
			log.info("Creating new cache - " + file.getPath());
				
			file.getParentFile().mkdirs();
		}
			
		this.cache = this.createCache(file);
	}
    
    private File getCacheFile(){
    	String cacheFilePath = 
	    		this.cachePath + "/cache.out";
    	
    	return new File(cacheFilePath);
    }

   /**
	 * Write cache.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeCache() {
		this.db.commit();
	}

	/**
	 * Sets the cache update period.
	 *
	 * @param cacheUpdatePeriod the new cache update period
	 */
	public void setCacheUpdatePeriod(int cacheUpdatePeriod) {
		this.cacheUpdatePeriod = cacheUpdatePeriod;
	}

	/**
	 * Gets the cache update period.
	 *
	 * @return the cache update period
	 */
	public int getCacheUpdatePeriod() {
		return cacheUpdatePeriod;
	}

	public String getCachePath() {
		return cachePath;
	}

	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
		if(this.propertiesSet){
			this.fireApiKeyChangeEvent();
		}
	}
}
