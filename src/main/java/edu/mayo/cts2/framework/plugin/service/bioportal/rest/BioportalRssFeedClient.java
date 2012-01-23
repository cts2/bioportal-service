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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import edu.mayo.cts2.framework.model.exception.Cts2RuntimeException;

/**
 * The Class BioportalRssFeedClient.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRssFeedClient {

	private static Log log = LogFactory.getLog(BioportalRssFeedClient.class);
	
	private static final String BIOPORTAL_RSS_URL = "http://bioportal.bioontology.org/syndication/rss";

	/**
	 * Gets the bioportal rss feed.
	 *
	 * @return the bioportal rss feed
	 */
	public SyndFeed getBioportalRssFeed(){
		 SyndFeedInput input = new SyndFeedInput();

		 SyndFeed feed;
		try {
			feed = input.build(new XmlReader(new URL(BIOPORTAL_RSS_URL)));
			log.debug(feed);
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		} 
		
		 return feed;
	}
}
