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
package edu.mayo.cts2.framework.plugin.service.bioportal.transform;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.collect.Iterables;

import edu.mayo.cts2.framework.model.core.EntryDescription;
import edu.mayo.cts2.framework.model.core.OntologySyntaxReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.ResourceVersionDescription;
import edu.mayo.cts2.framework.model.core.ResourceVersionDescriptionDirectoryEntry;
import edu.mayo.cts2.framework.model.core.SourceAndNotation;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class AbstractBioportalOntologyVersionTransformTemplate.
 *
 * @param <R> the generic type
 * @param <S> the generic type
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public abstract class AbstractBioportalOntologyVersionTransformTemplate<R extends ResourceVersionDescription, S extends ResourceVersionDescriptionDirectoryEntry> extends AbstractOntologyTransform {
	private static final String DOWNLOAD_LOCATION = "downloadLocation";
	
	@SuppressWarnings("unchecked")
	private Map<Integer,List<S>> 
		cachedVersionSummaries = new LRUMap(1);
	
	private static final ThreadLocal<SoftReference<DateFormat>> threadLocal = new ThreadLocal<SoftReference<DateFormat>>();

	private static DateFormat getDateFormat() {
		SoftReference<DateFormat> ref = threadLocal.get();
		if (ref != null) {
			DateFormat result = ref.get();
			if (result != null) {
				return result;
			}
		}
		DateFormat result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S zzz");
		ref = new SoftReference<DateFormat>(result);
		threadLocal.set(ref);
		return result;
	}

	/**
	 * Creates the new resource version.
	 *
	 * @return the r
	 */
	protected abstract R createNewResourceVersion();
	
	/**
	 * Sets the name.
	 *
	 * @param resource the resource
	 * @param name the name
	 * @return the r
	 */
	protected abstract R setName(R resource, String name);
	
	/**
	 * Gets the name.
	 *
	 * @param resource the resource
	 * @return the name
	 */
	protected abstract String getName(R resource);
	
	/**
	 * Gets the resource name.
	 *
	 * @param ontologyId the ontology id
	 * @return the resource name
	 */
	protected abstract String getResourceName(String ontologyId);
	
	/**
	 * Gets the resource version name.
	 *
	 * @param ontologyId the ontology id
	 * @param ontologyVersionId the ontology version id
	 * @return the resource version name
	 */
	protected abstract String getResourceVersionName(String ontologyId, String ontologyVersionId);
	
	/**
	 * Sets the name.
	 *
	 * @param summary the summary
	 * @param name the name
	 * @return the s
	 */
	protected abstract S setName(S summary, String name);
	
	/**
	 * Gets the about.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @param name the name
	 * @return the about
	 */
	protected abstract String getAbout(String ontologyVersionId, String name);
	
	/**
	 * Gets the href.
	 *
	 * @param resourceName the resource name
	 * @param resourceVersionName the resource version name
	 * @return the href
	 */
	protected abstract String getHref(String resourceName, String resourceVersionName);
	
	/**
	 * Decorate resource version summary.
	 *
	 * @param node the node
	 * @param resourceName the resource name
	 * @param summary the summary
	 * @return the s
	 */
	protected abstract S decorateResourceVersionSummary(Node node, String resourceName, S summary);
	
	/**
	 * Decorate resource version summary.
	 *
	 * @param resource the resource
	 * @param summary the summary
	 * @return the s
	 */
	protected abstract S decorateResourceVersionSummary(R resource, S summary);
	
	/**
	 * Decorate resource version.
	 *
	 * @param node the node
	 * @param resourceName the resource name
	 * @param resource the resource
	 * @return the r
	 */
	protected abstract R decorateResourceVersion(Node node, String resourceName, R resource);

	/**
	 * Creates the new resource version summary.
	 *
	 * @return the s
	 */
	protected abstract S createNewResourceVersionSummary();
	
	/**
	 * Transform resource version.
	 *
	 * @param xml the xml
	 * @return the r
	 */
	public R transformResourceVersion(String xml) {

		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), ONTOLOGY_BEAN);

		String abbreviation = TransformUtils.getNamedChildText(node, ABBREVIATION);
		String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
		String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
		String displayLabel = TransformUtils.getNamedChildText(node, DISPLAY_LABEL);
		String description = TransformUtils.getNamedChildText(node, DESCRIPTION);
		String format = TransformUtils.getNamedChildText(node, FORMAT);
		String downloadLocation = TransformUtils.getNamedChildText(node, DOWNLOAD_LOCATION);
		
		String resourceName = 
			this.getResourceName(ontologyId);
		
		String resourceVersionName = 
			this.getResourceVersionName(
					ontologyId,
					ontologyVersionId);
		
		String about = this.getAbout(ontologyVersionId, resourceName);
		
		R resourceVersion = this.createNewResourceVersion();
		resourceVersion.setAbout(about);
		resourceVersion = this.setName(resourceVersion, resourceVersionName);
		resourceVersion.setFormalName(displayLabel);
		resourceVersion.setResourceSynopsis(new EntryDescription());
		resourceVersion.getResourceSynopsis().setValue(ModelUtils.toTsAnyType(description));
		resourceVersion.setOfficialResourceVersionId(this.getOfficialResourceVersionId(node));
		resourceVersion.addKeyword(abbreviation);
		resourceVersion.addKeyword(ontologyId);
		resourceVersion.addKeyword(ontologyVersionId);
		resourceVersion.setSourceAndNotation(new SourceAndNotation());
		resourceVersion.getSourceAndNotation().setSourceDocumentSyntax(new OntologySyntaxReference());
		resourceVersion.getSourceAndNotation().getSourceDocumentSyntax().setContent(format);
		
		resourceVersion.getSourceAndNotation().setSourceDocument(downloadLocation);

		resourceVersion.setOfficialReleaseDate(this.getDateReleased(node));
		
		resourceVersion.setProperty(Iterables.toArray(
				this.getProperties(node, resourceName), Property.class));
		
		resourceVersion.addSourceAndRole(this.getSourceAndRoleReference(node));
		
		resourceVersion.setDocumentURI(this.getIdentityConverter().getDocumentUri(ontologyVersionId));
		
		resourceVersion = this.decorateResourceVersion(node, resourceName, resourceVersion);
		
		resourceVersion.addProperty(this.createOntologyIdProperty(ontologyId));
		resourceVersion.addProperty(this.createOntologyVersionIdProperty(ontologyVersionId));
	
		return resourceVersion;
	}

	/**
	 * Transform versions of resource.
	 *
	 * @param xml the xml
	 * @return the list
	 */
	public List<S> transformVersionsOfResource(
			String xml) {
		List<S> entryList = 
			new ArrayList<S>();
	 
		Document doc = BioportalRestUtils.getDocument(xml);

		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, "success.data.list.ontologyBean");
		
		for(Node node : nodeList){
			S entry = null;
			try {
				entry = transformVersionSummary(node);
			} catch (HttpClientErrorException e) {
				log.warn("An HTTP Error Occured connecting to Bioportal.", e);
				continue;
			}
			
			entryList.add(entry);
		}
	
		return entryList;
	}

	/**
	 * Transform resource versions.
	 *
	 * @param xml the xml
	 * @return the list
	 */
	public List<S> transformResourceVersions(
			String xml){

		if(! this.cachedVersionSummaries.containsKey(xml.hashCode())){
			this.cachedVersionSummaries.clear();
			
			this.cachedVersionSummaries.put(xml.hashCode(),
				this.doTransformResourceVersions(xml));
		}
		
		return new ArrayList<S>(
				this.cachedVersionSummaries.get(xml.hashCode()));
	}
	
	/**
	 * Do transform resource versions.
	 *
	 * @param xml the xml
	 * @return the list
	 */
	private List<S> doTransformResourceVersions(
			String xml) {
		List<S> entryList = 
			new ArrayList<S>();
	 
		Document doc = BioportalRestUtils.getDocument(xml);

	
		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, "success.data.list.ontologyBean");

		for(Node node : nodeList){
			
			String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
			
			String versionsXml = 
				this.getBioportalRestService().getOntologyVersionsByOntologyId(ontologyId);
			
			Document versionsDoc = BioportalRestUtils.getDocument(versionsXml);
			
			List<Node> versionsNodeList = TransformUtils.getNodeListWithPath(versionsDoc, "success.data.list.ontologyBean");
			
			for(Node version : versionsNodeList) {
					
				S entry = transformVersionSummary(version);
	
				entryList.add(entry);
			}
		}
	
		return entryList;
	}

	
	/**
	 * Transform version summary.
	 *
	 * @param node the node
	 * @return the s
	 */
	protected S transformVersionSummary(
			Node node) {
		String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
		String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
		String displayLabel = TransformUtils.getNamedChildText(node, DISPLAY_LABEL);
		String description = TransformUtils.getNamedChildText(node, DESCRIPTION);
		
		S entry = this.createNewResourceVersionSummary();
		
		String resourceName = this.getResourceName(ontologyId);
		String resourceVersionName = this.getResourceVersionName(ontologyId, ontologyVersionId);

		entry.setAbout(this.getAbout(ontologyVersionId, resourceName));
		entry.setFormalName(displayLabel);
		entry = this.setName(entry, resourceVersionName);
		entry.setResourceName(resourceName);
		entry.setResourceSynopsis(new EntryDescription());
		entry.getResourceSynopsis().setValue(ModelUtils.toTsAnyType(description));

		entry.setDocumentURI(this.getIdentityConverter().getDocumentUri(ontologyVersionId));
		String version= this.getIdentityConverter().codeSystemVersionNameToVersion(resourceVersionName);
//		entry.setHref(this.getHref(resourceName, version));
		entry.setHref(getHref(resourceName, resourceVersionName, ontologyId, ontologyVersionId ));
		entry.setOfficialResourceVersionId(version);
		
		entry = this.decorateResourceVersionSummary(node, resourceName, entry);
		
		return entry;
	}
	
	
	protected String getHref(String resourceName, String resourceVersionName, String ontologyId, String ontologyVersionId) {
		String version= this.getIdentityConverter().codeSystemVersionNameToVersion(resourceVersionName);
		return this.getHref(resourceName, version);
	}
	/**
	 * Gets the date released.
	 *
	 * @param node the node
	 * @return the date released
	 */
	private Date getDateReleased(Node node){
		String dateReleased = TransformUtils.getNamedChildText(node, DATE_RELEASED);
		return this.doParseDate(dateReleased);
	}
	
	/**
	 * Do parse date.
	 *
	 * @param date the date
	 * @return the date
	 */
	private Date doParseDate(String date){
		try {
			return getDateFormat().parse(date);
		} catch (ParseException e) {
			log.warn("Cannot parse date: " + date);
		}
		return null;
	}
	
	/**
	 * Gets the official resource version id.
	 *
	 * @param node the node
	 * @return the official resource version id
	 */
	private String getOfficialResourceVersionId(Node node){
		String versionString = TransformUtils.getNamedChildText(node, VERSION);
		
		if(StringUtils.isBlank(versionString)){
			return "";
		}
		
		if(versionString.equalsIgnoreCase(BioportalConstants.UNKNOWN_VERSION)){
			return "";
		}
		
		return versionString;
	}
	
	
}
