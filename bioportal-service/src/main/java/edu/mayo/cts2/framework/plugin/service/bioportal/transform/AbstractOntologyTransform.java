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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.RoleReference;
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference;
import edu.mayo.cts2.framework.model.core.ValueSetReference;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class AbstractOntologyTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public abstract class AbstractOntologyTransform extends AbstractTransform {
	
	private static final String MAILTO = "mailto:";
	private static final String CONTACT_ROLE = "contact";

	protected static final String ONTOLOGY_BEAN = "success.data.ontologyBean";
	protected static final String ONTOLOGY_BEAN_LIST = "success.data.list.ontologyBean";
	protected static final String ABBREVIATION = "abbreviation";
	protected static final String ONTOLOGY_VERSION_ID = "id";
	protected static final String ONTOLOGY_ID = "ontologyId";
	protected static final String DISPLAY_LABEL = "displayLabel";
	protected static final String FORMAT = "format";
	protected static final String VERSION = "versionNumber";
	protected static final String DESCRIPTION = "description";
	protected static final String TAG = "versionStatus";

	protected static final String IS_FLAT = "isFlat";
	protected static final String IS_FOUNDRY = "isFoundry";
	protected static final String IS_METADATAONLY = "isMetadataOnly";
	protected static final String IS_VIEW = "isView";
	protected static final String VIEW_DEFINITION = "viewDefinition";
	protected static final String DATE_CREATED = "dateCreated";
	protected static final String DATE_RELEASED = "dateReleased";
	protected static final String CONTACT_NAME = "contactName";
	protected static final String CONTACT_EMAIL = "contactEmail";
	
	protected static final String URN = "urn";
	protected static final String FILENAMES = "filenames";
	protected static final String STRING = "string";
	
	/**
	 * Builds the definition of value set reference.
	 *
	 * @param valueSetName the value set name
	 * @return the value set reference
	 */
	protected ValueSetReference buildDefinitionOfValueSetReference(String valueSetName){
		ValueSetReference valueSetReference = new ValueSetReference();
		String valueSetPath = this.getUrlConstructor().createValueSetUrl(valueSetName);

		valueSetReference.setContent(valueSetName);
		valueSetReference.setHref(valueSetPath);
		valueSetReference.setUri(this.getIdentityConverter().getValueSetAbout(valueSetName, BioportalConstants.DEFAULT_VIEW_ABOUT));
		
		return valueSetReference;
	}
	
	/**
	 * Gets the properties.
	 *
	 * @param node the node
	 * @param namespace the namespace
	 * @return the properties
	 */
	protected List<Property> getProperties(Node node, String namespace){
		List<Property> returnList = new ArrayList<Property>();
		
		String isFlat = TransformUtils.getNamedChildText(node, IS_FLAT);
		String isFoundry = TransformUtils.getNamedChildText(node, IS_FOUNDRY);
		String isMetadataOnly = TransformUtils.getNamedChildText(node, IS_METADATAONLY);
		
		this.addProperty(IS_FLAT, namespace, isFlat, returnList);
		this.addProperty(IS_FOUNDRY, namespace, isFoundry, returnList);
		this.addProperty(IS_METADATAONLY, namespace, isMetadataOnly, returnList);
		
		return returnList;
	}
	
	/**
	 * Adds the property.
	 *
	 * @param predicateName the predicate name
	 * @param predicateNamespace the predicate namespace
	 * @param value the value
	 * @param list the list
	 */
	private void addProperty(
			String predicateName, 
			String predicateNamespace,
			String value, List<Property> list){
		if(StringUtils.isBlank(value)){
			return;
		}
		
		Property prop = new Property();
		prop.setPredicate(new PredicateReference());
		prop.getPredicate().setName(predicateName);
		prop.getPredicate().setNamespace(predicateNamespace);
		
		StatementTarget target = new StatementTarget();
		target.setLiteral(ModelUtils.createOpaqueData(value));
		
		prop.addValue(target);
		
		list.add(prop);
	}
	
	/**
	 * Gets the source and role reference.
	 *
	 * @param node the node
	 * @return the source and role reference
	 */
	protected SourceAndRoleReference getSourceAndRoleReference(Node node){
		String name = TransformUtils.getNamedChildText(node, CONTACT_NAME);
		String email = TransformUtils.getNamedChildText(node, CONTACT_EMAIL);
		
		SourceAndRoleReference source = new SourceAndRoleReference();
		source.setSource(new SourceReference());
		source.getSource().setContent(name);
		source.getSource().setUri(MAILTO + email);
		
		source.setRole(new RoleReference());
		source.getRole().setContent(CONTACT_ROLE);
		
		return source;
	}
	
	/**
	 * Gets the code system current version reference.
	 *
	 * @param codeSystemName the code system name
	 * @return the code system current version reference
	 */
	protected CodeSystemVersionReference getCodeSystemCurrentVersionReference(
			String codeSystemName){
		String ontologyId = 
			this.getIdentityConverter().codeSystemNameToOntologyId(codeSystemName);
		String xml = this.getBioportalRestService().getLatestOntologyVersionByOntologyId(ontologyId);
		
		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), ONTOLOGY_BEAN);

		String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
		
		String codeSystemVersionName = this.getIdentityConverter().
			ontologyVersionIdToCodeSystemVersionName(ontologyId, ontologyVersionId);
		String version = this.getIdentityConverter().
				codeSystemVersionNameToVersion(codeSystemVersionName);
		
		CodeSystemVersionReference ref = new CodeSystemVersionReference();
		ref.setCodeSystem(this.buildCodeSystemReference(codeSystemName));
		NameAndMeaningReference csv = new NameAndMeaningReference();
		csv.setContent(codeSystemVersionName);
		csv.setHref(this.getUrlConstructor().createCodeSystemVersionUrl(codeSystemName, version));
		ref.setVersion(csv);
		
		return ref;
	}
	
	/**
	 * Gets the value set current version reference.
	 *
	 * @param valueSetName the value set name
	 * @return the value set current version reference
	 */
	protected ValueSetDefinitionReference getValueSetCurrentVersionReference(
			String valueSetName){
		String ontologyId = 
			this.getIdentityConverter().valueSetNameToOntologyId(valueSetName);
		
		String xml = this.getBioportalRestService().getLatestOntologyVersionByOntologyId(ontologyId);
		
		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), ONTOLOGY_BEAN);

		String ontologyVersionId = TransformUtils.getNamedChildText(node, ONTOLOGY_VERSION_ID);
		
		String valueSetDefinitionName = this.getIdentityConverter().
			ontologyVersionIdToValueSetDefinitionName(ontologyId, ontologyVersionId);
		
		ValueSetDefinitionReference ref = new ValueSetDefinitionReference();
		ref.setValueSet(this.buildDefinitionOfValueSetReference(valueSetName));
		NameAndMeaningReference def = new NameAndMeaningReference();
		def.setContent(valueSetName);
		def.setHref(this.getUrlConstructor().createValueSetDefinitionUrl(valueSetName, valueSetDefinitionName));
		ref.setValueSetDefinition(def);
		
		return ref;
	}
	

	/**
	 * Creates the ontology id property.
	 *
	 * @param ontologyId the ontology id
	 * @return the property
	 */
	protected Property createOntologyIdProperty(String ontologyId) {
		return this.doCreateIdProperty(
				ontologyId, 
				BioportalConstants.BIOPORTAL_ONTOLOGY_ID_ABOUT, 
				BioportalConstants.BIOPORTAL_ONTOLOGY_ID_NAME);
	}
	
	/**
	 * Creates the ontology version id property.
	 *
	 * @param ontologyVersionId the ontology version id
	 * @return the property
	 */
	protected Property createOntologyVersionIdProperty(String ontologyVersionId) {
		return this.doCreateIdProperty(
				ontologyVersionId, 
				BioportalConstants.BIOPORTAL_ONTOLOGYVERSION_ID_ABOUT, 
				BioportalConstants.BIOPORTAL_ONTOLOGYVERSION_ID_NAME);
	}
	
	/**
	 * Do create id property.
	 *
	 * @param id the id
	 * @param uri the uri
	 * @param name the name
	 * @return the property
	 */
	private Property doCreateIdProperty(String id, String uri, String name) {
		Property prop = new Property();
		prop.setPredicate(new PredicateReference());
		prop.getPredicate().setName(name);
		prop.getPredicate().setUri(uri);
		prop.getPredicate().setNamespace(name);
		
		StatementTarget target = new StatementTarget();
		target.setLiteral(ModelUtils.createOpaqueData(id));
		
		prop.addValue(target);
		
		return prop;
	}
}
