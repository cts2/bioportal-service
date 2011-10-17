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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.entity.EntityDescription;
import edu.mayo.cts2.framework.model.util.RestModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.EntityDescriptionTransform;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionName;

/**
 * The Class BioportalRestEntityDescriptionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestEntityDescriptionReadService 
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.entitydescription.EntityDescriptionReadService>
	implements EntityDescriptionReadService {

	@Resource
	private BioportalRestService bioportalRestService;

	@Resource
	private EntityDescriptionTransform entityDescriptionTransform;

	@Resource
	private IdentityConverter identityConverter;

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
	public EntityDescription read(EntityDescriptionName id) {
		String codeSystemVersionName = id.getCodeSystemVersionName();
		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(codeSystemVersionName);
		
		ScopedEntityName entityName = id.getResourceId();
		
		String ontologyId = this.identityConverter
				.codeSystemNameToOntologyId(codeSystemName);

		String xml = this.bioportalRestService
				.getEntityByOntologyIdAndEntityId(
						ontologyId,
						entityName.getName());

		return RestModelUtils.toEntityDescription(
				entityDescriptionTransform.transformEntityDescription(xml,
					codeSystemName, codeSystemVersionName));
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(EntityDescriptionName identifier) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityDescription readByUri(String uri) {
		throw new UnsupportedOperationException();
	}
}
