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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import edu.mayo.cts2.framework.filter.directory.AbstractCallbackDirectoryBuilder.Callback;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.EntityDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;

/**
 * The Class BioportalRestEntityDescriptionQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestEntityDescriptionQueryService 
	extends AbstractBioportalRestQueryService<edu.mayo.cts2.framework.model.service.entitydescription.EntityDescriptionQueryService>
	implements EntityDescriptionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;

	@Resource
	private EntityDescriptionTransform entityDescriptionTransform;

	@Resource
	private IdentityConverter identityConverter;

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
					codeSystemName, 
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
		final String ontologyVersionId = identityConverter.codeSystemVersionNameToOntologyVersionId(codeSystemVersionName);
		
		String xml = bioportalRestService.getAllEntitiesByOntologyVersionId(
				ontologyVersionId, 
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
			final String codeSystemName, 
			final String codeSystemVersionName) {
		final String ontologyId = identityConverter.codeSystemNameToOntologyId(codeSystemName);
		final String ontologyVersionId = identityConverter.codeSystemVersionNameToOntologyVersionId(codeSystemName);
		
		EntityDirectoryBuilder builder = this.getEntitiesOfCodeSystemVersionDirectoryBuilder(
				ontologyId, 
				ontologyVersionId,
				codeSystemName, 
				codeSystemVersionName);
		
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
	 * @see org.cts2.rest.service.EntityDescriptionService#getEntityDescriptionByEntityName(java.lang.String, java.lang.String, java.lang.String)
	 */
	/**
	 * Gets the entity description by name.
	 *
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param entityName the entity name
	 * @return the entity description by name
	 */
	public EntityDescription getEntityDescriptionByName(
			String codeSystemName, 
			String codeSystemVersionName,
			ScopedEntityName entityName) {
		String ontologyId = this.identityConverter
				.codeSystemNameToOntologyId(codeSystemName);

		String xml = this.bioportalRestService
				.getEntityByOntologyIdAndEntityId(
						ontologyId,
						entityName.getName());

		return ModelUtils.toEntityDescription(
				entityDescriptionTransform.transformEntityDescription(xml,
					codeSystemName, codeSystemVersionName));
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

	protected List<MatchAlgorithmReference> getKnownMatchAlgorithmReferences() {
		List<MatchAlgorithmReference> returnList = new ArrayList<MatchAlgorithmReference>();
		
		MatchAlgorithmReference contains = 
			StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		
		MatchAlgorithmReference exactMatch = 
			StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		
		returnList.add(contains);
		returnList.add(exactMatch);
		
		return returnList;
	}

	protected List<ModelAttributeReference> getKnownModelAttributeReferences() {
		List<ModelAttributeReference> returnList =
			new ArrayList<ModelAttributeReference>();
		
		returnList.add(
				StandardModelAttributeReference.RESOURCE_SYNOPSIS.getModelAttributeReference());

		returnList.add(BioportalRestService.DEFINITIONS);
		returnList.add(BioportalRestService.PROPERTIES);
		
		return returnList;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPropertyReference(String nameOrUri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.AbstractQueryService#registerPredicateReferences()
	 */
	@Override
	protected List<? extends PredicateReference> getAvailablePredicateReferences() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.AbstractQueryService#registerMatchAlgorithmReferences()
	 */
	@Override
	protected List<MatchAlgorithmReference> getAvailableMatchAlgorithmReferences() {
		return this.getKnownMatchAlgorithmReferences();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.AbstractQueryService#registerModelAttributeReferences()
	 */
	@Override
	protected List<ModelAttributeReference> getAvailableModelAttributeReferences() {
		return this.getKnownModelAttributeReferences();
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
		String ontologyId = identityConverter.codeSystemNameToOntologyId(codeSystemName);
		String ontologyVersionId = identityConverter.codeSystemVersionNameToOntologyVersionId(codeSystemVersionName);
		
		return this.getEntitiesOfCodeSystemVersionDirectoryBuilder(
						ontologyId, 
						ontologyVersionId, 
						codeSystemName,
						codeSystemVersionName).restrict(filterComponent).count();
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
			final String ontologyId, 
			final String ontologyVersionId,
			final String codeSystemName,
			final String codeSystemVersionName){
		return  new EntityDirectoryBuilder(new Callback<EntityDirectoryEntry>(){

			public DirectoryResult<EntityDirectoryEntry> execute(
					ResolvedFilter filterComponent,
					float minScore,
					int start, 
					int maxResults) {
				
				Page bioportalPage = getPageForStartAndMax(start, maxResults);
			
				String xml = bioportalRestService.searchEntitiesByOntologyId(
						ontologyId, 
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
				
				String xml = bioportalRestService.searchEntitiesByOntologyId(
						ontologyId,
						filterComponent,
						bioportalPage);
				
				return getCount(xml);
			}

			public DirectoryResult<EntityDirectoryEntry> execute(
					int start,
					int maxResults) {
				
				Page bioportalPage = getPageForStartAndMax(start, maxResults);
				
				String xml = bioportalRestService.getAllEntitiesByOntologyVersionId(
						ontologyVersionId, 
						bioportalPage);
				
				DirectoryResult<EntityDirectoryEntry> directoryResult = 
						entityDescriptionTransform.transformEntityDirectory(
								xml,
								codeSystemName,
								codeSystemVersionName);
					
				return directoryResult;
			}

			public int executeCount() {
				Page bioportalPage = new Page();
				bioportalPage.setPage(0);
				bioportalPage.setMaxToReturn(1);
				
				String xml = bioportalRestService.
					getAllEntitiesByOntologyVersionId(ontologyVersionId, bioportalPage);
				
				return getCount(xml);
			}
		},
		getKnownMatchAlgorithmReferences());
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
					
					String xml = bioportalRestService.searchEntitiesOfLatestOntologyVersions(
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
					
					String xml = bioportalRestService.searchEntitiesOfLatestOntologyVersions(
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
			getKnownMatchAlgorithmReferences());
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
			Query query, 
			Set<ResolvedFilter> filterComponent,
			EntityDescriptionQueryServiceRestrictions restrictions, 
			Page page) {
		
		if(StringUtils.isNotBlank(restrictions.getCodesystem()) &&
				StringUtils.isNotBlank(restrictions.getCodesystemversion())){
			return this.getEntityDescriptionsOfCodeSystemVersion(
					query, 
					filterComponent, 
					page,
					restrictions.getCodesystem(),
					restrictions.getCodesystemversion());
		} else {
			return this.getAllEntityDescriptions(
					query,
					filterComponent,
					page);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<EntityDescription> getResourceList(
			Query query,
			Set<ResolvedFilter> filterComponent,
			EntityDescriptionQueryServiceRestrictions restrictions, 
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(
			Query query, 
			Set<ResolvedFilter> filterComponent,
			EntityDescriptionQueryServiceRestrictions restrictions) {
		throw new UnsupportedOperationException();
	}
}
