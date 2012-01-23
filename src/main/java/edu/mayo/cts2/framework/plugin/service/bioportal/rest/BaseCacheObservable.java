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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class BaseCacheObservable.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class BaseCacheObservable implements CacheObservable {

	private Set<CacheObserver> observers = new HashSet<CacheObserver>();

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObservable#addObserver(edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObserver)
	 */
	public void addObserver(CacheObserver o) {
		this.observers.add(o);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObservable#removeObserver(edu.mayo.cts2.framework.plugin.service.bioportal.rest.CacheObserver)
	 */
	public void removeObserver(CacheObserver o) {
		this.observers.remove(o);
	}
	
	/**
	 * Fire on code systems change event.
	 *
	 * @param ontologyIds the ontology ids
	 */
	protected void fireOnCodeSystemsChangeEvent(List<String> ontologyIds){
		for (CacheObserver observer : observers) {
			observer.onCodeSystemsChangeEvent(ontologyIds);
		}
	}
}
