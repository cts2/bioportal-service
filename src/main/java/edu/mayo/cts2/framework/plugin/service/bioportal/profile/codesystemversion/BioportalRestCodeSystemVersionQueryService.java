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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion;

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
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.CodeSystemVersionDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.CodeSystemVersionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;
import edu.mayo.cts2.framework.service.command.restriction.CodeSystemVersionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQuery;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQueryService;

/**
 * The Class BioportalRestCodeSystemVersionQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestCodeSystemVersionQueryService 
	extends AbstractBioportalRestQueryService<
		edu.mayo.cts2.framework.model.service.codesystemversion.CodeSystemVersionQueryService>
	implements CodeSystemVersionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private CodeSystemVersionTransform codeSystemVersionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

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
	
	public Set<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>> getSupportedProperties(){
		Set<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>> returnSet =
			new HashSet<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>>();
		
		ResolvablePredicateReference<CodeSystemVersionCatalogEntry> ref = 
			new ResolvablePredicateReference<CodeSystemVersionCatalogEntry>(
					
					new AttributeResolver<CodeSystemVersionCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								CodeSystemVersionCatalogEntry modelObject) {
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
	
	public Set<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>> getSupportedModelAttributes(){
		Set<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>> returnSet =
			new HashSet<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>>();
		
		ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary> refName = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_NAME.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemVersionCatalogEntrySummary>(){

						public Iterable<String> resolveAttribute(
								CodeSystemVersionCatalogEntrySummary modelObject) {
							return Arrays.asList(modelObject.getCodeSystemVersionName());
						}
					});
		
		ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary> refAbout = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.ABOUT.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemVersionCatalogEntrySummary>(){

						public Iterable<String> resolveAttribute(
								CodeSystemVersionCatalogEntrySummary modelObject) {
							return Arrays.asList(modelObject.getAbout());
						}
					});
		
		ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary> refSynopsis = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_SYNOPSIS.getModelAttributeReference(), 
					new AttributeResolver<CodeSystemVersionCatalogEntrySummary>(){

						public Iterable<String> resolveAttribute(
								CodeSystemVersionCatalogEntrySummary modelObject) {
							return Arrays.asList(ModelUtils.getResourceSynopsisValue(modelObject));
						}
					});
		
		
		returnSet.add(refName);
		returnSet.add(refAbout);
		returnSet.add(refSynopsis);
		
		return returnSet;
	}
	

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPropertyReference(String nameOrUri) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemVersionCatalogEntrySummary> getResourceSummaries(
			CodeSystemVersionQuery query, 
			SortCriteria sort,
			Page page) {
		
		CodeSystemVersionQueryServiceRestrictions restrictions = query.getRestrictions();
		
		String xml;
		
		if(restrictions != null && restrictions.getCodeSystem() != null){
			//TODO: This does not resolve the URI if the restriction is a CodeSystemURI
			String ontologyId = 
					this.identityConverter.codeSystemNameToOntologyId(
							restrictions.getCodeSystem().getName());
			xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);
		} else {
			xml = this.bioportalRestService.getLatestOntologyVersions();
		}
	
		CodeSystemVersionDirectoryBuilder builder = new CodeSystemVersionDirectoryBuilder(
				this.codeSystemVersionTransform.transformVersionsOfResource(xml),
				this.getSupportedMatchAlgorithms(),
				this.getSupportedModelAttributes(),
				null
				);
		
		return builder.
				restrict(query.getFilterComponent()).
				addMaxToReturn(page.getMaxToReturn()).
				addStart(page.getStart()).
				resolve();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemVersionCatalogEntry> getResourceList(
			CodeSystemVersionQuery query, 
			SortCriteria sort,
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(CodeSystemVersionQuery query) {
	String xml = this.bioportalRestService.getLatestOntologyVersions();
		
		CodeSystemVersionDirectoryBuilder builder = new CodeSystemVersionDirectoryBuilder(
					this.codeSystemVersionTransform.transformResourceVersions(xml),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedModelAttributes(),
					null
					);
	
		return builder.restrict(query.getFilterComponent()).count();
	}
}
