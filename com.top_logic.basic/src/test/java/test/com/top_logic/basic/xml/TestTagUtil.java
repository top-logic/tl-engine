/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagUtil;

/**
 * Test case for {@link TagUtil}.
 * 
 * @see TestTagWriter
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTagUtil extends TestCase {

    /** Testcase for <code>encodeHTML</code>. */
    public void testInefficientEncodeXML() {
        assertEquals("", TagUtil.encodeXML(null));
        assertEquals("", TagUtil.encodeXML(""));
        assertEquals("Da lag da cadava aba", TagUtil.encodeXML("Da lag da cadava aba"));
        assertEquals("So&lt;script&gt;alert('&amp;Evil')&lt;/script&gt;",
        		TagUtil.encodeXML("So<script>alert('&Evil')</script>"));

        assertEquals("This needs no Encoding", TagUtil.encodeXML("This needs no Encoding"));
        assertEquals("ƒ÷‹ ‰ˆ¸ﬂ ^Ù∞·‡~µ@|", TagUtil.encodeXML("ƒ÷‹ ‰ˆ¸ﬂ ^Ù∞·‡~µ@|"));
        assertEquals("&lt; This &amp; That &gt; \"will\" need encoding", 
        		TagUtil.encodeXML("< This & That > \"will\" need encoding"));
    }

    /** Testcase for <code>encodeAttributeValue</code>. */
    public void testInefficientEncodeXMLAttribute() {
        assertEquals("", TagUtil.encodeXMLAttribute(null));
        assertEquals("", TagUtil.encodeXMLAttribute(""));
        assertEquals("Da lag da cadava aba", TagUtil.encodeXMLAttribute("Da lag da cadava aba"));
		assertEquals("&quot;That" + TagUtil.APOS_CHAR + "s the way it is&quot; said Adam &amp; Eve",
			TagUtil.encodeXMLAttribute("\"That's the way it is\" said Adam & Eve"));
		assertEquals(
			"&quot;" + TagUtil.APOS_CHAR + ">&lt;script>alert(" + TagUtil.APOS_CHAR
				+ "Hello moon &amp; world!" + TagUtil.APOS_CHAR + ");&lt;/script>&lt;div ",
			TagUtil.encodeXMLAttribute("\"'><script>alert('Hello moon & world!');</script><div "));
    }


}
