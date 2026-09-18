/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;

/**
 * A field holding several values, each edited by the control its value type asks for.
 *
 * <p>
 * The field model holds the whole collection; one {@link ListElementFieldModel} per element
 * addresses a single position in it, and the element provider builds the control editing that
 * position. A text, a number, a date and a truth value are therefore displayed and entered exactly
 * as they are in a single-valued field - same format, same link, same checkbox - and the list
 * around them only adds the order, the separator between them and, while the field is editable, the
 * buttons that add and remove a value.
 * </p>
 *
 * <p>
 * Where the order of the values is part of the value - an {@link FieldSpec#isOrdered() ordered}
 * field - the user arranges them: a value is moved to another position, and the values it passes
 * shift to make room for it. A field whose values form a set is displayed in the order they are
 * stored in, and moving one of them is refused, since it would change nothing.
 * </p>
 *
 * <p>
 * Which field is displayed this way is decided in
 * {@link FieldControlRegistry#createControl(ReactContext, FieldSpec, FieldModel, ReactFieldControlProvider)}:
 * every multi-valued field whose provider edits one value at a time.
 * </p>
 */
public class ReactValueListControl extends ReactFormFieldControl {

	private static final String REACT_MODULE = "TLValueList";

	/** Command sent by the client to append an empty value. */
	public static final String CMD_ADD_ELEMENT = "addElement";

	/** Command sent by the client to drop one of the values. */
	public static final String CMD_REMOVE_ELEMENT = "removeElement";

	/** Command sent by the client to move one of the values to another position. */
	public static final String CMD_MOVE_ELEMENT = "moveElement";

	/** State key for the controls editing the single values, one per element and in element order. */
	protected static final String ELEMENTS = "elements";

	/**
	 * State key for whether the user may arrange the values, see {@link FieldSpec#isOrdered()}.
	 */
	public static final String ORDERED = "ordered";

	/**
	 * State key for how the values are arranged, either {@link #LAYOUT_INLINE} or
	 * {@link #LAYOUT_BLOCK}.
	 */
	protected static final String LAYOUT = "layout";

	/** {@link #LAYOUT} of values that read as one text, separated from each other. */
	public static final String LAYOUT_INLINE = "inline";

	/** {@link #LAYOUT} of values that each take a line of their own. */
	public static final String LAYOUT_BLOCK = "block";

	private final FieldSpec _elementSpec;

	private final boolean _ordered;

	private final ReactFieldControlProvider _elementProvider;

	private final List<ListElementFieldModel> _elementModels = new ArrayList<>();

	private final List<ReactControl> _elementControls = new ArrayList<>();

	/**
	 * Creates a {@link ReactValueListControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param listModel
	 *        Holds the whole collection of values.
	 * @param fieldSpec
	 *        Describes the multi-valued field as a whole; each of its values is described by
	 *        {@link FieldSpec#elementSpec()}, which is what the element controls are created with.
	 * @param elementProvider
	 *        Creates the control editing a single value.
	 */
	public ReactValueListControl(ReactContext context, FieldModel listModel, FieldSpec fieldSpec,
			ReactFieldControlProvider elementProvider) {
		super(context, listModel, REACT_MODULE);
		_elementSpec = fieldSpec.elementSpec();
		_ordered = fieldSpec.isOrdered();
		_elementProvider = elementProvider;
		// The values are the element controls; the collection itself is nothing the client draws.
		putState(VALUE, null);
		putState(LAYOUT, _elementSpec.getMultilineRows() > 0 ? LAYOUT_BLOCK : LAYOUT_INLINE);
		putState(ORDERED, Boolean.valueOf(_ordered));
		reconcile();
	}

	/**
	 * Reconciles the elements with the collection the field model now holds.
	 *
	 * @implNote Called for every change of the collection-valued field, so it is written to cost
	 *           nothing where nothing changed: an element whose value is unchanged keeps its model
	 *           and its control, and only a changed length rebuilds the state property holding the
	 *           children. That is what makes an edit of one element - which writes the whole
	 *           collection back - leave every other element alone.
	 */
	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		reconcile();
	}

	private void reconcile() {
		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		int count = values.size();
		boolean editable = getFieldModel().isEditable();

		int kept = Math.min(count, _elementModels.size());
		for (int n = 0; n < kept; n++) {
			ListElementFieldModel element = _elementModels.get(n);
			element.setEditable(editable);
			element.syncValue(values.get(n));
		}
		if (count == _elementModels.size()) {
			return;
		}

		// The controls of the dropped elements listen to their own element models, never to the
		// collection-valued model this reconciliation runs for. None of them is therefore pending
		// in the notification being delivered, and disposing them here cannot reach a control that
		// is torn down while the same notification still carries it.
		List<ReactControl> dropped = new ArrayList<>();
		while (_elementModels.size() > count) {
			int last = _elementModels.size() - 1;
			_elementModels.remove(last);
			dropped.add(_elementControls.remove(last));
		}
		for (int n = _elementModels.size(); n < count; n++) {
			createElement(n, values.get(n), editable);
		}
		putState(ELEMENTS, new ArrayList<>(_elementControls));
		for (ReactControl control : dropped) {
			control.cleanupTree();
		}
	}

	private void createElement(int index, Object value, boolean editable) {
		ListElementFieldModel elementModel = new ListElementFieldModel(getFieldModel(), index, value);
		elementModel.setEditable(editable);
		ReactControl control = _elementProvider.createControl(getReactContext(), _elementSpec, elementModel);
		_elementModels.add(elementModel);
		_elementControls.add(control);
		registerChildControl(control);
		if (isAttached()) {
			control.attach();
		}
	}

	/**
	 * Appends an empty value, which the user then enters into the control that appears for it.
	 */
	@ReactCommandHandler(CMD_ADD_ELEMENT)
	final void handleAddElement() {
		if (!acceptsClientValue()) {
			return;
		}
		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		values.add(null);
		getFieldModel().setValue(values);
	}

	/**
	 * Drops the value at the position the client names.
	 */
	@ReactCommandHandler(CMD_REMOVE_ELEMENT)
	final void handleRemoveElement(RemoveElementArguments args) {
		if (!acceptsClientValue()) {
			return;
		}
		int index = args.getIndex();
		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		if (index < 0 || index >= values.size()) {
			return;
		}
		values.remove(index);
		getFieldModel().setValue(values);
	}

	/**
	 * Moves the value the client names to the position it names, shifting the values in between.
	 *
	 * @implNote The elements are not moved: they keep their position and take over the values that
	 *           are now stored there, which is the same reconciliation a collection rearranged from
	 *           elsewhere goes through.
	 */
	@ReactCommandHandler(CMD_MOVE_ELEMENT)
	final void handleMoveElement(MoveElementArguments args) {
		if (!acceptsClientValue()) {
			return;
		}
		if (!_ordered) {
			return;
		}
		int index = args.getIndex();
		int targetIndex = args.getTargetIndex();
		if (index == targetIndex) {
			return;
		}
		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		int count = values.size();
		if (index < 0 || index >= count || targetIndex < 0 || targetIndex >= count) {
			return;
		}
		values.add(targetIndex, values.remove(index));
		getFieldModel().setValue(values);
	}

}
