/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.demo.model.annotations;

import com.top_logic.basic.func.IFunction3;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DynamicLabel;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link DynamicLabel} function adding the users time zone to the label.
 *
 * @see DynamicLabel
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelWithTimeZone implements IFunction3<Object, TLObject, ResKey, TLStructuredTypePart> {

	@Override
	public Object apply(TLObject object, ResKey defaultLabel, TLStructuredTypePart attribute) {
		String timeZone = TLContext.getTimeZone().getID();
		return Resources.getInstance().getString(defaultLabel) + " (" + timeZone + ")";
	}

}

