/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.model.TLModelPart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link OptionMapping} for {@link TLModelPart}s.
 * 
 * @param <N>
 *        The type of the reference that is used as selection.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractModelPartMapping<N> implements OptionMapping {

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}

		if (selection instanceof Iterable<?>) {
			List<Object> result = new ArrayList<>();

			for (Object name : (Iterable<?>) selection) {
				Object option = asOptionSingleton(name);
				if (option != null) {
					result.add(option);
				}
			}

			return result;
		} else {
			return asOptionSingleton(selection);
		}
	}

	private TLModelPart asOptionSingleton(Object selection) {
		try {
			return resolveName(name(selection));
		} catch (ConfigurationException | TopLogicException ex) {
			Logger.error("Cannot resolve option for '" + selection + "'.", ex, AbstractModelPartMapping.class);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private N name(Object selection) {
		return (N) selection;
	}

	@Override
	public Object toSelection(Object option) {
		if (option == null) {
			return null;
		}

		if (option instanceof Iterable<?>) {
			List<Object> result = new ArrayList<>();

			for (Object o : (Iterable<?>) option) {
				result.add(buildName((TLModelPart) o));
			}

			return result;
		}

		return buildName((TLModelPart) option);
	}

	/**
	 * Resolves the name to a {@link TLModelPart}.
	 */
	protected abstract TLModelPart resolveName(N name) throws ConfigurationException;

	/**
	 * Builds a name for the given {@link TLModelPart}.
	 */
	protected abstract N buildName(TLModelPart option);

}
