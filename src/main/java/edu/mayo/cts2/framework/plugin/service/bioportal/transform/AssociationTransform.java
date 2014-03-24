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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.collect.Iterables;

import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.core.EntitySynopsis;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;

/**
 * The Class AssociationTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class AssociationTransform extends AbstractTransform{

	private static final String NODE = "class";

	/**
	 * Transform entities for relationship.
	 *
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param predicateName the predicate name
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<EntityDirectoryEntry> transformEntitiesForRelationship(
			String xml,
			String codeSystemName,
			String codeSystemVersionName,
			String predicateName) throws Exception {
		
		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), NODE);
		
		return transformEntityNodeForRelationships(
				codeSystemName, 
				codeSystemVersionName,
				predicateName, 
				node);
	}

	private interface Transform<T> {
		public T transform(Node node);
	}
	
	private <T> List<T> doTransformEntityNodeForRelationships(
			Transform<T> transform,
			String codeSystemName,
			String codeSystemVersionName, 
			String predicateName, 
			Node node) {
		
		List<Node> predicates = new ArrayList<Node>();
		
		List<Node> entries = 
				TransformUtils.getNodeListWithPath(node, "relations.entry");

		if(StringUtils.isNotBlank(predicateName)){
			for(Node predicate : entries){
				String foundPredicateName = 
						TransformUtils.getNamedChildText(predicate, "string");
				
				if(StringUtils.equals(predicateName, foundPredicateName)){
					predicates.add(predicate);
				}
			}
		} else {
			predicates = entries;
		}
		
		List<T> returnList = new ArrayList<T>();
		
		for(Node predicate : predicates){

			List<Node> targets = this.getTargetNodes(predicate);
			
			for(Node entryNode : targets){

				T entry = transform.transform(entryNode);
				
				returnList.add(entry);
			}
		}		
		return returnList;
		
	}
	/**
	 * Transform entity node for relationships.
	 *
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param predicateName the predicate name
	 * @param node the node
	 * @return the list
	 */
	protected List<EntityDirectoryEntry> transformEntityNodeForRelationships(
			final String codeSystemName,
			final String codeSystemVersionName, 
			final String predicateName, 
			final Node xmlNode) {
		
		return this.doTransformEntityNodeForRelationships(new Transform<EntityDirectoryEntry>(){

			@Override
			public EntityDirectoryEntry transform(Node entryNode) {
				EntityDirectoryEntry entry = new EntityDirectoryEntry();

				String about = getAbout(entryNode);
				String name = getName(entryNode);
				String label = getLabel(entryNode);
				
				entry.setAbout(about);
				String version = getIdentityConverter().versionNameToVersion(codeSystemVersionName);
				entry.addKnownEntityDescription(createKnownEntityDescription(
						codeSystemName, 
						codeSystemVersionName, 
						label));
				
				entry.setHref(getUrlConstructor().createEntityUrl(
						codeSystemName, 
						version, 
						name));
				entry.setName(ModelUtils.createScopedEntityName(name, codeSystemName));
				
				return entry;
			}
		}, 
		codeSystemName, 
		codeSystemVersionName, 
		predicateName, 
		xmlNode);

	}


	/**
	 * Transform entity node for relationships.
	 *
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param predicateName the predicate name
	 * @param node the node
	 * @return the list
	 */
	public URIAndEntityName[] transformURIAndEntityNameForRelationships(
			String xml,
			final String codeSystemName,
			final String codeSystemVersionName, 
			final String predicateName) {
		
		Node xmlNode = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), NODE);

		List<URIAndEntityName> returnList = this.doTransformEntityNodeForRelationships(new Transform<URIAndEntityName>(){

			@Override
			public URIAndEntityName transform(Node entryNode) {
				URIAndEntityName entityName = new URIAndEntityName();

				String about = getAbout(entryNode);
				String name = getName(entryNode);
			
				entityName.setName(name);
				entityName.setNamespace(codeSystemName);
				String version = getIdentityConverter().versionNameToVersion(codeSystemVersionName);
				entityName.setHref(getUrlConstructor().createEntityUrl(
						codeSystemName, 
						version, 
						name));
				entityName.setUri(about);
				
				return entityName;
			}
		}, 
		codeSystemName, 
		codeSystemVersionName, 
		predicateName, 
		xmlNode);
	
		return Iterables.toArray(returnList, URIAndEntityName.class);
	}
	
	
	/**
	 * Transform association node.
	 *
	 * @param <T> the generic type
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param subjectNode the subject node
	 * @param predicateNode the predicate node
	 * @param targetNode the target node
	 * @param clazz the clazz
	 * @return the t
	 */
	private <T extends AssociationDirectoryEntry> T transformAssociationNode(
			String codeSystemName,
			String codeSystemVersionName,
			Node subjectNode,
			Node predicateNode,
			Node targetNode,
			Class<T> clazz) {

		String subjectName = TransformUtils.getNamedChildText(
				subjectNode, "id");

		String subjectAbout = TransformUtils.getNamedChildText(
			subjectNode, "fullId");

		String predicateName = this.getPredicate(predicateNode);
		
		String targetName = TransformUtils.getNamedChildText(
				targetNode, "id");

		String targetAbout = TransformUtils.getNamedChildText(
				targetNode, "fullId");
		String version=  this.getIdentityConverter().versionNameToVersion(codeSystemVersionName);

		T entry;
		try {
			entry = clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException();
		}
		
		entry.setSubject(new URIAndEntityName());
		entry.getSubject().setName(subjectName);
		entry.getSubject().setNamespace(codeSystemName);
		entry.getSubject().setUri(subjectAbout);
		entry.getSubject().setHref(
				this.getUrlConstructor().createEntityUrl(codeSystemName,
						version, subjectName));
		
		entry.setAssertedBy(this.buildCodeSystemVersionReference(codeSystemName, codeSystemVersionName));
        
		entry.setTarget(new StatementTarget());
		entry.getTarget().setEntity(new URIAndEntityName());
		entry.getTarget().getEntity().setName(targetName);
		entry.getTarget().getEntity().setNamespace(codeSystemName);
		entry.getTarget().getEntity().setUri(targetAbout);
		entry.getTarget().getEntity().setHref(
				this.getUrlConstructor().createEntityUrl(codeSystemName,
						version, targetName));
		
		if(clazz.equals(GraphNode.class)){
			String targetLabel = TransformUtils.getNamedChildText(
					targetNode, "label");
			
			GraphNode graphNode = (GraphNode)entry;
			graphNode.setNodeEntity(new EntitySynopsis());
			graphNode.getNodeEntity().setDesignation(targetLabel);
			
			graphNode.getNodeEntity().setName(targetName);
			graphNode.getNodeEntity().setNamespace(codeSystemName);
			graphNode.getNodeEntity().setUri(targetAbout);
			graphNode.getNodeEntity().setHref(
					this.getUrlConstructor().createEntityUrl(codeSystemName,
							version, targetName));
		}

		PredicateReference predicateRef = new PredicateReference();
		predicateRef.setName(predicateName);
		predicateRef.setUri(PREDICATE_URI_PREFIX + predicateName);
		predicateRef.setNamespace(codeSystemName);
		entry.setPredicate(predicateRef);
		
		return entry;
	}

	/**
	 * Transform association for relationships.
	 *
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the list
	 */
	private List<AssociationDirectoryEntry> transformAssociationForRelationships(
			String xml,
			String codeSystemName,
			String codeSystemVersionName) {

		return this.transformAssociationForRelationships(
				xml,
				codeSystemName,
				codeSystemVersionName,
				AssociationDirectoryEntry.class);
	}


	/**
	 * Transform association for relationships.
	 *
	 * @param <T> the generic type
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param clazz the clazz
	 * @return the list
	 */
	private <T extends AssociationDirectoryEntry> List<T> transformAssociationForRelationships(
			String xml,
			String codeSystemName,
			String codeSystemVersionName,
			Class<T> clazz) {
		List<T> entryList = new ArrayList<T>();

		Document doc = BioportalRestUtils.getDocument(xml);

		Node subjectNode = TransformUtils.getNamedChildWithPath(doc, NODE);

		Node relationsNode = TransformUtils.getNamedChild(subjectNode,
				"relations");

		List<Node> relations = TransformUtils.getNodeList(relationsNode,
				"entry");

		for (Node predicateNode : relations) {

			List<Node> objects = TransformUtils.getNodeListWithPath(
					predicateNode,
					"list.classBean");

			if (objects != null) {

				for (Node objectNode : objects) {

					T entry = this.transformAssociationNode(
							codeSystemName,
							codeSystemVersionName,
							subjectNode,
							predicateNode,
							objectNode,
							clazz);
					if (StringUtils.isNotBlank(entry.getPredicate().getName()) && ! entry.getPredicate().getName().startsWith("[R]")) {
					   entryList.add(entry);
					}
				}
			}
		}

		return entryList;
	}

	/**
	 * Gets the label.
	 *
	 * @param node the node
	 * @return the label
	 */
	private String getLabel(Node node) {
		return TransformUtils.getNamedChild(node, "label").getTextContent();
	}

	/**
	 * Gets the target nodes.
	 *
	 * @param node the node
	 * @return the target nodes
	 */
	private List<Node> getTargetNodes(Node node){
		Node list = 
			TransformUtils.getNamedChild(node, "list");
		
		if(list == null){
			return new ArrayList<Node>();
		}

		return TransformUtils.getNodeList(list, "classBean");
	}
	
	/**
	 * Gets the about.
	 *
	 * @param node the node
	 * @return the about
	 */
	private String getAbout(Node node){
		return TransformUtils.getNamedChild(node, "fullId").getTextContent();
	}
	
	/**
	 * Gets the name.
	 *
	 * @param node the node
	 * @return the name
	 */
	private String getName(Node node){
		return TransformUtils.getNamedChild(node, "id").getTextContent();
	}
	
	/**
	 * Gets the predicate.
	 *
	 * @param node the node
	 * @return the predicate
	 */
	private String getPredicate(Node node){
		return TransformUtils.getNamedChild(node, "string").getTextContent();
	}

	/**
	 * Transform subject of associations for entity.
	 *
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the directory result
	 */
	public DirectoryResult<AssociationDirectoryEntry> transformSubjectOfAssociationsForEntity(
			String xml, 
			String codeSystemName, 
			String codeSystemVersionName) {
		
		DirectoryResult<AssociationDirectoryEntry> result = new
			DirectoryResult<AssociationDirectoryEntry>(
					this.transformAssociationForRelationships(
							xml, 
							codeSystemName, 
							codeSystemVersionName), true);

		return result;
	}
}
