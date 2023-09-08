/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.util.TLModelUtil;

/**
 * Option provider function for a {@link PropertyDescriptor} of type
 * {@link PolymorphicConfiguration}.
 * 
 * <p>
 * Computes all options for compatible implementation classes that are annotated {@link InApp}. If a
 * second annotation {@link AcceptableClassifiers} is given, only those {@link InApp}
 * implementations are provided that have a matching set of {@link InApp#classifiers()}. An
 * implementation class is eligible for being selected, if it has at least one classifier in common
 * with the {@link AcceptableClassifiers} annotation, or it is not classified at all (has no
 * classifiers).
 * </p>
 * 
 * @see InApp
 * @see AcceptableClassifiers
 * @see Options#fun()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InAppImplementations extends AllInAppImplementations {

	private String[] _requiredClassifiers;

	private boolean _unclassified;

	/**
	 * Creates {@link InAppImplementations}.
	 */
	@CalledByReflection
	public InAppImplementations(DeclarativeFormOptions options) {
		super(options);
		PropertyDescriptor property = options.getProperty();
		AcceptableClassifiers acceptableClassifiers = property.getAnnotation(AcceptableClassifiers.class);
		if (acceptableClassifiers != null) {
			_requiredClassifiers = acceptableClassifiers.value();
			_unclassified = acceptableClassifiers.unclassified();
		}
	}

	@Override
	protected Stream<? extends Class<?>> acceptableImplementations() {
		Stream<? extends Class<?>> result = super.acceptableImplementations();
		if (_requiredClassifiers != null) {
			result = result.filter(TLModelUtil.classifierPredicate(_requiredClassifiers, _unclassified));
		}
		return result;
	}

}
