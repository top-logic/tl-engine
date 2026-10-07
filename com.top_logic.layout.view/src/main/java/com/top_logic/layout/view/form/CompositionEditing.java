/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl.EditorSession;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.model.TLObject;

/**
 * The edit of the parts of a composition of a table row in a dialog: a
 * {@link ReactCompactFieldControl} shows the labels of the parts and opens a
 * {@link RowSetTableControl} of the parts.
 *
 * <p>
 * The dialog edits the parts on a level of its own on top of the level the row is edited in, by a
 * {@link RowSetEditSession#isNested() nested} {@link RowSetEditSession}: each part shown is edited
 * through a buffer of the dialog, nothing below the parts is copied. OK writes the parts into the
 * buffer of the row, as long as they are valid; Cancel drops the dialog's buffers, so the row is
 * left as it was. A composition of a part is edited the same way, in a dialog opened from the
 * dialog's table, so the edit nests as deep as the compositions do. When the edit of the row ends
 * while the dialog is open, because the form leaves edit mode or a dialog below is closed, the
 * dialog closes.
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
			Utils::isEmpty, new CompositionEditing(model));
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
		TLObject row = (TLObject) _model.getObject();
		AttributeRowSetBinding binding = new AttributeRowSetBinding(_model.getPart().getName());
		binding.resolve(row);
		RowSetEditSession session =
			new RowSetEditSession(RowSetOwner.ofRow(_model.getForm(), row), binding, _model.getLevel());
		session.setEditable(editable);
		session.start();
		return new Session(_model, session);
	}

	/**
	 * The edit of the parts while the dialog is open.
	 */
	private static final class Session implements EditorSession {

		private final CompositionCellModel _model;

		private final RowSetEditSession _session;

		private Runnable _onDispose;

		Session(CompositionCellModel model, RowSetEditSession session) {
			_model = model;
			_session = session;
		}

		@Override
		public ReactControl createEditor(ReactContext context, Runnable closeDialog) {
			_onDispose = () -> {
				_session.end();
				closeDialog.run();
			};
			_model.addDisposeAction(_onDispose);

			RowSetTableControl table =
				new RowSetTableControl(new DefaultViewContext(context), _session, List.of(), RowEditPolicy.ALL);
			table.init();
			return table;
		}

		@Override
		public boolean apply() {
			if (!_session.isEditable()) {
				close();
				return true;
			}
			if (!_session.checkValid()) {
				return false;
			}
			_session.commit();
			detach();
			// The parts reached the row: the row's field and the form show the change.
			_model.refreshFromObject();
			_model.getForm().updateDirtyState();
			return true;
		}

		@Override
		public void revert() {
			close();
		}

		private void close() {
			_session.end();
			detach();
		}

		private void detach() {
			if (_onDispose != null) {
				_model.removeDisposeAction(_onDispose);
				_onDispose = null;
			}
		}

	}

}
