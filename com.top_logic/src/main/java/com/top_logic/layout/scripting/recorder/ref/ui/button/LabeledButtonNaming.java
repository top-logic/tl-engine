/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.button;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.form.popupmenu.MenuButtonRenderer;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractLabeledButtonActionOp;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ModelNamingScheme} for identifying buttons by their label.
 * 
 * @see AbstractLabeledButtonActionOp
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LabeledButtonNaming
		extends AbstractModelNamingScheme<CommandModel, LabeledButtonNaming.LabeledButtonName> {

	/**
	 * {@link ModelName} of the {@link LabeledButtonNaming}.
	 */
	public interface LabeledButtonName extends ModelName {

		/** The label of the button. */
		String getLabel();

		/** @see #getLabel() */
		void setLabel(String label);

		/**
		 * The {@link LayoutComponent} where the button is rendered.
		 * 
		 * <p>
		 * If component is <code>null</code>, it is interpreted as the top level component of the
		 * current browser window, i.e. either the {@link MainLayout} or a {@link WindowComponent}.
		 * </p>
		 * 
		 * @see LayoutComponent#getEnclosingFrameScope()
		 */
		ModelName getComponent();

		/** @see #getComponent() */
		void setComponent(ModelName component);

		/**
		 * An optional {@link ModelName} to identify the button.
		 * 
		 * <p>
		 * When the button can not be identified unique by its label, {@link #getBusinessObject()}
		 * may be set to attach additional informations to identify the button.
		 * </p>
		 * 
		 * @return May be <code>null</code>, which menas the label is enough to identifiy the
		 *         button.
		 * 
		 * @see LabeledButtonNaming#BUSINESS_OBJECT
		 */
		ModelName getBusinessObject();

		/**
		 * Setter for {@link #getBusinessObject()}.
		 */
		void setBusinessObject(ModelName name);

	}

	/**
	 * Property to attach an additional business object to the {@link CommandModel} when the label
	 * is not sufficient to identify the button.
	 * 
	 * @see LabeledButtonName#getBusinessObject()
	 */
	public static final Property<Object> BUSINESS_OBJECT =
		TypedAnnotatable.property(Object.class, "business object");

	@Override
	public Class<LabeledButtonName> getNameClass() {
		return LabeledButtonName.class;
	}

	@Override
	public Class<CommandModel> getModelClass() {
		return CommandModel.class;
	}

	@Override
	public CommandModel locateModel(ActionContext context, LabeledButtonName name) {
		String label = name.getLabel();
		FrameScope scope = resolveScope(context, name);
		return findButton(label, scope, context.resolve(name.getBusinessObject())).getModel();
	}

	private FrameScope resolveScope(ActionContext context, LabeledButtonName name) {
		ModelName component = name.getComponent();
		if (component != null) {
			return ((LayoutComponent) context.resolve(component)).getEnclosingFrameScope();
		} else {
			return context.getDisplayContext().getWindowScope().getTopLevelFrameScope();
		}
	}

	/**
	 * Searches for a single, clickable button with the given label.
	 * 
	 * @param frameScope
	 *        The {@link FrameScope} in which the button should is displayed.
	 * @param businessObject
	 *        An optional identifier for the button.
	 */
	public static ButtonControl findButton(String searchedLabel, FrameScope frameScope, Object businessObject) {
		List<ButtonControl> exactMatches = new ArrayList<>();
		/* If the business object is null, it may be that a former script is executed where in the
		 * meanwhile the button carries a business object. In that situation the fuzzy matches
		 * contain all buttons that have the same label, also when they have a non-null business
		 * object. */
		List<ButtonControl> fuzzyMatches;
		if (businessObject == null) {
			fuzzyMatches = new ArrayList<>();
		} else {
			fuzzyMatches = null;
		}
		addButtonControls(exactMatches, fuzzyMatches, frameScope, searchedLabel, businessObject);
		switch (exactMatches.size()) {
			case 0:
				if (fuzzyMatches == null) {
					throw new RuntimeException("No clickable button with label '" + searchedLabel + "' found!");
				} else {
					switch (fuzzyMatches.size()) {
						case 0:
							throw new RuntimeException("No clickable button with label '" + searchedLabel + "' found!");
						case 1:
							return fuzzyMatches.get(0);
						default:
							throw new RuntimeException(
								"Found multiple clickable buttons labeled with '" + searchedLabel + "'."
									+ " Buttons: " + fuzzyMatches);

					}
				}
			case 1:
				return exactMatches.get(0);
			default:
				throw new RuntimeException("Found multiple clickable buttons labeled with '" + searchedLabel + "'."
					+ " Buttons: " + exactMatches);

		}
	}

	private static void addButtonControls(List<ButtonControl> exactMatches, List<ButtonControl> fuzzyMatches,
			FrameScope frameScope, String searchedLabel, Object businessObject) {
		Collection<CommandListener> commandListeners = frameScope.getCommandListener();
		for (CommandListener commandListener : commandListeners) {
			if (commandListener instanceof ButtonControl) {
				ButtonControl buttonControl = (ButtonControl) commandListener;
				if (inBackground(buttonControl)) {
					/* A dialog is opened over the button. Do *not* exclude buttons which are not
					 * visible by their model. These are needed to be found to be able to execute
					 * assertions that a command is hidden. */
					continue;
				}
				CommandModel button = buttonControl.getModel();
				if (!hasSearchedLabel(button, searchedLabel)) {
					// Different label
					continue;
				}
				if (hasBusinessObject(button, businessObject)) {
					exactMatches.add(buttonControl);
				} else if (businessObject == null) {
					fuzzyMatches.add(buttonControl);
				}

			}
			if (commandListener instanceof PopupMenuButtonControl) {
				PopupMenuButtonControl popupButton = (PopupMenuButtonControl) commandListener;
				if (inBackground(popupButton)) {
					/* A dialog is opened over the button. Do *not* exclude pop-ups which are not
					 * visible by their model. Buttons in the menu must be found to be able to
					 * execute assertions that a command is hidden. */
					continue;
				}
				Menu menu = popupButton.getModel().getMenu();
				for (CommandModel button : menu.buttons()) {
					{
						if (!hasSearchedLabel(button, searchedLabel)) {
							// Different label
							continue;
						}

						// Fake displaying the button hidden in the menu. This is necessary to be
						// able to retrieve its scope later on (when simulating the click on the
						// button) - at least if this button itself opens another popup menu.
						final ButtonControl menuButton = new ButtonControl(button, MenuButtonRenderer.INSTANCE);
						DisplayContext context = DefaultDisplayContext.getDisplayContext();
						context.executeScoped(popupButton, new Command() {
							@Override
							public HandlerResult executeCommand(DisplayContext renderingContext) {
								try {
									menuButton.write(renderingContext, new TagWriter());
								} catch (IOException ex) {
									throw new IOError(ex);
								}
								return HandlerResult.DEFAULT_RESULT;
							}
						});
						// Drop from surrounding scope, but keep own scope.
						menuButton.detach();
						if (hasBusinessObject(button, businessObject)) {
							exactMatches.add(menuButton);
						} else if (businessObject == null) {
							fuzzyMatches.add(menuButton);
						}
					}
				}
			}
		}
		if (exactMatches.isEmpty()) {
			FrameScope enclosingScope = frameScope.getEnclosingScope();
			if (enclosingScope != null) {
				addButtonControls(exactMatches, fuzzyMatches, enclosingScope, searchedLabel, businessObject);
			}
		}
	}

	private static boolean inBackground(AbstractControlBase control) {
		return control.isViewDisabled();
	}

	private static boolean hasBusinessObject(CommandModel button, Object businessObject) {
		return Objects.equals(businessObject, button.get(LabeledButtonNaming.BUSINESS_OBJECT));
	}

	private static boolean hasSearchedLabel(CommandModel button, String searchedLabel) {
		return StringServices.equals(button.getLabel(), searchedLabel);
	}

	@Override
	protected void initName(LabeledButtonName name, CommandModel model) {
		throw new UnsupportedOperationException("This cannot be recorded generically.");
	}

	@Override
	protected boolean isCompatibleModel(CommandModel model) {
		return false;
	}

}
