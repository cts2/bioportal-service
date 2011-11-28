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

import javax.xml.xpath.XPathExpression;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterables;

import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.core.EntitySynopsis;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.core.URIAndEntityName;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.Designation;
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
	
	private static final String NODE = "success.data.classBean";

	/**
	 * Transform associations for code system version.
	 *
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<AssociationDirectoryEntry> transformAssociationsForCodeSystemVersion(
			String xml,
			String codeSystemName,
			String codeSystemVersionName) throws Exception {
		List<EntityDirectoryEntry> returnList = new ArrayList<EntityDirectoryEntry>();
		
		List<Node> nodeList = this.getSourceNodes(xml);
		
		for(Node node : nodeList){
			
			returnList.addAll(this.transformEntityNodeForRelationships(
					codeSystemName, 
					codeSystemVersionName, 
					null, node));
		}

		return null;//returnList;
	}
	
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
				predicateName, node);
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
	private List<EntityDirectoryEntry> transformEntityNodeForRelationships(
			String codeSystemName,
			String codeSystemVersionName, 
			String predicateName, Node node) {
		List<EntityDirectoryEntry> entryList = new ArrayList<EntityDirectoryEntry>();

		XPathExpression relationships;
		if(StringUtils.isNotBlank(predicateName)){
			relationships = TransformUtils.getXpathExpression(					
					"relations/entry[string/text()='" + predicateName + "']");
		} else {

			relationships = TransformUtils.getXpathExpression(
			"relations/entry");
		}

		NodeList predicateNodeList = 
			TransformUtils.evalXpathExpressionForNodeList(relationships, node);

		for(int i=0;i<predicateNodeList.getLength();i++){

			Node predicate = predicateNodeList.item(i);

			List<Node> targets = this.getTargetNodes(predicate);
			
			for(Node entryNode : targets){

				EntityDirectoryEntry entry = new EntityDirectoryEntry();

				String about = this.getAbout(entryNode);
				String name = this.getName(entryNode);
				String label = this.getLabel(entryNode);
				

				entry.setAbout(about);
				String version= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
				entry.addKnownEntityDescription(this.createKnownEntityDescription(
						codeSystemName, 
						codeSystemVersionName, 
						label));
				
				entry.setHref(this.getUrlConstructor().createEntityUrl(
						codeSystemName, 
						version, 
						name));
				entry.setName(ModelUtils.createScopedEntityName(name, codeSystemName));

				entryList.add(entry);
			}
		}

		return entryList;
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
	public   URIAndEntityName[] transformURIAndEntityNameForRelationships(
			String xml,
			String codeSystemName,
			String codeSystemVersionName, 
			String predicateName) {
		List<URIAndEntityName> entityList = new ArrayList<URIAndEntityName>();
		Node node = TransformUtils.getNamedChildWithPath(BioportalRestUtils.getDocument(xml), NODE);


		XPathExpression relationships;
		if(StringUtils.isNotBlank(predicateName)){
			relationships = TransformUtils.getXpathExpression(					
					"relations/entry[string/text()='" + predicateName + "']");
		} else {

			relationships = TransformUtils.getXpathExpression(
			"relations/entry");
		}

		NodeList predicateNodeList = 
			TransformUtils.evalXpathExpressionForNodeList(relationships, node);

		for(int i=0;i<predicateNodeList.getLength();i++){

			Node predicate = predicateNodeList.item(i);

			List<Node> targets = this.getTargetNodes(predicate);
			
			for(Node entryNode : targets){

				URIAndEntityName entityName = new URIAndEntityName();

				String about = this.getAbout(entryNode);
				String name = this.getName(entryNode);
				String label = this.getLabel(entryNode);
				

				entityName.setName(name);
				entityName.setNamespace(codeSystemName);
				String version= this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);
				entityName.setHref(this.getUrlConstructor().createEntityUrl(
						codeSystemName, 
						version, 
						name));
				//entityName.setUri(uri)
				

				entityList.add(entityName);
			}
		}		
		return Iterables.toArray(entityList, URIAndEntityName.class);
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
		String version=  this.getIdentityConverter().codeSystemVersionNameToVersion(codeSystemVersionName);

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
	 * Transform association for graph.
	 *
	 * @param xml the xml
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the list
	 */
	public List<GraphNode> transformAssociationForGraph(
			String xml,
			String codeSystemName,
			String codeSystemVersionName) {
		
		return this.transformAssociationForRelationships(
				xml, 
				codeSystemName, 
				codeSystemVersionName, 
				GraphNode.class);
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
		
		Node subjectNode = TransformUtils.getNamedChildWithPath(doc,
			"success.data.classBean");
		
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
	 * Gets the source nodes.
	 *
	 * @param xml the xml
	 * @return the source nodes
	 */
	private List<Node> getSourceNodes(String xml){
		Document doc = 
			BioportalRestUtils.getDocument(xml);
		
		Node node = 
			TransformUtils.getNamedChildWithPath(doc, "success.data.page.contents.classBeanResultList");
		
		
		return TransformUtils.getNodeList(node, "classBean");
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
							codeSystemVersionName), true, true);

		return result;
	}
}
