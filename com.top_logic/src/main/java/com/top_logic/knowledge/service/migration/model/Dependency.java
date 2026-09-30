/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.model;

/**
 * A synthetic dependency.
 */
public class Dependency {

	private final MigrationRef _migration;

	private final String _reason;

	/**
	 * Creates a {@link Dependency}.
	 */
	public Dependency(MigrationRef migration, String reason) {
		_migration = migration;
		_reason = reason;
	}

	/**
	 * The target of the dependency.
	 */
	public MigrationRef getMigration() {
		return _migration;
	}

	/**
	 * Description why this dependency was introduced.
	 */
	public String getReason() {
		return _reason;
	}
}
