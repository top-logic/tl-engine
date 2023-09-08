/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.gui;

import com.top_logic.layout.form.FormContainer;

/**
 * Builds objects with user interaction.
 * 
 * <p>
 * In {@link #createUI(FormContainer, Object)} the UI is created with all all relevant form members
 * necessary for user interaction. In {@link #build(FormContainer)} the user input can be retrieved
 * and the result object is being built.
 * </p>
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public interface InteractiveBuilder<R, A> {

	/**
	 * Hook to fill container with all necessary members for user-interaction.
	 * 
	 * @param container
	 *        the container to fill
	 * @param arg
	 *        a global context e.f. if the container itself has sub-container filled usind an
	 *        {@link InteractiveBuilder}
	 */
	public void createUI(FormContainer container, A arg);

	/**
	 * @param container
	 *        the container that has been filled in {@link #createUI(FormContainer, Object)}
	 *        and has been displayed to the user for interaction.
	 * @return the result-object based on the input from the container
	 */
	public R build(FormContainer container);

}
