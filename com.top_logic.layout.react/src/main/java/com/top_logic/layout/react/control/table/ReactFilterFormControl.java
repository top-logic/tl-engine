/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.table.FilterState;

/**
 * The content of the filter popover of a column whose filter is edited in a form - a text, a range,
 * a yes/no choice, or the filter UI an application supplies ({@link ColumnFilterUI}): the form, and
 * below it the button removing the filter.
 *
 * <p>
 * A form of the built-in filters takes effect by itself, field by field: whoever builds it lets
 * every change of a field filter the table. The filter UI of an application keeps an explicit
 * apply, since evaluating it may be expensive: the form then offers "apply", which reads the form
 * and hands its filter state to the table.
 * </p>
 *
 * <p>
 * Removing the filter clears it and builds the form anew from the filter as it is then: the form of
 * a built-in filter shows no filter; the filter UI of an application shows what it builds - one
 * that remembers its last input shows it again (see {@link ColumnFilterUI#buildForm}).
 * </p>
 *
 * <p>
 * The form stands for the filter it was built from or last filtered by. A filter of the column
 * changed elsewhere - the filter bar clearing every filter, a named filter setting another one -
 * builds it anew in the same way ({@link #refresh(FilterState)}); a change by the form itself, or
 * rows rebuilt for another reason, leave it as it is, so that its fields keep their input and the
 * focus.
 * </p>
 */
public class ReactFilterFormControl extends ReactControl {

	/** Command reading the form and filtering the table by it; offered only by a form that {@link #APPLIES}. */
	public static final String CMD_APPLY = "apply";

	/** Command removing the filter of the column. */
	public static final String CMD_CLEAR = "clear";

	/** State key of the form. */
	private static final String FORM = "form";

	/** State key of whether the column is filtered now, i.e. whether there is a filter to remove. */
	private static final String ACTIVE = "active";

	/** State key of whether the form takes effect only on {@link #CMD_APPLY}. */
	private static final String APPLIES = "applies";

	private final Function<Consumer<FilterState>, ReactControl> _formFactory;

	private final Supplier<FilterState> _read;

	private final Consumer<FilterState> _apply;

	/**
	 * The filter the form stands for: the one of the column when it was built, or the one it filtered
	 * the column by last; {@code null} for none.
	 */
	private FilterState _shown;

	/**
	 * Creates a {@link ReactFilterFormControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param formFactory
	 *        Builds the form from the filter of the column as it is when called: once now, and
	 *        again whenever the filter changed other than by the form. It receives what a field
	 *        filtering by itself filters the column with; a form that only applies explicitly
	 *        ignores it.
	 * @param read
	 *        Reads the filter state from the form on {@link #CMD_APPLY}; {@code null} for a form
	 *        whose fields filter by themselves, which offers no apply.
	 * @param apply
	 *        Filters the column by the given state, {@code null} for no filter.
	 * @param current
	 *        The filter of the column now, {@code null} for none.
	 */
	public ReactFilterFormControl(ReactContext context, Function<Consumer<FilterState>, ReactControl> formFactory,
			Supplier<FilterState> read, Consumer<FilterState> apply, FilterState current) {
		super(context, null, "TLFilterForm");
		_formFactory = formFactory;
		_read = read;
		_apply = apply;
		_shown = current;
		putState(APPLIES, Boolean.valueOf(read != null));
		putState(ACTIVE, Boolean.valueOf(isActive(current)));
		putState(FORM, formFactory.apply(this::filter));
	}

	/**
	 * Shows the filter of the column as it is now: whether there is a filter to remove, and - where
	 * the filter changed other than by the form - the form built anew from it. Called whenever the
	 * rows of the table were rebuilt.
	 *
	 * @param current
	 *        The filter of the column now, {@code null} for none.
	 */
	public void refresh(FilterState current) {
		putState(ACTIVE, Boolean.valueOf(isActive(current)));
		if (!Objects.equals(normalized(current), normalized(_shown))) {
			_shown = current;
			replaceForm();
		}
	}

	/**
	 * Handles "apply": filters the column by what the form holds.
	 */
	@ReactCommandHandler(CMD_APPLY)
	void handleApply() {
		if (_read == null) {
			return;
		}
		filter(_read.get());
	}

	/**
	 * Handles "remove the filter": clears the filter of the column, and replaces the form by one
	 * built from no filter.
	 */
	@ReactCommandHandler(CMD_CLEAR)
	void handleClear() {
		filter(null);
		replaceForm();
	}

	/**
	 * Filters the column by the given state on behalf of the form. The form stands for it before the
	 * table hears of it: the table refreshes this content while it filters, and the form must not be
	 * taken for one showing another filter.
	 */
	private void filter(FilterState state) {
		_shown = state;
		_apply.accept(state);
	}

	private void replaceForm() {
		ReactControl old = (ReactControl) getState(FORM);
		putState(FORM, _formFactory.apply(this::filter));
		if (old != null) {
			old.cleanupTree();
		}
	}

	private static boolean isActive(FilterState state) {
		return normalized(state) != null;
	}

	/** The given state, {@code null} for an empty one: the table keeps no empty filter. */
	private static FilterState normalized(FilterState state) {
		return state == null || state.isEmpty() ? null : state;
	}

}
