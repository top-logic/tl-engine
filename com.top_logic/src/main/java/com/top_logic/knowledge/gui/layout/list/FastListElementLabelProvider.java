/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.list;

import com.top_logic.basic.Named;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * Constant holder describing how labels for FastListElements are fetched.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FastListElementLabelProvider implements LabelProvider {

	public static LabelProvider INSTANCE = new FastListElementLabelProvider();

	/**
	 * Creates a {@link FastListElementLabelProvider}.
	 */
	private FastListElementLabelProvider() {
		super();
	}

	/**
	 * Fetch label via base LabelProvider then translate via Resources.
	 */
	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}

		// Cast to Named is for backwards compatibility.
		return Resources.getInstance().getString(internalKey((Named) object));
	}

	/**
	 * @see TLModelNamingConvention#classifierKey(TLClassifier)
	 */
	public static ResKey labelKey(TLClassifier classifier) {
		return TLModelNamingConvention.classifierKey(classifier);
	}

	/**
	 * @see TLModelNamingConvention#enumKey(TLEnumeration)
	 */
	public static ResKey labelKey(TLEnumeration classification) {
		return TLModelNamingConvention.enumKey(classification);
	}

	private static ResKey internalKey(Named object) {
		if (object instanceof TLClassifier) {
			return labelKey((TLClassifier) object);
		}
		if (object instanceof TLEnumeration) {
			return TLModelNamingConvention.enumKey((TLEnumeration) object);
		}

		// Compatibility fallback.
		return labelKey(object.getName());
	}

	private static ResKey labelKey(String name) {
		return ResKey.fallback(
			ResKey.deprecated(ResKey.legacy(name)),
			ResKey.text(name));
	}

}
