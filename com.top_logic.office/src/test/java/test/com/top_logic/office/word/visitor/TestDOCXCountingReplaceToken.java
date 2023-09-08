/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.office.word.visitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.docx4j.wml.Text;

import com.top_logic.office.word.visitor.DOCXCountingReplaceToken;

/**
 * Tests {@link DOCXCountingReplaceToken}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDOCXCountingReplaceToken extends TestCase {

	public void testReplacementSingle() {
		DOCXCountingReplaceToken replacer = new DOCXCountingReplaceToken("replace", "replaced");

		Text visitedText1 = new Text();
		visitedText1.setValue("PREFIX_replace_POSTFIX");
		replacer.accept(visitedText1);
		assertEquals("PREFIX_replaced_0_POSTFIX", visitedText1.getValue());

		Text visitedText2 = new Text();
		visitedText2.setValue("PREFIX_replace_POSTFIX");
		replacer.accept(visitedText2);
		assertEquals("PREFIX_replaced_1_POSTFIX", visitedText2.getValue());
		replacer.accept(visitedText2);
		assertEquals("PREFIX_replaced_2d_1_POSTFIX", visitedText2.getValue());
	}

	public void testReplacementDouble() {
		DOCXCountingReplaceToken replacer = new DOCXCountingReplaceToken("replace", "replaced");

		Text visitedText1 = new Text();
		visitedText1.setValue("PREFIX_replace_MIDDLE_replace_POSTFIX");
		replacer.accept(visitedText1);
		assertEquals("PREFIX_replaced_0_MIDDLE_replaced_0_POSTFIX", visitedText1.getValue());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDOCXCountingReplaceToken}.
	 */
	public static Test suite() {
		return new TestSuite(TestDOCXCountingReplaceToken.class);
	}

}
