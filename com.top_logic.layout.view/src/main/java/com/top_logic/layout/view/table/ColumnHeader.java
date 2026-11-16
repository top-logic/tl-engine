/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DelegatingColumn;

/**
 * What the heading of a column shows beyond its label: the color role it is tinted in and the CSS
 * class it carries, computed from the values of channels.
 *
 * <p>
 * Role and class are evaluated anew whenever the heading is displayed; the header keeps no value of
 * its own. It names the channels it reads, so that the table {@link #observe(List, Runnable)
 * follows} them and sends the headings again when one of them takes a new value, without the
 * column being built again.
 * </p>
 *
 * @see Column#headerRole()
 * @see Column#headerClass()
 */
public final class ColumnHeader {

	/**
	 * The heading without a color role and without a CSS class of its own.
	 */
	public static final ColumnHeader NONE = new ColumnHeader(null, null, List.of(), new Object[0]);

	/** The script computing the role, {@code null} for none. */
	private final QueryExecutor _role;

	/** The script computing the class, {@code null} for none. */
	private final QueryExecutor _cssClass;

	/** The channels whose values lead the arguments of the scripts. */
	private final List<ViewChannel> _channels;

	/** The values the scripts receive behind the values of the {@link #_channels}. */
	private final Object[] _supplied;

	private ColumnHeader(QueryExecutor role, QueryExecutor cssClass, List<ViewChannel> channels,
			Object[] supplied) {
		_role = role;
		_cssClass = cssClass;
		_channels = channels;
		_supplied = supplied;
	}

	/**
	 * The heading whose role and class the given scripts compute from the values of the given
	 * channels.
	 *
	 * @param role
	 *        The compiled script computing the color role, see
	 *        {@link AbstractColumnDeclaration#valueColor(Object)}; {@code null} for none.
	 * @param cssClass
	 *        The compiled script computing the CSS class, see
	 *        {@link AbstractColumnDeclaration#cssClass(Object)}; {@code null} for none.
	 * @param inputs
	 *        The channels whose current values lead the arguments of the scripts.
	 * @param supplied
	 *        The values the scripts receive behind the values of the channels, fixed for the
	 *        heading, e.g. the object a dynamic column stands for.
	 * @return {@link #NONE} where neither script is given.
	 */
	public static ColumnHeader of(QueryExecutor role, QueryExecutor cssClass, List<ViewChannel> inputs,
			Object... supplied) {
		if (role == null && cssClass == null) {
			return NONE;
		}
		return new ColumnHeader(role, cssClass, List.copyOf(inputs), supplied);
	}

	/**
	 * The color role of the heading for the current values of its {@link #channels()},
	 * {@code null} for none.
	 */
	public ValueColor role() {
		if (_role == null) {
			return null;
		}
		return AbstractColumnDeclaration.valueColor(_role.execute(ChannelInputs.arguments(_channels, _supplied)));
	}

	/**
	 * The CSS class of the heading for the current values of its {@link #channels()}, {@code null}
	 * for none.
	 */
	public String cssClass() {
		if (_cssClass == null) {
			return null;
		}
		return AbstractColumnDeclaration.cssClass(_cssClass.execute(ChannelInputs.arguments(_channels, _supplied)));
	}

	/**
	 * The channels whose values the {@link #role()} and the {@link #cssClass()} depend on.
	 */
	public List<ViewChannel> channels() {
		return _channels;
	}

	/**
	 * The given column with this heading: its {@link Column#headerRole()} is the {@link #role()},
	 * the one of the given column where this heading computes none, and its
	 * {@link Column#headerClass()} the class of the given column joined with the
	 * {@link #cssClass()}.
	 */
	<R, V> Column<R, V> decorate(Column<R, V> column) {
		if (this == NONE) {
			return column;
		}
		return new DelegatingColumn<>(column) {
			@Override
			public ValueColor headerRole() {
				ValueColor own = ColumnHeader.this.role();
				return own != null ? own : super.headerRole();
			}

			@Override
			public String headerClass() {
				String inner = super.headerClass();
				String own = ColumnHeader.this.cssClass();
				if (StringServices.isEmpty(own)) {
					return inner;
				}
				return StringServices.isEmpty(inner) ? own : inner + " " + own;
			}
		};
	}

	/**
	 * Follows the headings of the given columns: runs the given action whenever one of the channels
	 * they read takes a new value, once per change.
	 *
	 * @param setups
	 *        The columns of a table.
	 * @param onChange
	 *        What to run, e.g.
	 *        {@link com.top_logic.layout.react.control.table.TableViewControl#refreshColumnHeaders()}.
	 * @return The registration ending the observation, to be disposed with the table.
	 */
	public static Registration observe(List<ColumnSetup> setups, Runnable onChange) {
		List<ViewChannel> channels = new ArrayList<>();
		for (ColumnSetup setup : setups) {
			channels.addAll(setup.header().channels());
		}
		return ChannelInputs.observe(channels, onChange);
	}

}
