/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.basic.html;

import com.top_logic.basic.exception.I18NException;

/**
 * Algorithm for filtering HTML contents for safety reasons.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface HTMLChecker {

	/**
	 * Checks the given HTML fragment to contain only safe contents.
	 * 
	 * <p>
	 * HTML content is safe, if it cannot be used to generate a cross-site-scripting attack when
	 * injected into the UI of an arbitrary user. Save content must be a well-formed XML fragment
	 * (XML without necessarily having a single root tag) consisting only of tags, text and
	 * comments. Only certain tag and attribute names are allow in safe content.
	 * </p>
	 * 
	 * @param html
	 *        The HTML string to be checked.
	 * @throws I18NException
	 *         If the given HTML contains content that is not guaranteed to be safe.
	 */
	void check(String html) throws I18NException;

	/**
	 * Checks the given tag name.
	 * 
	 * @param tag
	 *        The name of the tag.
	 * @throws I18NException
	 *         if the given tag is not allowed.
	 */
	void checkTag(String tag) throws I18NException;

	/**
	 * Checks the given attribute name and value.
	 * 
	 * @param tag
	 *        The tag in which the attribute occurs.
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The value of the attribute.
	 * @throws I18NException
	 *         if the given attribute value is not allowed.
	 */
	void checkAttribute(String tag, String attribute, String value) throws I18NException;

	/**
	 * Checks the given attribute value.
	 * 
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The value of the attribute.
	 * @throws I18NException
	 *         if the given attribute value is not allowed.
	 * 
	 * @see #checkAttribute(String, String, String)
	 */
	void checkAttributeValue(String attribute, String value) throws I18NException;

	/**
	 * The {@link AttributeChecker} for the given attribute name or <code>null</code>, if no checker
	 * is configured for this attribute.
	 */
	AttributeChecker getAttributeChecker(String attribute);

	/**
	 * Checks whether the given attribute is allowed.
	 * 
	 * @param tag
	 *        The tag in which the attribute occurs.
	 * @param attribute
	 *        The name of the attribute.
	 * @throws I18NException
	 *         if the given attribute is not allowed.
	 */
	void checkAttributeName(String tag, String attribute) throws I18NException;

	/**
	 * @param tag
	 *        To be checked tag name.
	 * @return TRUE if this tag is allowed, otherwise false.
	 */
	boolean isAllowedTagName(String tag);

	/**
	 * @param attribute
	 *        To be checked attribute name.
	 * @return TRUE if this attribute is allowed, otherwise false.
	 */
	boolean isAllowedAttributeName(String attribute);

}
