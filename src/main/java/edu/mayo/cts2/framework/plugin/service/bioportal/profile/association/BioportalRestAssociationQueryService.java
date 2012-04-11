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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.association;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvablePropertyReference;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.association.types.GraphDirection;
import edu.mayo.cts2.framework.model.association.types.GraphFocus;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.types.AssociationDirection;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ParentOrChildOfEntityDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;
import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;

/**
 * The Class BioportalRestAssociationQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestAssociationQueryService 
	extends AbstractBioportalRestService
	implements AssociationQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private AssociationTransform associationTransform;

	@Override
	public Set<ResolvablePropertyReference<EntityDirectoryEntry>> getSupportedSearchReferences() {
		Set<ResolvablePropertyReference<EntityDirectoryEntry>> returnSet =
			new HashSet<ResolvablePropertyReference<EntityDirectoryEntry>>();
		
		ResolvablePropertyReference<EntityDirectoryEntry> refName = 
				ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference(), 
					new AttributeResolver<EntityDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								EntityDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getResourceName());
						}
					});
		
		ResolvablePropertyReference<EntityDirectoryEntry> refAbout = 
				ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.ABOUT.getPropertyReference(), 
					new AttributeResolver<EntityDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								EntityDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getAbout());
						}
					});
	
		
		returnSet.add(refName);
		returnSet.add(refAbout);
		
		return returnSet;
	}

	public Set<ResolvableMatchAlgorithmReference> getSupportedMatchAlgorithms() {
		Set<ResolvableMatchAlgorithmReference> returnSet = new HashSet<ResolvableMatchAlgorithmReference>();
		
		MatchAlgorithmReference exactMatch = 
			StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		
		returnSet.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(exactMatch, new ExactMatcher()));
		
		MatchAlgorithmReference contains = 
			StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		
		returnSet.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(contains, new ContainsMatcher()));
		
		return returnSet;
	}
	
	public DirectoryResult<EntityDirectoryEntry> doGetAssociationsOfEntity(
			final String codeSystemName, 
			final String codeSystemVersionName, 
			final String entity,
			final String predicateName,
			Set<ResolvedFilter> filterComponent,
			Page page) {

		String ontologyVersionId = 
			this.identityConverter.codeSystemVersionNameToOntologyVersionId(
					codeSystemVersionName);

		final String xml = this.bioportalRestService.
			getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, entity);
		
		ParentOrChildOfEntityDirectoryBuilder builder;
		try {
			builder = new ParentOrChildOfEntityDirectoryBuilder(
					this.associationTransform.transformEntitiesForRelationship(
							xml, 
							codeSystemName,
							codeSystemVersionName, 
							predicateName),
						getSupportedMatchAlgorithms(),
						getSupportedSearchReferences()
					);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return builder.restrict(filterComponent).
			addStart(page.getStart()).
			addMaxToReturn(page.getMaxToReturn()).
			resolve();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<AssociationDirectoryEntry> getResourceSummaries(
			AssociationQuery query, 
			SortCriteria sortCriteria,
			Page page) {
		EntityDescriptionReadId id= null;
		
		AssociationQueryServiceRestrictions restrictions = query.getRestrictions();
		
		if (restrictions != null &&
				restrictions.getSourceEntity() != null && 
				restrictions.getCodeSystemVersion() != null) {
			id= new EntityDescriptionReadId(
					restrictions.getSourceEntity().getEntityName(),
					restrictions.getCodeSystemVersion());
		}
		if (id != null) {
			return getDirectoryResult(id);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<Association> getResourceList(
			AssociationQuery query, 
			SortCriteria sortCriteria,
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(
			AssociationQuery query) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.association.AssociationQueryService#getChildrenAssociationsOfEntity(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, edu.mayo.cts2.framework.service.command.Page, edu.mayo.cts2.framework.service.profile.entitydescription.id.EntityDescriptionId)
	 */
	/*
	@Override
	public DirectoryResult<EntityDirectoryEntry> getChildrenAssociationsOfEntity(
			EntityDescriptionReadId id,
			EntityDescriptionQuery query,		
			ResolvedReadContext readContext,
			Page page) {

		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(id.getCodeSystemVersion().getName());
		
		return this.doGetAssociationsOfEntity(
				codeSystemName,
				id.getCodeSystemVersion().getName(),
				id.getEntityName().getName(),
				CHILDREN_PREDICATE,
				query != null ? query.getFilterComponent() : null,
				page);
	}
	*/

	@Override
	public Set<? extends PropertyReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.association.AdvancedAssociationQueryService#getAssociationGraph(edu.mayo.cts2.framework.service.profile.entitydescription.id.EntityDescriptionId, edu.mayo.cts2.framework.model.association.types.GraphDirection, long)
	 */
	public DirectoryResult<GraphNode> getAssociationGraph(
			GraphFocus focusType,
			EntityDescriptionReadId id, 
			GraphDirection direction,
			long depth) {	
		
		String codeSystemVersionName = id.getCodeSystemVersion().getName();
		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(codeSystemVersionName);
		
		ScopedEntityName focusEntityName = id.getEntityName();
		
		if(depth != 1){
			throw new UnsupportedOperationException("Only depth of '1' is allowed.");
		}
		if(direction != GraphDirection.FORWARD){
			throw new UnsupportedOperationException("Only GraphDirection of 'FORWARD' is allowed.");
		}

		String ontologyVersionId = this.identityConverter.
			codeSystemVersionNameToOntologyVersionId(codeSystemVersionName);
		
		String xml;
		
		if(focusEntityName.getName().equals("TOP_NODE")){
			xml = bioportalRestService.getHierarchyRootsByOntolotyVersionId(ontologyVersionId);
		} else {
			xml = bioportalRestService.getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, focusEntityName.getName());
		}
		
	
		List<GraphNode> associations = 
			this.associationTransform.transformAssociationForGraph(xml, codeSystemName, codeSystemVersionName);
		
		for(long i=0;i<associations.size();i++){
			GraphNode entry = associations.get((int)i);
			entry.setNodeNumber(i);
			entry.setNextNodeNumber(i+1);
			
			entry.setDirection(AssociationDirection.SOURCE_TO_TARGET);
			
		}
		
		return new DirectoryResult<GraphNode>(associations,true);
	}

	
	private DirectoryResult<AssociationDirectoryEntry> getDirectoryResult(EntityDescriptionReadId id) {
		String ontologyVersionId = 
				this.identityConverter.codeSystemVersionNameToOntologyVersionId(
						id.getCodeSystemVersion().getName());
			
			String codeSystemName = this.identityConverter.
					codeSystemVersionNameCodeSystemName(id.getCodeSystemVersion().getName());

			final String xml = this.bioportalRestService.
				getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, id.getEntityName().getName());
		
			return this.associationTransform.transformSubjectOfAssociationsForEntity(
					xml, 
					codeSystemName, 
					id.getCodeSystemVersion().getName());
			
	}
}
