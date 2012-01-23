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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.filter.match.ResolvablePredicateReference;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntry;
import edu.mayo.cts2.framework.model.codesystem.CodeSystemCatalogEntrySummary;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.exception.ExceptionFactory;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.CodeSystemDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.CodeSystemTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.ResourceQuery;
import edu.mayo.cts2.framework.service.profile.codesystem.CodeSystemQueryService;

/**
 * The Class BioportalRestCodeSystemQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestCodeSystemQueryService 
	extends AbstractBioportalRestQueryService<
		edu.mayo.cts2.framework.model.service.codesystem.CodeSystemQueryService>
	implements CodeSystemQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private CodeSystemTransform codeSystemTransform;

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.CodeSystemService#getCodeSystemById(java.lang.String)
	 */
	private List<CodeSystemCatalogEntry> getAllCodeSystemCatalogEntries() {
			
		String xml = this.bioportalRestService.getLatestOntologyVersions();

		return this.codeSystemTransform.transformResources(xml);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPredicateReference(String nameOrUri) {
		for(PredicateReference ref : this.getSupportedProperties()){
			if(ref.getName().equals(nameOrUri)){
				return ref;
			}
		}
		
		throw ExceptionFactory.createUnsupportedPredicateReference(nameOrUri);
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
	
	/**
	 * Gets the known predicate references.
	 *
	 * @return the known predicate references
	 */
	public Set<ResolvablePredicateReference<CodeSystemCatalogEntry>> getSupportedProperties(){
		Set<ResolvablePredicateReference<CodeSystemCatalogEntry>> returnSet =
			new HashSet<ResolvablePredicateReference<CodeSystemCatalogEntry>>();
		
		ResolvablePredicateReference<CodeSystemCatalogEntry> ref = 
			new ResolvablePredicateReference<CodeSystemCatalogEntry>(
					
					new AttributeResolver<CodeSystemCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemCatalogEntry modelObject) {
							List<String> returnList = new ArrayList<String>();
							for(Property prop : modelObject.getProperty()){
								if(prop.getPredicate().getName().equals(
										BioportalConstants.BIOPORTAL_ONTOLOGY_ID_NAME)){
									for(StatementTarget value : prop.getValue()){
										String content = 
											value.getLiteral().getValue().getContent();
										
										returnList.add(content);
									}
								}
							}
							
							return returnList;
						}
					});
		
		ref.setName(BioportalConstants.BIOPORTAL_ONTOLOGY_ID_NAME);
		ref.setUri(BioportalConstants.BIOPORTAL_ONTOLOGY_ID_ABOUT);
		ref.setNamespace(BioportalConstants.BIOPORTAL_NAMESPACE_NAME);
		
		returnSet.add(ref);
		
		return returnSet;
	}
	
	public Set<ResolvableModelAttributeReference<CodeSystemCatalogEntry>> getSupportedModelAttributes(){
		Set<ResolvableModelAttributeReference<CodeSystemCatalogEntry>> returnSet =
			new HashSet<ResolvableModelAttributeReference<CodeSystemCatalogEntry>>();
		
		ResolvableModelAttributeReference<CodeSystemCatalogEntry> refName = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_NAME.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemCatalogEntry modelObject) {
							return Arrays.asList( 
								modelObject.getCodeSystemName());
						}
					});
		
		ResolvableModelAttributeReference<CodeSystemCatalogEntry> refAbout = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.ABOUT.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemCatalogEntry modelObject) {
							return Arrays.asList( 
								modelObject.getAbout());
						}
					});
		
		ResolvableModelAttributeReference<CodeSystemCatalogEntry> refSynopsis = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_SYNOPSIS.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemCatalogEntry modelObject) {
							return Arrays.asList(ModelUtils.getResourceSynopsisValue(
									modelObject));
						}
					});
		
		ResolvableModelAttributeReference<CodeSystemCatalogEntry> keyword = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.KEYWORD.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemCatalogEntry modelObject) {
							
							return Arrays.asList(
									modelObject.getKeyword());
						}
					});
		
		
		returnSet.add(refName);
		returnSet.add(refAbout);
		returnSet.add(refSynopsis);
		returnSet.add(keyword);
		
		return returnSet;
	}



	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemCatalogEntrySummary> getResourceSummaries(
			ResourceQuery query, 
			SortCriteria sortCriteria,
			Page page) {
		
		CodeSystemDirectoryBuilder builder = 
			new CodeSystemDirectoryBuilder(
					this.codeSystemTransform,
					this.getAllCodeSystemCatalogEntries(),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedModelAttributes(),
					this.getSupportedProperties()
					);

		return builder.restrict(query.getFilterComponent()).
			addStart(page.getStart()).
			addMaxToReturn(page.getMaxToReturn()).resolve();
	}



	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemCatalogEntry> getResourceList(
			ResourceQuery query, 
			SortCriteria sortCriteria,
			Page page) {
		throw new UnsupportedOperationException();
	}



	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(ResourceQuery query) {
		throw new UnsupportedOperationException();
	}
}