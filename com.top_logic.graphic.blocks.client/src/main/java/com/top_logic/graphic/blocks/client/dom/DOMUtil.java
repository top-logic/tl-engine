/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.dom;

import com.google.gwt.dom.client.Element;

/**
 * Utilities for working on DOM {@link Element}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DOMUtil {

	/**
	 * Workaround for {@link Element#addClassName(String)} not working on SVG elements.
	 * 
	 * @see #getClassName(Element)
	 */
	public static boolean addClassName(Element element, String className) {
		className = DOMUtil.trimClassName(className);
	
		// Get the current style string.
		String oldClassName = DOMUtil.getClassName(element);
		int idx = DOMUtil.indexOfName(oldClassName, className);
	
		// Only add the style if it's not already present.
		if (idx == -1) {
			if (oldClassName.length() > 0) {
				setClassName(element, oldClassName + " " + className);
			} else {
				setClassName(element, className);
			}
			return true;
		}
		return false;
	}

	/**
	 * Workaround for {@link Element#removeClassName(String)} not working on SVG elements.
	 * 
	 * @see #getClassName(Element)
	 */
	public static boolean removeClassName(Element element, String className) {
		className = DOMUtil.trimClassName(className);
	
		// Get the current style string.
		String oldStyle = getClassName(element);
		int idx = DOMUtil.indexOfName(oldStyle, className);
	
		// Don't try to remove the style if it's not there.
		if (idx != -1) {
			// Get the leading and trailing parts, without the removed name.
			String begin = oldStyle.substring(0, idx).trim();
			String end = oldStyle.substring(idx + className.length()).trim();
	
			// Some contortions to make sure we don't leave extra spaces.
			String newClassName;
			if (begin.length() == 0) {
				newClassName = end;
			} else if (end.length() == 0) {
				newClassName = begin;
			} else {
				newClassName = begin + " " + end;
			}
	
			setClassName(element, newClassName);
			return true;
		}
		return false;
	}

	/**
	 * Workaround for {@link Element#setClassName(String)} not working for SVG elements.
	 */
	public static native void setClassName(Element element, String className) /*-{
		var newValue = className || "";
		var oldValue = element.className;
		if (!oldValue || !oldValue.baseVal) {
			element.className = newValue;
		} else {
			oldValue.baseVal = newValue;
		}
	}-*/;

	/**
	 * Workaround for SVG elements returning an "animated class name" object from
	 * {@link Element#getClassName()}. Such object has not the same properties and functions as a
	 * string object.
	 */
	public static native String getClassName(Element element) /*-{
		var result = element.className;
		if (!result) {
			return "";
		}
		return result.baseVal || result;
	}-*/;

	static String trimClassName(String className) {
		assert (className != null) : "Unexpectedly null class name";
		className = className.trim();
		assert !className.isEmpty() : "Unexpectedly empty class name";
		return className;
	}

	static int indexOfName(String nameList, String name) {
		int idx = nameList.indexOf(name);
	
		// Calculate matching index.
		while (idx != -1) {
			if (idx == 0 || nameList.charAt(idx - 1) == ' ') {
				int last = idx + name.length();
				int lastPos = nameList.length();
				if ((last == lastPos)
					|| ((last < lastPos) && (nameList.charAt(last) == ' '))) {
					break;
				}
			}
			idx = nameList.indexOf(name, idx + 1);
		}
	
		return idx;
	}

}
