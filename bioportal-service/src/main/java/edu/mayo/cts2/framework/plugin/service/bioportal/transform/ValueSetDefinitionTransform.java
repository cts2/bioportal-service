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

import edu.mayo.cts2.framework.model.core.ValueSetReference;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class ValueSetDefinitionTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ValueSetDefinitionTransform extends AbstractBioportalOntologyVersionTransformTemplate<ValueSetDefinition, ValueSetDefinitionDirectoryEntry> {

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersion()
	 */
	@Override
	protected ValueSetDefinition createNewResourceVersion() {
		return new ValueSetDefinition();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#setName(org.cts2.core.ResourceVersionDescription, java.lang.String)
	 */
	@Override
	protected ValueSetDefinition setName(ValueSetDefinition resource,
			String name) {
		//no-op
		return resource;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getName(org.cts2.core.ResourceVersionDescription)
	 */
	@Override
	protected String getName(ValueSetDefinition resource) {
		return resource.getDocumentURI();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getResourceName(java.lang.String)
	 */
	@Override
	protected String getResourceName(String ontologyId) {
		return this.getIdentityConverter().ontologyIdToValueSetName(ontologyId);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getResourceVersionName(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getResourceVersionName(String ontologyId,
			String ontologyVersionId) {
		return this.getIdentityConverter().ontologyVersionIdToValueSetDefinitionName(ontologyId, ontologyVersionId);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#setName(org.cts2.core.ResourceVersionDescriptionDirectoryEntry, java.lang.String)
	 */
	@Override
	protected ValueSetDefinitionDirectoryEntry setName(
			ValueSetDefinitionDirectoryEntry summary, String name) {
		summary.setResourceName(name);
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getAbout(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getAbout(String ontologyVersionId, String name) {
		return this.getValueSetDefinitionAbout(ontologyVersionId, name);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#getHref(java.lang.String, java.lang.String)
	 */
	@Override
	protected String getHref(String resourceName, String resourceVersionName) {
		return this.getUrlConstructor().createValueSetDefinitionUrl(resourceName, resourceVersionName);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersionSummary(org.w3c.dom.Node, java.lang.String, org.cts2.core.ResourceVersionDescriptionDirectoryEntry)
	 */
	@Override
	protected ValueSetDefinitionDirectoryEntry decorateResourceVersionSummary(
			Node node, String resourceName,
			ValueSetDefinitionDirectoryEntry summary) {
		String tag = TransformUtils.getNamedChildText(node, TAG);
		
		summary.addVersionTag(new VersionTagReference());
		summary.getVersionTag(0).setContent(tag);
		
		ValueSetReference csr = new ValueSetReference();
		csr.setContent(resourceName);
		csr.setHref(this.getUrlConstructor().createValueSetUrl(resourceName));
		
		summary.setDefinedValueSet(csr);
		
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersionSummary(org.cts2.core.ResourceVersionDescription, org.cts2.core.ResourceVersionDescriptionDirectoryEntry)
	 */
	@Override
	protected ValueSetDefinitionDirectoryEntry decorateResourceVersionSummary(
			ValueSetDefinition resource,
			ValueSetDefinitionDirectoryEntry summary) {
		//TODO
		return summary;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#decorateResourceVersion(org.w3c.dom.Node, java.lang.String, org.cts2.core.ResourceVersionDescription)
	 */
	@Override
	protected ValueSetDefinition decorateResourceVersion(Node node,
			String resourceName, ValueSetDefinition resourceVersion) {
	
		resourceVersion.setDefinedValueSet(this.buildDefinitionOfValueSetReference(resourceName));
		
		String viewDefinition = TransformUtils.getNamedChildText(node, VIEW_DEFINITION);

		//TODO: not sure where to put this, as there are no Definitions on ValueSetDefinition
		
		resourceVersion.addAdditionalDocumentation(viewDefinition);
		
		return resourceVersion;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersionSummary()
	 */
	@Override
	protected ValueSetDefinitionDirectoryEntry createNewResourceVersionSummary() {
		return new ValueSetDefinitionDirectoryEntry();
	}
	
	/**
	 * Gets the value set definition about.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @param valueSetName the value set name
	 * @return the value set definition about
	 */
	private String getValueSetDefinitionAbout(String ontologyVersionId, String valueSetName){
		return this.getIdentityConverter().getValueSetAbout(valueSetName, BioportalConstants.DEFAULT_VIEW_ABOUT);
	}
}
