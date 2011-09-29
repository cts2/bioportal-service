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
package edu.mayo.cts2.framework.plugin.service.bioportal.util;

/**
 * The Class BioportalConstants.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class BioportalConstants {
	
	public static final String UNKNOWN_VERSION = "unknown";
	
	public static final String BIOPORTAL_NAMESPACE_URI = "http://purl.bioontology.org";
	public static final String BIOPORTAL_NAMESPACE_NAME = "bioportal";
	
	public static final String DEFAULT_ONTOLOGY_ABOUT = "http://purl.bioontology.org/ontology/";
	public static final String DEFAULT_VIEW_ABOUT = "http://purl.bioontology.org/view/";
	
	public static final String BIOPORTAL_ONTOLOGY_ID_NAME = "ontologyId";
	public static final String BIOPORTAL_ONTOLOGY_ID_ABOUT = "http://id.bioontology.org/predicate/ontologyId";
	public static final String BIOPORTAL_ONTOLOGYVERSION_ID_NAME = "ontologyVersionId";
	public static final String BIOPORTAL_ONTOLOGYVERSION_ID_ABOUT = "http://id.bioontology.org/predicate/ontologyVersionId";
	
	/**
	 * Instantiates a new bioportal constants.
	 */
	private BioportalConstants(){
		super();
	}
}
