/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Collection;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Utility methods for creating drag'n drop enabled {@link Control}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DNDUtil {

	/**
	 * Writes the {@link HTMLConstants#TL_DRAG_N_DROP} attribute for the {@link Drag} source
	 * control.
	 * @param out
	 *        The current writer.
	 * @param scope
	 *        The {@link FrameScope} in which the given {@link Drop} targets will reside.
	 * @param targets
	 *        The potential {@link Drop} targets.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	public static <T> void writeDNDInfo(TagWriter out, FrameScope scope, Collection<Drop<? super T>> targets) throws IOException {
		if (targets.size() > 0) {
			out.beginAttribute(HTMLConstants.TL_DRAG_N_DROP);
			out.append("[");
			boolean first = true;
			for (Drop<?> drop : targets) {
				if (first) {
					first = false;
				} else {
					out.append(",");
				}
				ensureId(scope, drop);
				out.append('\'');
				out.append(drop.getID());
				out.append('\'');
			}
			/* replace last ',' by ']' */
			out.append(']');
			out.endAttribute();
		}
	}

	static void ensureId(FrameScope scope, Drop<?> drop) {
		if (drop instanceof AbstractControlBase) {
			((AbstractControlBase) drop).fetchID(scope);
		}
	}

}
