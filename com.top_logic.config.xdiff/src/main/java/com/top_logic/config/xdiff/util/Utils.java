/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.config.xdiff.XApplyException;
import com.top_logic.config.xdiff.ms.MSXDiffSchema;

/**
 * Utility functions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Utils {

	private static final String UTF_8 = "utf-8";

	/**
	 * Updates the given {@link MessageDigest} with an UTF-8 representation of the given string.
	 */
	public static void updateUTF8(MessageDigest md5, String s) {
		if (s != null) {
			try {
				md5.update(s.getBytes(UTF_8));
			} catch (UnsupportedEncodingException ex) {
				throw (AssertionError) new AssertionError("No utf-8 encoding supported.").initCause(ex);
			}
		}
	}

	/**
	 * Whether the given objects are equal or both <code>null</code>.
	 */
	public static boolean equalsNullsafe(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	/**
	 * The intrinsic order of the given two {@link Comparable}s extended to <code>null</code>.
	 */
	public static <C extends Comparable<C>> int compareNullsafe(C c1, C c2) {
		if (c1 == null) {
			if (c2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (c2 == null) {
				return 1;
			} else {
				return c1.compareTo(c2);
			}
		}
	}

	/**
	 * Converts the varargs parameter value into a {@link List}.
	 */
	public static <T> List<T> list(T... children) {
		if (children == null || children.length == 0) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(children);
		}
	}

	/**
	 * @see #nonNullList(Collection)
	 */
	public static <T> ArrayList<T> nonNullList(T... originalValues) {
		return nonNullList(list(originalValues));
	}

	/**
	 * Filters non <code>null</code> values from the given collection and returns them in a new
	 * list.
	 * 
	 * @param <T>
	 *        The element type of the values.
	 * @param originalValues
	 *        The original values potentially containing <code>null</code> values.
	 * @return All non-<code>null</code> values for the original collection.
	 */
	public static <T> ArrayList<T> nonNullList(Collection<T> originalValues) {
		ArrayList<T> nonNullValues = new ArrayList<>(originalValues.size());
		for (T attr : originalValues) {
			if (attr == null) {
				continue;
			}
			nonNullValues.add(attr);
		}
		return nonNullValues;
	}

	/**
	 * Whether the given string solely consists of {@link Character#isWhitespace(char)} characters.
	 */
	public static boolean isWhiteSpace(String str) {
		for (int n = 0, cnt = str.length(); n < cnt; n++) {
			if (!Character.isWhitespace(str.charAt(n))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether the first ordered list is a subset of the second ordered list according to the given
	 * order.
	 * 
	 * @param <T>
	 *        The element type of the lists.
	 * @param order
	 *        The order according to which the given lists are sorted and which decides about
	 *        equality of list elements.
	 * @param orderedSubSet
	 *        The candidate subset.
	 * @param orderedSuperSet
	 *        The candidate super set.
	 * @return Whether the candidate subset is a subset of the candidate super set.
	 */
	public static <T> boolean isSubset(Comparator<? super T> order, List<? extends T> orderedSubSet,
			List<? extends T> orderedSuperSet) {
	
		int subSetCnt = orderedSubSet.size();
		int superSetCnt = orderedSuperSet.size();
	
		int subSetIdx = 0;
		int superSetIdx = 0;
		while (subSetIdx < subSetCnt && superSetIdx < superSetCnt) {
			T item1 = orderedSubSet.get(subSetIdx);
			T item2 = orderedSuperSet.get(superSetIdx);
	
			int comparision = order.compare(item1, item2);
			if (comparision == 0) {
				subSetIdx++;
				superSetIdx++;
			} else if (comparision > 0) {
				superSetIdx++;
			} else {
				return false;
			}
		}
	
		boolean allSubSetElementsConsumed = subSetIdx == subSetCnt;
		return allSubSetElementsConsumed;
	}

	/**
	 * Make sure that the given elemnt is empty.
	 * 
	 * @param element
	 *        The {@link Element} to check.
	 * 
	 * @throws XApplyException
	 *         If the check fails.
	 */
	public static void checkEmpty(Element element) throws XApplyException {
		if (element.getFirstChild() != null) {
			throw new XApplyException("Element '" + MSXDiffSchema.ATTRIBUTE_SET_ELEMENT + "' must be empty.");
		}
	}

	/**
	 * Make sure that the given attribute value is non-<code>null</code>.
	 * 
	 * @param elementName
	 *        The element name (for generating the error message).
	 * @param attributeName
	 *        The attribute name (for generating the error message).
	 * @param value
	 *        The attribute value to check.
	 * 
	 * @throws XApplyException
	 *         If the check fails.
	 */
	public static void checkRequiredAttribute(String elementName, String attributeName, String value)
			throws XApplyException {
		if (value == null) {
			throw new XApplyException("Attribute '" + attributeName + "' of '" + elementName + "' is required.");
		}
	}

	/**
	 * Looks up the given (non-namespaced) attribute from the given {@link Element}, but return
	 * <code>null</code> instead of the empty string, if the attribute was not declared.
	 * 
	 * @param element
	 *        The {@link Element} to access.
	 * @param attributeName
	 *        The attribute local name to look up.
	 * @return The attribute value, or <code>null</code>, if the attribute was not declared.
	 */
	public static String getAttributeOrNull(Element element, String attributeName) {
		String value = element.getAttributeNS(null, attributeName);
		if (value.length() == 0) {
			// Quirks to be able do decide, whether the attribute was not declared or not.
			return element.hasAttributeNS(null, attributeName) ? value : null;
		} else {
			return value;
		}
	}

	/**
	 * Test that only the declared attributes are set on the given {@link Element}.
	 * 
	 * @param declaredAttributes
	 *        The allowed attribute local names.
	 * @param element
	 *        The {@link Element} to check.
	 * @throws XApplyException
	 *         If other as the declared attributes are used on the given element, or a namespaced
	 *         attribute is used.
	 */
	public static void checkAttributes(Set<String> declaredAttributes, Element element) throws XApplyException {
		NamedNodeMap attributes = element.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Node attribute = attributes.item(n);
			if (attribute.getNamespaceURI() != null) {
				throw new XApplyException("No namespace expected on attribute '" + attribute.getLocalName()
					+ "' of element '" + element.getLocalName() + "'.");
			}
			if (!declaredAttributes.contains(attribute.getLocalName())) {
				throw new XApplyException("Usage of undeclared attribute '" + attribute.getLocalName()
					+ "' of element '" + element.getLocalName() + "'.");
			}
		}
	}

}
