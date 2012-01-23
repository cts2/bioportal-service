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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valuesetesolution;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.service.core.ReadContext;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import edu.mayo.cts2.framework.service.profile.valuesetresolution.ResolvedValueSetReadService;
import edu.mayo.cts2.framework.service.profile.valuesetresolution.name.ResolvedValueSetReadId;

/**
 * The Class BioportalRestValueSetDefinitionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetDefinitionResolutionReadService
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.valuesetdefinition.ValueSetDefinitionResolution>
	implements ResolvedValueSetReadService {



	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetDefinitionTransform valueSetDefinitionTransform;
	
	@Resource
	private IdentityConverter identityConverter;
	
	@Override
	public ResolvedValueSet read(ResolvedValueSetReadId identifier) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#read(java.lang.Object)
	 
	@Override
	public LocalIdValueSetDefinition read(ValueSetDefinitionReadId id, ResolvedReadContext readContext) {
		String ontologyVersionId=null;
		if (StringUtils.isNotBlank(id.getName())) {
			 ontologyVersionId= id.getName();
		} else {
		  ontologyVersionId = 
			this.identityConverter.valueSetDefinitionNameToOntologyVersionId(
					id.getUri());
		}
		
		String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);

		return new LocalIdValueSetDefinition(this.valueSetDefinitionTransform.transformResourceVersion(xml));
	}
*/

}
