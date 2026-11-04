/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.function.Consumer;
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

	private final Supplier<ReactControl> _formFactory;

	private final Supplier<FilterState> _read;

	private final Consumer<FilterState> _apply;

	/**
	 * Creates a {@link ReactFilterFormControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param formFactory
	 *        Builds the form from the filter of the column as it is when called: once now, and
	 *        again after the filter was removed.
	 * @param read
	 *        Reads the filter state from the form on {@link #CMD_APPLY}; {@code null} for a form
	 *        whose fields filter by themselves, which offers no apply.
	 * @param apply
	 *        Filters the column by the given state, {@code null} for no filter.
	 * @param active
	 *        Whether the column is filtered now.
	 */
	public ReactFilterFormControl(ReactContext context, Supplier<ReactControl> formFactory,
			Supplier<FilterState> read, Consumer<FilterState> apply, boolean active) {
		super(context, null, "TLFilterForm");
		_formFactory = formFactory;
		_read = read;
		_apply = apply;
		putState(APPLIES, Boolean.valueOf(read != null));
		putState(ACTIVE, Boolean.valueOf(active));
		putState(FORM, formFactory.get());
	}

	/**
	 * Tells whether the column is filtered now, which enables the removal of the filter.
	 */
	public void setActive(boolean active) {
		putState(ACTIVE, Boolean.valueOf(active));
	}

	/**
	 * Handles "apply": filters the column by what the form holds.
	 */
	@ReactCommandHandler(CMD_APPLY)
	void handleApply() {
		if (_read == null) {
			return;
		}
		_apply.accept(_read.get());
	}

	/**
	 * Handles "remove the filter": clears the filter of the column, and replaces the form by one
	 * built from no filter.
	 */
	@ReactCommandHandler(CMD_CLEAR)
	void handleClear() {
		_apply.accept(null);
		ReactControl old = (ReactControl) getState(FORM);
		putState(FORM, _formFactory.get());
		if (old != null) {
			old.cleanupTree();
		}
	}

}
