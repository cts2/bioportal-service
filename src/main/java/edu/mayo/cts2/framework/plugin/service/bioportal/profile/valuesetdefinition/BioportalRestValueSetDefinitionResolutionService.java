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

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.*;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.resolvedvalueset.BioportalRestResolvedValueSetResolutionService;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.name.ResolvedValueSetReadId;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class BioportalRdfResolvedValueSetResolutionService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestValueSetDefinitionResolutionService extends AbstractBioportalRestService 
	implements ValueSetDefinitionResolutionService {
	
	@Resource
	private IdentityConverter identityConverter;

	@Resource
	private BioportalRestResolvedValueSetResolutionService bioportalRestResolvedValueSetResolutionService;
	
	@Override
	public Set<? extends ComponentReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}

	@Override
	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
		HashSet<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();
		
		returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
		returnSet.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());

		return returnSet;
	}

	@Override
	public Set<? extends ComponentReference> getSupportedSearchReferences() {
		HashSet<ComponentReference> returnSet = new HashSet<ComponentReference>();

		returnSet.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference());
		
		return returnSet;
	}

	@Override
	public ResolvedValueSetResult<URIAndEntityName> resolveDefinition(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag,
			SortCriteria sortCriteria, ResolvedReadContext readContext,
			Page page) {

		String valueSetDefinitionName = definitionId.getName();
		
		IdentityConverter.AcronymAndSubmissionId localId =
			this.identityConverter.versionNameToAcronymAndSubmissionId(valueSetDefinitionName);
		
		ResolvedValueSetReadId id = new ResolvedValueSetReadId(
				localId.getSubmissionId(),
				definitionId.getValueSet(), 
				definitionId);
		
		return bioportalRestResolvedValueSetResolutionService.
			getResolution(
					id,
					// TODO: Check for correctness - DEEPAK
					//query != null ? query.getFilterComponent() : null,
					null,
					page);
	}



	@Override
	public ResolvedValueSet resolveDefinitionAsCompleteSet(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag,
			SortCriteria sortCriteria, ResolvedReadContext readContext) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResolvedValueSetResult<EntityDirectoryEntry> resolveDefinitionAsEntityDirectory(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag,
			ResolvedValueSetResolutionEntityQuery query,
			SortCriteria sortCriteria, ResolvedReadContext readContext,
			Page page) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
