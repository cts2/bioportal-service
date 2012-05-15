package edu.mayo.cts2.framework.plugin.service.bioportal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.xerces.util.XMLChar;

import edu.mayo.cts2.framework.model.util.ModelUtils;

public class UriUtils {
	
	public static String getLocalName(String uri){
		if( !ModelUtils.isValidUri(uri) ){
			return uri;
		}

		String name;
		
		int splitPoint = splitNamespace(uri);
		
		if(splitPoint == uri.length()){
			name = StringUtils.substringAfterLast(uri, "/");
		} else {
			name = uri.substring(splitPoint);
		}
		
		if(StringUtils.contains(name, '#')){
			name = StringUtils.substringAfterLast(uri, "#");
		}
		
		return name;
	}

	/**
	 * Given an absolute URI, determine the split point between the namespace
	 * part and the localname part. If there is no valid localname part then the
	 * length of the string is returned. The algorithm tries to find the longest
	 * NCName at the end of the uri, not immediately preceeded by the first
	 * colon in the string.
	 * 
	 * @param uri
	 * @return the index of the first character of the localname
	 */
	private static int splitNamespace(String uri) {

		// XML Namespaces 1.0:
		// A qname name is NCName ':' NCName
		// NCName ::= NCNameStartChar NCNameChar*
		// NCNameChar ::= NameChar - ':'
		// NCNameStartChar ::= Letter | '_'
		//
		// XML 1.0
		// NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] |
		// [#xD8-#xF6] | [#xF8-#x2FF] |
		// [#x370-#x37D] | [#x37F-#x1FFF] |
		// [#x200C-#x200D] | [#x2070-#x218F] |
		// [#x2C00-#x2FEF] | [#x3001-#xD7FF] |
		// [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
		// NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
		// [#x0300-#x036F] | [#x203F-#x2040]
		// Name ::= NameStartChar (NameChar)*

		char ch;
		int lg = uri.length();
		if (lg == 0)
			return 0;
		int i = lg - 1;
		for (; i >= 1; i--) {
			ch = uri.charAt(i);
			if (notNameChar(ch))
				break;
		}

		int j = i + 1;

		if (j >= lg)
			return lg;

		// Check we haven't split up a %-encoding.
		if (j >= 2 && uri.charAt(j - 2) == '%')
			j = j + 1;
		if (j >= 1 && uri.charAt(j - 1) == '%')
			j = j + 2;

		// Have found the leftmost NCNameChar from the
		// end of the URI string.
		// Now scan forward for an NCNameStartChar
		// The split must start with NCNameStart.
		for (; j < lg; j++) {
			ch = uri.charAt(j);
			// if (XMLChar.isNCNameStart(ch))
			// break ;
			if (XMLChar.isNCNameStart(ch)) {
				// "mailto:" is special.
				// Keep part after mailto: at least one charcater.
				// Do a quick test before calling .startsWith
				// OLD: if ( uri.charAt(j - 1) == ':' && uri.lastIndexOf(':', j
				// - 2) == -1)
				if (j == 7 && uri.startsWith("mailto:"))
					continue; // split "mailto:me" as "mailto:m" and "e" !
				else
					break;
			}
		}
		return j;
	}

	/**
	 * answer true iff this is not a legal NCName character, ie, is a possible
	 * split-point start.
	 */
	private static boolean notNameChar(char ch) {
		return !XMLChar.isNCName(ch);
	}
}
