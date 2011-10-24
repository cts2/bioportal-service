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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.codesystemversion;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.core.ReadContext;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.CodeSystemVersionTransform;
import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionReadService;

/**
 * The Class BioportalRestCodeSystemVersionReadService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestCodeSystemVersionReadService 
	extends AbstractBioportalRestService<edu.mayo.cts2.framework.model.service.codesystemversion.CodeSystemVersionReadService>
	implements CodeSystemVersionReadService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private CodeSystemVersionTransform codeSystemVersionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#read(java.lang.Object)
	 */
	@Override
	public CodeSystemVersionCatalogEntry read(NameOrURI codeSystemVersionName, ReadContext readContext) {
		String ontologyVersionId = 
			this.identityConverter.codeSystemVersionNameToOntologyVersionId(
					codeSystemVersionName.getName());
		
		String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);

		return this.codeSystemVersionTransform.transformResourceVersion(xml);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.ReadService#exists(java.lang.Object)
	 */
	@Override
	public boolean exists(NameOrURI codeSystemVersion, ReadContext readContext) {
		String ontologyVersionId = this.identityConverter
				.codeSystemVersionNameToOntologyVersionId(codeSystemVersion.getName());

		try {
			this.bioportalRestService
					.getOntologyByOntologyVersionId(ontologyVersionId);
		} catch (RestClientException ex) {
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionReadService#existsCodeSystemVersionForCodeSystem(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean existsCodeSystemVersionForCodeSystem(NameOrURI codeSystem,
			String tagName) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionReadService#getCodeSystemVersionForCodeSystem(java.lang.String, java.lang.String)
	 */
	@Override
	public CodeSystemVersionCatalogEntry getCodeSystemVersionForCodeSystem(
			NameOrURI codeSystem, String tagName, ReadContext readContext) {
		throw new UnsupportedOperationException();
	}


	@Override
	public CodeSystemVersionCatalogEntry getCodeSystemVersionForCodeSystem(
			NameOrURI codeSystem, String tagName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean existsVersionId(NameOrURI codeSystem,
			String officialResourceVersionId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CodeSystemVersionCatalogEntry getCodeSystemByVersionId(
			NameOrURI codeSystemName, 
			String officialResourceVersionId,
			ReadContext readContext) {
		String codeSystemVersionName = 
				this.identityConverter.codeSystemNameAndVersionIdToCodeSystemVersionName(
						codeSystemName.getName(), officialResourceVersionId);
		
		return this.read(ModelUtils.nameOrUriFromName(codeSystemVersionName), readContext);
	}

}
