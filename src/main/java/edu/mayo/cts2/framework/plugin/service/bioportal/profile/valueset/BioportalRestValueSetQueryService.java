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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valueset;

import edu.mayo.cts2.framework.filter.match.*;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ValueSetDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ValueSetDirectoryBuilder.ValueSetCodeSystemExtractor;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetTransform;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class BioportalRestValueSetService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetQueryService 
	extends AbstractBioportalRestService
	implements ValueSetQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetTransform valueSetTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPredicateReference(String nameOrUri) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<ValueSetCatalogEntry> getAllValueSetCatalogEntries() {
		
		String xml = this.bioportalRestService.getLatestOntologySubmissions(true);

		return this.valueSetTransform.transformResources(xml);
	}

	/**
	 * Gets the value set by name.
	 *
	 * @param valueSetName the value set name
	 * @return the value set by name
	 */
	public ValueSetCatalogEntry getValueSetByName(String valueSetName) {
		String xml = this.bioportalRestService.getOntologyByAcronym(valueSetName);

		return this.valueSetTransform.transformResource(xml);
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
	
	public Set<ResolvablePropertyReference<ValueSetCatalogEntry>> getSupportedSearchReferences(){
		Set<ResolvablePropertyReference<ValueSetCatalogEntry>> returnSet =
			new HashSet<ResolvablePropertyReference<ValueSetCatalogEntry>>();
		
		ResolvablePropertyReference<ValueSetCatalogEntry> refName = 
			ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference(), 
					new AttributeResolver<ValueSetCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetCatalogEntry modelObject) {
							return Arrays.asList( 
								modelObject.getValueSetName());
						}
					});
		
		ResolvablePropertyReference<ValueSetCatalogEntry> refAbout = 
			ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.ABOUT.getPropertyReference(), 
					new AttributeResolver<ValueSetCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetCatalogEntry modelObject) {
							return Arrays.asList( 
								modelObject.getAbout());
						}
					});
		
		ResolvablePropertyReference<ValueSetCatalogEntry> refSynopsis = 
			ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference(), 
					new AttributeResolver<ValueSetCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetCatalogEntry modelObject) {
							return Arrays.asList(ModelUtils.getResourceSynopsisValue(
									modelObject));
						}
					});
		
		ResolvablePropertyReference<ValueSetCatalogEntry> keyword = 
			ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.KEYWORD.getPropertyReference(), 
					new AttributeResolver<ValueSetCatalogEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetCatalogEntry modelObject) {
							
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
	
	/**
	 * The Class CodeSystemExtractor.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private class CodeSystemExtractor implements ValueSetCodeSystemExtractor {

		/* (non-Javadoc)
		 * @see edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ValueSetDirectoryBuilder.ValueSetCodeSystemExtractor#getCodeSystemsOfValueSet(java.lang.String)
		 */
		public Set<String> getCodeSystemsOfValueSet(String valueSetName) {
			Set<String> returnSet = new HashSet<String>();

			return returnSet;
		}
	}


	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetCatalogEntrySummary> getResourceSummaries(
			ValueSetQuery query, 
			SortCriteria sort,
			Page page) {
		ValueSetDirectoryBuilder builder = 
				new ValueSetDirectoryBuilder(
						this.valueSetTransform,
						this.getAllValueSetCatalogEntries(),
						new CodeSystemExtractor(), 
						this.getSupportedMatchAlgorithms(),
						this.getSupportedSearchReferences());

			return builder.
					restrict(query.getRestrictions()).
					restrict(query.getFilterComponent()).
					addStart(page.getStart()).
					addMaxToReturn(page.getMaxToReturn()).
					restrict(query.getQuery()).
					resolve();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetCatalogEntry> getResourceList(
			ValueSetQuery query, 
			SortCriteria sort,
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(
			ValueSetQuery query) {
		
			ValueSetDirectoryBuilder builder = 
				new ValueSetDirectoryBuilder(
						this.valueSetTransform,
						this.getAllValueSetCatalogEntries(),
						new CodeSystemExtractor(), 
						this.getSupportedMatchAlgorithms(),
						this.getSupportedSearchReferences()
						);

			return builder.
					restrict(query.getRestrictions()).
					restrict(query.getFilterComponent()).
					restrict(query.getQuery()).
					count();
	}

	@Override
	public Set<? extends PropertyReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}

}
