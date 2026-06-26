/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.contribution;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;

/**
 * A single contribution of a view into a named extension point declared by some upstream view.
 *
 * <p>
 * A contribution is pure wiring: it names the {@link #getTarget() extension point} to inject into
 * and the {@link #getView() view file} to embed there, plus the presentation ({@link #getLabel()
 * label}, {@link #getIcon() icon}, {@link #getRoute() route}) and {@link #getRank() ordering} of the
 * produced entry. The contributed content itself stays an ordinary {@code .view.xml} loaded through
 * the regular {@code <view-ref>} machinery.
 * </p>
 *
 * <p>
 * Contributions are collected across all modules by {@link ViewContributionService}; because the
 * service's list is keyed by {@link #getId()}, a module appends contributions without any module
 * depending on it.
 * </p>
 */
public interface Contribution extends ConfigurationItem {

	/** Configuration name for {@link #getId()}. */
	String ID = "id";

	/** Configuration name for {@link #getTarget()}. */
	String TARGET = "target";

	/** Configuration name for {@link #getView()}. */
	String VIEW = "view";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/** Configuration name for {@link #getIcon()}. */
	String ICON = "icon";

	/** Configuration name for {@link #getRoute()}. */
	String ROUTE = "route";

	/** Configuration name for {@link #getRank()}. */
	String RANK = "rank";

	/**
	 * Stable unique identifier of this contribution.
	 *
	 * <p>
	 * Serves as the merge key across modules (a downstream module may override an upstream
	 * contribution by reusing its id) and as the default {@link #getRoute() route segment} of the
	 * produced entry.
	 * </p>
	 */
	@Name(ID)
	@Mandatory
	String getId();

	/**
	 * The id of the extension point to contribute into (e.g. the {@code extension-id} of a
	 * {@code <tab-bar>}).
	 */
	@Name(TARGET)
	@Mandatory
	String getTarget();

	/**
	 * Path of the {@code .view.xml} to embed, relative to {@code /WEB-INF/views/}.
	 */
	@Name(VIEW)
	@Mandatory
	String getView();

	/**
	 * Presentation label of the produced entry (e.g. the tab label).
	 */
	@Name(LABEL)
	ResKey getLabel();

	/**
	 * CSS icon class shown on the produced entry (e.g. {@code "css:bi bi-terminal"}), or empty for
	 * no icon.
	 */
	@Name(ICON)
	String getIcon();

	/**
	 * Route segment of the produced entry. Defaults to {@link #getId()} when empty.
	 */
	@Name(ROUTE)
	String getRoute();

	/**
	 * Order of this contribution among all contributions to the same {@link #getTarget() target};
	 * lower ranks come first.
	 */
	@Name(RANK)
	@IntDefault(100)
	int getRank();
}
