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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.resolvedvalueset;

import edu.mayo.cts2.framework.filter.match.*;
import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ResolvedValueSetDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ResolvedValueSetTransform;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQuery;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQueryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class BioportalRestCodeSystemVersionQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestResolvedValueSetQueryService 
	extends AbstractBioportalRestService
	implements ResolvedValueSetQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ResolvedValueSetTransform resolvedValueSetTransform;

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
	
	@Override
	public Set<ResolvablePropertyReference<ResolvedValueSetDirectoryEntry>> getSupportedSearchReferences(){
		Set<ResolvablePropertyReference<ResolvedValueSetDirectoryEntry>> returnSet =
			new HashSet<ResolvablePropertyReference<ResolvedValueSetDirectoryEntry>>();
	
		ResolvablePropertyReference<ResolvedValueSetDirectoryEntry> refAbout = 
			ResolvablePropertyReference.toPropertyReference(
					StandardModelAttributeReference.ABOUT.getPropertyReference(), 
					new AttributeResolver<ResolvedValueSetDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ResolvedValueSetDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getResolvedValueSetURI());
						}
					});
		
		returnSet.add(refAbout);
		
		return returnSet;
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
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.framework.service.command.Page)
	 */
	@Override
	public DirectoryResult<ResolvedValueSetDirectoryEntry> getResourceSummaries(
			ResolvedValueSetQuery query, 
			SortCriteria sortCriteria, 
			Page page) {
		
		String xml = this.bioportalRestService.getLatestOntologySubmissions(true);
	
		ResolvedValueSetDirectoryBuilder builder = new ResolvedValueSetDirectoryBuilder(
				this.resolvedValueSetTransform.transfrom(xml),
				this.getSupportedMatchAlgorithms(),
				this.getSupportedSearchReferences());
		
		return builder.restrict(
				query.getResolvedValueSetQueryServiceRestrictions()).
				restrict(query.getFilterComponent()).
				addMaxToReturn(page.getMaxToReturn()).
				addStart(page.getStart()).
				resolve();
	}


	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(ResolvedValueSetQuery query) {
		String xml = this.bioportalRestService.getLatestOntologySubmissions(true);
		
		ResolvedValueSetDirectoryBuilder builder = new ResolvedValueSetDirectoryBuilder(
					this.resolvedValueSetTransform.transfrom(xml),
					this.getSupportedMatchAlgorithms(),
					this.getSupportedSearchReferences());
	
		return builder.restrict(
				query.getResolvedValueSetQueryServiceRestrictions()).
				restrict(query.getFilterComponent()).count();
	}
}
