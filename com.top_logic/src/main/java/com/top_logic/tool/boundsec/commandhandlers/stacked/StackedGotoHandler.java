/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers.stacked;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.DialogSupport;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.TLContext;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.error.TopLogicException;

/**
 * Override the default GotoHandler to cooperate with the {@link StackHandler}.
 * 
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StackedGotoHandler extends GotoHandler {

	private static Property<StackHandler> STACK_HANDLER = TypedAnnotatable.property(StackHandler.class, "stackHandler");

	/**
	 * Configuration of the {@link StackedGotoHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends GotoHandler.Config {

		/** Name for the property {@link #getExcludeLayouts()}. */
		String EXCLUDE_LAYOUTS_PROPERTY_NAME = "exclude-layouts";

		/**
		 * The layout that are that may be stacked.
		 * 
		 * @return A map of names of {@link LayoutComponent} to their "is as stacked layout"
		 *         property.
		 */
		@MapBinding(key = "name", attribute = "not-stacked")
		@Name(EXCLUDE_LAYOUTS_PROPERTY_NAME)
		Map<ComponentName, Boolean> getExcludeLayouts();

	}

	/** Names of Components that will be considered when stacking GOTOs. */
	private Set<ComponentName> _excludedLayouts;

	/**
	 * Creates a {@link StackedGotoHandler}.
	 */
	public StackedGotoHandler(InstantiationContext context, Config config) {
		super(context, config);
		_excludedLayouts = StackedGotoHandler.fetchLayoutNames(config);
	}

	/**
	 * Check if a component belongs to the set of dialogs to be stacked.
	 */
	public boolean isStackedComponent(LayoutComponent aComponent) {
		return !this._excludedLayouts.contains(aComponent.getName());
	}

	@Override
	public LayoutComponent gotoLayout(LayoutComponent contextComponent, Object targetObject, ComponentName targetComponentName) {
		if (contextComponent instanceof LayoutContainer) {
			/* No useful goto back to a LayoutContainer possible. Therefore no recording. Such can
			 * e.g. occur by executing a "stable bookmark". In such case the context component is
			 * the MainLayout. */
			clearStack(contextComponent);
			return super.gotoLayout(contextComponent, targetObject, targetComponentName);
		}

		// Source and Target of the goto must be configured for a Stacked GOTO
		if (!this._excludedLayouts.contains(contextComponent.getName())) {
			Object startModel = getGotoModel(contextComponent);
			LayoutComponent targetComponent = super.gotoLayout(contextComponent, targetObject, targetComponentName);
			if (targetComponent != null && !this._excludedLayouts.contains(targetComponent.getName())) {
				GotoArgument backArgs = newGotoArgument(contextComponent, startModel);
				GotoArgument forwardArgs = newGotoArgument(targetComponent, targetObject);
				registerGotoInStack(contextComponent.getMainLayout(), backArgs, forwardArgs);
			} else {
				clearStack(contextComponent);
			}
			return targetComponent;
		} else {
			clearStack(contextComponent);
			return super.gotoLayout(contextComponent, targetObject, targetComponentName);
		}

	}

	private Object getGotoModel(LayoutComponent contextComponent) {
		/* Use same heuristic as goto handler to fetch model for goto back. */
		if (contextComponent instanceof Selectable) {
			return ((Selectable) contextComponent).getSelected();
		} else {
			return contextComponent.getModel();
		}
	}

	void gotoBack(DisplayContext context, LayoutComponent contextComponent, ComponentName targetComponentName,
			Object targetObject) {
		try {
			LayoutComponent formerComponent = this.gotoLayout(contextComponent, targetObject, targetComponentName);
			makeTargetVisible(context, contextComponent, formerComponent);
		} catch (TopLogicException ex) {
			/* Goto back is not possible. This may e.g. occur if the model is deleted (open a the
			 * dialog on a grid and delete the object), or the user has changed the object such that
			 * he is not longer allowed to see the object. */
			clearStack(contextComponent);
		}
	}

	private void clearStack(LayoutComponent contextComponent) {
		// clear Stack when going out of scope.
		getStackHandler(contextComponent).clear();
	}

	private void registerGotoInStack(MainLayout ml, GotoArgument contextArgs, GotoArgument gotoArgs) {
		StackHandler stackHandler = getStackHandler(contextArgs.getComponent(ml));
		Stack<GotoArgument> argumentStack = stackHandler.getDialogStack();

		LayoutComponent targetComponent = gotoArgs.getComponent(ml);
		if (targetComponent.openedAsDialog()) {
			int indexOfTargetObject = argumentStack.indexOf(gotoArgs);
			final boolean notVisitedBefore = indexOfTargetObject == -1;
			if (notVisitedBefore) {
				// Push current model on stack to reuse it later.
				if (argumentStack.isEmpty()) {
					argumentStack.push(contextArgs);
				}
				argumentStack.push(gotoArgs);
				handleUpcomingClose(ml, stackHandler, gotoArgs);
			} else {
				final boolean startOfGotoChain = indexOfTargetObject == 0;
				if (startOfGotoChain) {
					/* Goto back to first context component. Therefore the complete stack must be
					 * cleared. */
					argumentStack.clear();
				} else {
					// Object must be displayed again, remove from stack, here
					for (int thePos = argumentStack.size() - 1; thePos > indexOfTargetObject; thePos--) {
						argumentStack.remove(thePos);
					}
					handleUpcomingClose(ml, stackHandler, gotoArgs);
				}
			}
		} else {
			// target component is not a dialog, so the stack can not be navigated backwards.
			stackHandler.clear();
		}
	}

	private void handleUpcomingClose(MainLayout ml, final StackHandler stackHandler, final GotoArgument gotoArgs) {
		final LayoutComponent actuallyOpenedDialog = getActuallyOpenDialog(ml, gotoArgs);
		DialogSupport dialogs = actuallyOpenedDialog.getDialogSupport();
		final DialogComponent dialogComponent = dialogs.getOpenedDialogs().get(actuallyOpenedDialog);
		if (stackHandler.hasCloseListener(dialogComponent)) {
			/* The close handler is same for each situation in which the dialog is closed. */
			return;
		}
		final ToBeValidated gotoBackToStartComponent = new ToBeValidated() {

			@Override
			public void validate(DisplayContext context) {
				Stack<GotoArgument> stack = stackHandler.getDialogStack();
				if (stack.isEmpty()) {
					/* This happens when a goto from an goto-dialog to a non dialog component
					 * occurs. */
					return;
				}
				GotoArgument lastGoto = stack.peek();
				if (actuallyOpenedDialog != getActuallyOpenDialog(ml, lastGoto)) {
					/* The actual goto is not to a component which has as dialog the currently
					 * closed model. This means the dialog was closed due to a goto to a different
					 * dialog. In such case no goto back must occur. */
					/* Can not compare DialogModel: Goto back to same component would fail, because
					 * DialogModel of last Goto is the currently closed model and therefore already
					 * removed. */
					return;
				}

				/* Simulate a goto from the last goto arguments to the next-to-last goto arguments */

				/* Remove last goto args because we want to go back to the next-to-last goto. */
				stack.pop();
				if (stack.isEmpty()) {
					/* Last goto was a goto the the component from which the first goto starts, i.e.
					 * the goto circle came back to the start of the gotos. In such case no goto
					 * must occur. */
					return;
				}
				/* Must not pop next-to-last goto argument, because if stack is then empty,
				 * algorithm thinks it was an initial goto, not a goto back from a formerly visited
				 * component. */
				GotoArgument nextToLastGoto = stack.peek();
				ComponentName nextToLastComponent = nextToLastGoto.getComponentName();
				Object nextToLastModel = nextToLastGoto.getModel();
				StackedGotoHandler.this.gotoBack(context, lastGoto.getComponent(ml), nextToLastComponent,
					nextToLastModel);
			}
		};
		DialogClosedListener dialogCloseListener = new DialogClosedListener() {

			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					return;
				}
				DisplayContext currentInteraction = DefaultDisplayContext.getDisplayContext();
				currentInteraction.getLayoutContext().notifyInvalid(gotoBackToStartComponent);
				stackHandler.removeListener(dialogComponent);
			}
		};

		stackHandler.addCloseListener(dialogComponent, dialogCloseListener);
	}

	static LayoutComponent getActuallyOpenDialog(MainLayout ml, GotoArgument gotoArgs) {
		return gotoArgs.getComponent(ml).getDialogTopLayout();
	}

	static DialogComponent getDialog(MainLayout ml, GotoArgument args) {
		LayoutComponent actuallyOpenedDialog = getActuallyOpenDialog(ml, args);
		DialogSupport dialogs = actuallyOpenedDialog.getDialogSupport();
		return dialogs.getOpenedDialogs().get(actuallyOpenedDialog);
	}

	private static GotoArgument newGotoArgument(LayoutComponent component, Object model) {
		return new GotoArgument(component, model);
	}

	private static Set<ComponentName> fetchLayoutNames(Config config) {
		Map<ComponentName, Boolean> layouts = config.getExcludeLayouts();
		Set<ComponentName> configuredLayouts = new HashSet<>(layouts.size());

		for (Entry<ComponentName, Boolean> theEntry : layouts.entrySet()) {
			if (theEntry.getValue().booleanValue()) {
				configuredLayouts.add(theEntry.getKey());
			}
		}

		Set<ComponentName> resultSet;
		/* Optimize access to configured layouts by shrinking sets in simple case. */
		switch (configuredLayouts.size()) {
			case 0: {
				resultSet = Collections.emptySet();
				break;
			}
			case 1: {
				resultSet = Collections.singleton(configuredLayouts.iterator().next());
				break;
			}
			default: {
				resultSet = configuredLayouts;
				break;
			}
		}
		return resultSet;

	}

	private StackHandler getStackHandler(LayoutComponent aComponent) {
		TLContext theContext = TLContext.getContext();

		if (theContext != null) {
			StackHandler theHandler = theContext.get(StackedGotoHandler.STACK_HANDLER);

			if (theHandler == null) {
				theHandler = newStackHandler();
				theContext.set(StackedGotoHandler.STACK_HANDLER, theHandler);
			}

			return theHandler;
		}
		else {
			throw new TopLogicException(StackedGotoHandler.class, "stack.handler.noContext",
				new Object[] { aComponent });
		}
	}

	/**
	 * Creates a new {@link StackHandler} to store goto arguments.
	 */
	protected StackHandler newStackHandler() {
		return new StackHandler();
	}

	/**
	 * Holder for the name of the target component of a goto and the model.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public final static class GotoArgument {

		// Note: only store the component name instead of the component itself, since the component
		// could be replaced while in-app editing the layout.
		private final ComponentName _componentName;

		/** @see #getModel() */
		private final Object _model;

		/**
		 * Creates a new {@link GotoArgument}.
		 * 
		 * @param component
		 *        See {@link #getComponent(MainLayout)}.
		 * @param model
		 *        See {@link #getModel()}.
		 */
		public GotoArgument(LayoutComponent component, Object model) {
			_componentName = component.getName();
			_model = model;
		}

		/**
		 * {@link ComponentName} of the goto target.
		 */
		public ComponentName getComponentName() {
			return _componentName;
		}

		/**
		 * Target component of the goto.
		 */
		public LayoutComponent getComponent(MainLayout ml) {
			return ml.getComponentByName(_componentName);
		}

		/**
		 * Model used to execute the goto.
		 */
		public Object getModel() {
			return _model;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_componentName == null) ? 0 : _componentName.hashCode());
			result = prime * result + ((_model == null) ? 0 : _model.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GotoArgument other = (GotoArgument) obj;
			if (_componentName == null) {
				if (other._componentName != null)
					return false;
			} else if (!_componentName.equals(other._componentName))
				return false;
			if (_model == null) {
				if (other._model != null)
					return false;
			} else if (!_model.equals(other._model))
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("GotoArgument [_component=");
			builder.append(_componentName);
			builder.append(", _model=");
			builder.append(_model);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * Handle the stack of open dialogs.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class StackHandler {

		// Attributes

		/** A stack of business objects that shall be shown again when a dialog is closed */
		private Stack<GotoArgument> dialogStack = new Stack<>();

		private Set<DialogModel> _knownModels = new HashSet<>();

		/**
		 * Creates a {@link StackHandler}.
		 */
		public StackHandler() {
		}

		// Public methods

		/**
		 * Return the stack of dialogs.
		 * 
		 * @return The requested stack, never <code>null</code>.
		 */
		public Stack<GotoArgument> getDialogStack() {
			return this.dialogStack;
		}

		void addCloseListener(DialogModel model, DialogClosedListener closeListener) {
			if (_knownModels.add(model)) {
				model.addListener(DialogModel.CLOSED_PROPERTY, closeListener);
			}
		}

		boolean hasCloseListener(DialogModel model) {
			return _knownModels.contains(model);
		}

		void removeListener(DialogModel model) {
			_knownModels.remove(model);
		}

		void clear() {
			dialogStack.clear();
			_knownModels.clear();
		}

	}
}
