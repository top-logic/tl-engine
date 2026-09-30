/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.meta.search.quick.QuickSearchCommand.QuickSearchConfig;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * View allowing the user searching for objects using the {@link QuickSearchCommand}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QuickSearchView extends WithPropertiesBase implements HTMLFragment {

	/**
	 * CSS class for the quick-search button.
	 */
	private static final String QUICK_SEARCH_BUTTON_CSS_CLASS = "quickSearchButton";

	/**
	 * CSS class for the quick-search field.
	 */
	private static final String QUICK_SEARCH_INPUT_CSS_CLASS = "quickSearchInput";

	/**
	 * Provide special CSS class for the quick-search button.
	 */
	private static final AbstractButtonRenderer<?> BUTTON_RENDERER =
		ImageButtonRenderer.newSystemButtonRenderer(QUICK_SEARCH_BUTTON_CSS_CLASS);

	private HTMLFragment _input;

	private HTMLFragment _button;

	/**
	 * Creates a new {@link QuickSearchView}.
	 * 
	 * @param config
	 *        Static configuration of the search command.
	 */
	public QuickSearchView(String fieldName, QuickSearchConfig config) {
		FormContext form = new FormContext("quickSearch", I18NConstants.QUICK_SEARCH);
		createViews(form, fieldName, config);
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		Icons.QUICK_SEARCH_VIEW.get().write(context, out, this);
	}

	/**
	 * Creates the views that offers the user an input field to enter the search string and a button
	 * to execute the search.
	 * 
	 * @implNote The implementor must fill {@link #getInput()} and {@link #getButton()}.
	 * 
	 * @param form
	 *        {@link FormContext} to add fields to.
	 * @param searchFieldName
	 *        Name of the input field to enter search string.
	 * @param config
	 *        Configuration of the {@link QuickSearchCommand}.
	 */
	protected void createViews(FormContext form, String searchFieldName, QuickSearchConfig config) {
		StringField searchField = FormFactory.newStringField(searchFieldName);
		searchField.setCssClasses(QUICK_SEARCH_INPUT_CSS_CLASS);
		searchField.setPlaceholder(Resources.getInstance().getString(I18NConstants.PLACEHOLDER));
		form.addMember(searchField);

		TextInputControl input = new TextInputControl(searchField);
		setInput(input);

		Command command = createSearchCommand(input, config);

		CommandField searchCommand =
			FormFactory.newCommandField("searchCommand", command, executability(searchField, config));
		searchCommand.setImage(Icons.SEARCH);
		searchCommand.setLabel(I18NConstants.EXECUTE_QUICK_SEARCH);
		form.addMember(searchCommand);

		searchField.addKeyListener(toKeyEventListener(searchCommand));

		ButtonControl button = new ButtonControl(searchCommand, BUTTON_RENDERER);
		setButton(button);
	}

	private ExecutabilityModel executability(StringField searchField, QuickSearchConfig config) {
		class ListeningExecutability extends AbstractExecutabilityModel implements ValueListener {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				String oldSearchString = StringServices.nonNull((String) oldValue);
				String newSearchString = StringServices.nonNull((String) newValue);
				if (searchPossible(newSearchString)) {
					if (!searchPossible(oldSearchString)) {
						updateExecutabilityState();
					}
				} else {
					if (searchPossible(oldSearchString)) {
						updateExecutabilityState();
					}
				}
			}

			@Override
			protected ExecutableState calculateExecutability() {
				if (searchPossible(searchField.getAsString())) {
					return ExecutableState.EXECUTABLE;
				}
				return ExecutableState
					.createDisabledState(I18NConstants.SEARCH_STRING_TOO_SHORT__MIN.fill(minSearchStringLength()));
			}

			private boolean searchPossible(String searchString) {
				return searchString.length() >= minSearchStringLength();
			}

			private int minSearchStringLength() {
				return config.getMinSearchStringLength();
			}

		}
		ListeningExecutability listeningExecutability = new ListeningExecutability();
		searchField.addValueListener(listeningExecutability);
		return listeningExecutability;
	}

	/**
	 * Creates a {@link Command} for executing the search.
	 * 
	 * @param input
	 *        The input of the search text.
	 * @param config
	 *        Configuration of the {@link QuickSearchCommand}.
	 */
	protected Command createSearchCommand(TextInputControl input, QuickSearchConfig config) {
		return new QuickSearchCommand(input, config);
	}

	/**
	 * Create the key event listener for "enter" executing the given {@link Command}.
	 * 
	 * @param command
	 *        The command to be executed upon enter.
	 * @return The {@link KeyEventListener} proxy for the given command.
	 */
	protected KeyEventListener toKeyEventListener(final Command command) {
		return KeyEventListener.onEnter(command);
	}

	/**
	 * The input field for the search string.
	 */
	@TemplateVariable("input")
	public HTMLFragment getInput() {
		return _input;
	}

	/**
	 * Setter for {@link #getInput()}.
	 */
	public void setInput(HTMLFragment input) {
		_input = input;
	}

	/**
	 * The button to execute the search.
	 */
	@TemplateVariable("button")
	public HTMLFragment getButton() {
		return _button;
	}

	/**
	 * Setter for {@link #getButton()}.
	 */
	public void setButton(HTMLFragment button) {
		_button = button;
	}

}

