/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;

/**
 * Immutable display of a {@link FormField} value independently of the display state of the
 * underlying field model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ValueDisplayControl extends SimpleConstantControl<Object> implements ValueListener {

	/** Default {@link ControlCommand}s for a {@link ValueDisplayControl}. */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ValueFieldInspector());

	/**
	 * {@link ControlProvider} for {@link ValueDisplayControl}s using special rendering.
	 */
	public static final class ValueDisplay implements ControlProvider {

		/**
		 * {@link ControlProvider} creating {@link ValueDisplayControl} with default rendering.
		 */
		public static final ControlProvider INSTANCE = new ValueDisplay(ResourceRenderer.INSTANCE);

		private final Renderer<Object> _renderer;

		/**
		 * Creates a {@link ValueDisplay}.
		 * 
		 * @param renderer
		 *        The renderer to use.
		 */
		public ValueDisplay(Renderer<?> renderer) {
			_renderer = renderer.generic();
		}

		@Override
		public Control createControl(Object model, String style) {
			return new ValueDisplayControl((FormField) model, _renderer);
		}

	}

	private final FormField _field;

	/**
	 * Creates a {@link ValueDisplayControl}.
	 * 
	 * @param field
	 *        The {@link FormField} to take the value from.
	 * @param aRenderer
	 *        The {@link Renderer} used to render the value.
	 */
	public ValueDisplayControl(FormField field, Renderer<Object> aRenderer) {
		this(field, COMMANDS, aRenderer);
	}

	/**
	 * Creates a {@link ValueDisplayControl}.
	 * 
	 * @param field
	 *        The {@link FormField} to take the value from.
	 * @param commandsByName
	 *        Commands known by this control.
	 * @param aRenderer
	 *        The {@link Renderer} used to render the value.
	 */
	protected ValueDisplayControl(FormField field, Map<String, ControlCommand> commandsByName,
			Renderer<Object> aRenderer) {
		super(field.getValue(), commandsByName, aRenderer);
		_field = field;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getFieldModel().addValueListener(this);

		// Load initial value.
		updateModel();
	}

	@Override
	protected void internalDetach() {
		getFieldModel().removeValueListener(this);
		super.internalDetach();
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		updateModel();
	}

	private void updateModel() {
		setModel(getFieldModel().getValue());
	}

	/**
	 * The {@link FormField} whose value is displayed.
	 */
	protected FormField getFieldModel() {
		return _field;
	}

	/**
	 * {@link GuiInspectorCommand} to inspect the {@link FormField} of a
	 * {@link ValueDisplayControl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class ValueFieldInspector extends GuiInspectorCommand<ValueDisplayControl, FormField> {

		ValueFieldInspector() {
		}

		@Override
		protected FormField findInspectedGuiElement(ValueDisplayControl control, Map<String, Object> arguments)
				throws AssertionError {
			return control.getFieldModel();
		}

		@Override
		protected void buildInspector(InspectorModel inspector, FormField model) {
			GuiInspectorPluginFactory.createFieldAssertions(inspector, model);
		}
	}
}
