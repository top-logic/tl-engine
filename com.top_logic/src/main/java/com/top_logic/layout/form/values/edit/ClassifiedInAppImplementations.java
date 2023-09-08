/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.stream.Stream;

import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AllInAppImplementations} option provider that filters implementations based on a given
 * classifier list.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ClassifiedInAppImplementations extends AllInAppImplementations {

	private String[] _requiredClassifiers;

	/** 
	 * Creates a {@link ClassifiedInAppImplementations}.
	 */
	public ClassifiedInAppImplementations(DeclarativeFormOptions options, String... requiredClassifiers) {
		super(options);
		_requiredClassifiers = requiredClassifiers;
	}

	@Override
	protected Stream<? extends Class<?>> acceptableImplementations() {
		return super.acceptableImplementations().filter(TLModelUtil.classifierPredicate(_requiredClassifiers, true));
	}

}