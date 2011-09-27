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

import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils.NodeFilter;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class ValueSetTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ValueSetTransform extends AbstractBioportalOntologyTransformTemplate<ValueSetCatalogEntry, ValueSetCatalogEntrySummary>  {

	private static NodeFilter NODE_FILTER = new NodeFilter(){

		public boolean add(Node node) {
			return ! BooleanUtils.toBoolean(
					TransformUtils.getNamedChildText(node, IS_VIEW));
		}
	};
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#createNewResource()
	 */
	@Override
	protected ValueSetCatalogEntry createNewResource() {
		return new ValueSetCatalogEntry();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#setName(org.cts2.core.AbstractResourceDescription, java.lang.String)
	 */
	@Override
	protected ValueSetCatalogEntry setName(
			ValueSetCatalogEntry resource,
			String name) {
		resource.setValueSetName(name);
		
		return resource;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getName(org.cts2.core.AbstractResourceDescription)
	 */
	@Override
	protected String getName(ValueSetCatalogEntry resource) {
		return resource.getValueSetName();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getName(java.lang.String)
	 */
	@Override
	protected String getName(String ontologyId) {
		return this.getIdentityConverter().ontologyIdToValueSetName(ontologyId);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#setName(org.cts2.core.AbstractResourceDescriptionDirectoryEntry, java.lang.String)
	 */
	@Override
	protected ValueSetCatalogEntrySummary setName(
			ValueSetCatalogEntrySummary summary, 
			String name) {
		summary.setValueSetName(name);
		
		return summary;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getSummaryNodeFilter()
	 */
	@Override
	protected NodeFilter getSummaryNodeFilter() {
		return NODE_FILTER;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getAbout(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getAbout(String name) {		
		return this.getIdentityConverter().getValueSetAbout(name, BioportalConstants.DEFAULT_VIEW_ABOUT);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getHref(java.lang.String)
	 */
	@Override
	protected String getHref(String name) {
		return this.getUrlConstructor().createValueSetUrl(name);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateSummary(org.w3c.dom.Node, org.cts2.core.AbstractResourceDescriptionDirectoryEntry)
	 */
	@Override
	protected ValueSetCatalogEntrySummary decorateSummary(Node node,
			ValueSetCatalogEntrySummary summary) {
		
		String valueSetName = summary.getValueSetName();
		
		summary.setCurrentDefinition(this.getValueSetCurrentVersionReference(valueSetName));
			
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateSummary(org.cts2.core.AbstractResourceDescription, org.cts2.core.AbstractResourceDescriptionDirectoryEntry)
	 */
	@Override
	protected ValueSetCatalogEntrySummary decorateSummary(
			ValueSetCatalogEntry resource, ValueSetCatalogEntrySummary summary) {
		//TODO:
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateResource(org.w3c.dom.Node, org.cts2.core.AbstractResourceDescription)
	 */
	@Override
	protected ValueSetCatalogEntry decorateResource(Node node,
			ValueSetCatalogEntry resource) {
		String viewDefinition = TransformUtils.getNamedChildText(node, VIEW_DEFINITION);
		
		//TODO: not sure where to put this, as there are no Definitions on ValueSetDefinition
		
		resource.addAdditionalDocumentation(viewDefinition);
		
		return resource;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#createNewResourceSummary()
	 */
	@Override
	protected ValueSetCatalogEntrySummary createNewResourceSummary() {
		return new ValueSetCatalogEntrySummary();
	}
}
