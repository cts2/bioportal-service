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
package edu.mayo.cts2.framework.plugin.service.bioportal.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;

/**
 * The Class BioportalRssModuleParser.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class BioportalRssModuleParser implements ModuleParser {
	
	private static final String NS_URI = "http://purl.org/dc/elements/1.1/";

	/* (non-Javadoc)
	 * @see com.sun.syndication.io.ModuleParser#getNamespaceUri()
	 */
	public String getNamespaceUri() {
		return NS_URI;
	}

	/* (non-Javadoc)
	 * @see com.sun.syndication.io.ModuleParser#parse(org.jdom.Element)
	 */
	public Module parse(Element element) {	
		Namespace ns = Namespace.getNamespace(NS_URI);
		String date = element.getChildText("date", ns);
		
		 DateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
		try {
			return new DateModule(formatter.parse(date));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}