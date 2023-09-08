/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.CommonMarkControl;
import com.top_logic.layout.form.control.TextInputControl;

/**
 * {@link MultiLineText} rendering multi line values in <i>CommonMark</i> syntax.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommonMarkText extends MultiLineText {

	/** {@link CommonMarkText} instance. */
	@SuppressWarnings("hiding")
	public static final CommonMarkText INSTANCE;
	static {
		Config config = TypedConfiguration.newConfigItem(MultiLineText.Config.class);
		config.setImplementationClass(CommonMarkText.class);
		INSTANCE = (CommonMarkText) TypedConfigUtil.createInstance(config);
	}

	/**
	 * Creates a new {@link CommonMarkText}.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CommonMarkText(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected TextInputControl createControl(FormField field) {
		return new CommonMarkControl(field);
	}

}

