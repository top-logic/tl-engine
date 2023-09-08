/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import static com.top_logic.basic.CollectionUtil.*;

import java.util.List;

/**
 * An {@link Updater} representing a list of {@link Updater}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class CombinedUpdater extends Updater {

	private final List<Updater> _updaters;

	/**
	 * Creates a {@link CombinedUpdater}.
	 * 
	 * @param updaters
	 *        The given list must not be changed afterwards. Is allowed to be <code>null</code>. Is
	 *        not allowed to contain <code>null</code>.
	 */
	public CombinedUpdater(List<Updater> updaters) {
		_updaters = nonNull(updaters);
	}

	@Override
	public void install(ConfigurationItem item, ConfigurationListener updateListener) {
		for (Updater updater : _updaters) {
			updater.install(item, updateListener);
		}
	}

	@Override
	public void uninstall(ConfigurationItem item, ConfigurationListener updateListener) {
		for (Updater updater : _updaters) {
			updater.uninstall(item, updateListener);
		}
	}

}
