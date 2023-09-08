/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import junit.framework.TestCase;

import com.top_logic.layout.scripting.recorder.ref.AttributeIdParser;

/**
 * The name says everything.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestAttributeIdParser extends TestCase {

	private final class TestCase {

		private final String attributeFieldName;
		private final String expectedAttributeId;
		private final String expectedObjectId;

		public TestCase(String attributeFieldName, String attributeId, String objectId) {
			this.attributeFieldName = attributeFieldName;
			this.expectedAttributeId = attributeId;
			this.expectedObjectId = objectId;
		}

		public String getAttributeFieldName() {
			return attributeFieldName;
		}

		public String getExpectedAttributeId() {
			return expectedAttributeId;
		}

		public String getExpectedObjectId() {
			return expectedObjectId;
		}

		void run() {
			AttributeIdParser parser = AttributeIdParser.newAttributeIdParser(attributeFieldName);
			assertTrue(attributeIdFailedMessage(), parser.isAttributeIdParsable());
			assertTrue(objectIdFailedMessage(), parser.isObjectIdParsable());
			assertEquals(
				buildErrorMessageAttributeIdWrong(parser.getAttributeId()),
				expectedAttributeId,
				parser.getAttributeId());
			assertEquals(
				buildErrorMessageObjectIdWrong(parser.getObjectId()),
				expectedObjectId,
				parser.getObjectId());
		}

		private String attributeIdFailedMessage() {
			return "Failed to parse the AttributeId in a valid AttributeFieldName! "
				+ buildAttributeFieldNameDescription();
		}

		private String objectIdFailedMessage() {
			return "Failed to parse the ObjectId in a valid AttributeFieldName! "
				+ buildAttributeFieldNameDescription();
		}

		private String buildErrorMessageAttributeIdWrong(String actual) {
			return "Wrong AttributeId parsed from AttributeFieldName! Actual AttributeId: '" + actual + "'; "
				+ buildAttributeFieldNameDescription();
		}

		private String buildErrorMessageObjectIdWrong(String actual) {
			return "Wrong ObjectId parsed from AttributeFieldName! Actual ObjectId: '" + actual + "'; "
				+ buildAttributeFieldNameDescription();
		}

		private String buildAttributeFieldNameDescription() {
			return "AttributeFieldName: '" + attributeFieldName + "'; Expected AttributeId: '"
				+ expectedAttributeId + "'; Expected ObjectId: '" + expectedObjectId + "'";
		}

	}

	public void testAttributeId() {
		new TestCase(
			"att_" + "47" + "_" + "11",
			"47",
			"11").run();
	}

}
