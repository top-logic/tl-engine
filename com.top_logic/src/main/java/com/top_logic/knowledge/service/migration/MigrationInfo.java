/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.Collections;
import java.util.List;

/**
 * Description of migrations to deploy.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MigrationInfo {

	/**
	 * No migrations, no downgrade.
	 */
	public static final MigrationInfo NO_MIGRATION = new MigrationInfo(false, Collections.emptyList());

	private final boolean _downGrade;

	private final List<MigrationConfig> _migrations;

	/**
	 * Creates a {@link MigrationInfo}.
	 */
	private MigrationInfo(boolean downGrade, List<MigrationConfig> migrations) {
		_downGrade = downGrade;
		_migrations = migrations;
	}

	/**
	 * The migrations to perform.
	 */
	public List<MigrationConfig> getMigrations() {
		return _migrations;
	}

	/**
	 * Whether a version downgrade is performed.
	 */
	public boolean isDownGrade() {
		return _downGrade;
	}

	/**
	 * Creates a {@link MigrationInfo}.
	 *
	 * @param downGrade
	 *        See {@link #isDownGrade()}.
	 * @param migrations
	 *        See {@link #getMigrations()}.
	 */
	public static MigrationInfo migrations(boolean downGrade, List<MigrationConfig> migrations) {
		return new MigrationInfo(downGrade, migrations);
	}

	/**
	 * Whether nothing is to do during startup.
	 */
	public boolean nothingToDo() {
		return !isDownGrade() && getMigrations().isEmpty();
	}

}
