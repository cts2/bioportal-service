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

import java.util.Date;

import com.sun.syndication.feed.module.Module;

/**
 * The Class DateModule.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class DateModule implements Module {

	private static final long serialVersionUID = -1846720606473008214L;
	public static final String URI = "http://purl.org/dc/elements/1.1/#date";

	private Date date;
	
	/**
	 * Instantiates a new date module.
	 *
	 * @param date the date
	 */
	public DateModule(Date date){
		this.date = date;
	}
	
	/* (non-Javadoc)
	 * @see com.sun.syndication.feed.CopyFrom#getInterface()
	 */
	public Class<?> getInterface() {
		return Date.class;
	}

	/* (non-Javadoc)
	 * @see com.sun.syndication.feed.CopyFrom#copyFrom(java.lang.Object)
	 */
	public void copyFrom(Object obj) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.sun.syndication.feed.module.Module#getUri()
	 */
	public String getUri() {
		return URI;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
}
