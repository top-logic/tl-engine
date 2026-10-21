/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl;
import com.top_logic.util.Resources;

/**
 * A text field that is edited as its source text and displayed as the HTML the server renders from
 * that text.
 *
 * <p>
 * For a text written in a markup language (e.g. <i>CommonMark</i>): while the {@link FieldModel}
 * is {@link FieldModel#isEditable() editable}, the field shows the input given at construction,
 * which edits the text itself. While it is not, the field shows the HTML a renderer produces from
 * the text in a {@link ReactHtmlControl}, and follows every change of the value. The field switches
 * between the two when the editability of the model changes.
 * </p>
 *
 * <p>
 * The text is the value itself for a text field. A field whose value is not a plain text - an
 * internationalized text, for instance - names the text to display for its value.
 * </p>
 *
 * <p>
 * The rendered HTML is inserted into the page, so it passes the application's HTML safety check
 * ({@link SafeHTML}) first. HTML the check rejects - a script, an attribute that carries one - is
 * not displayed; its place is taken by the message the check reports.
 * </p>
 *
 * <p>
 * Composed from existing controls: a {@code TLDeckPane} showing either the input or a
 * {@code TLHtml}. Each of them is built the first time it is shown. In a form field, the deck hands
 * the field's label on to the pane it shows, so the input - and the display - is named by the
 * field's label as if it stood in the field itself.
 * </p>
 */
public class ReactRenderedTextControl extends ReactDeckPaneControl {

	/** Index of the pane holding the input. */
	private static final int INPUT_PANE = 0;

	/** Index of the pane holding the rendered HTML. */
	private static final int DISPLAY_PANE = 1;

	private final Panes _panes;

	private final FieldModel _model;

	private final FieldModelListener _listener = new FieldModelListener() {
		@Override
		public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
			_panes.render(newValue);
		}

		@Override
		public void onEditabilityChanged(FieldModel source, boolean editable) {
			selectChild(paneFor(editable));
		}

		@Override
		public void onValidationChanged(FieldModel source) {
			// The rendered text does not depend on the validity of the value.
		}
	};

	/**
	 * Creates a {@link ReactRenderedTextControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the text.
	 * @param input
	 *        Creates the control that edits the text of the given model, shown while the model is
	 *        editable.
	 * @param renderer
	 *        The HTML fragment for a text that is not empty.
	 */
	public ReactRenderedTextControl(ReactContext context, FieldModel model, ChildFactory input,
			Function<String, String> renderer) {
		this(context, model, input, value -> value == null ? null : value.toString(), renderer);
	}

	/**
	 * Creates a {@link ReactRenderedTextControl} for a field whose value is not a plain text.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model.
	 * @param input
	 *        Creates the control that edits the value of the given model, shown while the model is
	 *        editable.
	 * @param text
	 *        The text to display for a value of the model, <code>null</code> for none.
	 * @param renderer
	 *        The HTML fragment for a text that is not empty.
	 */
	public ReactRenderedTextControl(ReactContext context, FieldModel model, ChildFactory input,
			Function<Object, String> text, Function<String, String> renderer) {
		this(context, model, new Panes(context, model, input, text, renderer));
	}

	private ReactRenderedTextControl(ReactContext context, FieldModel model, Panes panes) {
		super(context, List.of(panes::input, panes::display), paneFor(model.isEditable()));
		_panes = panes;
		_model = model;
		model.addListener(_listener);
	}

	@Override
	protected void onCleanup() {
		super.onCleanup();
		_model.removeListener(_listener);
	}

	/**
	 * The control editing the text, built on first use.
	 */
	public ReactControl getInput() {
		return _panes.input();
	}

	/**
	 * The control displaying the rendered text, built on first use.
	 */
	public ReactHtmlControl getDisplay() {
		return _panes.display();
	}

	private static int paneFor(boolean editable) {
		return editable ? INPUT_PANE : DISPLAY_PANE;
	}

	/**
	 * The two panes, which are created before the deck that shows them.
	 */
	private static final class Panes {

		private final ReactContext _context;

		private final FieldModel _model;

		private final ChildFactory _inputFactory;

		private final Function<Object, String> _text;

		private final Function<String, String> _renderer;

		private ReactControl _input;

		private ReactHtmlControl _display;

		Panes(ReactContext context, FieldModel model, ChildFactory inputFactory, Function<Object, String> text,
				Function<String, String> renderer) {
			_context = context;
			_model = model;
			_inputFactory = inputFactory;
			_text = text;
			_renderer = renderer;
		}

		ReactControl input() {
			if (_input == null) {
				_input = _inputFactory.create();
			}
			return _input;
		}

		ReactHtmlControl display() {
			if (_display == null) {
				_display = new ReactHtmlControl(_context, ReactHtmlControl.DISPLAY_INLINE, false, null, 0, 0);
				render(_model.getValue());
			}
			return _display;
		}

		/**
		 * Shows the given value in the display, if it has been built.
		 */
		void render(Object value) {
			if (_display == null) {
				return;
			}
			String text = _text.apply(value);
			if (text == null || text.isEmpty()) {
				_display.setHtml(null);
				return;
			}

			String html = _renderer.apply(text);
			try {
				SafeHTML.getInstance().check(html);
			} catch (I18NException ex) {
				_display.setError(Resources.getInstance().getString(ex.getErrorKey()));
				return;
			}
			_display.setHtml(html);
		}
	}

}
