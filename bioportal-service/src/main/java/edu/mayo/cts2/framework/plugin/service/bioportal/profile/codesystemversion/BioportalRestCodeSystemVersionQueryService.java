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
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.filter.match.ResolvablePredicateReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.util.RestModelUtils;
import edu.mayo.cts2.framework.service.command.Page;
import edu.mayo.cts2.framework.service.command.restriction.CodeSystemVersionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQueryService;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
import edu.mayo.cts2.framework.model.core.FilterComponent;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.Property;
import edu.mayo.cts2.framework.model.core.StatementTarget;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.CodeSystemVersionDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.CodeSystemVersionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.BioportalConstants;

/**
 * The Class BioportalRestCodeSystemVersionQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestCodeSystemVersionQueryService 
	extends AbstractBioportalRestQueryService<edu.mayo.cts2.framework.model.service.codesystemversion.CodeSystemVersionQueryService>
	implements CodeSystemVersionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private CodeSystemVersionTransform codeSystemVersionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	protected List<ResolvableMatchAlgorithmReference> getKnownMatchAlgorithmReferences(){
		List<ResolvableMatchAlgorithmReference> returnList = new ArrayList<ResolvableMatchAlgorithmReference>();
		
		MatchAlgorithmReference exactMatch = 
			StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		
		returnList.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(exactMatch, new ExactMatcher()));
		
		MatchAlgorithmReference contains = 
			StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		
		returnList.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(contains, new ContainsMatcher()));
		
		return returnList;
	}
	
	protected List<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>> getKnownPredicateReferences(){
		List<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>> returnList =
			new ArrayList<ResolvablePredicateReference<CodeSystemVersionCatalogEntry>>();
		
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
		
		returnList.add(ref);
		
		return returnList;
	}
	
	protected List<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>> getKnownModelAttributeReferences(){
		List<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>> returnList =
			new ArrayList<ResolvableModelAttributeReference<CodeSystemVersionCatalogEntrySummary>>();
		
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
							return Arrays.asList(RestModelUtils.getResourceSynopsisValue(modelObject));
						}
					});
		
		
		returnList.add(refName);
		returnList.add(refAbout);
		returnList.add(refSynopsis);
		
		return returnList;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerPredicateReferences()
	 */
	@Override
	protected List<? extends PredicateReference> getAvailablePredicateReferences() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerMatchAlgorithmReferences()
	 */
	@Override
	protected List<? extends MatchAlgorithmReference> getAvailableMatchAlgorithmReferences() {
		return this.getKnownMatchAlgorithmReferences();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerModelAttributeReferences()
	 */
	@Override
	protected List<? extends ModelAttributeReference> getAvailableModelAttributeReferences() {
		return this.getKnownModelAttributeReferences();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPropertyReference(String nameOrUri) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.sdk.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemVersionCatalogEntrySummary> getResourceSummaries(
			Query query, 
			FilterComponent filterComponent,
			CodeSystemVersionQueryServiceRestrictions restrictions, 
			Page page) {
		String xml;
		
		if(StringUtils.isNotBlank(restrictions.getCodesystem())){
			String ontologyId = this.identityConverter.codeSystemNameToOntologyId(restrictions.getCodesystem());
			xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);
		} else {
			xml = this.bioportalRestService.getLatestOntologyVersions();
		}
	
		CodeSystemVersionDirectoryBuilder builder = new CodeSystemVersionDirectoryBuilder(
				this.codeSystemVersionTransform.transformVersionsOfResource(xml),
				this.getKnownMatchAlgorithmReferences(),
				this.getKnownModelAttributeReferences(),
				null
				);
		
		return builder.restrict(filterComponent).resolve();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.sdk.service.command.Page)
	 */
	@Override
	public DirectoryResult<CodeSystemVersionCatalogEntry> getResourceList(
			Query query, FilterComponent filterComponent,
			CodeSystemVersionQueryServiceRestrictions restrictions, Page page) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(Query query, FilterComponent filterComponent,
			CodeSystemVersionQueryServiceRestrictions restrictions) {
	String xml = this.bioportalRestService.getLatestOntologyVersions();
		
		CodeSystemVersionDirectoryBuilder builder = new CodeSystemVersionDirectoryBuilder(
					this.codeSystemVersionTransform.transformResourceVersions(xml),
					this.getKnownMatchAlgorithmReferences(),
					this.getKnownModelAttributeReferences(),
					null
					);
	
		return builder.restrict(filterComponent).count();
	}
}
