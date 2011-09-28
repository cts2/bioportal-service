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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import edu.mayo.cts2.framework.filter.directory.AbstractRemovingDirectoryBuilder;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetTransform;
import edu.mayo.cts2.framework.service.command.restriction.ValueSetQueryServiceRestrictions;
import edu.mayo.cts2.framework.model.core.FilterComponent;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary;

/**
 * The Class ValueSetDirectoryBuilder.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ValueSetDirectoryBuilder extends AbstractRemovingDirectoryBuilder<ValueSetCatalogEntry,ValueSetCatalogEntrySummary> {

	private ValueSetTransform valueSetTransform;
	
	private ValueSetCodeSystemExtractor valueSetCodeSystemExtractor;

	
	/**
	 * Instantiates a new value set directory builder.
	 *
	 * @param valueSetTransform the value set transform
	 * @param allPossibleResults the all possible results
	 */
	public ValueSetDirectoryBuilder(
			ValueSetTransform valueSetTransform,
			List<ValueSetCatalogEntry> allPossibleResults) {
		super(allPossibleResults);
		this.valueSetTransform = valueSetTransform;
	}

	/**
	 * Instantiates a new value set directory builder.
	 *
	 * @param valueSetTransform the value set transform
	 * @param allPossibleResults the all possible results
	 * @param valueSetCodeSystemExtractor the value set code system extractor
	 * @param matchAlgorithmReferences the match algorithm references
	 * @param resolvableModelAttributeReferences the resolvable model attribute references
	 */
	public ValueSetDirectoryBuilder(
			ValueSetTransform valueSetTransform,
			List<ValueSetCatalogEntry> allPossibleResults,
			ValueSetCodeSystemExtractor valueSetCodeSystemExtractor,
			List<ResolvableMatchAlgorithmReference> matchAlgorithmReferences,
			List<ResolvableModelAttributeReference<ValueSetCatalogEntry>> resolvableModelAttributeReferences) {
		super(allPossibleResults, 
				matchAlgorithmReferences,
				resolvableModelAttributeReferences,
				null);
		this.valueSetTransform = valueSetTransform;
		this.valueSetCodeSystemExtractor = valueSetCodeSystemExtractor;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.filter.directory.AbstractRemovingDirectoryBuilder#transformResults(java.util.List)
	 */
	@Override
	protected List<ValueSetCatalogEntrySummary> transformResults(
			List<ValueSetCatalogEntry> results) {
		List<ValueSetCatalogEntrySummary> returnList = new ArrayList<ValueSetCatalogEntrySummary>();
		
		for(ValueSetCatalogEntry summary : results){
			returnList.add(
					this.valueSetTransform.transformResourceToSummary(summary));
		}
		
		return returnList;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.filter.directory.AbstractDirectoryBuilder#restrict(edu.mayo.cts2.framework.model.core.FilterComponent)
	 */
	@Override
	public ValueSetDirectoryBuilder restrict(
			FilterComponent filterComponent) {
		return (ValueSetDirectoryBuilder) super.restrict(filterComponent);
	}

	/**
	 * Restrict.
	 *
	 * @param restrictions the restrictions
	 * @return the value set directory builder
	 */
	public ValueSetDirectoryBuilder restrict(ValueSetQueryServiceRestrictions restrictions){
		List<String> codeSystems = restrictions.getCodesystem();
		
		if(CollectionUtils.isNotEmpty(codeSystems)){
			this.addRestriction(new CodeSystemRestriction(codeSystems));
		}
		return this;
	}
	
	/**
	 * The Interface ValueSetCodeSystemExtractor.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	public interface ValueSetCodeSystemExtractor {
		
		/**
		 * Gets the code systems of value set.
		 *
		 * @param valueSetName the value set name
		 * @return the code systems of value set
		 */
		public Set<String> getCodeSystemsOfValueSet(String valueSetName);
	}
	
	/**
	 * The Class CodeSystemRestriction.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	private class CodeSystemRestriction implements Restriction<ValueSetCatalogEntry> {

		private List<String> codeSystems;
		
		/**
		 * Instantiates a new code system restriction.
		 *
		 * @param codeSystems the code systems
		 */
		public CodeSystemRestriction(List<String> codeSystems){
			this.codeSystems = codeSystems;
		}
		
		/* (non-Javadoc)
		 * @see edu.mayo.cts2.framework.filter.directory.AbstractDirectoryBuilder.Restriction#passRestriction(java.lang.Object)
		 */
		public boolean passRestriction(ValueSetCatalogEntry candidate) {
			String valueSetName = candidate.getValueSetName();
			
			return valueSetCodeSystemExtractor.
				getCodeSystemsOfValueSet(valueSetName).containsAll(codeSystems);
		}
	}
}
