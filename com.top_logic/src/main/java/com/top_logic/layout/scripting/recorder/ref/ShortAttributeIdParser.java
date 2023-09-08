/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the attribute id and the object id out of the attribute field name.
 * 
 * <p>
 * This implementation cares for short knowledge object ids.
 * </p>
 * 
 * @see LongAttributeIdParser
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ShortAttributeIdParser extends AttributeIdParser {

	// Example field name: att_47_11
	// Other example: att_47
	// Example object name: 47

	private static final String DOMN_PATTERN = "(att)";

	private static final String KO_ID_PATTERN = "([1-9][0-9]*)";

	private static final String ATTRIBUTE_FIELD_NAME_PATTERN =
		"^(?:" + DOMN_PATTERN + "_)?(?:" + KO_ID_PATTERN + ")(?:_" + KO_ID_PATTERN + ")?$";

	// Only once, compiling RexExps is expensive.
	private static final Pattern COMPILED_FIELD_NAME_ATTRIBUTE_PATTERN = Pattern.compile(ATTRIBUTE_FIELD_NAME_PATTERN);

	/**
	 * <code>null</code> means: Not parsable.
	 * 
	 * @see #getAttributeId()
	 */
	private final String attributeId;

	/**
	 * <code>null</code> means: Not parsable.
	 * 
	 * @see #getObjectId()
	 */
	private final String objectId;

	/**
	 * Takes care of parsing an AttributeFieldName.
	 * 
	 * <p>
	 * Performance note: The parsing happens in the constructor, which means, just once.
	 * 
	 * @param attributeFieldName
	 *        Must not be <code>null</code>.
	 */
	ShortAttributeIdParser(String attributeFieldName) {
		super(attributeFieldName);
		Matcher matcher = COMPILED_FIELD_NAME_ATTRIBUTE_PATTERN.matcher(attributeFieldName);

		// Matcher.matches has two functions:
		// 1.) It returns whether the Matcher matches.
		// 2.) It initializes the Match data which can than be requested via 'start', 'end' and
		// 'group' methods.
		// Ugly design. :-(
		if (matcher.matches()) {
			attributeId = extractId(matcher, 2);
			objectId = extractObjectId(matcher, 3);
		} else {
			attributeId = null;
			objectId = null;
		}
	}

	@Override
	public boolean isAttributeIdParsable() {
		return attributeId != null;
	}

	@Override
	public boolean isObjectIdParsable() {
		return objectId != null;
	}

	/**
	 * The attribute id from the attribute field name.
	 * 
	 * @return Is never <code>null</code>. Throws an assertion if AttributeFieldName is not
	 *         parsable.
	 */
	@Override
	public String getAttributeId() {
		assert isAttributeIdParsable() : getUnparsableErrorMessage();
		return (attributeId);
	}

	/**
	 * The object id from the attribute field name.
	 * 
	 * @return Is never <code>null</code>. Throws an assertion if AttributeFieldName is not
	 *         parsable.
	 */
	@Override
	public String getObjectId() {
		assert isObjectIdParsable() : getUnparsableErrorMessage();
		return (objectId);
	}

	private String extractObjectId(Matcher matcher, int matcherGroupIndexOffset) {
		return extractId(matcher, matcherGroupIndexOffset);
	}

	private String extractId(Matcher matcher, int matcherGroupIndexOffset) {
		return matcher.group(matcherGroupIndexOffset);
	}

	private String getUnparsableErrorMessage() {
		return "The given AttributeFieldName is not parsable. AttributeFieldName: '" + attributeFieldName
			+ "'; Pattern: '" + ATTRIBUTE_FIELD_NAME_PATTERN + "'";
	}
}
