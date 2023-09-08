/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.Collections;
import java.util.function.Predicate;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.PartAnnotationOptions;
import com.top_logic.layout.form.model.utility.TargetTypeFilter;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.provider.DefaultProvider;

/**
 * {@link Function1} that returns the {@link DefaultProvider} that are suitable for the
 * {@link TLModelPart} given by a qualified name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultProviderOptions extends Function2<OptionModel<Class<?>>, TLType, TLTypeKind> {

	private AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link DefaultProviderOptions}.
	 */
	@CalledByReflection
	public DefaultProviderOptions(DeclarativeFormOptions options) {
		_customizations = options.getCustomizations();
	}

	@Override
	public OptionModel<Class<?>> apply(TLType type, TLTypeKind kind) {
		if (type == null) {
			return new DefaultListOptionModel<>(Collections.emptyList());
		} else {
			return PartAnnotationOptions.findOptions(_customizations, DefaultProvider.class,
				createOptionsFilter(type, kind));
		}
	}

	private Predicate<Class<?>> createOptionsFilter(TLType type, TLTypeKind kind) {
		Predicate<Class<?>> hiddenFilter = new HiddenFilter(_customizations);

		return hiddenFilter.and(new TargetTypeFilter(_customizations, type, kind));
	}

}

