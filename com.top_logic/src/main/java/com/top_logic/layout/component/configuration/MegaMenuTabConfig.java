/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.control.IButtonRenderer;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.control.MegaMenuOptionControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogControl.HorizontalPopupPosition;
import com.top_logic.layout.structure.PopupDialogControl.VerticalPopupPosition;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Navigation button that displays the labels and tooltips of all tabs in a tab bar.
 * 
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 *
 */
public class MegaMenuTabConfig
		extends AbstractViewConfiguration<com.top_logic.layout.component.configuration.MegaMenuTabConfig.Config> {

	/**
	 * Configuration interface for {@link MegaMenuTabConfig}.
	 * 
	 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
	 *
	 */
	public interface Config extends AbstractViewConfiguration.Config<AbstractViewConfiguration<?>> {

		/**
		 * @see #getRenderer()
		 */
		String RENDERER = "renderer";

		/**
		 * @see #getHorizontalPosition()
		 */
		String HORIZONTAL_POSITION = "horizontalPosition";

		/**
		 * @see #getVerticalPosition()
		 */
		String VERTICAL_POSITION = "verticalPosition";

		/**
		 * The {@link IButtonRenderer} to create the visual representation.
		 */
		@Name(RENDERER)
		@InstanceFormat
		IButtonRenderer getRenderer();

		/**
		 * Horizontal position where the pop-up is opened.
		 */
		@Name(HORIZONTAL_POSITION)
		HorizontalPopupPosition getHorizontalPosition();

		/**
		 * Vertical position where the pop-up is opened.
		 */
		@Name(VERTICAL_POSITION)
		VerticalPopupPosition getVerticalPosition();

		/**
		 * Gets {@ComponentName} from the xml file.
		 * 
		 * @return {@ComponentName} to get {@LayoutComponent} for main tab.
		 */
		@Name("component")
		ComponentName getComponent();
	}

	private final ComponentName _componentName;

	private final IButtonRenderer _renderer;

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for getting the information from xml file.
	 * @throws ConfigurationException
	 *         Error handling.
	 */
	public MegaMenuTabConfig(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_componentName = config.getComponent();

		if (config.getRenderer() == null) {
			_renderer = ButtonRenderer.INSTANCE;
		} else {
			_renderer = config.getRenderer();
		}
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		LayoutComponent layoutComponent = component.getMainLayout().getComponentByName(_componentName);
		if (layoutComponent == null) {
			return Fragments.empty();
		}
		TabComponent tabComponent = (TabComponent) layoutComponent;
		TabConfig tabInfo = tabComponent.getParent().getConfig().getTabInfo();
		ThemeImage tabImage = tabInfo.getImage();

		ArrayList<LayoutComponent> mainTabList = addChildren(tabComponent);
		SelectField megaMenu = FormFactory.newSelectField("navigationButton", mainTabList, false, false);
		megaMenu.setMandatory(true);

		addValueEventListener(megaMenu);
		AbstractCommandModel mainTabbarCommand = createCommandToWritePopup(layoutComponent, mainTabList, megaMenu);
		ButtonControl mainTabbarField = new ButtonControl(mainTabbarCommand, _renderer);
		mainTabbarCommand.setImage(tabImage);
		mainTabbarCommand.setLabel(Resources.getInstance().getString(tabInfo.getLabel(), null));
		addVisibilityListener(tabComponent, megaMenu, mainTabbarField);

		return mainTabbarField;
	}

	private ArrayList<LayoutComponent> addChildren(TabComponent tabComponent) {
		ArrayList<LayoutComponent> mainTabList = new ArrayList<>();
		for (LayoutComponent child : tabComponent.getChildList()) {
			mainTabList.add(child);
		}
		return mainTabList;
	}

	private AbstractCommandModel createCommandToWritePopup(LayoutComponent layoutComponent,
			ArrayList<LayoutComponent> mainTabList, SelectField megaMenu) {
		return new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				ButtonControl buttonControl = context.get(Command.EXECUTING_CONTROL);
				PopupDialogControl popupDialog;
				int nbrOfOptionElements = mainTabList.size();
				boolean isGridNeeded = nbrOfOptionElements > 4;
				if (isGridNeeded) {
					popupDialog = createPopUpDialogWindow(context, 600, Scrolling.AUTO);
				} else {
					popupDialog = createPopUpDialogWindow(context, 300, Scrolling.AUTO);
				}

				popupDialog.setContent(new HTMLFragment() {

					@Override
					public void write(DisplayContext context1, TagWriter out) throws IOException {
						out.beginBeginTag(HTMLConstants.DIV);
						if (isGridNeeded) {
							out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuGridContainer");
						} else {
							out.writeAttribute(HTMLConstants.CLASS_ATTR, "megaMenuFlexContainer");
						}
						out.endBeginTag();

						for (int i = 0; i < nbrOfOptionElements; i++) {
							LayoutComponent currOption = mainTabList.get(i);
							MegaMenuOptionControl option = new MegaMenuOptionControl(currOption, megaMenu, popupDialog);
							option.write(context1, out);
						}
						out.endTag(HTMLConstants.DIV);
					}
				});
				buttonControl.getWindowScope().openPopupDialog(popupDialog);
				return HandlerResult.DEFAULT_RESULT;
			}

			private PopupDialogControl createPopUpDialogWindow(DisplayContext context, int aWidth,
					Scrolling aScrollable) {
				int borderWidth = Icons.MEGA_MENU_BORDER_WIDTH.get();
				DefaultLayoutData popupLayout =
					new DefaultLayoutData(DisplayDimension.dim(aWidth, DisplayUnit.PIXEL), 100, DisplayDimension.dim(0,
						DisplayUnit.PIXEL), 100, aScrollable);
				DefaultPopupDialogModel defaultPopupModel = new DefaultPopupDialogModel(null, popupLayout, borderWidth);

				ButtonControl buttonControl = context.get(Command.EXECUTING_CONTROL);
				String buttonControlID = buttonControl.getID();
				HorizontalPopupPosition horizontalPosition = getConfig().getHorizontalPosition();
				VerticalPopupPosition verticalPosition = getConfig().getVerticalPosition();

				PopupDialogControl popupDialogControl =
					new PopupDialogControl(buttonControl.getFrameScope(), defaultPopupModel,
						buttonControlID, horizontalPosition, verticalPosition);
				return popupDialogControl;
			}

			@Override
			public boolean isLinkActive() {
				return layoutComponent.isVisible();
			}
		};
	}

	private void addValueEventListener(SelectField selectField) {
		selectField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				SelectField megaMenu = (SelectField) field;
				if (!CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
					LayoutComponent singleSelection = (LayoutComponent) megaMenu.getSingleSelection();
					singleSelection.makeVisible();
				}
			}
		});
	}

	private void addVisibilityListener(TabComponent tabComponent, SelectField megaMenu, ButtonControl mainTabbarField) {
		mainTabbarField.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
			VisibilityListener _visibilityListener = new VisibilityListener() {
				@Override
				public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
					if (sender instanceof TabComponent) {
						LayoutComponent currComponent = (LayoutComponent) sender;
						// This code is supposed to be executed only when user is leaving the
						// currently visiting main tab.
						if (_componentName.equals(currComponent.getName()) && !newVisibility) {
							megaMenu.setAsSingleSelection(null);
						}
					}
					mainTabbarField.requestRepaint();
					return Bubble.BUBBLE;
				}
			};

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					tabComponent.addListener(LayoutComponent.VISIBILITY_EVENT, _visibilityListener);
				} else {
					tabComponent.removeListener(LayoutComponent.VISIBILITY_EVENT, _visibilityListener);
				}
			}
		});
	}
}