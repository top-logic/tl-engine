/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.basic.ResourceRenderer.*;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.renderer.UnclickableCellRenderer;
import com.top_logic.layout.table.renderer.UniformCellRenderer;

/**
 * A {@link UniformCellRenderer} that does not write clickable content.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UnclickableUniformCellRenderer extends UniformCellRenderer implements UnclickableCellRenderer {

	/** Creates a {@link UnclickableUniformCellRenderer}. */
	private UnclickableUniformCellRenderer(ResourceProvider resourceProvider, boolean withImage) {
		super(newResourceRenderer(resourceProvider, !USE_LINK, USE_TOOLTIP, withImage));
	}

	/**
	 * Creates a {@link UnclickableUniformCellRenderer} that writes images.
	 * 
	 * @param resourceProvider
	 *        Is not allowed to be null.
	 */
	public static UnclickableUniformCellRenderer withImage(ResourceProvider resourceProvider) {
		return new UnclickableUniformCellRenderer(resourceProvider, USE_IMAGE);
	}

	/**
	 * Creates a {@link UnclickableUniformCellRenderer} that does not write images.
	 * 
	 * @param resourceProvider
	 *        Is not allowed to be null.
	 */
	public static UnclickableUniformCellRenderer withoutImage(ResourceProvider resourceProvider) {
		return new UnclickableUniformCellRenderer(resourceProvider, !USE_IMAGE);
	}

}
