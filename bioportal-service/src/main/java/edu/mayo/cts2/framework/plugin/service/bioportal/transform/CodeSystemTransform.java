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

import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntrySummary;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils.NodeFilter;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class CodeSystemTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class CodeSystemTransform extends AbstractBioportalOntologyTransformTemplate<CodeSystemCatalogEntry, CodeSystemCatalogEntrySummary> {

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
	protected CodeSystemCatalogEntry createNewResource() {
		return new CodeSystemCatalogEntry();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#setName(org.cts2.core.AbstractResourceDescription, java.lang.String)
	 */
	@Override
	protected CodeSystemCatalogEntry setName(
			CodeSystemCatalogEntry resource,
			String name) {
		resource.setCodeSystemName(name);
		return resource;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getName(org.cts2.core.AbstractResourceDescription)
	 */
	@Override
	protected String getName(CodeSystemCatalogEntry resource) {
		return resource.getCodeSystemName();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getName(java.lang.String)
	 */
	@Override
	protected String getName(String ontologyId) {
		return this.getIdentityConverter().ontologyIdToCodeSystemName(ontologyId);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#setName(org.cts2.core.AbstractResourceDescriptionDirectoryEntry, java.lang.String)
	 */
	@Override
	protected CodeSystemCatalogEntrySummary setName(
			CodeSystemCatalogEntrySummary summary, String name) {
		summary.setCodeSystemName(name);
		
		return summary;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getAbout(java.lang.String)
	 */
	protected String getAbout(String codeSystemName){	
		return this.getIdentityConverter().getCodeSystemAbout(codeSystemName, BioportalConstants.DEFAULT_ONTOLOGY_ABOUT);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getHref(java.lang.String)
	 */
	@Override
	protected String getHref(String name) {
		return this.getUrlConstructor().createCodeSystemUrl(name);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#getSummaryNodeFilter()
	 */
	@Override
	protected NodeFilter getSummaryNodeFilter() {
		return NODE_FILTER;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#createNewResourceSummary()
	 */
	@Override
	protected CodeSystemCatalogEntrySummary createNewResourceSummary() {
		return new CodeSystemCatalogEntrySummary();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateSummary(org.w3c.dom.Node, org.cts2.core.AbstractResourceDescriptionDirectoryEntry)
	 */
	@Override
	protected CodeSystemCatalogEntrySummary decorateSummary(
			Node node,
			CodeSystemCatalogEntrySummary summary) {
		//no need to use the Node here
		return this.doDecorateSummary(summary);
	}
	
	/**
	 * Do decorate summary.
	 *
	 * @param <T> the generic type
	 * @param summary the summary
	 * @return the code system catalog entry summary
	 */
	protected <T> CodeSystemCatalogEntrySummary doDecorateSummary(
			CodeSystemCatalogEntrySummary summary) {
		String codeSystemName = summary.getCodeSystemName();
		
		summary.setCurrentVersion(this.getCodeSystemCurrentVersionReference(codeSystemName));
		summary.setVersions(this.getUrlConstructor().createVersionsOfCodeSystemUrl(codeSystemName));
		
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateSummary(org.cts2.core.AbstractResourceDescription, org.cts2.core.AbstractResourceDescriptionDirectoryEntry)
	 */
	@Override
	protected CodeSystemCatalogEntrySummary decorateSummary(
			CodeSystemCatalogEntry resource,
			CodeSystemCatalogEntrySummary summary) {
		//no need to use the resource here
		return this.doDecorateSummary(summary);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.service.bioportal.transform.AbstractBioportalOntologyTransformTemplate#decorateResource(org.w3c.dom.Node, org.cts2.core.AbstractResourceDescription)
	 */
	@Override
	protected CodeSystemCatalogEntry decorateResource(
			Node node,
			CodeSystemCatalogEntry resource) {
		//no need to use the Node here
		String codeSystemName = resource.getCodeSystemName();
		
		resource.setCurrentVersion(this.getCodeSystemCurrentVersionReference(codeSystemName));
		resource.setVersions(this.getUrlConstructor().createVersionsOfCodeSystemUrl(codeSystemName));
		
		return resource;
	}	
}
