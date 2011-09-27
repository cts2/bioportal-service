package edu.mayo.cts2.framework.plugin.service.bioportal.identity;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import groovy.xml.XmlUtil

import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*

import org.junit.Test
import org.w3c.dom.*

class IdentityConverterTest {
	
	@Test
	void testBuildCodeSystemName() {
		IdentityConverter identityConverter = new IdentityConverter();

		def node = new XmlParser().
		parse("src/test/resources/bioportalXml/codeSystemVersion.xml").data.ontologyBean

		javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
		javax.xml.parsers.DocumentBuilder db = factory.newDocumentBuilder()

		org.xml.sax.InputSource inStream = new org.xml.sax.InputSource()

		inStream.setCharacterStream(new java.io.StringReader(XmlUtil.serialize(node.get(0))))

		def csNode = db.parse(inStream)
	
		String codeSystemName = identityConverter.buildName(csNode.childNodes.item(0))
		
		assertThat codeSystemName, is("BRO")
	}
	
	@Test
	void testBuildCodeSystemVersionName() {
		IdentityConverter identityConverter = new IdentityConverter();

		def node = new XmlParser().
		parse("src/test/resources/bioportalXml/codeSystemVersion.xml").data.ontologyBean

		javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
		javax.xml.parsers.DocumentBuilder db = factory.newDocumentBuilder()

		org.xml.sax.InputSource inStream = new org.xml.sax.InputSource()

		inStream.setCharacterStream(new java.io.StringReader(XmlUtil.serialize(node.get(0))))

		def csNode = db.parse(inStream)
	
		String codeSystemVersionName = identityConverter.buildVersionName(csNode.childNodes.item(0))
		
		assertThat codeSystemVersionName, is("BRO_3-2-1_OWL-FULL")
	}
}