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

import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
import edu.mayo.cts2.framework.model.core.CodeSystemReference;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class CodeSystemVersionTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class CodeSystemVersionTransform extends AbstractBioportalOntologyVersionTransformTemplate<CodeSystemVersionCatalogEntry, CodeSystemVersionCatalogEntrySummary> {

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersion()
	 */
	@Override
	protected CodeSystemVersionCatalogEntry createNewResourceVersion() {
		return new CodeSystemVersionCatalogEntry();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#setName(org.cts2.core.ResourceVersionDescription, java.lang.String)
	 */
	@Override
	protected CodeSystemVersionCatalogEntry setName(
			CodeSystemVersionCatalogEntry resource, String name) {
		resource.setCodeSystemVersionName(name);
		return resource;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getName(org.cts2.core.ResourceVersionDescription)
	 */
	@Override
	protected String getName(CodeSystemVersionCatalogEntry resource) {
		return resource.getCodeSystemVersionName();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getResourceName(java.lang.String)
	 */
	@Override
	protected String getResourceName(String ontologyId) {
		return this.getIdentityConverter().ontologyIdToCodeSystemName(ontologyId);
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getResourceVersionName(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getResourceVersionName(String ontologyId,
			String ontologyVersionId) {
		return this.getIdentityConverter().ontologyVersionIdToCodeSystemVersionName(ontologyId, ontologyVersionId);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#setName(org.cts2.core.ResourceVersionDescriptionDirectoryEntry, java.lang.String)
	 */
	@Override
	protected CodeSystemVersionCatalogEntrySummary setName(
			CodeSystemVersionCatalogEntrySummary summary, String name) {
		summary.setCodeSystemVersionName(name);
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getAbout(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getAbout(String ontologyVersionId, String name) {
		return this.getCodeSystemVersionAbout(ontologyVersionId, name);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getHref(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getHref(String resourceName, String resourceVersionName) {
		return this.getUrlConstructor().createCodeSystemVersionUrl(resourceName, resourceVersionName);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersionSummary(org.w3c.dom.Node, java.lang.String, org.cts2.core.ResourceVersionDescriptionDirectoryEntry)
	 */
	@Override
	protected CodeSystemVersionCatalogEntrySummary decorateResourceVersionSummary(
			Node node, String resourceName, CodeSystemVersionCatalogEntrySummary summary) {
		String tag = TransformUtils.getNamedChildText(node, TAG);
		
		summary.addCodeSystemVersionTag(new VersionTagReference());
		summary.getCodeSystemVersionTag(0).setContent(tag);
		
		CodeSystemReference csr = new CodeSystemReference();
		csr.setContent(resourceName);
		csr.setHref(this.getUrlConstructor().createCodeSystemUrl(resourceName));
		
		summary.setVersionOf(csr);
		
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersionSummary(org.cts2.core.ResourceVersionDescription, org.cts2.core.ResourceVersionDescriptionDirectoryEntry)
	 */
	@Override
	protected CodeSystemVersionCatalogEntrySummary decorateResourceVersionSummary(
			CodeSystemVersionCatalogEntry resource,
			CodeSystemVersionCatalogEntrySummary summary) {
		//TODO:
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersion(org.w3c.dom.Node, java.lang.String, org.cts2.core.ResourceVersionDescription)
	 */
	@Override
	protected CodeSystemVersionCatalogEntry decorateResourceVersion(Node node,
			String resourceName,
			CodeSystemVersionCatalogEntry resourceVersion) {
		String resourceVersionName = resourceVersion.getCodeSystemVersionName();

		resourceVersion.setVersionOf(this.buildCodeSystemReference(resourceName));
		
		resourceVersion.setEntityDescriptions(this.getUrlConstructor().createEntitiesOfCodeSystemVersionUrl(resourceName, resourceVersionName));
		resourceVersion.setAssociations(this.getUrlConstructor().createAssociationsOfCodeSystemVersionUrl(resourceName, resourceVersionName));
		
		return resourceVersion;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersionSummary()
	 */
	@Override
	protected CodeSystemVersionCatalogEntrySummary createNewResourceVersionSummary() {
		return new CodeSystemVersionCatalogEntrySummary();
	}
	

	/**
	 * Gets the code system version about.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @param codeSystemName the code system name
	 * @return the code system version about
	 */
	private String getCodeSystemVersionAbout(String ontologyVersionId, String codeSystemName){	
		return this.getIdentityConverter().getCodeSystemAbout(codeSystemName, BioportalConstants.DEFAULT_ONTOLOGY_ABOUT);
	}
}
