/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.function.Predicate;

import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.PartAnnotationOptions;
import com.top_logic.layout.form.model.utility.TypeKindFilter;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.provider.DefaultProvider;

/**
 * Base class for {@link DefaultProvider} option providers that select from those that have a
 * specific {@link TLTypeKind}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class StaticDefaultProviderOptions extends Function0<OptionModel<Class<?>>> {

	private Predicate<Class<?>> _filter;
	private AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link StaticDefaultProviderOptions}.
	 */
	public StaticDefaultProviderOptions(DeclarativeFormOptions options, TLTypeKind expectedKind) {
		_customizations = options.getCustomizations();
		_filter = new HiddenFilter(_customizations).and(new TypeKindFilter(_customizations, expectedKind));
	}

	@Override
	public OptionModel<Class<?>> apply() {
		return PartAnnotationOptions.findOptions(_customizations, DefaultProvider.class, _filter);
	}

}

