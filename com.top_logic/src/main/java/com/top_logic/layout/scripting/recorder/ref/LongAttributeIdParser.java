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
 * This implementation cares for long knowledge object ids.
 * </p>
 * 
 * @see ShortAttributeIdParser
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class LongAttributeIdParser extends AttributeIdParser {

	// Example field name: att_1a5af9f_122fefe54b9__7f77_1a5af9f_122fefe54b9__7dd3
	// Other example: att_65016b18_130029adae9__7e9d_mandatorStructure
	// Example object name: 1a5af9f:122fefe54b9:-7f48

	private static final String DOMN_PATTERN = "(att)";

	private static final String KO_ID_PATTERN = "(_)?([a-zA-Z0-9]+)_([a-zA-Z0-9]+)_(_)?([a-zA-Z0-9]+)";
	private static final String KO_ID_PATTERN_SIMPLE = "(\\S*)";
	private static final String ATTRIBUTE_FIELD_NAME_PATTERN =
		"^(?:" + DOMN_PATTERN + "_)?(?:" + KO_ID_PATTERN + ")(?:_" + KO_ID_PATTERN_SIMPLE + ")?$";

	// Only once, compiling RexExps is expensive.
	private static final Pattern COMPILED_KO_ID_PATTERN = Pattern.compile(KO_ID_PATTERN);
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
	LongAttributeIdParser(String attributeFieldName) {
		super(attributeFieldName);
		Matcher matcher = COMPILED_FIELD_NAME_ATTRIBUTE_PATTERN.matcher(attributeFieldName);

		// Matcher.matches has two functions:
		// 1.) It returns whether the Matcher matches.
		// 2.) It initializes the Match data which can than be requested via 'start', 'end' and
		// 'group' methods.
		// Ugly design. :-(
		if (matcher.matches()) {
			attributeId = extractId(matcher, 2);
			objectId = extractObjectId(matcher, 7);
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
		String encodedObjectId = matcher.group(matcherGroupIndexOffset);
		if (encodedObjectId == null) {
			return null;
		}
		Matcher objectIdMatcher = COMPILED_KO_ID_PATTERN.matcher(encodedObjectId);
		return objectIdMatcher.matches() ? extractId(objectIdMatcher, 1) : encodedObjectId;
	}

	private String extractId(Matcher matcher, int matcherGroupIndexOffset) {

		boolean isNegative = matcher.group(matcherGroupIndexOffset) != null;
		String idPart1 = matcher.group(1 + matcherGroupIndexOffset);
		String idPart2 = matcher.group(2 + matcherGroupIndexOffset);
		boolean isNegative2 = matcher.group(3 + matcherGroupIndexOffset) != null;
		String idPart3 = matcher.group(4 + matcherGroupIndexOffset);

		if ((idPart1 == null) || (idPart2 == null) || (idPart3 == null)) {
			return null;
		} else {
			return (isNegative ? "-" : "") + idPart1 + ':' + idPart2 + ":" + (isNegative2 ? "-" : "") + idPart3;
		}
	}

	private String getUnparsableErrorMessage() {
		return "The given AttributeFieldName is not parsable. AttributeFieldName: '" + attributeFieldName
			+ "'; Pattern: '" + ATTRIBUTE_FIELD_NAME_PATTERN + "'";
	}
}
