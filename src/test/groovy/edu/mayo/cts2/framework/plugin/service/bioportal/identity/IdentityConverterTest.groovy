package edu.mayo.cts2.framework.plugin.service.bioportal.identity
import groovy.xml.XmlUtil
import org.junit.Ignore
import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

@Ignore
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
	
		String codeSystemVersionName = identityConverter.buildVersionName(csNode.childNodes.item(0), true)
		
		assertThat codeSystemVersionName, is("BRO_3-2-1_OWL-FULL")
	}
}