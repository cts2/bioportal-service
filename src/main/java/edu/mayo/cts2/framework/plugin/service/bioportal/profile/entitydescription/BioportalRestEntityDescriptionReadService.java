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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.*;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.entity.EntityList;
import edu.mayo.cts2.framework.model.entity.EntityListEntry;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.util.EntityResolver;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * The Class BioportalRestEntityDescriptionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestEntityDescriptionReadService 
	extends AbstractBioportalRestService
	implements EntityDescriptionReadService {

	@Resource
	private BioportalRestService bioportalRestService;

	@Resource
	private EntityDescriptionTransform entityDescriptionTransform;

	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private EntityResolver entityResolver;

	/**
	 * Gets the entity description name from uri.
	 *
	 * @param uri the uri
	 * @return the entity description name from uri
	 */
	public String getEntityDescriptionNameFromUri(String uri) {
		return this.bioportalRestService.getEntityByUri(uri);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#read(java.lang.Object)
	 */
	@Override
	public EntityDescription read(EntityDescriptionReadId id, ResolvedReadContext readContext) {
		String codeSystemVersionName = id.getCodeSystemVersion().getName();
		IdentityConverter.AcronymAndSubmissionId versionId =
                this.identityConverter.versionNameToAcronymAndSubmissionId(codeSystemVersionName);
		
		ScopedEntityName entityName = id.getEntityName();
		
		String xml;
		
		if(entityName != null){
			xml = this.entityResolver.getEntityXml(
					entityName,
                    versionId.getAcronym());
		} else {
			xml = this.entityResolver.getEntityXml(
					id.getUri(),
                    versionId.getAcronym());
        }

		if(StringUtils.isBlank(xml)){
			return null;
		} else  {
			return ModelUtils.toEntityDescription(
				entityDescriptionTransform.transformEntityDescription(xml,
                        versionId.getAcronym(), codeSystemVersionName));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(EntityDescriptionReadId identifier, ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityList readEntityDescriptions(
			EntityNameOrURI entityId,
			ResolvedReadContext readContext) {
		throw new UnsupportedOperationException("Lookup by URI is currently not supported without specifying a CodeSystemVersion.");
	}

	@Override
	public List<CodeSystemReference> getKnownCodeSystems() {
		return null;
	}

	@Override
	public List<CodeSystemVersionReference> getKnownCodeSystemVersions() {
		return null;
	}

	@Override
	public List<VersionTagReference> getSupportedVersionTags() {
		return null;
	}

	@Override
	public EntityReference availableDescriptions(EntityNameOrURI entityId,
			ResolvedReadContext readContext) {
		throw new UnsupportedOperationException("Lookup by URI is currently not supported without specifying a CodeSystemVersion.");
	}

	@Override
	public DirectoryResult<EntityListEntry> readEntityDescriptions(
			EntityNameOrURI entityId, SortCriteria sortCriteria,
			ResolvedReadContext readContext, Page page) {
		throw new UnsupportedOperationException("Lookup by URI is currently not supported without specifying a CodeSystemVersion.");
	}

}
