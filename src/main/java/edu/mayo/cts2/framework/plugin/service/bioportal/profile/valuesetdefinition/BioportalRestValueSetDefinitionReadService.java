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

import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.VersionTagReference;
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * The Class BioportalRestValueSetReadService.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestValueSetDefinitionReadService extends
		AbstractBioportalRestService implements ValueSetDefinitionReadService {

	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private BioportalRestService bioportalRestService;

	@Override
	public boolean existsByTag(NameOrURI valueSet, VersionTagReference tag,
			ResolvedReadContext readContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<VersionTagReference> getSupportedTags() {
		return Arrays.asList(new VersionTagReference("CURRENT"));
	}

	/*  
	 * This is not complete -- the only use for this is to redirect
	 * to the current ValueSetDefinition.
	 */
	@Override
	public LocalIdValueSetDefinition readByTag(
			NameOrURI valueSet,
			VersionTagReference tag, 
			ResolvedReadContext readContext) {
		if (tag.getContent() == null || !tag.getContent().equals("CURRENT")) {
			throw new RuntimeException("Only 'CURRENT' tag is supported");
		}
		
		String valueSetName = valueSet.getName();

		String ontologyVersion = 
			this.bioportalRestService.getLatestOntologySubmissionByAcronym(valueSetName);
		
		Document doc = BioportalRestUtils.getDocument(ontologyVersion);

		String submissionId = TransformUtils.getNamedChildTextWithPath(
				doc, 
				"success.data.ontologyBean.id");
		
		return new LocalIdValueSetDefinition(
                submissionId,
				null);
	}

	@Override
	public boolean exists(ValueSetDefinitionReadId arg0,
			ResolvedReadContext arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LocalIdValueSetDefinition read(ValueSetDefinitionReadId arg0,
			ResolvedReadContext arg1) {
		throw new UnsupportedOperationException();
	}

}
