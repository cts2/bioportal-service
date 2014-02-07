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
package edu.mayo.cts2.framework.plugin.service.bioportal.identity;

import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import javax.annotation.Resource;
import java.util.List;

/**
 * The Class IdentityConverter.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class IdentityConverter {
	
	private static final Log log = LogFactory.getLog(IdentityConverter.class);

	@Resource
	private BioportalRestService bioportalRestService;

	private static final String ACRONYM = "acronym";
	private static final String SUBMISSION_ID = "submissionId";
	private static final String ONTOLOGY_ID = "ontologyId";
	private static final String DISPLAY_LABEL = "displayLabel";
	private static final String FORMAT = "format";
	private static final String VERSION = "version";
	private static final String IS_VIEW = "isView";
	
	protected static final String URN = "urn";
	protected static final String FILENAMES = "filenames";
	protected static final String CODING_SCHEME = "codingScheme";
	protected static final String ONTOLOGY_BEAN = "success.data.ontologyBean";
	protected static final String STRING = "string";

	
    public AcronymAndSubmissionId versionNameToAcronymAndSubmissionId(String versionName){
        return new AcronymAndSubmissionId(
                StringUtils.substringBeforeLast(versionName, "-"),
                StringUtils.substringAfterLast(versionName, "-"));
    }

    public String acronymAndSubmissionIdToVersionName(AcronymAndSubmissionId id){
        return this.acronymAndSubmissionIdToVersionName(id.getAcronym(), id.getSubmissionId());
    }

    public String acronymAndSubmissionIdToVersionName(String acronym, String submissionId){
        return acronym + "-" + submissionId;
    }

    public String versionNameToVersion(String versionName){
        AcronymAndSubmissionId id = this.versionNameToAcronymAndSubmissionId(versionName);

        String xml = this.bioportalRestService.getOntologySubmissionByAcronymAndSubmissionId(
                id.getAcronym(),
                id.getSubmissionId());

        return TransformUtils.getNamedChildText(BioportalRestUtils.getDocument(xml), VERSION);
    }

    public String getVersionAbout(String versionName) {
        //TODO
        return "test";
    }

    public String getAbout(String acronym) {
        String xml = this.bioportalRestService.getOntologyByAcronym(acronym);

        return TransformUtils.getNamedChildText(BioportalRestUtils.getDocument(xml), ACRONYM);
    }

    public String acronymAndVersionToVersionName(String acronym, String version) {
        String xml = this.bioportalRestService.getOntologySubmissionsByAcronym(acronym);

        List<Node> nodes = TransformUtils.getNodeListWithPath(BioportalRestUtils.getDocument(xml), "ontologySubmissionCollection.ontologySubmission");

        for(Node node : nodes){
            String foundVersion = TransformUtils.getNamedChildText(node, VERSION);
            if(StringUtils.equals(version, foundVersion)){
                String submissionId = TransformUtils.getNamedChildText(node, SUBMISSION_ID);

                return this.acronymAndSubmissionIdToVersionName(acronym, submissionId);
            }
        }

        return null;
    }

    public static class AcronymAndSubmissionId {

        private String acronym;
        private String submissionId;

        public AcronymAndSubmissionId(String acronym, String submissionId) {
            this.acronym = acronym;
            this.submissionId = submissionId;
        }

        public String getAcronym() {
            return acronym;
        }

        public void setAcronym(String acronym) {
            this.acronym = acronym;
        }

        public String getSubmissionId() {
            return submissionId;
        }

        public void setSubmissionId(String submissionId) {
            this.submissionId = submissionId;
        }
    }
}
