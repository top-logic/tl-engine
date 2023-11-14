/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * An adapter that is a {@link ControlProvider} and has an {@link HTMLFragmentProvider}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FragmentControlProvider extends AbstractConfiguredInstance<FragmentControlProvider.Config>
		implements ControlProvider {

	/** {@link ConfigurationItem} for the {@link FragmentControlProvider}. */
	public interface Config extends PolymorphicConfiguration<FragmentControlProvider> {

		/** Property name of {@link #getFragmentProvider()}. */
		String FRAGMENT_PROVIDER = "fragmentProvider";

		/** If is is null, an empty fragment is created, which writes nothing. */
		@Name(FRAGMENT_PROVIDER)
		@InstanceFormat
		HTMLFragmentProvider getFragmentProvider();

	}

	private final HTMLFragmentProvider _fragmentProvider;

	/** {@link TypedConfiguration} constructor for {@link FragmentControlProvider}. */
	public FragmentControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_fragmentProvider = config.getFragmentProvider();
	}

	@Override
	public Control createControl(Object model, String cssStyle) {
		return new FragmentControl(createFragment(model, cssStyle));
	}

	@Override
	public HTMLFragment createFragment(Object model, String cssStyle) {
		return createInternal(model, cssStyle);
	}

	private HTMLFragment createInternal(Object model, String cssStyle) {
		if (_fragmentProvider == null) {
			return Fragments.empty();
		}
		HTMLFragment fragment = _fragmentProvider.createFragment(model, cssStyle);
		if (fragment == null) {
			return Fragments.empty();
		}
		return fragment;
	}

}
