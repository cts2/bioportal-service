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

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.SortCriteria;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ResolvedValueSetTransform;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQuery;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetQueryService;

/**
 * The Class BioportalRestValueSetDefinitionService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalResResolvedValueSetQueryService 
	extends AbstractBioportalRestQueryService<
		edu.mayo.cts2.framework.model.service.valuesetdefinition.ResolvedValueSetQueryService>
	implements ResolvedValueSetQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ResolvedValueSetTransform ResolvedValueSetTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	@Override
	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends ModelAttributeReference> getSupportedModelAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<? extends PredicateReference> getSupportedProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectoryResult<ResolvedValueSetDirectoryEntry> getResourceSummaries(
			ResolvedValueSetQuery query, SortCriteria sort, Page page) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
