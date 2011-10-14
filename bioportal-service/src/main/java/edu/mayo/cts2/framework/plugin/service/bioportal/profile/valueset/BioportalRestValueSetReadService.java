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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valueset;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetTransform;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService;
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;

/**
 * The Class BioportalRestValueSetReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetReadService 
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.valueset.ValueSetReadService>
	implements ValueSetReadService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetTransform valueSetTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#read(java.lang.Object)
	 */
	@Override
	public ValueSetCatalogEntry read(String valueSetName) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(valueSetName);
		
		String xml = this.bioportalRestService.getLatestOntologyVersionByOntologyId(ontologyId);

		return this.valueSetTransform.transformResource(xml);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(String identifier) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValueSetCatalogEntry readByUri(String uri) {
		throw new UnsupportedOperationException();
	}
}
