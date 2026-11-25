/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;

import com.top_logic.basic.func.GenericFunction;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * {@link DynamicMode} hiding one of the two ways to define the access of a type while the other is
 * taken.
 *
 * <p>
 * A type either delegates its access decision to an {@link AccessParentConfig access parent}, or
 * is marked to need no access check or to be internal - never both, see
 * {@link AccessParentStandsAlone}. A form editing the access rights of a type offers only the way
 * not excluded by the other: the marks are hidden while the type delegates, the access parent while
 * the type has a mark. The grants are no such way: they decide beside an access parent, see
 * {@link ForGrants}.
 * </p>
 *
 * <p>
 * The first argument is the value of the property whose mode is computed, the others are the
 * values making up the other way. The property is hidden while it is empty itself and one of the
 * others is set. A property that is set is never hidden: a configuration taking both ways - written
 * by hand, say - shows both, so that the problem {@link AccessParentStandsAlone} reports can be
 * fixed in the form.
 * </p>
 *
 * <p>
 * A value is set if it is a non-empty collection, <code>true</code>, an access parent, or the
 * definition of an access parent that delegates - anything but {@link SelfAccessParent}.
 * </p>
 */
public class HideBesideOtherDefinition extends GenericFunction<FieldMode> {

	@Override
	public FieldMode invoke(Object... args) {
		if (args.length == 0 || isSet(args[0])) {
			return FieldMode.ACTIVE;
		}
		for (int n = 1; n < args.length; n++) {
			if (isSet(args[n])) {
				return FieldMode.INVISIBLE;
			}
		}
		return FieldMode.ACTIVE;
	}

	private static boolean isSet(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Collection<?> collection) {
			return !collection.isEmpty();
		}
		if (value instanceof Boolean flag) {
			return flag.booleanValue();
		}
		if (value instanceof SelfAccessParent.Config) {
			return false;
		}
		return true;
	}

	@Override
	public int getArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasVarArgs() {
		return true;
	}

	/**
	 * {@link DynamicMode} for the grants of a type, which are hidden while the type is without
	 * security - see {@link SecurityConfigurationService.TypeBasedAccessRights#isWithoutSecurity()}.
	 *
	 * <p>
	 * The only argument is whether the type is without security. Beside an access parent the
	 * grants are offered: they decide the operations it does not delegate, and the delegated ones as
	 * well when it names the object itself as candidate. Grants the access parent ignores are
	 * reported by {@link IgnoredGrants}. Offering them independently of the access parent keeps them
	 * available while the candidates of the access parent are edited in the same form.
	 * </p>
	 */
	public static class ForGrants extends GenericFunction<FieldMode> {

		@Override
		public FieldMode invoke(Object... args) {
			return args.length > 0 && Boolean.TRUE.equals(args[0]) ? FieldMode.INVISIBLE : FieldMode.ACTIVE;
		}

		@Override
		public int getArgumentCount() {
			return 0;
		}

		@Override
		public boolean hasVarArgs() {
			return true;
		}

	}

}
