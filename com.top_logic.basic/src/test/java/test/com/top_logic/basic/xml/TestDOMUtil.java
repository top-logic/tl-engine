/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.DefaultNamespaceContext;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Test case for {@link DOMUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDOMUtil extends TestCase {

	private Document doc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		doc = createDocument();
	}

	private Document createDocument() {
		return DOMUtil.parse(createDocumentSource());
	}

	private String createDocumentSource() {
		return ""
			+ "<a>"
			+	 "<b a1='v1'/>"
			+	 "text"
			+	 "<b xmlns='http://foo' a1='v2'/>"
			+	 "<!--comment-->"
			+	 "<![CDATA[text]]>"
			+	 "<c xmlns='http://bar' a2='v3'/>"
			+ 	"<ns:nsTest xmlns:ns='http://foo' xmlns:pre1='http://bar1' pre1:a2='v3' xmlns:pre2='http://bar2' pre2:a2='v3'/>"
			+ "</a>";
	}
	
	@Override
	protected void tearDown() throws Exception {
		doc = null;
		
		super.tearDown();
	}
	
	public void testMultipleNSPrefix() throws IOException {
		testNameSpaceClash("ns0", false);
		testNameSpaceClash("ns1", false);
		testNameSpaceClash("NS0", false);
		testNameSpaceClash("NS1", false);
	}

	public void testMultipleNSPrefixWithNormalization() throws IOException {
		testNameSpaceClash("ns0", true);
		testNameSpaceClash("ns1", true);
		testNameSpaceClash("NS0", true);
		testNameSpaceClash("NS1", true);
	}

	private void testNameSpaceClash(String namespacePrefix, boolean normalizing) throws IOException {
		String source = "<a xmlns:" + namespacePrefix + "='http://bar' " + namespacePrefix + ":bar='bar1' />";
		Document sourceDocument = DOMUtil.parse(source);
		Element a = sourceDocument.getDocumentElement();
		assertEquals("bar1", a.getAttributeNS("http://bar", "bar"));
		a.setAttributeNS("http://foo", "foo", "foo1");
		assertEquals("foo1", a.getAttributeNS("http://foo", "foo"));
		if (normalizing) {
			sourceDocument.normalizeDocument();
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtil.serializeXMLDocument(out, false, sourceDocument);
		Document recreatedDoc = DOMUtil.parse(out.toString());
		Element a2 = recreatedDoc.getDocumentElement();
		assertEquals("Ticket #18858: Namespace clash between defined in file and generated one", "bar1",
			a2.getAttributeNS("http://bar", "bar"));
		assertEquals("Ticket #18858: Namespace clash between defined in file and generated one", "foo1",
			a2.getAttributeNS("http://foo", "foo"));
	}

	public void testParallelGetDocumentElement() {
		try {
			for (int i = 0; i < 100; i++) {
				checkParallelAccess(createDocument());
			}
			fail("#5970: expected DOM implementation is not threadsafe");
		} catch (RuntimeException ex) {
			//expected
		}
	}

	private void checkParallelAccess(final Node node) {
		int parallelThreads = 10;
		class GetElementThread extends Thread {

			Node result = null;

			@Override
			public void run() {
				result = node.getFirstChild();
			}
		}
		final GetElementThread[] threads = new GetElementThread[parallelThreads];
		for (int i = 0; i < parallelThreads; i++) {
			threads[i] = new GetElementThread();
		}
		for (int i = 0; i < parallelThreads; i++) {
			threads[i].start();
		}

		try {
			for (int i = 0; i < parallelThreads; i++) {
				threads[i].join();
			}
		} catch (InterruptedException ex) {
			// must not occur
		}

		ArrayList<Integer> nullResult = new ArrayList<>();
		Set<Node> differentNodes = new HashSet<>();
		for (int i = 0; i < parallelThreads; i++) {
			Node threadResult = threads[i].result;
			if (threadResult == null) {
				nullResult.add(i - 1);
			} else {
				differentNodes.add(threadResult);
			}
		}
		if (!nullResult.isEmpty() || differentNodes.size() == 1) {
			StringBuilder error = new StringBuilder("Failure accessing " + parallelThreads + " times: ");
			if (!nullResult.isEmpty()) {
				error.append(nullResult.size() + " threads have null as child node.");
			}
			if (differentNodes.size() > 1) {
				error.append(differentNodes.size() + " different results: " + differentNodes);
			}
			throw new RuntimeException(error.toString());
		}
	}
	
	public void testElementSearch() {
		check("/a/child::*", DOMUtil.elements(doc.getDocumentElement()), false);
		check("/a/child::*", DOMUtil.elements(doc.getDocumentElement(), true), true);
	}

	public void testNamespaceSearch() {
		check("/a/child::bar:*", DOMUtil.elementsNS(doc.getDocumentElement(), "http://bar"), false);
		check("/a/child::bar:*", DOMUtil.elementsNS(doc.getDocumentElement(), "http://bar", true), true);
	}
	
	public void testNoNamespaceSearch() {
		check("/a/child::*[namespace-uri(.)='']", DOMUtil.elementsNS(doc.getDocumentElement(), null), false);
		check("/a/child::*[namespace-uri(.)='']", DOMUtil.elementsNS(doc.getDocumentElement(), null, true), true);
	}
	
	public void testNameSearch() {
		check("/a/child::foo:b", DOMUtil.elementsNS(doc.getDocumentElement(), "http://foo", "b"), false);
		check("/a/child::foo:b", DOMUtil.elementsNS(doc.getDocumentElement(), "http://foo", "b", true), true);
	}
	
	public void testNameInNoNamespaceSearch() {
		check("/a/child::*[namespace-uri(.)='' and local-name(.)='b']", DOMUtil.elementsNS(doc.getDocumentElement(), null, "b"), false);
		check("/a/child::*[namespace-uri(.)='' and local-name(.)='b']", DOMUtil.elementsNS(doc.getDocumentElement(), null, "b", true), true);
	}
	
	public void testTextSearch() {
		check("/a/text()", DOMUtil.texts(doc.getDocumentElement()), false);
		check("/a/text()", DOMUtil.texts(doc.getDocumentElement(), true), true);
	}
	
	public void testCommentSearch() {
		check("/a/comment()", DOMUtil.comments(doc.getDocumentElement()), false);
		check("/a/comment()", DOMUtil.comments(doc.getDocumentElement(), true), true);
	}
	
	public void testChildrenSearch() {
		check("/a/node()", DOMUtil.children(doc.getDocumentElement()), false);
		check("/a/node()", DOMUtil.children(doc.getDocumentElement(), true), true);
	}
	
	public void testPartialRead() throws XMLStreamException, UnsupportedEncodingException {
		ByteArrayInputStream input = new ByteArrayInputStream("<a><b><c1/><c2/></b><d/></a>".getBytes("utf-8"));
		XMLStreamReader in = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(input);
		XMLStreamUtil.nextStartTag(in);
		Document document = DOMUtil.newDocument();
		Element root = document.createElement("r");
		document.appendChild(root);
		DOMUtil.appendStripped(in, root);
		assertEquals("<r><b><c1/><c2/></b><d/></r>", DOMUtil.toStringRaw(root));
	}

	public void testPartialReadNS() throws XMLStreamException, UnsupportedEncodingException {
		ByteArrayInputStream input =
			new ByteArrayInputStream(
				"<a xmlns:x='http://foo.com/' xmlns:y='http://bar.com/'><x:b><y:c1/><y:c2/></x:b><d/></a>"
					.getBytes("utf-8"));
		XMLStreamReader in = XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(input);
		XMLStreamUtil.nextStartTag(in);
		Document document = DOMUtil.newDocument();
		Element root = document.createElement("r");
		document.appendChild(root);
		DOMUtil.appendStripped(in, root);
		assertEquals("<r xmlns:x=\"http://foo.com/\" xmlns:y=\"http://bar.com/\"><x:b><y:c1/><y:c2/></x:b><d/></r>",
			DOMUtil.toStringRaw(root));
	}

	public void testNSPrefix() throws XMLStreamException {
		ByteArrayInputStream input = new ByteArrayInputStream(createDocumentSource().getBytes());
		Document parseStripped = DOMUtil.parseStripped(input);
		Iterable<Node> children = DOMUtil.children(parseStripped.getDocumentElement());
		Element nsTest = null;
		for (Node child : children) {
			if ("nsTest".equals(child.getLocalName())) {
				if (nsTest != null) {
					fail("More than one node for namespace test. Can not ensure that the correct one is found.");
				}
				nsTest = (Element) child;
			}
		}
		if (nsTest == null) {
			fail("No node for namespace test.");
			return;
		}
		assertEquals("ns", nsTest.getPrefix());
		Attr a2Attr1 = nsTest.getAttributeNodeNS("http://bar1", "a2");
		assertEquals("pre1", a2Attr1.getPrefix());
		assertEquals("pre1:a2", a2Attr1.getName());
		Attr a2Attr2 = nsTest.getAttributeNodeNS("http://bar2", "a2");
		assertEquals("pre2", a2Attr2.getPrefix());
		assertEquals("pre2:a2", a2Attr2.getName());
	}

	private void check(String xpath, Iterable<? extends Node> elements, boolean reverse) {
		try {
			XPath xpathImpl = XPathFactory.newInstance().newXPath();
			DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
			nsContext.setPrefix("foo", "http://foo");
			nsContext.setPrefix("bar", "http://bar");
			xpathImpl.setNamespaceContext(nsContext);
			NodeList expectedNodes = (NodeList) xpathImpl.evaluate(xpath, doc, XPathConstants.NODESET);
			int expectedLength = expectedNodes.getLength();
			
			int expectedPos = reverse ? expectedLength - 1 : 0;
			int inc = reverse ? -1 : 1;
			
			int actualPos = 0;
			for (Node actualNode : elements) {
				if (actualPos >= expectedLength) {
					fail("Unexpected element: " + actualNode);
				}
				
				Node expectedNode = expectedNodes.item(expectedPos);
				assertSame(expectedNode, actualNode);
				
				expectedPos += inc;
			}
		} catch (XPathExpressionException ex) {
			throw (AssertionError) new AssertionError("XPath evaluation failed.").initCause(ex);
		}
	}
	
}
