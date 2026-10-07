/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl.EditorSession;
import com.top_logic.layout.view.DefaultViewContext;

/**
 * The edit of the parts of a composition of a table row in a dialog: a
 * {@link ReactCompactFieldControl} shows the labels of the parts and opens a
 * {@link RowSetTableControl} over the {@link CompositionCellModel#session() edit session} of the
 * parts.
 *
 * <p>
 * The dialog edits the parts within the edit session of the form: OK keeps the changes in the
 * session - the form saves them with the row - as long as the parts are valid, Cancel returns the
 * parts to the state they had when the dialog opened. When the edit session of the parts ends
 * while the dialog is open, because the form leaves edit mode, the dialog closes.
 * </p>
 */
public class CompositionEditing implements ReactCompactFieldControl.Editing {

	/** Separates the labels of the parts in the preview. */
	private static final String PREVIEW_SEPARATOR = ", ";

	private final CompositionCellModel _model;

	/**
	 * Creates a {@link CompositionEditing}.
	 *
	 * @param model
	 *        The field of the composition.
	 */
	public CompositionEditing(CompositionCellModel model) {
		_model = model;
	}

	/**
	 * Creates the control of a composition cell: the labels of the parts and a button opening the
	 * table of the parts in a dialog.
	 *
	 * @param context
	 *        The context to create the control in.
	 * @param model
	 *        The field of the composition.
	 * @param label
	 *        The label of the composition, naming the dialog.
	 */
	public static ReactControl createControl(ReactContext context, CompositionCellModel model, String label) {
		return new ReactCompactFieldControl(context, model, label, CompositionEditing::previewText,
			new CompositionEditing(model));
	}

	/**
	 * The labels of the given parts, separated by commas.
	 */
	public static String previewText(Object value) {
		if (!(value instanceof Collection<?> parts)) {
			return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
		}
		StringBuilder result = new StringBuilder();
		for (Object part : parts) {
			if (result.length() > 0) {
				result.append(PREVIEW_SEPARATOR);
			}
			result.append(MetaLabelProvider.INSTANCE.getLabel(part));
		}
		return result.toString();
	}

	@Override
	public EditorSession open(FieldModel model, boolean editable) {
		return new Session(_model.session());
	}

	/**
	 * The edit of the parts while the dialog is open.
	 */
	private static final class Session implements EditorSession {

		private final RowSetEditSession _session;

		private final RowSetEditSession.Snapshot _snapshot;

		private RowSetEditSession.Listener _endListener;

		Session(RowSetEditSession session) {
			_session = session;
			_snapshot = session.snapshot();
		}

		@Override
		public ReactControl createEditor(ReactContext context, Runnable closeDialog) {
			_endListener = new RowSetEditSession.Listener() {
				@Override
				public void onRowsChanged(RowSetEditSession source) {
					// Displayed by the table.
				}

				@Override
				public void onRowSetValidationChanged(RowSetEditSession source) {
					// Displayed by the table.
				}

				@Override
				public void onEnded(RowSetEditSession source) {
					detach();
					closeDialog.run();
				}
			};
			_session.addListener(_endListener);

			RowSetTableControl table =
				new RowSetTableControl(new DefaultViewContext(context), _session, List.of(), RowEditPolicy.ALL);
			table.init();
			return table;
		}

		@Override
		public boolean apply() {
			if (!_session.checkValid()) {
				return false;
			}
			detach();
			return true;
		}

		@Override
		public void revert() {
			_session.restore(_snapshot);
			detach();
		}

		private void detach() {
			if (_endListener != null) {
				_session.removeListener(_endListener);
				_endListener = null;
			}
		}

	}

}
