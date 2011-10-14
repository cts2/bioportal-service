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

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import edu.mayo.cts2.framework.model.exception.UnspecifiedCts2RuntimeException;
import edu.mayo.cts2.framework.service.command.Page;

/**
 * The Class BioportalRestUtils.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class BioportalRestUtils {
		
	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
	
	private static final DocumentBuilder DOCUMENT_BUILDER;
	static {
		try{
		DOCUMENT_BUILDER = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
		} catch(Exception e){
			throw new UnspecifiedCts2RuntimeException(e);
		}
	}
	
	/**
	 * Instantiates a new bioportal rest utils.
	 */
	private BioportalRestUtils(){
		super();
	}
	
	/**
	 * Gets the document.
	 *
	 * @param xml the xml
	 * @return the document
	 */
	public static Document getDocument(String xml) {
		StringReader reader = new StringReader(xml);

		InputSource inputStream = new InputSource(reader);
		
		Document doc;
		
		try {
			synchronized(DOCUMENT_BUILDER){
				doc = DOCUMENT_BUILDER.parse(inputStream);
			}
		} catch (Exception e) {
			throw new UnspecifiedCts2RuntimeException(e);
		} 
		
		return doc;
	}
	
	/**
	 * Gets the end.
	 *
	 * @param page the page
	 * @return the end
	 */
	public static int getEnd(Page page){
		return ( page.getPage() + 1 ) * page.getMaxtoreturn();
	}
	
	
	
	/**
	 * Gets the start.
	 *
	 * @param page the page
	 * @return the start
	 */
	public static int getStart(Page page){
		return page.getPage() * page.getMaxtoreturn();
	}
	
}
