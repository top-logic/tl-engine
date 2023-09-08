/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.html;

import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.html.I18NConstants;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;

/**
 * Test case for {@link SafeHTML}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TestSafeHTML extends BasicTestCase {

	private static final String HREF_ATTRIBUTE = "href";

	private static final String JAVASCRIPT_ALERT_VALUE = "javascript:alert()";

	private static final String ANCHOR_TAG = "a";

	private static final String LOCALHOST_ATTRIBUTE_VALUE = "localhost:8080";

	private static final String ONCLICK_ATTRIBUTE = "onclick";

	private static final String ID_ATTRIBUTE = "id";

	private static final String IMG_TAG = "img";

	private static final String DIV_TAG = "div";

	private static final String BOLD_TAG = "b";

	private static final String VIDEO_TAG = "abc";

	private static final String HELLO_WORLD_TEXT = "Hello World";

	private SafeHTML _safeHTML;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_safeHTML = SafeHTML.getInstance();
	}

	/**
	 * Creates a {@link TestSafeHTML} instance.
	 * 
	 * @param name
	 *        Name of test to execute.
	 */
	public TestSafeHTML(String name) {
		super(name);
	}

	/**
	 * Test an invalid tag name, for instance <video></video>.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testInvalidTagName() throws IOException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginTag(VIDEO_TAG);
			tagWriter.endTag(VIDEO_TAG);

			_safeHTML.check(tagWriter.toString());
			
			fail();
		} catch (I18NException exception) {
			assertTrue(I18NConstants.INVALID_TAG_NAME.fill(VIDEO_TAG).equals(getErrorKey(exception)));
		}
	}

	/**
	 * Test a valid tag name, for instance <b></b>.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testValidTagName() throws IOException, I18NException {
		try (TagWriter tagWriter = new TagWriter()) {

			tagWriter.beginTag(BOLD_TAG);
			tagWriter.endTag(BOLD_TAG);

			_safeHTML.check(tagWriter.toString());
		}
	}

	/**
	 * Test nested valid tag names, for instance <b><div>Hello World</div></b>.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testNestedValidTagName() throws IOException, I18NException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginTag(BOLD_TAG);
			tagWriter.beginTag(DIV_TAG);
			tagWriter.writeText(HELLO_WORLD_TEXT);
			tagWriter.endTag(DIV_TAG);
			tagWriter.endTag(BOLD_TAG);

			_safeHTML.check(tagWriter.toString());
		}
	}

	/**
	 * Test nested tag names where the inner tag is invalid, for instance <b><video></video></b>.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testNestedInvalidTagName() throws IOException {
		try (TagWriter tagWriter = new TagWriter()) {

			tagWriter.beginTag(BOLD_TAG);
			tagWriter.beginTag(VIDEO_TAG);
			tagWriter.endTag(VIDEO_TAG);
			tagWriter.endTag(BOLD_TAG);

			_safeHTML.check(tagWriter.toString());

			fail();
		} catch (I18NException exception) {
			assertTrue(I18NConstants.INVALID_TAG_NAME.fill(VIDEO_TAG).equals(getErrorKey(exception)));
		}
	}

	/**
	 * Test a valid tag with a valid attribute but an invalid attribute value, for instance
	 * <a href="javascript:alert()" />.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testLinkAttributeChecker() throws IOException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginBeginTag(ANCHOR_TAG);
			tagWriter.writeAttribute(HREF_ATTRIBUTE, JAVASCRIPT_ALERT_VALUE);
			tagWriter.endBeginTag();

			tagWriter.endTag(ANCHOR_TAG);

			_safeHTML.check(tagWriter.toString());

			fail();
		} catch (I18NException exception) {
			assertTrue(I18NConstants.NO_JAVASCRIPT_ALLOWED.equals(getErrorKey(exception)));
		}
	}

	/**
	 * Test a valid empty tag, for instance <img />.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testValidEmptyTag() throws IOException, I18NException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginBeginTag(IMG_TAG);
			tagWriter.endEmptyTag();

			_safeHTML.check(tagWriter.toString());
		}
	}

	/**
	 * Test a valid tag with a valid attribute and a valid attribute value, for instance
	 * <img id="1" />.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testValidAttributes() throws IOException, I18NException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginBeginTag(IMG_TAG);
			tagWriter.writeAttribute(ID_ATTRIBUTE, 1);
			tagWriter.endEmptyTag();

			_safeHTML.check(tagWriter.toString());
		}
	}

	/**
	 * Test a valid tag with an invalid attribute, for instance <img onclick="localhost:8080" />.
	 * 
	 * @throws IOException
	 *         Exception while writing the HTML {@link String}.
	 */
	public void testInvalidAttributes() throws IOException {
		try (TagWriter tagWriter = new TagWriter()) {
			tagWriter.beginBeginTag(IMG_TAG);
			tagWriter.writeAttribute(ONCLICK_ATTRIBUTE, LOCALHOST_ATTRIBUTE_VALUE);
			tagWriter.endEmptyTag();

			_safeHTML.check(tagWriter.toString());

			fail();
		} catch (I18NException exception) {
			assertTrue(
				I18NConstants.INVALID_ATTRIBUTE_NAME.fill(ONCLICK_ATTRIBUTE, IMG_TAG).equals(getErrorKey(exception)));
		}
	}

	private ResKey getErrorKey(I18NFailure exception) {
		return exception.getErrorKey();
	}

	/**
	 * The suite of tests to perform.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestSafeHTML.class,
			SafeHTML.Module.INSTANCE));
	}

	/**
	 * Main function for direct testing.
	 *
	 * @param args
	 *        Will be ignored.
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}
}
