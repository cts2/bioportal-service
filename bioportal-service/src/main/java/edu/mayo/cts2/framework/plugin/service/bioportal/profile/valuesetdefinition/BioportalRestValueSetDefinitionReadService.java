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

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;

/**
 * The Class BioportalRestValueSetDefinitionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetDefinitionReadService
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.valuesetdefinition.ValueSetDefinitionReadService>
	implements ValueSetDefinitionReadService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetDefinitionTransform valueSetDefinitionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.ReadService#read(java.lang.Object)
	 */
	@Override
	public ValueSetDefinition read(String valueSetDefinitionDocumentUri) {
		
		String ontologyVersionId = 
			this.identityConverter.valueSetDefinitionNameToOntologyVersionId(
					valueSetDefinitionDocumentUri);
		
		String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);

		return this.valueSetDefinitionTransform.transformResourceVersion(xml);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.ReadService#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(String valueSetDefinitionDocumentUri) {
		String ontologyVersionId = this.identityConverter
				.valueSetDefinitionNameToOntologyVersionId(
						valueSetDefinitionDocumentUri);

		try {
			this.bioportalRestService
					.getOntologyByOntologyVersionId(ontologyVersionId);
		} catch (RestClientException ex) {
			return false;
		}

		return true;
	}
}
