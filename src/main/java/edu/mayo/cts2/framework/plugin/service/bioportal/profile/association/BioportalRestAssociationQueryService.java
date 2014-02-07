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

import edu.mayo.cts2.framework.filter.match.*;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.association.types.GraphDirection;
import edu.mayo.cts2.framework.model.association.types.GraphFocus;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.*;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ParentOrChildOfEntityDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.EntityResolver;
import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	
	@Resource
	private EntityResolver entityResolver;

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
			final ScopedEntityName entity,
			final String predicateName,
			Set<ResolvedFilter> filterComponent,
			Page page) {

		final String xml = 
			this.entityResolver.getEntityXml(entity, codeSystemName);

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
	    throw new UnsupportedOperationException();
	}


	private DirectoryResult<AssociationDirectoryEntry> getDirectoryResult(EntityDescriptionReadId id) {
		IdentityConverter.AcronymAndSubmissionId versionId =
				this.identityConverter.versionNameToAcronymAndSubmissionId(
                        id.getCodeSystemVersion().getName());

			String codeSystemName = versionId.getAcronym();

			final String xml = this.entityResolver.getEntityXml(
						id.getEntityName(),
                        codeSystemName);

			return this.associationTransform.transformSubjectOfAssociationsForEntity(
					xml,
					codeSystemName,
					id.getCodeSystemVersion().getName());

	}
}
