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
package edu.mayo.cts2.framework.plugin.service.bioportal.transform;

import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetDirectoryEntry;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class CodeSystemVersionTransform.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class ResolvedValueSetTransform extends AbstractOntologyTransform {

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.transform.AbstractBioportalOntologyVersionTransformTemplate#createNewResourceVersion()
	 */
	public List<ResolvedValueSetDirectoryEntry> transfrom(String xml) {
		Document doc = BioportalRestUtils.getDocument(xml);

		List<Node> nodeList = TransformUtils.getNodeListWithPath(doc, ONTOLOGY_SUBMISSION_LIST, ONTOLOGY_SUBMISSION);
	
		List<ResolvedValueSetDirectoryEntry> returnList = new ArrayList<ResolvedValueSetDirectoryEntry>();
		
		for(Node node : nodeList){
			ResolvedValueSetDirectoryEntry entry = new ResolvedValueSetDirectoryEntry();

			String submissionId = TransformUtils.getNamedChildText(node, SUBMISSION_ID);

			ResolvedValueSetHeader header;
			try {
				header = this.getHeader(node);
			} catch (Exception e) {
				//sometimes these ontologies may be removed from Bioportal.. if so, skip.
				continue;
			}
		
			entry.setResolvedHeader(header);
			
			entry.setHref(this.getUrlConstructor().createResolvedValueSetUrl(
					header.getResolutionOf().getValueSet().getContent(), 
					header.getResolutionOf().getValueSetDefinition().getContent(),
                    submissionId));
			
			entry.setResolvedValueSetURI(header.getResolutionOf().getValueSet().getUri() + "/resolution/" + submissionId);
			
			returnList.add(entry);
		}
		
		return returnList;
	}
	
	public ResolvedValueSetHeader getHeader(Node node){
        ResolvedValueSetHeader header = new ResolvedValueSetHeader();
		Node ontologyNode = TransformUtils.getNamedChild(node, ONTOLOGY);

        header.addResolvedUsingCodeSystem(
                this.getCodeSystemCurrentVersionReference(TransformUtils.getNamedChildText(ontologyNode, ACRONYM)));

        header.setResolutionOf(
                this.getValueSetCurrentVersionReference()

        return header;
	}

}
