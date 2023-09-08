/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accordion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.link.AbstractLinkRenderer;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.AbstractButtonLinkRenderer;
import com.top_logic.layout.form.control.AbstractButtonRenderer;

/**
 * {@link AbstractButtonRenderer} that creates a command link in the style of an entry in the
 * {@link AccordionControl}.
 * 
 * <p>
 * The visual representation is created by delegating to the low-level
 * {@link AccordionEntryRenderer}.
 * </p>
 * 
 * @see AccordionEntryRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccordionButtonRenderer<C extends AccordionButtonRenderer.Config<?>>
		extends AbstractButtonLinkRenderer<C> {

	/**
	 * Typed configuration interface definition for {@link AccordionButtonRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AccordionButtonRenderer<?>> extends AbstractButtonLinkRenderer.Config<I> {
		// configuration interface definition
	}

	/**
	 * Create a {@link AccordionButtonRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AccordionButtonRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected String getTypeCssClass(AbstractButtonControl<?> button) {
		return null;
	}

	@Override
	protected AbstractLinkRenderer commandRenderer() {
		return AccordionEntryRenderer.INSTANCE;
	}

}
