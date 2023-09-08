/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.PageContext;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Utilities for rendering model elements.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaTagUtil {

	private static final String DISPLAYED_ATTRIBUTES_ATTRIBUTE = "displayedAttributes";

	/**
	 * Marks a certain model attribute as being displayed at the current page.
	 */
	public static void addDisplayedAttribute(PageContext pageContext, Wrapper model, TLStructuredTypePart attribute) {
		Map<Wrapper, Set<TLStructuredTypePart>> displayedAttributes = mkDisplayedAttributes(pageContext);
	
		Set<TLStructuredTypePart> attributesOfObject = displayedAttributes.get(model);
		if (attributesOfObject == null) {
			attributesOfObject = new HashSet<>();
			displayedAttributes.put(model, attributesOfObject);
		}
	
		attributesOfObject.add(attribute);
	}

	/**
	 * Whether a certain model attribute has already been displayed on the current page.
	 */
	public static boolean wasDisplayed(PageContext pageContext, TLObject model, TLStructuredTypePart attribute) {
		Map<Wrapper, Set<TLStructuredTypePart>> displayedAttributes = lookupDisplayedAttributes(pageContext);
		if (displayedAttributes == null) {
			return false;
		}
	
		Set<TLStructuredTypePart> displayedAttributesOfObject = displayedAttributes.get(model);
		if (displayedAttributesOfObject == null) {
			return false;
		}
	
		return displayedAttributesOfObject.contains(attribute);
	}

	private static Map<Wrapper, Set<TLStructuredTypePart>> mkDisplayedAttributes(PageContext pageContext) {
		Map<Wrapper, Set<TLStructuredTypePart>> displayedAttributes = lookupDisplayedAttributes(pageContext);
		if (displayedAttributes == null) {
			displayedAttributes = new HashMap<>();
			pageContext.setAttribute(DISPLAYED_ATTRIBUTES_ATTRIBUTE, displayedAttributes);
		}
		return displayedAttributes;
	}

	private static Map<Wrapper, Set<TLStructuredTypePart>> lookupDisplayedAttributes(PageContext pageContext) {
		@SuppressWarnings("unchecked")
		Map<Wrapper, Set<TLStructuredTypePart>> displayedAttributes =
			(Map<Wrapper, Set<TLStructuredTypePart>>) pageContext.getAttribute(DISPLAYED_ATTRIBUTES_ATTRIBUTE);
		return displayedAttributes;
	}

}
