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

import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.exception.Cts2RuntimeException;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class TransformUtils.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class TransformUtils {

	private static final String PAGE_COUNT = "class.pageCount";
	private static final String PAGE_NUMBER = "class.page";
	private static final String PAGE_SIZE = "success.data.page.pageSize";

	/**
	 * Instantiates a new transform utils.
	 */
	private TransformUtils(){
		super();
	}
	

	/**
	 * Gets the input source.
	 *
	 * @param xml the xml
	 * @return the input source
	 */
	public static InputSource getInputSource(String xml) {
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

			return new InputSource(is);
		} catch (UnsupportedEncodingException e) {
			throw new Cts2RuntimeException(e);
		}
	}



	/**
	 * Url encode.
	 *
	 * @param url the url
	 * @return the string
	 */
	public static String urlEncode(String url) {
		try {
			return URIUtil.encodePath(url);
		} catch (Exception e) {
			throw new Cts2RuntimeException(e);
		}
	}

	/**
	 * Checks for filter.
	 *
	 * @param resolvedFilter the filter
	 * @return true, if successful
	 */
	public static boolean hasFilter(ResolvedFilter resolvedFilter) {
		return resolvedFilter != null && StringUtils.isNotBlank(resolvedFilter.getMatchValue());
	}

	/**
	 * Gets the total count.
	 *
	 * @param xml the xml
	 * @return the total count
	 */
	public static int getPageCount(String xml) {
		return Integer.valueOf(TransformUtils.getNamedChildTextWithPath(
				BioportalRestUtils.getDocument(xml), PAGE_COUNT));
	}
	
	/**
	 * Gets the page number.
	 *
	 * @param xml the xml
	 * @return the page number
	 */
	public static int getPageNumber(String xml) {
		return Integer.valueOf(TransformUtils.getNamedChildTextWithPath(
				BioportalRestUtils.getDocument(xml), PAGE_NUMBER));
	}
	
	/**
	 * Gets the page size.
	 *
	 * @param xml the xml
	 * @return the page size
	 */
	public static int getPageSize(String xml) {
		return Integer.valueOf(TransformUtils.getNamedChildTextWithPath(
				BioportalRestUtils.getDocument(xml), PAGE_SIZE));
	}

	/**
	 * Gets the named child.
	 *
	 * @param node the node
	 * @param childName the child name
	 * @return the named child
	 */
	public static Node getNamedChild(Node node, String childName) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (StringUtils.equals(childNode.getNodeName(), childName)) {
				return childNode;
			}
		}

		return null;
	}

	/**
	 * Gets the named child with path.
	 *
	 * @param node the node
	 * @param dotSepartedPath the dot separted path
	 * @return the named child with path
	 */
	public static Node getNamedChildWithPath(Node node, String dotSepartedPath) {
		String[] childNodes = StringUtils.split(dotSepartedPath, '.');

		return getNamedChildWithPath(node, childNodes);
	}

	/**
	 * Gets the named child with path.
	 *
	 * @param node the node
	 * @param childNodes the child nodes
	 * @return the named child with path
	 */
	public static Node getNamedChildWithPath(Node node, String... childNodes) {
		if (childNodes.length == 1) {
			return getNamedChild(node, childNodes[0]);
		}

		String childNodeName = childNodes[0];

		return getNamedChildWithPath(getNamedChild(node, childNodeName),
				(String[]) ArrayUtils.remove(childNodes, 0));

	}

	/**
	 * Gets the named child text with path.
	 *
	 * @param node the node
	 * @param dotSepartedPath the dot separted path
	 * @return the named child text with path
	 */
	public static String getNamedChildTextWithPath(Node node,
			String dotSepartedPath) {
		return getNodeText(getNamedChildWithPath(node, dotSepartedPath));
	}

	/**
	 * Gets the named child text.
	 *
	 * @param node the node
	 * @param childNode the child node
	 * @return the named child text
	 */
	public static String getNamedChildText(Node node, String childNode) {
		Node child = getNamedChild(node, childNode);
		if (child != null) {
			return getNodeText(child);
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the node text.
	 *
	 * @param node the node
	 * @return the node text
	 */
	public static String getNodeText(Node node){
		return StringUtils.trim(node.getTextContent()).replaceAll("[\\n\\t]", "");
	}

	/**
	 * Gets the node list with path.
	 *
	 * @param node the node
	 * @param childNodes the child nodes
	 * @return the node list with path
	 */
	public static List<Node> getNodeListWithPath(Node node, String... childNodes) {
		if (childNodes.length == 1) {
			return getNodeList(node, childNodes[0]);
		}

		String childNodeName = childNodes[0];

		return getNodeListWithPath(getNamedChild(node, childNodeName),
				(String[]) ArrayUtils.remove(childNodes, 0));
	}

	/**
	 * Gets the node list with path.
	 *
	 * @param node the node
	 * @param dotSepartedPath the dot separted path
	 * @return the node list with path
	 */
	public static List<Node> getNodeListWithPath(Node node,
			String dotSepartedPath) {
		String[] childNodes = StringUtils.split(dotSepartedPath, '.');

		return getNodeListWithPath(node, childNodes);
	}

	/**
	 * Gets the node list.
	 *
	 * @param node the node
	 * @param childName the child name
	 * @return the node list
	 */
	public static List<Node> getNodeList(Node node, String childName) {
		return getNodeList(node, childName, null);
	}
	

	/**
	 * Gets the node list.
	 *
	 * @param node the node
	 * @param childName the child name
	 * @param filter the filter
	 * @return the node list
	 */
	public static List<Node> getNodeList(Node node, String childName, NodeFilter filter) {
		if(node == null){
			return null;
		}
		
		List<Node> returnList = new ArrayList<Node>();

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (StringUtils.equals(childNode.getNodeName(), childName)) {
				if(filter == null){
					returnList.add(childNode);
				} else {
					if(filter.add(childNode)){
						returnList.add(childNode);
					}
				}
			}
		}

		return returnList;

	}
	
	/**
	 * The Interface NodeFilter.
	 *
	 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
	 */
	public static interface NodeFilter {
		
		/**
		 * Adds the.
		 *
		 * @param node the node
		 * @return true, if successful
		 */
		public boolean add(Node node);
	}


	/**
	 * Node list to list.
	 *
	 * @param list the list
	 * @return the list
	 */
	public static List<Node> nodeListToList(NodeList list) {
		List<Node> returnList = new ArrayList<Node>();

		for (int i = 0; i < list.getLength(); i++) {
			returnList.add(list.item(i));
		}

		return returnList;
	}
}
