/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.display;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * Says that the parts of a composition are shown folded away until the reader opens them.
 *
 * <p>
 * For parts that belong to an object but are not what a reader looks at first - the sources a
 * research run read and did not cite, say. A form that shows the object puts such a composition in a
 * group of its own, closed; the chat's side pane does.
 * </p>
 *
 * <pre>
 * &lt;reference name="alsoRead" composite="true" ...&gt;
 *     &lt;annotations&gt;
 *         &lt;collapsed/&gt;
 *     &lt;/annotations&gt;
 * &lt;/reference&gt;
 * </pre>
 */
@TagName(CollapsedDisplay.TAG_NAME)
@TargetType(value = { TLTypeKind.COMPOSITION, TLTypeKind.REF })
@InApp(classifiers = { "formRelevant" })
public interface CollapsedDisplay extends BooleanAnnotation, TLAttributeAnnotation {

	/** The tag of the annotation in a model file. */
	String TAG_NAME = "collapsed";

	/**
	 * Whether {@code part} is shown collapsed.
	 *
	 * @param part
	 *        The attribute; may be {@code null}.
	 */
	static boolean isCollapsed(TLStructuredTypePart part) {
		if (part == null) {
			return false;
		}
		CollapsedDisplay annotation = TLAnnotations.getAnnotation(part, CollapsedDisplay.class);
		return annotation != null && annotation.getValue();
	}
}
