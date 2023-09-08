/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.formats;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MORepository;

/**
 * {@link ValueParser} that resolves types from the {@link MORepository} context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MigrationValueParser extends ValueParser<ConfigurationError> {
	private MORepository _repository;

	/**
	 * Creates a {@link MigrationValueParser}.
	 * @param repository
	 *        The {@link MORepository} type repository to resolve type names.
	 */
	public MigrationValueParser(MORepository repository) {
		_repository = repository;
	}

	@Override
	protected MetaObject resolve(String typeName) throws ConfigurationError {
		try {
			return _repository.getType(typeName);
		} catch (UnknownTypeException ex) {
			throw new ConfigurationError("Unknown type: " + typeName, ex);
		}
	}

	@Override
	protected ConfigurationError parseError(String message, Throwable cause) throws ConfigurationError {
		throw new ConfigurationError(message, cause);
	}
}