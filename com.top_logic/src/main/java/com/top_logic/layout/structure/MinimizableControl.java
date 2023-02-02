/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.SizeInfo;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.layoutRenderer.WrappingControlRenderer;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link LayoutControl} allowing minimization of its contents to a fixed small size.
 * 
 * <p>
 * The contents of this control is overlayed on hover with a small minimize button. Using this
 * button, the state can be toggled from normalized to minimized. In contrast to
 * {@link CollapsibleControl}, the contents of this control is still displayed when in minimized
 * state, but a fixed small size is used.
 * </p>
 * 
 * @see CollapsibleControl
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MinimizableControl extends WrappingControl<MinimizableControl> implements Expandable, ExpandableListener {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(Toggle.INSTANCE);

	private final Expandable _model;

	private final LayoutData _minimizedConstraint;

	private LayoutData _normalizedConstraint;

	/**
	 * Creates a {@link MinimizableControl}.
	 */
	public MinimizableControl(LayoutData minimizedConstraint, Expandable model) {
		super(COMMANDS);
		_minimizedConstraint = minimizedConstraint;

		_model = model;
	}

	/**
	 * The {@link Expandable} model.
	 */
	@Override
	public Expandable getModel() {
		return _model;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_model.addListener(Expandable.STATE, this);
		if (_normalizedConstraint == null) {
			// Save initial constraint that is assigned lately.
			_normalizedConstraint = getConstraint();

			if (_model.getExpansionState() == ExpansionState.MINIMIZED) {
				setConstraint(_minimizedConstraint);
			}
		}
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();

		_model.removeListener(Expandable.STATE, this);
	}

	@Override
	public Bubble notifyExpansionStateChanged(Expandable sender, ExpansionState oldValue, ExpansionState newValue) {
		if (newValue == ExpansionState.MINIMIZED) {
			setConstraint(_minimizedConstraint);
		} else {
			setConstraint(_normalizedConstraint);
		}

		// Forward to control listeners, e.g. flexible flow layout.
		notifyListeners(Expandable.STATE, this, oldValue, newValue);

		return Bubble.BUBBLE;
	}

	@Override
	public ExpansionState getExpansionState() {
		return _model.getExpansionState();
	}

	@Override
	public void setExpansionState(ExpansionState newState) {
		_model.setExpansionState(newState);
	}

	/**
	 * Toggles the minimization state.
	 * 
	 * @param commandContext
	 *        The current request context.
	 * @return The result of the command execution.
	 */
	public HandlerResult toggle(DisplayContext commandContext) {
		_model.setExpansionState(_model.getExpansionState().toggleMinimized());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Default {@link LayoutControlRenderer} for {@link MinimizableControl}.
	 */
	public static class Renderer extends LayoutControlRenderer<MinimizableControl> {

		/**
		 * Top-level control CSS class.
		 */
		private static final String MINIMIZABLE_CONTROL_CSS_CLASS = "cMinimizable";

		/**
		 * CSS class added to the top-level tag when in minimized state.
		 */
		private static final String MINIMIZED_CSS_CLASS = "mcMinimized";

		/**
		 * CSS class for the toggle button.
		 */
		private static final String TOGGLE_CSS_CLASS = "mcToggle";

		/**
		 * Singleton {@link WrappingControlRenderer} instance.
		 */
		public static final Renderer INSTANCE = new Renderer();

		private Renderer() {
			// Singleton constructor.
		}

		@Override
		protected void writeControlTagAttributes(DisplayContext context, TagWriter out, MinimizableControl control)
				throws IOException {
			super.writeControlTagAttributes(context, out, control);

			LayoutControl self = control;

			writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
			self.writeLayoutSizeAttribute(out);
		}

		@Override
		public void appendControlCSSClasses(Appendable out, MinimizableControl control) throws IOException {
			super.appendControlCSSClasses(out, control);

			out.append(MINIMIZABLE_CONTROL_CSS_CLASS);

			if (control.getModel().getExpansionState() == ExpansionState.MINIMIZED) {
				out.append(MINIMIZED_CSS_CLASS);
			}
		}

		@Override
		protected void writeControlContents(DisplayContext context, TagWriter out, MinimizableControl control)
				throws IOException {
			MinimizableControl self = control;
			self.getChildControl().write(context, out);

			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, TOGGLE_CSS_CLASS);
			out.beginAttribute(ONCLICK_ATTR);
			out.append("return ");
			out.append(FormConstants.FORM_PACKAGE);
			out.append(".MinimizableControl.toggle(");
			out.beginJsString();
			out.append(self.getID());
			out.endJsString();
			out.append(");");
			out.endAttribute();
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out,
				context.getResources().getString(I18NConstants.TOGGLE_MINIMIZE_LABEL));
			out.endBeginTag();
			out.endTag(DIV);
		}

	}

	/**
	 * {@link LayoutControlProvider} wrapping a component in a {@link MinimizableControl}.
	 */
	public static class Provider extends ConfiguredLayoutControlProvider<Provider.Config> {

		/**
		 * Configuration options of {@link Provider}.
		 */
		public interface Config extends PolymorphicConfiguration<LayoutControlProvider> {

			/**
			 * @see #getMinimizedSize()
			 */
			String MINIMIZED_SIZE = "minimizedSize";

			/**
			 * The {@link SizeInfo} to use in minimized state.
			 */
			@Name(MINIMIZED_SIZE)
			@Mandatory
			SizeInfo getMinimizedSize();

		}

		/**
		 * Creates a {@link MinimizableControl.Provider} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Provider(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
			MinimizableControl control =
				new MinimizableControl(new DefaultLayoutData(getConfig().getMinimizedSize()), component);
			LayoutControl contentControl = LayoutControlAdapter.wrap(strategy.createDefaultLayout(component));
			control.setChildControl(contentControl);
			return control;
		}

	}

	/**
	 * {@link ControlCommand} that toggles between normalized and minimized state.
	 */
	public static class Toggle extends ControlCommand {

		/**
		 * Singleton {@link MinimizableControl.Toggle} instance.
		 */
		public static final ControlCommand INSTANCE = new Toggle("toggle");

		/**
		 * Creates a {@link Toggle} command.
		 * 
		 * @param id
		 *        The command id.
		 */
		public Toggle(String id) {
			super(id);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TOGGLE_MINIMIZE;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			return ((MinimizableControl) control).toggle(commandContext);
		}

	}

	@Override
	protected ControlRenderer<MinimizableControl> createDefaultRenderer() {
		return MinimizableControl.Renderer.INSTANCE;
	}

	@Override
	public MinimizableControl self() {
		return this;
	}
}
