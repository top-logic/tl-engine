/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import static com.top_logic.basic.xml.DOMUtil.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.basic.xml.fixtures.Fixtures;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.xml.XMLCompare;

/**
 * Test case for {@link XMLCompare}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXMLCompare extends TestCase {

	private static final List<String> IGNORED_NAMESPACES = Arrays.asList("http://ignored.name.space/");
	private static final Filter<? super Object> NAMESPACE_FILTER = FilterFactory.not(new SetFilter(IGNORED_NAMESPACES));

	public void test1() {
		doTestSimilar("test-1");
	}
	
	public void test2() {
		doTestSimilar("test-2");
	}

	public void testFail1() {
		doTestDifferent("fail-1");
	}
	
	public void testFail2() {
		doTestDifferent("fail-2");
	}
	
	public void testFail3() {
		doTestDifferent("fail-3");
	}
	
	public void testFail4() {
		doTestDifferent("fail-4");
	}
	
	public void testAttributeRemoveDetection() {
		doTestDifferent("<a><v a1='v1'/></a>", "<a><v/></a>");
	}
	
	public void testEqualElementNamespace() {
		doTestIdentical("<a xmlns:foo='http://foo'><foo:v/></a>", "<a xmlns:bar='http://foo'><bar:v/></a>");
	}
	
	public void testEqualAttributeNamespace() {
		doTestIdentical("<a xmlns:foo='http://foo'><v foo:a1='v1'/></a>", "<a xmlns:bar='http://foo'><v bar:a1='v1'/></a>");
	}
	
	public void testDifferentElementNamespace() {
		doTestDifferent("<a xmlns:foo='http://foo'><foo:v/></a>", "<a xmlns:foo='http://bar'><foo:v/></a>");
	}
	
	public void testDifferentAttributeNamespace() {
		doTestDifferent("<a xmlns:foo='http://foo'><v foo:a1='v1'/></a>", "<a xmlns:foo='http://bar'><v foo:a1='v1'/></a>");
	}

	public void testTextPositionComputation() {
		doTestDifferent("<a>a<c/>b</a>", "<a>a<c/>d</a>");
	}
	
	public void testMissingElement() {
		doTestDifferent("<a><c/><c/></a>", "<a><c/></a>");
	}
	
	public void testElementVsText() {
		doTestDifferent("<a><c/><c/></a>", "<a><c/>c</a>");
	}
	
	public void testWhiteSpaceContent() {
		doTestSimilar("<a> </a>", "<a>   </a>");
	}
	
	public void testWhiteSpaceBeforeChildren() {
		doTestSimilar("<a><c/><c/></a>", "<a>   <c/><c/></a>");
	}
	
	public void testWhiteSpaceBetweenChildren() {
		doTestSimilar("<a><c/><c/></a>", "<a><c/>   <c/></a>");
	}
	
	public void testWhiteSpaceAfterChildren() {
		doTestSimilar("<a><c/><c/></a>", "<a><c/><c/>   </a>");
	}
	
	public void testWhiteSpacePreceeding() {
		doTestSimilar("<a>A</a>", "<a> A</a>");
	}
	
	public void testWhiteSpaceTailing() {
		doTestSimilar("<a>A</a>", "<a>A </a>");
	}

	public void testWhiteSpaceSurrounding() {
		doTestSimilar("<a>A</a>", "<a> A </a>");
	}
	
	public void testWhiteSpaceBetweenText() {
		doTestDifferent("<a>A B</a>", "<a>A  B</a>");
	}
	
	public void testWhiteSpaceAndCData() {
		doTestDifferent("<a>   </a>", "<a><![CDATA[   ]]></a>");
	}
	
	public void testWhiteSpaceWithEntities() {
		doTestDifferent("<a>&lt; &gt;</a>", "<a>&lt;  &gt;</a>");
	}

	public void testCData() {
		doTestSimilar("<a>A</a>", "<a><![CDATA[A]]></a>");
	}

// Cannot be guaranteed with the current approach.
//
//	public void testWhiteSpaceBetweenCData() {
//		doTestFail("<a><![CDATA[A]]> <![CDATA[B]]></a>", "<a><![CDATA[A]]>  <![CDATA[B]]></a>");
//	}
	
	public void testWhiteSpaceWithComments() {
		doTestSimilar("<a><!--A--> <!--B--></a>", "<a><!--A-->  <!--B--></a>");
	}
	
	public void testWhiteSpaceWithCommentsAndText2() {
		doTestSimilar("<a><!--A--> C</a>", "<a><!--A-->  C</a>");
	}
	
	public void testWhiteSpaceWithCommentsAndText() {
		doTestSimilar("<a><!--A--> <!--B-->C</a>", "<a><!--A-->  <!--B-->C</a>");
	}
	
	protected void doTestIdentical(String expected, String given) {
		doTestIdentical(parse(expected), parse(given));
	}
	
	private void doTestIdentical(Document expected, Document given) {
		doTestOkLoose(expected, given);
		doTestOkStrict(expected, given);
	}
	
	protected void doTestSimilar(String baseName) {
		Document expected = Fixtures.getDocument(baseName + "-expected.xml");
		Document given = Fixtures.getDocument(baseName + "-given.xml");
		doTestSimilar(expected, given);
	}

	protected void doTestSimilar(String expected, String given) {
		doTestSimilar(parse(expected), parse(given));
	}
	
	private void doTestSimilar(Document expected, Document given) {
		doTestOkLoose(expected, given);
		doTestFailStrict(expected, given);
	}
	
	private void doTestDifferent(String baseName) {
		Document expected = Fixtures.getDocument(baseName + "-expected.xml");
		Document given = Fixtures.getDocument(baseName + "-given.xml");
		doTestDifferent(expected, given);
	}

	private void doTestDifferent(String expected, String given) {
		doTestDifferent(parse(expected), parse(given));
	}

	private void doTestDifferent(Document expected, Document given) {
		doTestFail(false, expected, given);
		doTestFail(true, expected, given);
	}
	
	private void doTestOkLoose(Document expected, Document given) {
		doTestOk(false, expected, given);
	}
	
	private void doTestOkStrict(Document expected, Document given) {
		doTestOk(true, expected, given);
		doTestOk(false, expected, given);
	}

	private void doTestFailStrict(Document expected, Document given) {
		doTestFail(true, expected, given);
	}
	
	private void doTestOk(boolean strict, Document expected, Document given) {
		XMLCompare xmlCompare = new XMLCompare(new AssertProtocol(), strict, NAMESPACE_FILTER);
		xmlCompare.assertEqualsNode(expected, given);
		xmlCompare.assertEqualsNode(given, expected);
	}
	
	private void doTestFail(boolean strict, Document expected, Document given) {
		XMLCompare xmlCompare = new XMLCompare(new ExpectedFailureProtocol(), strict, NAMESPACE_FILTER);
		try {
			xmlCompare.assertEqualsNode(expected, given);
			fail("Undetected difference.");
		} catch (ExpectedFailure ex) {
			// expected.
		}
		try {
			xmlCompare.assertEqualsNode(given, expected);
			fail("Undetected difference.");
		} catch (ExpectedFailure ex) {
			// expected.
		}
	}
	
}
