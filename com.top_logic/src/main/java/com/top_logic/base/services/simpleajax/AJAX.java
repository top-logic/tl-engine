/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Configuration for AJAX.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface AJAX extends ConfigurationItem {

	/** Configuration name for the value of {@link #getProgressBarDelay()}. */
	String PROGRESS_BAR_DELAY = "progressBarDelay";

	/** Configuration name for the value of {@link #getUseWaitPaneInFormula()}. */
	String USE_WAIT_PANE_IN_FORMULA = "useWaitPaneInFormula";

	/** Configuration name for the value of {@link #getWaitPaneDelay()}. */
	String WAIT_PANE_DELAY = "waitPaneDelay";

	/**
	 * Whether the wait pane is automatically displayed when a user action is made in a form.
	 */
	@Name(USE_WAIT_PANE_IN_FORMULA)
	boolean getUseWaitPaneInFormula();

	/**
	 * Time in milliseconds that is waited before the progress bar in the button area is
	 * automatically displayed.
	 */
	@Name(PROGRESS_BAR_DELAY)
	@LongDefault(500)
	@Format(MillisFormat.class)
	long getProgressBarDelay();

	/**
	 * Time in milliseconds that is waited before the wait pane is automatically displayed.
	 */
	@Name(WAIT_PANE_DELAY)
	@LongDefault(1000)
	@Format(MillisFormat.class)
	long getWaitPaneDelay();
}
