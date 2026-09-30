/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link SimpleFormDialog} displaying a mandatory, single select, {@link SelectField} for forcing
 * the user to select one of the given options.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleSelectDialog<T> extends SimpleFormDialog implements ValueListener {

	/** Default width of the opened dialog. */
	public static final DisplayDimension DEFAULT_WIDTH = dim(400, DisplayUnit.PIXEL);

	/** Default height of the opened dialog. */
	public static final DisplayDimension DEFAULT_HEIGHT = dim(300, DisplayUnit.PIXEL);

	/** Command to execute when the OK button is pressed. */
	protected final CommandModel _okButton = MessageBox.button(ButtonType.OK, new Command() {

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			if (!getFormContext().checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(getFormContext());
			}

			SelectField field = (SelectField) getFormContext().getField(INPUT_FIELD);
			@SuppressWarnings("unchecked")
			T singleSelection = (T) field.getSingleSelection();

			HandlerResult handleSelectionResult = getSelectionHandler().apply(context, singleSelection);
			if (handleSelectionResult.isSuccess()) {
				return getDiscardClosure().executeCommand(context);
			} else {
				/* Do not close dialog in case of accepting selection fails. */
				return handleSelectionResult;
			}

		}
	});

	private BiFunction<DisplayContext, ? super T, HandlerResult> _selectionHandler =
		(context, selection) -> HandlerResult.DEFAULT_RESULT;

	private final Iterable<? extends T> _options;

	private LabelProvider _labels = null;

	private T _defaultOption;

	/**
	 * Creates a new {@link SimpleSelectDialog} with {@link #DEFAULT_WIDTH} and
	 * {@link #DEFAULT_HEIGHT} .
	 * 
	 * @see #SimpleSelectDialog(ResPrefix, DisplayDimension, DisplayDimension, Iterable)
	 */
	public SimpleSelectDialog(ResPrefix resourcePrefix, Iterable<? extends T> options) {
		this(resourcePrefix, DEFAULT_WIDTH, DEFAULT_HEIGHT, options);
	}

	/**
	 * Creates a new {@link SimpleSelectDialog}.
	 * 
	 * <p>
	 * It is strongly recommend to call {@link #setSelectionHandler(BiFunction)}, because otherwise
	 * the selection vanishes into thin air.
	 * </p>
	 * 
	 * @param resourcePrefix
	 *        See
	 *        {@link SimpleFormDialog#SimpleFormDialog(ResPrefix, DisplayDimension, DisplayDimension)}.
	 * @param width
	 *        See
	 *        {@link SimpleFormDialog#SimpleFormDialog(ResPrefix, DisplayDimension, DisplayDimension)}.
	 * @param height
	 *        See
	 *        {@link SimpleFormDialog#SimpleFormDialog(ResPrefix, DisplayDimension, DisplayDimension)}.
	 * @param options
	 *        Options to display in the select field.
	 */
	public SimpleSelectDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height,
			Iterable<? extends T> options) {
		super(resourcePrefix, width, height);
		_options = options;
	}

	/**
	 * The default option to select in the UI.
	 * 
	 * <p>
	 * A value of <code>null</code> means not to pre-fill the form.
	 * </p>
	 */
	public T getDefaultOption() {
		return _defaultOption;
	}

	/**
	 * @see #getDefaultOption()
	 * 
	 * @return This instance for call-chaining.
	 */
	public SimpleSelectDialog<T> setDefaultOption(T defaultOption) {
		_defaultOption = defaultOption;
		return this;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		SelectField selectField = createSelectField(INPUT_FIELD, _options);
		context.addMember(selectField);
		selectField.addValueListener(this);
		setExecutableState(selectField.getSelection());
	}

	/**
	 * Creates the actual {@link SelectField}.
	 * 
	 * @param fieldName
	 *        Name for the result field.
	 * @param options
	 *        The options for the {@link SelectField}.
	 */
	protected SelectField createSelectField(String fieldName, Iterable<? extends T> options) {
		SelectField selectField = FormFactory.newSelectField(fieldName, options);
		selectField.setMandatory(FormFactory.MANDATORY);
		if (_defaultOption != null) {
			selectField.initializeField(Collections.singletonList(_defaultOption));
		}
		if (getLabels() != null) {
			selectField.setOptionLabelProvider(getLabels());
		}
		return selectField;
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(_okButton);
		addCancel(buttons);
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		setExecutableState((Collection<?>) newValue);
	}

	private void setExecutableState(Collection<?> currentSelection) {
		if (currentSelection.isEmpty()) {
			_okButton.setNotExecutable(com.top_logic.layout.form.I18NConstants.NOT_EMPTY);
		} else {
			_okButton.setExecutable();
		}
	}

	/**
	 * The selection handler that is triggered when the user has selected an option and confirms the
	 * selection.
	 */
	public BiFunction<DisplayContext, ? super T, HandlerResult> getSelectionHandler() {
		return _selectionHandler;
	}

	/**
	 * Setter for {@link #getSelectionHandler()}.
	 * 
	 * @return This dialog.
	 */
	public SimpleSelectDialog<T> setSelectionHandler(
			BiFunction<DisplayContext, ? super T, HandlerResult> selectionHandler) {
		_selectionHandler = Objects.requireNonNull(selectionHandler);
		return this;
	}

	/**
	 * Optional {@link LabelProvider} to use as {@link SelectField#getOptionLabelProvider() label
	 * provider} for the {@link SelectField}.
	 * 
	 * @return May be <code>null</code>. In this case the default label provider is used.
	 */
	public LabelProvider getLabels() {
		return _labels;
	}

	/**
	 * Setter for {@link #getLabels()}.
	 * 
	 * @return This dialog.
	 */
	public SimpleSelectDialog<T> setLabels(LabelProvider labels) {
		_labels = labels;
		return this;
	}
}
