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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.collect.Iterables;

import edu.mayo.cts2.framework.model.core.AbstractResourceDescription;
import edu.mayo.cts2.framework.model.core.AbstractResourceDescriptionDirectoryEntry;
import edu.mayo.cts2.framework.model.core.EntryDescription;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils.NodeFilter;

/**
 * The Class AbstractBioportalOntologyTransformTemplate.
 *
 * @param <R> the generic type
 * @param <S> the generic type
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public abstract class AbstractBioportalOntologyTransformTemplate<R extends AbstractResourceDescription, S extends AbstractResourceDescriptionDirectoryEntry> extends AbstractOntologyTransform {

	/**
	 * Transform resource.
	 *
	 * @param xml the xml
	 * @return the r
	 */
	public R transformResource(String xml) {
		Document doc = BioportalRestUtils.getDocument(xml);

		Node node = TransformUtils.getNamedChildWithPath(doc, "success.data.ontologyBean");

		return this.transformResource(node);
	}
	
	/**
	 * Transform resources.
	 *
	 * @param xml the xml
	 * @return the list
	 */
	public List<R> transformResources(String xml) {
		List<R> entryList = 
			new ArrayList<R>();
	 
		List<Node> list = getResourceSummaryNodeList(xml);

		for(Node node : list){

			R entry = this.transformResource(node);
			entryList.add(entry);
		}
		
		return entryList;
	}
	
	/**
	 * Transform resource.
	 *
	 * @param node the node
	 * @return the r
	 */
	public R transformResource(Node node) {
		
		String abbreviation = TransformUtils.getNamedChildText(node, ABBREVIATION);
		String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
		String displayLabel = TransformUtils.getNamedChildText(node, DISPLAY_LABEL);
		String description = TransformUtils.getNamedChildText(node, DESCRIPTION);

		String name = this.getName(ontologyId);
		
		String about = this.getAbout(name);
		
		R resource = this.createNewResource();
		resource.setAbout(about);
		resource = this.setName(resource, name);
		resource.setFormalName(displayLabel);
		resource.setResourceSynopsis(new EntryDescription());
		resource.getResourceSynopsis().setValue(ModelUtils.toTsAnyType(description));
		resource.addKeyword(abbreviation);
		resource.addKeyword(ontologyId);
		resource.addSourceAndRole(this.getSourceAndRoleReference(node));

		resource.setProperty(Iterables.toArray(
				this.getProperties(node, name), Property.class));
		
		resource.addProperty(this.createOntologyIdProperty(ontologyId));
		
		resource = this.decorateResource(node, resource);

		return resource;
	}

	/**
	 * Creates the new resource.
	 *
	 * @return the r
	 */
	protected abstract R createNewResource();
	
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
	 * Gets the name.
	 *
	 * @param ontologyId the ontology id
	 * @return the name
	 */
	protected abstract String getName(String ontologyId);
	
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
	 * @param name the name
	 * @return the about
	 */
	protected abstract String getAbout(String name);
	
	/**
	 * Gets the href.
	 *
	 * @param name the name
	 * @return the href
	 */
	protected abstract String getHref(String name);
	
	/**
	 * Gets the summary node filter.
	 *
	 * @return the summary node filter
	 */
	protected abstract NodeFilter getSummaryNodeFilter();

	/**
	 * Transform resource summaries.
	 *
	 * @param xml the xml
	 * @return the list
	 */
	public List<S> transformResourceSummaries(
			String xml) {
		List<S> entryList = 
			new ArrayList<S>();
	 
		List<Node> list = getResourceSummaryNodeList(xml);

		for(Node node : list){

			S entry = this.transformResourceSummary(node);
			entryList.add(entry);
		}
		
		return entryList;
	}
	
	/**
	 * Gets the resource summary node list.
	 *
	 * @param xml the xml
	 * @return the resource summary node list
	 */
	public List<Node> getResourceSummaryNodeList(String xml){
		Document doc = BioportalRestUtils.getDocument(xml);
		
		return TransformUtils.getNodeList(
			TransformUtils.getNamedChildWithPath(doc, "success.data.list"), "ontologyBean", this.getSummaryNodeFilter());
	}
	
	/**
	 * Transform resource summary.
	 *
	 * @param node the node
	 * @return the s
	 */
	public S transformResourceSummary(Node node){
		
		String ontologyId = TransformUtils.getNamedChildText(node, ONTOLOGY_ID);
		String displayLabel = TransformUtils.getNamedChildText(node, DISPLAY_LABEL);
		String description = TransformUtils.getNamedChildText(node, DESCRIPTION);
		
		S entry = this.createNewResourceSummary();
		
		String name = this.getName(ontologyId);

		entry.setAbout(this.getAbout(name));
		entry.setFormalName(displayLabel);
		entry = this.setName(entry, name);
		entry.setResourceName(name);
		entry.setResourceSynopsis(new EntryDescription());
		entry.getResourceSynopsis().setValue(ModelUtils.toTsAnyType(description));
		
		entry.setHref(this.getHref(name));
		
		entry = this.decorateSummary(node, entry);

		return entry;
	}
	
	/**
	 * Decorate summary.
	 *
	 * @param node the node
	 * @param summary the summary
	 * @return the s
	 */
	protected abstract S decorateSummary(Node node, S summary);
	
	/**
	 * Decorate summary.
	 *
	 * @param resource the resource
	 * @param summary the summary
	 * @return the s
	 */
	protected abstract S decorateSummary(R resource, S summary);
	
	/**
	 * Decorate resource.
	 *
	 * @param node the node
	 * @param resource the resource
	 * @return the r
	 */
	protected abstract R decorateResource(Node node, R resource);

	/**
	 * Creates the new resource summary.
	 *
	 * @return the s
	 */
	protected abstract S createNewResourceSummary();

	/**
	 * Transform resource to summary.
	 *
	 * @param entry the entry
	 * @return the s
	 */
	public S transformResourceToSummary(
			R entry) {
		S summary = this.createNewResourceSummary();
		summary.setAbout(entry.getAbout());
		summary = this.setName(summary, this.getName(entry));
		summary.setFormalName(entry.getFormalName());
		summary.setHref(this.getHref(this.getName(entry)));
		summary.setResourceName(this.getName(entry));
		summary.setResourceSynopsis(entry.getResourceSynopsis());
		
		summary = this.decorateSummary(entry, summary);
		
		return summary;
	}
}
