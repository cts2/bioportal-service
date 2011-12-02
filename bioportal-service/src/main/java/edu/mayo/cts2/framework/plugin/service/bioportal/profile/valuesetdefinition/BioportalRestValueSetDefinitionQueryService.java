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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valuesetdefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ValueSetDefinitionDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;
import edu.mayo.cts2.framework.service.command.restriction.ValueSetDefinitionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService;

/**
 * The Class BioportalRestValueSetDefinitionService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetDefinitionQueryService 
	extends AbstractBioportalRestQueryService<
		edu.mayo.cts2.framework.model.service.valuesetdefinition.ValueSetDefinitionQueryService>
	implements ValueSetDefinitionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetDefinitionTransform valueSetDefinitionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.ValueSetDefinitionService#getValueSetDefinitions(org.cts2.rest.service.command.Page)
	 */
	/**
	 * Gets the value set definitions.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @return the value set definitions
	 */
	private DirectoryResult<ValueSetDefinitionDirectoryEntry> getValueSetDefinitions(
			Query query,
			Set<ResolvedFilter> filterComponent,
			Page page) {	
		
		String xml = this.bioportalRestService.getLatestViews();
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformResourceVersions(xml),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedModelAttributes()
					);
	
		return builder.restrict(query).
				restrict(filterComponent).
				addStart(page.getStart()).
				addMaxToReturn(page.getMaxToReturn()).
				resolve();	
	}
	
	/* (non-Javadoc)
	 * @see org.cts2.rest.service.ValueSetDefinitionService#getValueSetDefinitionsOfCodeSystem(org.cts2.rest.service.command.Page, java.lang.String)
	 */
	/**
	 * Gets the value set definitions of value set.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @param valueSetName the value set name
	 * @return the value set definitions of value set
	 */
	private DirectoryResult<ValueSetDefinitionDirectoryEntry> getValueSetDefinitionsOfValueSet(
			Query query,
			Set<ResolvedFilter> filterComponent,
			Page page,
			String valueSetName) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(valueSetName);
		
		String xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);

		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformVersionsOfResource(xml),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedModelAttributes()
					);
	
		return builder.restrict(query).
				restrict(filterComponent).
				addStart(page.getStart()).
				addMaxToReturn(page.getMaxToReturn()).
				resolve();	
	}

	/**
	 * Gets the value set definitions of value set count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param valueSetName the value set name
	 * @return the value set definitions of value set count
	 */
	private int getValueSetDefinitionsOfValueSetCount(
			Query query,
			Set<ResolvedFilter> filterComponent, 
			String valueSetName) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(valueSetName);
		
		String xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
				this.valueSetDefinitionTransform.transformVersionsOfResource(xml),
				this.getSupportedMatchAlgorithms(),
				this.getSupportedModelAttributes()
				);
		
		return builder.restrict(query).
				restrict(filterComponent).
				count();	
	}
	
	/**
	 * Gets the value set definitions count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @return the value set definitions count
	 */
	private int getValueSetDefinitionsCount(
			Query query,
			Set<ResolvedFilter> filterComponent) {
		String xml = this.bioportalRestService.getLatestOntologyVersions();
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformResourceVersions(xml),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedModelAttributes()
					);
	
		return builder.
				restrict(query).
				restrict(filterComponent).
				count();
	}


	public Set<ResolvableMatchAlgorithmReference> getSupportedMatchAlgorithms(){
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
	
	public Set<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>> getSupportedModelAttributes(){
		Set<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>> returnSet =
			new HashSet<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>>();
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refName = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_NAME.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getResourceName());
						}
					});
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refAbout = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.ABOUT.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getAbout());
						}
					});
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refSynopsis = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_SYNOPSIS.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(ModelUtils.getResourceSynopsisValue(modelObject));
						}
					});
		
		
		returnSet.add(refName);
		returnSet.add(refAbout);
		returnSet.add(refSynopsis);
		
		return returnSet;
	}


	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetDefinitionDirectoryEntry> getResourceSummaries(
			Query query, 
			Set<ResolvedFilter> filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions, 
			ResolvedReadContext readContext,
			Page page) {
		String valueSetName = restrictions.getValueset();
		
		if(StringUtils.isNotBlank(valueSetName)){
			return this.getValueSetDefinitionsOfValueSet(
					query, 
					filterComponent,
					page, 
					valueSetName);
		} else {
			return this.getValueSetDefinitions(
					query, 
					filterComponent,
					page);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetDefinition> getResourceList(
			Query query,
			Set<ResolvedFilter> filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions, 
			ResolvedReadContext readContext,
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(Query query,
			Set<ResolvedFilter> filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions) {
		String valueSetName = restrictions.getValueset();
		
		if(StringUtils.isNotBlank(valueSetName)){
			return this.getValueSetDefinitionsOfValueSetCount(
					query, 
					filterComponent,
					valueSetName);
		} else {
			return this.getValueSetDefinitionsCount(
					query, 
					filterComponent);
		}
	}

	@Override
	public Set<? extends PredicateReference> getSupportedProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
