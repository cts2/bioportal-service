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

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;

/**
 * The Class BioportalRestValueSetDefinitionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetDefinitionResolutionService
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.valuesetdefinition.ValueSetDefinitionReadService>
	implements ValueSetDefinitionResolutionService {



	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private EntityDescriptionTransform entityDescriptionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	@Override
	public DirectoryResult<ResolvedValueSetDirectoryEntry> resolveDefinitionAsDirectory(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag, Query query,
			Set<ResolvedFilter> filterComponent,
			ResolvedReadContext readContext, Page page) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public DirectoryResult<EntityDirectoryEntry> resolveDefinitionAsEntityDirectory(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag, Query query,
			Set<ResolvedFilter> filterComponent,
			EntityDescriptionQueryServiceRestrictions restrictions,
			ResolvedReadContext readContext, Page page) {
		// TODO Auto-generated method stub
		String ontologyVersionId= definitionId.getName();
		String valueSetName = definitionId.getValueSet().getName();
		String ontologyId= this.identityConverter.valueSetNameToOntologyId(valueSetName);

		String codeSystemName=this.identityConverter.ontologyIdToValueSetName(ontologyId);
		String codeSystemVersionName= this.identityConverter.ontologyVersionIdToCodeSystemVersionName(ontologyId, ontologyVersionId);
		String xml = bioportalRestService.getAllEntitiesByOntologyVersionId(
				ontologyVersionId, 
				page);
		return this.entityDescriptionTransform.transformEntityDirectory(
				xml, 
				codeSystemName, 
				codeSystemVersionName);

		
	}

	@Override
	public ResolvedValueSet resolveDefinitionAsCompleteSet(
			ValueSetDefinitionReadId definitionId,
			Set<NameOrURI> codeSystemVersions, NameOrURI tag,
			ResolvedReadContext readContext) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
