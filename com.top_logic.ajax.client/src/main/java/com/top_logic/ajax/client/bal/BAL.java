/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.bal;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * GWT wrapper for the <i>TopLogic</i> "browser abstraction layer".
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "BAL")
public class BAL {

	/**
	 * Retrieves the mouse coordinates of the given event relative to the given element.
	 */
	public native static Coordinates relativeMouseCoordinates(NativeEvent event, Element element);

	/**
	 * Retrieves the coordinates of the given element.
	 */
	public native static Coordinates getAbsoluteElementPosition(Element element);

}
