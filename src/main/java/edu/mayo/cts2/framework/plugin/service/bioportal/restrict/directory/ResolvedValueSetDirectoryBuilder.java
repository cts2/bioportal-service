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
package edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import edu.mayo.cts2.framework.filter.directory.AbstractRemovingDirectoryBuilder;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvablePropertyReference;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.service.command.restriction.ResolvedValueSetQueryServiceRestrictions;

/**
 * The Class CodeSystemVersionDirectoryBuilder.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ResolvedValueSetDirectoryBuilder 
	extends AbstractRemovingDirectoryBuilder<ResolvedValueSetDirectoryEntry,ResolvedValueSetDirectoryEntry> {

	/**
	 * Instantiates a new code system version directory builder.
	 *
	 * @param allPossibleResults the all possible results
	 */
	public ResolvedValueSetDirectoryBuilder(
			List<ResolvedValueSetDirectoryEntry> allPossibleResults) {
		super(allPossibleResults);
	}

	/**
	 * Instantiates a new code system version directory builder.
	 *
	 * @param allPossibleResults the all possible results
	 * @param matchAlgorithmReferences the match algorithm references
	 * @param resolvableModelAttributeReferences the resolvable model attribute references
	 * @param resolvablePredicateReferences the resolvable predicate references
	 */
	public ResolvedValueSetDirectoryBuilder(
			List<ResolvedValueSetDirectoryEntry> allPossibleResults,
			Set<ResolvableMatchAlgorithmReference> matchAlgorithmReferences,
			Set<ResolvablePropertyReference<ResolvedValueSetDirectoryEntry>> resolvablePropertyReference) {
		super(allPossibleResults, 
				matchAlgorithmReferences,
				resolvablePropertyReference);
	}
	
	public ResolvedValueSetDirectoryBuilder restrict(ResolvedValueSetQueryServiceRestrictions restrictions){
		if(restrictions != null){
			Set<NameOrURI> valueSets = restrictions.getValueSets();
			
			if(CollectionUtils.isNotEmpty(valueSets)){
				this.addRestriction(new ValueSetRestriction(valueSets));
			}
		}
		
		return this;
	}

	private class ValueSetRestriction implements Restriction<ResolvedValueSetDirectoryEntry> {

		private Set<String> valueSets;
		
		/**
		 * Instantiates a new code system restriction.
		 *
		 * @param codeSystems the code systems
		 */
		public ValueSetRestriction(Set<NameOrURI> valueSets){
			this.valueSets = new HashSet<String>();
			
			//TODO: Doesn't deal with URIs - only names
			for(NameOrURI nameOrURI : valueSets){
				this.valueSets.add(nameOrURI.getName());
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.mayo.cts2.framework.filter.directory.AbstractDirectoryBuilder.Restriction#passRestriction(java.lang.Object)
		 */
		public boolean passRestriction(ResolvedValueSetDirectoryEntry candidate) {
			String valueSetName = candidate.getResolvedHeader().getResolutionOf().getValueSet().getContent();
			
			return this.valueSets.contains(valueSetName);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.filter.directory.AbstractRemovingDirectoryBuilder#transformResults(java.util.List)
	 */
	@Override
	protected List<ResolvedValueSetDirectoryEntry> transformResults(
			List<ResolvedValueSetDirectoryEntry> results) {
		return results;
	}
}
