/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.resource;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.label.NullSafeLabelProvider;

/**
 * A {@link ResourceProvider} that returns null for the null value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class NullSafeResourceProvider extends NullSafeLabelProvider implements ResourceProvider {

	@Override
	public final String getType(Object model) {
		if (model == null) {
			return null;
		}
		return getTypeNullSafe(model);
	}

	/**
	 * Is called by {@link #getType(Object)} when the model is not null.
	 * 
	 * @param model
	 *        Never null.
	 */
	protected String getTypeNullSafe(Object model) {
		return null;
	}

	@Override
	public final String getTooltip(Object model) {
		if (model == null) {
			return null;
		}
		return getTooltipNullSafe(model);
	}

	/**
	 * Is called by {@link #getTooltip(Object)} when the model is not null.
	 * 
	 * @param model
	 *        Never null.
	 */
	protected String getTooltipNullSafe(Object model) {
		return null;
	}

	@Override
	public final ThemeImage getImage(Object model, Flavor flavor) {
		if (model == null) {
			return null;
		}
		return getImageNullSafe(model, flavor);
	}

	/**
	 * Is called by {@link #getImage(Object, Flavor)} when the model is not null.
	 * 
	 * @param model
	 *        Never null.
	 * @param flavor
	 *        See: {@link #getLink(DisplayContext, Object)}
	 */
	protected ThemeImage getImageNullSafe(Object model, Flavor flavor) {
		return null;
	}

	@Override
	public final String getLink(DisplayContext context, Object model) {
		if (model == null) {
			return null;
		}
		return getLinkNullSafe(context, model);
	}

	/**
	 * Is called by {@link #getLink(DisplayContext, Object)} when the model is not null.
	 * 
	 * @param context
	 *        See: {@link #getLink(DisplayContext, Object)}
	 * @param model
	 *        Never null.
	 */
	protected String getLinkNullSafe(DisplayContext context, Object model) {
		return null;
	}

	@Override
	public final String getCssClass(Object model) {
		if (model == null) {
			return null;
		}
		return getCssClassNullSafe(model);
	}

	/**
	 * Is called by {@link #getCssClass(Object)} when the model is not null.
	 * 
	 * @param model
	 *        Never null.
	 */
	protected String getCssClassNullSafe(Object model) {
		return null;
	}

}
