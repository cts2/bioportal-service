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
package edu.mayo.cts2.framework.plugin.service.bioportal.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.mayo.cts2.framework.model.exception.Cts2RuntimeException;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObserver;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class IdentityConverter.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class IdentityConverter implements InitializingBean, CacheObserver {
	
	private static final Log log = LogFactory.getLog(IdentityConverter.class);

	@Resource
	private BioportalRestService bioportalRestService;

	private static final String ABBREVIATION = "abbreviation";
	private static final String ONTOLOGY_VERSION_ID = "id";
	private static final String ONTOLOGY_ID = "ontologyId";
	private static final String DISPLAY_LABEL = "displayLabel";
	private static final String FORMAT = "format";
	private static final String VERSION = "versionNumber";
	private static final String IS_VIEW = "isView";
	
	protected static final String URN = "urn";
	protected static final String FILENAMES = "filenames";
	protected static final String CODING_SCHEME = "codingScheme";
	protected static final String ONTOLOGY_BEAN = "success.data.ontologyBean";
	protected static final String STRING = "string";

	private Map<String,String> ontologyIdToCodeSystemName = new HashMap<String,String>();
	private Map<String,String> ontologyIdToValueSetName = new HashMap<String,String>();
	private Map<String,String> codeSystemNameToOntologyId = new HashMap<String,String>();
	private Map<String,String> valueSetNameToOntologyId = new HashMap<String,String>();
	private Map<String,String> nameToOntologyVersionId = new HashMap<String,String>();
	private Map<String,String> ontologyVersionIdToName = new HashMap<String,String>();
	private Map<String,String> codeSystemVersionNameToVersion = new HashMap<String,String>();
	private Map<String,String> codeSystemNameToAbout = new HashMap<String,String>();
	private Map<String,String> codeSystemAboutToName = new HashMap<String,String>();
	private Map<String,String> valueSetNameToAbout = new HashMap<String,String>();
	private Map<String,String> versionNameToName = new HashMap<String,String>();
	private Map<String,String> codeSystemNameAndVersionIdToCodeSystemVersionName = new HashMap<String,String>();
	
	/**
	 * The Enum NameType.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private enum NameType {VALUESET,CODESYSTEM};
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		try {
			this.bioportalRestService.addObserver(this);
			this.cacheNameAndId();
		} catch (Exception e) {
			log.error("Error Starting BioPortal Service", e);
		}
	}

	@Override
	public void onApiKeyChange() {
		this.cacheNameAndId();
	}

	/**
	 * Code system about to name.
	 *
	 * @param about the about
	 * @return the string
	 */
	public String codeSystemAboutToName(String about){
		return this.codeSystemAboutToName.get(about);
	}

	/**
	 * Code system name to ontology id.
	 *
	 * @param codeSystemName the code system name
	 * @return the string
	 */
	public String codeSystemNameToOntologyId(String codeSystemName){
		return this.codeSystemNameToOntologyId.get(codeSystemName);
	}
	
	/**
	 * Value set name to ontology id.
	 *
	 * @param valueSetName the value set name
	 * @return the string
	 */
	public String valueSetNameToOntologyId(String valueSetName){
		return this.valueSetNameToOntologyId.get(valueSetName);
	}
	
	/**
	 * Ontology id to code system name.
	 *
	 * @param ontologyId the ontology id
	 * @return the string
	 */
	public String ontologyIdToCodeSystemName(String ontologyId){
		String codeSystemName = this.ontologyIdToCodeSystemName.get(ontologyId);
		
		if(StringUtils.isBlank(codeSystemName)){
			log.warn("ResourceNameOrUri for ontologyId: " + ontologyId + " is blank. Checking Virtual Ids...");
			
			this.cacheNameAndIdByVirtualId(ontologyId);
			this.cacheVersionNameAndOntologyVersionId(ontologyId);
			
			codeSystemName = this.ontologyIdToCodeSystemName.get(ontologyId);
		}
		
		return codeSystemName;
	}
	
	public Set<String> getAllCodeSystemOntologyIds(){
		return this.ontologyIdToCodeSystemName.keySet();
	}
	
	/**
	 * Ontology id to value set name.
	 *
	 * @param ontologyId the ontology id
	 * @return the string
	 */
	public String ontologyIdToValueSetName(String ontologyId){
		String valueSetName = this.ontologyIdToValueSetName.get(ontologyId);
		
		if(StringUtils.isBlank(valueSetName)){
			log.warn("ResourceNameOrUri for ontologyId: " + ontologyId + " is blank. Checking Virtual Ids...");
			
			this.cacheNameAndIdByVirtualId(ontologyId);
			this.cacheVersionNameAndOntologyVersionId(ontologyId);
			
			valueSetName = this.ontologyIdToCodeSystemName.get(ontologyId);
		}
		
		return valueSetName;
	}

	/**
	 * Cach name and id.
	 *
	 * @param xml the xml
	 * @param type the type
	 */
	private void cachNameAndId(String xml, NameType type ) {
		
		Document document = BioportalRestUtils.getDocument(xml);
		try {

			List<Node> nodeList = TransformUtils.getNodeListWithPath(document, 
			"success.data.list.ontologyBean");

			for(Node node : nodeList){
			
				String name = buildName(node);
				String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
				
				
				
				switch(type){
					case VALUESET :{
						this.valueSetNameToOntologyId.put(name, ontologyId);
						this.ontologyIdToValueSetName.put(ontologyId, name);
						//break;
					} case CODESYSTEM :{
						this.codeSystemNameToOntologyId.put(name, ontologyId);
						this.ontologyIdToCodeSystemName.put(ontologyId, name);
						
						this.cacheVersionNameAndOntologyVersionId(ontologyId);
						this.cacheCodeSystemAbout(ontologyId);
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}
	
	/**
	 * Cache name and id.
	 */
	private void cacheNameAndId() {
		String codeSystemXml = this.bioportalRestService.getLatestOntologyVersions(true);
		String valueSetXml = this.bioportalRestService.getLatestViews(true);

		this.cachNameAndId(codeSystemXml, NameType.CODESYSTEM);

		this.cachNameAndId(valueSetXml, NameType.VALUESET);
		
		this.updateStaleCache(codeSystemXml);
		this.updateStaleCache(valueSetXml);
	}
	
	private void updateStaleCache(){
		String codeSystemXml = this.bioportalRestService.getLatestOntologyVersions(true);
		String valueSetXml = this.bioportalRestService.getLatestViews(true);
		
		this.updateStaleCache(codeSystemXml);
		this.updateStaleCache(valueSetXml);
	}
	
	private void updateStaleCache(String xml){
		int highestCachedVersionId = 0;
		for(String ontologyVersionId : this.ontologyVersionIdToName.keySet()){
			int versionId = Integer.parseInt(ontologyVersionId);
			if(highestCachedVersionId < versionId){
				highestCachedVersionId = versionId;
			}
		}
		
		int highestFoundVersionId = this.getHighestVersionId(xml);
		
		if(highestFoundVersionId > highestCachedVersionId){
			for(int i=highestCachedVersionId+1;i<=highestFoundVersionId;i++){
				try { 
					this.cacheVersionNameAndOntologyVersionIdWithOntologyVersionId(Integer.toString(i));
				} catch (Exception e) {
					log.error("Error Starting BioPortal Service", e);
				}
			}
		}
	}
	
	private int getHighestVersionId(String xml){
		int highestVersionId = 0;
		Document document = BioportalRestUtils.getDocument(xml);
	

		List<Node> nodeList = TransformUtils.getNodeListWithPath(document,
				"success.data.list.ontologyBean");

		for (Node node : nodeList) {
			String ontologyVersionId = TransformUtils.getNamedChildText(node,
					ONTOLOGY_VERSION_ID);
			
			int versionId = Integer.parseInt(ontologyVersionId);
			
			if(versionId > highestVersionId){
				highestVersionId = versionId;
			}
		}

		return highestVersionId;
	}
	
	/**
	 * Cache name and id by virtual id.
	 *
	 * @param virtualId the virtual id
	 */
	private void cacheNameAndIdByVirtualId(String virtualId) {
		try {
			String xml = 
				this.bioportalRestService.getLatestOntologyVersionByVirtualId(virtualId);

			Document doc = BioportalRestUtils.getDocument(xml);

			Node node = TransformUtils.getNamedChildWithPath(doc, 
				"success.data.ontologyBean");

			String name = buildName(node);
				String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);

			if(BooleanUtils.toBoolean(TransformUtils.getNamedChildText(node, IS_VIEW))){
				this.valueSetNameToOntologyId.put(name, ontologyId);
				this.ontologyIdToValueSetName.put(ontologyId, name);
			} else {
				this.codeSystemNameToOntologyId.put(name, ontologyId);
				this.ontologyIdToCodeSystemName.put(ontologyId, name);
			}

		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}
	
	/**
	 *codeSystemVersionName to code system version string.
	 * @param codeSystemVersionName
	 *
	 * @return the string
	 */	
	public String codeSystemVersionNameToVersion(String codeSystemVersionName) {		
		String version = this.codeSystemVersionNameToVersion.get(codeSystemVersionName);
		
		//if there's no version, just return the full name
		if(version == null){
			version = codeSystemVersionName;
		}
		
		return version;
	}
		
	
	
	
	/**
	 * Ontology version id to code system version name.
	 *
	 * @param ontologyId the ontology id
	 * @param ontologyVersionId the ontology version id
	 * @return the string
	 */
	public String ontologyVersionIdToCodeSystemVersionName(
			String ontologyId,
			String ontologyVersionId){

		if(! this.ontologyVersionIdToName.containsKey(ontologyVersionId)){
			this.cacheVersionNameAndOntologyVersionId(ontologyId, true);
			
			if(! this.ontologyVersionIdToName.containsKey(ontologyVersionId)){
				this.cacheVersionNameAndOntologyVersionIdWithOntologyVersionId(ontologyVersionId);
			}
		}
		
		return this.ontologyVersionIdToName.get(ontologyVersionId);
	}
	
	public String ontologyVersionIdToCodeSystemVersionName(
			String ontologyVersionId){

		if(! this.ontologyVersionIdToName.containsKey(ontologyVersionId)){
		
			if(! this.ontologyVersionIdToName.containsKey(ontologyVersionId)){
				this.cacheVersionNameAndOntologyVersionIdWithOntologyVersionId(ontologyVersionId);
			}
		}
		
		return this.ontologyVersionIdToName.get(ontologyVersionId);
	}
	
	/**
	 * Ontology version id to value set definition name.
	 *
	 * @param ontologyId the ontology id
	 * @param ontologyVersionId the ontology version id
	 * @return the string
	 */
	public String ontologyVersionIdToValueSetDefinitionName(
			String ontologyId,
			String ontologyVersionId){

		return this.ontologyVersionIdToCodeSystemVersionName(ontologyId, ontologyVersionId);
	}

	/**
	 * Code system version name to ontology version id.
	 *
	 * @param codeSystemVersionName the code system version name
	 * @return the string
	 */
	public String codeSystemVersionNameToOntologyVersionId(
			String codeSystemVersionName){

		if(! this.nameToOntologyVersionId.containsKey(codeSystemVersionName)){		
			this.updateStaleCache();
			if(! this.nameToOntologyVersionId.containsKey(codeSystemVersionName)){		
				throw new RuntimeException("OntologyVersionId should be cached.");
			}
		}
		
		return this.nameToOntologyVersionId.get(codeSystemVersionName);
	}
	
	public String codeSystemVersionNameCodeSystemName(
			String codeSystemVersionName){

		if(! this.nameToOntologyVersionId.containsKey(codeSystemVersionName)){	
			this.updateStaleCache();
			if(! this.nameToOntologyVersionId.containsKey(codeSystemVersionName)){	
				throw new RuntimeException("OntologyVersionId should be cached.");
			}
		}
		
		return this.versionNameToName.get(codeSystemVersionName);
	}
	
	public String codeSystemNameAndVersionIdToCodeSystemVersionName(
			String codeSystemName,
			String versionId){
		String name = this.doGetCodeSystemNameAndVersionIdToCodeSystemVersionName(
				codeSystemName, 
				versionId);
		
		//try it twice -- the first time will fire a cache refresh.
		if(name == null){
			name = this.doGetCodeSystemNameAndVersionIdToCodeSystemVersionName(
					codeSystemName, 
					versionId);
		}
		
		if(name == null){
			throw new RuntimeException("OntologyVersionId should be cached.");
		}	
		
		return name;
	}
	
	private String doGetCodeSystemNameAndVersionIdToCodeSystemVersionName(
			String codeSystemName,
			String versionId){

		if(! this.codeSystemNameAndVersionIdToCodeSystemVersionName.containsKey(
				this.createNameVersionIdKey(codeSystemName, versionId))){	
			
			//If the 'versionId' is the CodeSystemVersionName
			if(this.codeSystemVersionNameToVersion.containsKey(versionId)){
				return versionId;
			} else {
				this.updateStaleCache();

			}
		}

		return this.codeSystemNameAndVersionIdToCodeSystemVersionName.get(
				this.createNameVersionIdKey(codeSystemName, versionId));
	}
	
	/**
	 * Value set definition name to ontology version id.
	 *
	 * @param valueSetDefinitionName the value set definition name
	 * @return the string
	 */
	public String valueSetDefinitionNameToOntologyVersionId(
			String valueSetDefinitionName){

		if(! this.nameToOntologyVersionId.containsKey(valueSetDefinitionName)){		
			this.updateStaleCache();
			if(! this.nameToOntologyVersionId.containsKey(valueSetDefinitionName)){	
				throw new RuntimeException("OntologyVersionId should be cached.");
			}
		}
		
		return this.nameToOntologyVersionId.get(valueSetDefinitionName);
	}

	
	private boolean hasUniqueVersions(String ontologyId) {
		try {
			String xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);

			Document doc = BioportalRestUtils.getDocument(xml);

			List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, 
			"success.data.list.ontologyBean");
			
			// We check to see if all the versions of an ontologyId has unique names. If they do, we
			// use the version as the cts2 version, else we use the ontology version id as the version.
			Set<String> version_set= new HashSet<String> ();
			boolean unique_versions= true;
            for(Node node : nodeList){				
				String version = TransformUtils.getNamedChildText(node, VERSION);
				if (!version_set.contains(version)) {
					version_set.add(version);
				} else {
					unique_versions= false;
				}				
			}
            return unique_versions;
          } catch (Exception e) {
				throw new Cts2RuntimeException(e);
		  }
	}

	private void cacheVersionNameAndOntologyVersionId(String ontologyId) {
		this.cacheVersionNameAndOntologyVersionId(ontologyId, false);
	}
	
	private void cacheVersionNameAndOntologyVersionId(String ontologyId, boolean forceRefresh) {
		try {
			String xml;
			try {
				xml = this.bioportalRestService
						.getOntologyVersionsByOntologyId(ontologyId, forceRefresh);
			} catch (Exception e) {
				log.warn("Error caching OntologyId: " + ontologyId + ". Skipping.", e);
				return;
			}

			Document doc = BioportalRestUtils.getDocument(xml);

			List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, 
			"success.data.list.ontologyBean");
			
			// We check to see if all the versions of an ontologyId has unique names. If they do, we
			// use the version as the cts2 version, else we use the ontology version id as the version.
			Set<String> version_set= new HashSet<String> ();
			boolean unique_versions= true;
            for(Node node : nodeList){				
				String version = TransformUtils.getNamedChildText(node, VERSION);
				if (!version_set.contains(version)) {
					version_set.add(version);
				} else {
					unique_versions= false;
				}				
			}

			for(Node node : nodeList){
				
				String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
				String versionName = this.buildVersionName(node, unique_versions);

				this.nameToOntologyVersionId.put(versionName, ontologyVersionId);
				this.ontologyVersionIdToName.put(ontologyVersionId, versionName);
				this.versionNameToName.put(versionName, this.buildName(node));
				String version;
				if (unique_versions) {
				    version = TransformUtils.getNamedChildText(node, VERSION);
				} else {
					version= ontologyVersionId;
				}
				codeSystemVersionNameToVersion.put(versionName, version);
				this.codeSystemNameAndVersionIdToCodeSystemVersionName.put(
						this.createNameVersionIdKey(
								TransformUtils.getNamedChildText(node, ABBREVIATION), version),
						versionName);
			}
			
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}
	
	private String createNameVersionIdKey(String name, String versionId){
		return name + versionId;
	}
	
	/**
	 * Cache code system about.
	 *
	 * @param ontologyId the ontology id
	 */
	private void cacheCodeSystemAbout(String ontologyId) {
		try {
			String xml = this.bioportalRestService.getLatestOntologyVersionByOntologyId(ontologyId);

			String name = this.ontologyIdToCodeSystemName(ontologyId);
			
			String about = this.getAboutFromXml(xml, name, BioportalConstants.DEFAULT_ONTOLOGY_ABOUT);
			
			this.codeSystemAboutToName.put(about, name);
			
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}
	
	/**
	 * Cache version name and ontology version id with ontology version id.
	 *
	 * @param ontologyVersionId the ontology version id
	 */
	private void cacheVersionNameAndOntologyVersionIdWithOntologyVersionId(String ontologyVersionId) {
		try {
			String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);

			Document doc = BioportalRestUtils.getDocument(xml);

			Node node = TransformUtils.getNamedChildWithPath(doc, "success.data.ontologyBean");

			String versionName = this.buildVersionName(node, false);

			this.nameToOntologyVersionId.put(versionName, ontologyVersionId);
			this.ontologyVersionIdToName.put(ontologyVersionId, versionName);
			
			this.versionNameToName.put(versionName, this.buildName(node));
			
			String version;
			
			if (this.hasUniqueVersions(TransformUtils.getNamedChildText(node, ONTOLOGY_ID))) {
			    version = TransformUtils.getNamedChildText(node, VERSION);
			} else {
				version = ontologyVersionId;
			}
			codeSystemVersionNameToVersion.put(versionName, version);
			this.codeSystemNameAndVersionIdToCodeSystemVersionName.put(
					this.createNameVersionIdKey(
							TransformUtils.getNamedChildText(node, ABBREVIATION), version),
					versionName);

		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}

	/**
	 * Builds the name.
	 *
	 * @param node the node
	 * @return the string
	 */
	public String buildName(Node node){

			try {

				String abbreviation = TransformUtils.getNamedChildText(node, ABBREVIATION);
				String displayLabel = TransformUtils.getNamedChildText(node, DISPLAY_LABEL);

				if(StringUtils.isNotBlank(abbreviation)){
					return abbreviation;
				}
				if(StringUtils.isNotBlank(displayLabel)){
					return displayLabel;
				}
				
				throw new RuntimeException("Could not find a name for node.");

			} catch (Exception e) {
				throw new Cts2RuntimeException(e);
			}
	}
	
	/**
	 * Builds the version name.
	 *
	 * @param node the node
	 * @return the string
	 */
	private String buildVersionName(Node node, boolean useVersion){

		try {
			String abbreviation = this.buildName(node);
		
			String format = TransformUtils.getNamedChildText(node, FORMAT);
			String version;
			if (useVersion) {
				 version = TransformUtils.getNamedChildText(node, VERSION);
			} else {			
			   version = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
			}

			StringBuffer sb = new StringBuffer();

			sb.append(abbreviation);
			
			if(StringUtils.isNotBlank(version) &&
					! version.equalsIgnoreCase(BioportalConstants.UNKNOWN_VERSION)){
				sb.append("_").append(version);
			}

			if(StringUtils.isNotBlank(format)){
				sb.append("_").append(format);
			}

			return sb.toString().replaceAll("\\.", "-");
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObserver#onCodeSystemsChangeEvent(java.util.List)
	 */
	public void onCodeSystemsChangeEvent(List<String> changedOntologyIds) {
		this.cacheNameAndId();
		
		for(String ontologyId : changedOntologyIds){
			this.cacheVersionNameAndOntologyVersionId(ontologyId);
		}
	}
	
	/**
	 * Gets the urn.
	 *
	 * @param xml the xml
	 * @return the urn
	 */
	public String getUrn(String xml){
		try {
			Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), ONTOLOGY_BEAN);

			String urn = TransformUtils.getNamedChildText(node, URN);

			if(StringUtils.isBlank(urn)){
				urn = TransformUtils.getNamedChildText(node, CODING_SCHEME);
			} 
			
			urn = StringUtils.trim(urn);
			
			return urn;
		} catch(Exception e){
			throw new Cts2RuntimeException(e);
		}
	}
	
	/**
	 * Gets the code system about.
	 *
	 * @param codeSystemName the code system name
	 * @param defaultAboutPrefix the default about prefix
	 * @return the code system about
	 */
	public String getCodeSystemAbout(String codeSystemName, String defaultAboutPrefix){
		return this.doGetAbout(codeSystemName, defaultAboutPrefix, NameType.CODESYSTEM);
	}
	
	/**
	 * Gets the value set about.
	 *
	 * @param valueSetName the value set name
	 * @param defaultAboutPrefix the default about prefix
	 * @return the value set about
	 */
	public String getValueSetAbout(String valueSetName, String defaultAboutPrefix){
		return this.doGetAbout(valueSetName, defaultAboutPrefix, NameType.VALUESET);
	}
	
	/**
	 * Do get about.
	 *
	 * @param name the name
	 * @param defaultAboutPrefix the default about prefix
	 * @param type the type
	 * @return the string
	 */
	private String doGetAbout(String name, String defaultAboutPrefix, NameType type){
		Map<String,String> map;
		switch(type){
			case CODESYSTEM: {
				map = this.codeSystemNameToAbout;
				break;
			}
			case VALUESET: {
				map = this.valueSetNameToAbout;
				break;
			}
			default: {
				throw new IllegalStateException();
			}
		}
		
		if(! map.containsKey(name)){
			String ontologyId;
				switch(type){
				case CODESYSTEM: {
					ontologyId = this.codeSystemNameToOntologyId(name);
					if (ontologyId == null) {
						ontologyId = this.valueSetNameToOntologyId(name);
					}
					break;
				}
				case VALUESET: {
					ontologyId = this.valueSetNameToOntologyId(name);
					break;
				}
				default: {
					throw new IllegalStateException();
				}
			}
			String xml = this.bioportalRestService.getLatestOntologyVersionByOntologyId(ontologyId);
			
			map.put(name, 
					this.getAboutFromXml(xml, name, defaultAboutPrefix));
		}
		
		return map.get(name);
	}
	
	/**
	 * Gets the about from xml.
	 *
	 * @param xml the xml
	 * @param resourceName the resource name
	 * @param defaultAboutPrefix the default about prefix
	 * @return the about from xml
	 */
	private String getAboutFromXml(String xml, String resourceName, String defaultAboutPrefix){
		return defaultAboutPrefix + resourceName;
	}
	
	/**
	 * Gets the document uri.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @return the document uri
	 */
	public String getDocumentUri(String ontologyVersionId){	
		String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);
		
		String uri = this.getUrn(xml);
		
		if(StringUtils.isBlank(uri)){
			List<String> fileNames = this.getFileNames(xml);
			
			StringBuilder sb = new StringBuilder();
			for(String fileName : fileNames){
				sb.append(fileName);
			}
			
			uri = sb.toString();
		}
		
		return uri;
	}
	
	/**
	 * Gets the file names.
	 *
	 * @param xml the xml
	 * @return the file names
	 */
	private List<String> getFileNames(String xml){
		List<String> returnList = new ArrayList<String>();
		
		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), ONTOLOGY_BEAN);
		node = TransformUtils.getNamedChild(node, FILENAMES);
		
		for(Node stringNode : TransformUtils.getNodeList(node, STRING)){
			returnList.add(TransformUtils.getNodeText(stringNode));
		}

		return returnList;
	}
	
	public boolean isCachedCodeSystemVersionName(String codeSystemVersionName) {
		return nameToOntologyVersionId.containsKey(codeSystemVersionName);
	}
}
