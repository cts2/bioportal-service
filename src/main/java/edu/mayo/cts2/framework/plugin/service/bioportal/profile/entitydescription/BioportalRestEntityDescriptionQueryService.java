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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import edu.mayo.cts2.framework.filter.directory.AbstractCallbackDirectoryBuilder.Callback;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.EntityReferenceList;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.ComponentReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURIList;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.association.BioportalRestAssociationQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.EntityDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions.HierarchyRestriction.HierarchyType;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;

/**
 * The Class BioportalRestEntityDescriptionQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestEntityDescriptionQueryService 
	extends AbstractBioportalRestService
	implements EntityDescriptionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;

	@Resource
	private EntityDescriptionTransform entityDescriptionTransform;

	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private BioportalRestAssociationQueryService bioportalRestAssociationQueryService;
	
	private static final String CHILDREN_PREDICATE = "SubClass";
	
	/**
	 * Gets the entity description name from uri.
	 *
	 * @param uri the uri
	 * @return the entity description name from uri
	 */
	public String getEntityDescriptionNameFromUri(String uri) {
		return this.bioportalRestService.getEntityByUri(uri);
	}

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.EntityDescriptionService#getAllEntityDescriptions(org.cts2.rest.service.command.Filter, org.cts2.rest.service.command.Page)
	 */
	/**
	 * Gets the all entity descriptions.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @return the all entity descriptions
	 */
	private DirectoryResult<EntityDirectoryEntry> getAllEntityDescriptions(
			Query query,
			Set<ResolvedFilter> filterComponent, 
			Page page) {
		
		EntityDirectoryBuilder builder = getAllEntitiesDirectoryBuilder();
		
		return builder.restrict(filterComponent).
			addMaxToReturn(page.getMaxToReturn()).
			addStart(page.getStart()).resolve();
	}
	
	/**
	 * Gets the count.
	 *
	 * @param xml the xml
	 * @return the count
	 */
	private int getCount(String xml){
		Document doc = BioportalRestUtils.getDocument(xml);
		
		String count = TransformUtils.getNamedChildTextWithPath(doc, "success.data.page.numResultsTotal");
		
		return Integer.valueOf(count);
	}
	
	/**
	 * Gets the page for start and max.
	 *
	 * @param start the start
	 * @param max the max
	 * @return the page for start and max
	 */
	private Page getPageForStartAndMax(int start, int max){
		Page page = new Page();
		if(start < max){
			page.setMaxToReturn(max);
			page.setPage(0);
		} else {
			int calculatedPage;
			if(start != 0){
				calculatedPage = start / max;
			} else {
				calculatedPage = 0;
			}
			page.setPage(calculatedPage);
			page.setMaxToReturn(
					(start - (calculatedPage * max)) + max);
		}
		
		return page;
	}

	/**
	 * Gets the entity descriptions of code system version.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the entity descriptions of code system version
	 */
	private DirectoryResult<EntityDirectoryEntry> getEntityDescriptionsOfCodeSystemVersion(
			Query query,
			Set<ResolvedFilter> filterComponent, 
			Page page, 
			String codeSystemName,
			String codeSystemVersionName) {
		if(filterComponent != null){
			return this.getEntityDescriptionsByCodeSystemVersionNameWithFilter(
					filterComponent, 
					page,
					codeSystemVersionName);
		} else {
			return this.getEntityDescriptionsByCodeSystemVersionNameWithoutFilter(
					page, 
					codeSystemName, 
					codeSystemVersionName);
		}
	}
	
	/**
	 * Gets the entity descriptions by code system version name without filter.
	 *
	 * @param page the page
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the entity descriptions by code system version name without filter
	 */
	private DirectoryResult<EntityDirectoryEntry> getEntityDescriptionsByCodeSystemVersionNameWithoutFilter(
			Page page, 
			String codeSystemName, 
			String codeSystemVersionName) {
		String xml = bioportalRestService.getAllEntitiesByAcronym(
                codeSystemName,
                page);
		
		return this.entityDescriptionTransform.transformEntityDirectory(
				xml, 
				codeSystemName, 
				codeSystemVersionName);

	}
	
	/* (non-Javadoc)
	 * @see org.cts2.rest.service.EntityDescriptionService#getEntityDescriptionsByCodeSystemVersionName(org.cts2.rest.service.command.Filter, org.cts2.rest.service.command.Page, java.lang.String, java.lang.String)
	 */
	/**
	 * Gets the entity descriptions by code system version name with filter.
	 *
	 * @param filterComponent the filter component
	 * @param page the page
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the entity descriptions by code system version name with filter
	 */
	private DirectoryResult<EntityDirectoryEntry> getEntityDescriptionsByCodeSystemVersionNameWithFilter(
			Set<ResolvedFilter> filterComponent,
			Page page,
			final String codeSystemVersionName) {

        IdentityConverter.AcronymAndSubmissionId id =
                this.identityConverter.versionNameToAcronymAndSubmissionId(codeSystemVersionName);

		EntityDirectoryBuilder builder = this.getEntitiesOfCodeSystemVersionDirectoryBuilder(
				id);
		
		return builder.restrict(filterComponent).
			addMaxToReturn(page.getMaxToReturn()).
			addStart(page.getStart()).resolve();
	}
	
	/**
	 * Calculate transform start.
	 *
	 * @param bioportalPage the bioportal page
	 * @param start the start
	 * @return the int
	 */
	private int calculateTransformStart(Page bioportalPage, int start){
		int bioportalStart = bioportalPage.getStart();

		return start - bioportalStart;
	}
	
	/**
	 * Calculate transform end.
	 *
	 * @param bioportalPage the bioportal page
	 * @param start the start
	 * @param max the max
	 * @return the int
	 */
	private int calculateTransformEnd(Page bioportalPage, int start, int max){
		return this.calculateTransformStart(bioportalPage, start) + max;
	}

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.EntityDescriptionService#getAllEntityDescriptionsCount(org.cts2.rest.service.command.Filter)
	 */
	/**
	 * Gets the entity descriptions count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @return the entity descriptions count
	 */
	public int getEntityDescriptionsCount(
			Query query,
			Set<ResolvedFilter> filterComponent) {
		if(filterComponent != null){
			return this.getAllEntitiesDirectoryBuilder().restrict(filterComponent).count();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.EntityDescriptionService#insertNamedEntityDescription(java.lang.String, java.lang.String, java.lang.String, org.cts2.entity.NamedEntityDescription)
	 */
	/**
	 * Creates the named entity description.
	 *
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param entity the entity
	 */
	public void createNamedEntityDescription(
			String codeSystemName, 
			String codeSystemVersionName,
			NamedEntityDescription entity) {
		throw new RuntimeException("Bioportal is READ-ONLY!");
	}

	public Set<MatchAlgorithmReference> getSupportedMatchAlgorithms() {
		Set<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();
		
		MatchAlgorithmReference contains = 
			StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		
		MatchAlgorithmReference exactMatch = 
			StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		
		returnSet.add(contains);
		returnSet.add(exactMatch);
		
		return returnSet;
	}

	@Override
	public Set<ComponentReference> getSupportedSearchReferences() {
		Set<ComponentReference> returnSet =
			new HashSet<ComponentReference>();
		
		returnSet.add(
				StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference());

		returnSet.add(BioportalRestService.DEFINITIONS);
		returnSet.add(BioportalRestService.PROPERTIES);
		
		return returnSet;
	}
	
	public BioportalRestService getBioportalRestService() {
		return bioportalRestService;
	}

	public void setBioportalRestService(BioportalRestService bioportalRestService) {
		this.bioportalRestService = bioportalRestService;
	}

	/**
	 * Gets the entity descriptions of code system version count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the entity descriptions of code system version count
	 */
	public int getEntityDescriptionsOfCodeSystemVersionCount(
			Query query,
			Set<ResolvedFilter> filterComponent, 
			String codeSystemName,
			String codeSystemVersionName) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Gets the entities of code system version directory builder.
	 *
	 * @param ontologyId the ontology id
	 * @param ontologyVersionId the ontology version id
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @return the entities of code system version directory builder
	 */
	private EntityDirectoryBuilder getEntitiesOfCodeSystemVersionDirectoryBuilder(
			final IdentityConverter.AcronymAndSubmissionId id){
		return new EntityDirectoryBuilder(new Callback<EntityDirectoryEntry>(){

			public DirectoryResult<EntityDirectoryEntry> execute(
					ResolvedFilter filterComponent,
					float minScore,
					int start, 
					int maxResults) {
				
				Page bioportalPage = getPageForStartAndMax(start, maxResults);
			
				String xml = bioportalRestService.searchEntitiesByAcronym(
                        id.getAcronym(),
                        filterComponent,
                        bioportalPage);
				
				DirectoryResult<EntityDirectoryEntry> directoryResult = 
						entityDescriptionTransform.transformEntityDirectoryFromSearch(
								calculateTransformStart(bioportalPage, start), 
								calculateTransformEnd(bioportalPage, start, maxResults),
								xml);
					
					return directoryResult;
			}
			
			public int executeCount(
					ResolvedFilter filterComponent,
					float minScore) {
				Page bioportalPage = new Page();
				bioportalPage.setPage(0);
				bioportalPage.setMaxToReturn(1);
				
				String xml = bioportalRestService.searchEntitiesByAcronym(
                        id.getAcronym(),
                        filterComponent,
                        bioportalPage);
				
				return getCount(xml);
			}

			public DirectoryResult<EntityDirectoryEntry> execute(
					int start,
					int maxResults) {
				
				Page bioportalPage = getPageForStartAndMax(start, maxResults);
				
				String xml = bioportalRestService.getAllEntitiesByAcronym(
                        id.getAcronym(),
                        bioportalPage);
				
				DirectoryResult<EntityDirectoryEntry> directoryResult = 
						entityDescriptionTransform.transformEntityDirectory(
								xml,
                                id.getAcronym(),
								identityConverter.acronymAndSubmissionIdToVersionName(id));
					
				return directoryResult;
			}

			public int executeCount() {
				Page bioportalPage = new Page();
				bioportalPage.setPage(0);
				bioportalPage.setMaxToReturn(1);
				
				String xml = bioportalRestService.
                        getAllEntitiesByAcronym(id.getAcronym(), bioportalPage);
				
				return getCount(xml);
			}
		},
		getSupportedMatchAlgorithms());
	}
	
	private EntityDirectoryBuilder getAllEntitiesDirectoryBuilder(){
		return new EntityDirectoryBuilder(			
	
			new Callback<EntityDirectoryEntry>(){

				public DirectoryResult<EntityDirectoryEntry> execute(
						ResolvedFilter filterComponent, 
						float minScore,
						int start, 
						int maxResults) {
				
					Page bioportalPage = getPageForStartAndMax(start, maxResults);
					
					String xml = bioportalRestService.searchEntitiesOfLatestOntologySubmissions(
							filterComponent,
							bioportalPage);
		
					DirectoryResult<EntityDirectoryEntry> directoryResult = 
							entityDescriptionTransform.transformEntityDirectoryFromSearch(
									calculateTransformStart(bioportalPage, start), 
									calculateTransformEnd(bioportalPage, start, maxResults),
									xml);
						
						return directoryResult;			
				}

				public int executeCount(
						ResolvedFilter filterComponent, 
						float minScore) {
					Page bioportalPage = new Page();
					bioportalPage.setPage(0);
					bioportalPage.setMaxToReturn(1);
					
					String xml = bioportalRestService.searchEntitiesOfLatestOntologySubmissions(
							filterComponent,
							bioportalPage);
					
					return getCount(xml);
				}

				public DirectoryResult<EntityDirectoryEntry> execute(
						int start,
						int maxResults) {
					throw new UnsupportedOperationException();
				}

				public int executeCount() {
					throw new UnsupportedOperationException();
				}
			},
			getSupportedMatchAlgorithms());
	}

	/**
	 * Does entity description exist.
	 *
	 * @param codeSystem the code system
	 * @param codeSystemVersionName the code system version name
	 * @param entityName the entity name
	 * @return true, if successful
	 */
	public boolean doesEntityDescriptionExist(
			String codeSystem,
			String codeSystemVersionName, 
			String entityName) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<EntityDirectoryEntry> getResourceSummaries(
			EntityDescriptionQuery query, 
			SortCriteria sort,
			Page page) {
		
		EntityDescriptionQueryServiceRestrictions restrictions = query.getRestrictions();
		
		if(restrictions != null && restrictions.getHierarchyRestriction()!= null){

			if(restrictions.getHierarchyRestriction().getHierarchyType() != HierarchyType.CHILDREN){
				throw new UnsupportedOperationException("Only CHILDREN queries supported.");
			}
			
			// TODO: Check this - DEEPAK
			String codeSystemVersionName = restrictions.getHierarchyRestriction().getEntity().getEntityName().getName();
			
			String codeSystemName = this.identityConverter.versionNameToAcronymAndSubmissionId(codeSystemVersionName).getAcronym();
			
			return this.bioportalRestAssociationQueryService.doGetAssociationsOfEntity(
					codeSystemName, 
					codeSystemVersionName, 
					restrictions.getHierarchyRestriction().getEntity().getEntityName(), 
					CHILDREN_PREDICATE, 
					query.getFilterComponent(), 
					page);
		
		} else if(restrictions != null && restrictions.getCodeSystemVersions() != null){
			
			DirectoryResult<EntityDirectoryEntry> results = null;
			
			// TODO : Check for correctness - DEEPAK
			for (NameOrURI codeSystemVersion : restrictions.getCodeSystemVersions())
			{
				String codeSystemVersionName = codeSystemVersion.getName();

				String codeSystemName = this.identityConverter.versionNameToAcronymAndSubmissionId(codeSystemVersionName).getAcronym();
            
				DirectoryResult<EntityDirectoryEntry> temp =  this.getEntityDescriptionsOfCodeSystemVersion(
					query.getQuery(), 
					query.getFilterComponent(), 
					page,
					codeSystemName,
					codeSystemVersionName);
				
				if (temp == null)
					continue;
				
				if (results == null)
					results = temp;
				else
					results.getEntries().addAll(temp.getEntries());
			}
			
			return results;
			
		} else {
			return this.getAllEntityDescriptions(
					query.getQuery(), 
					query.getFilterComponent(), 
					page);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<EntityListEntry> getResourceList(
			EntityDescriptionQuery query, 
			SortCriteria sort,
			Page page) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(
			EntityDescriptionQuery query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends VersionTagReference> getSupportedTags() {
		return null;
	}

	@Override
	public Set<? extends ComponentReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}

	@Override
	public boolean isEntityInSet(EntityNameOrURI entity,
			EntityDescriptionQuery restrictions, ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityReferenceList resolveAsEntityReferenceList(
			EntityDescriptionQuery restrictions, ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityNameOrURIList intersectEntityList(
			Set<EntityNameOrURI> entities, EntityDescriptionQuery restrictions,
			ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}
	
	/********************************************/
	
}
