/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.component.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.control.IButtonRenderer;
import com.top_logic.layout.form.control.MegaMenuControl.MegaMenuPopupDialogCreater;
import com.top_logic.layout.form.control.MegaMenuOptionControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogControl.HorizontalPopupPosition;
import com.top_logic.layout.structure.PopupDialogControl.VerticalPopupPosition;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.Card;
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
		@InstanceDefault(ButtonRenderer.class)
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
		_renderer = config.getRenderer();
	}

	@Override
	public HTMLFragment createView(LayoutComponent component) {
		LayoutComponent layoutComponent = component.getMainLayout().getComponentByName(_componentName);
		if (layoutComponent == null) {
			return Fragments.empty();
		}

		TabComponent entriesTabComponent = (TabComponent) layoutComponent;
		if (isButtonVisible(entriesTabComponent) || entriesTabComponent.getChildCount() == 0) {
			return Fragments.empty();
		}

		ArrayList<LayoutComponent> entriesList = getVisibleChildren(entriesTabComponent);
		if (entriesList.isEmpty()) {
			return Fragments.empty();
		}

		SelectField megaMenu = FormFactory.newSelectField("navigationButton", entriesList, false, false);
		megaMenu.setMandatory(true);

		addTabBarListener(entriesTabComponent, megaMenu);
		addValueEventListener(megaMenu);
		ButtonControl megaMenuButton =
			instantiateMegaMenuButton(layoutComponent, entriesTabComponent, entriesList, megaMenu);

		return megaMenuButton;
	}

	private ButtonControl instantiateMegaMenuButton(LayoutComponent layoutComponent, TabComponent entriesTabComponent,
			ArrayList<LayoutComponent> entriesList, SelectField megaMenu) {
		AbstractCommandModel mainTabbarCommand = createCommandToWritePopup(layoutComponent, entriesList, megaMenu);
		ButtonControl megaMenuButton = new ButtonControl(mainTabbarCommand, _renderer);

		TabConfig tabInfo = entriesTabComponent.getParent().getConfig().getTabInfo();
		ThemeImage tabImage = tabInfo.getImage();
		mainTabbarCommand.setImage(tabImage);
		mainTabbarCommand.setLabel(Resources.getInstance().getString(tabInfo.getLabel(), null));
		addVisibilityListener(entriesTabComponent, megaMenu, megaMenuButton);
		return megaMenuButton;
	}


	private boolean isButtonVisible(TabComponent entriesTabComponent) {
		LayoutComponent currentChild = entriesTabComponent;
		LayoutComponent current = entriesTabComponent.getParent();

		while (!(current instanceof TabComponent)) {
			currentChild = current;
			current = current.getParent();
		}

		return !((TabComponent) current).canBeVisible(currentChild);
	}

	private ArrayList<LayoutComponent> getVisibleChildren(TabComponent entriesTabComponent) {
		ArrayList<LayoutComponent> mainTabList = new ArrayList<>();
		for (int i = 0; i < entriesTabComponent.getChildCount(); i++) {
			if (entriesTabComponent.canBeVisible(entriesTabComponent.getChild(i))) {
				mainTabList.add(entriesTabComponent.getChild(i));
			}
		}
		return mainTabList;
	}

	private void addTabBarListener(TabComponent entriesTabComponent, SelectField megaMenu) {
		TabBarListener listener = new TabBarListener() {

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
					Object selectedObject) {
				if (selectedObject == null) {
					megaMenu.setAsSingleSelection(null);
				} else {
					LayoutComponent component = ((TabbedLayoutComponent) selectedObject).getContent();
					megaMenu.setAsSingleSelection(component);
				}
			}

			@Override
			public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
				// Nothing to do
			}

			@Override
			public void inactiveCardChanged(TabBarModel sender, Card aCard) {
				// Nothing to do
			}

		};

		entriesTabComponent.getTabBarModel().addTabBarListener(listener);
	}

	private AbstractCommandModel createCommandToWritePopup(LayoutComponent layoutComponent,
			ArrayList<LayoutComponent> mainTabList, SelectField megaMenu) {
		return new AbstractCommandModel() {

			{
				/* Won't record pressing the mega menu button as opening the administration view
				 * action. */
				ScriptingRecorder.annotateAsDontRecord(this);
			}

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				ButtonControl buttonControl = context.get(Command.EXECUTING_CONTROL);
				String buttonControlID = buttonControl.getID();

				int nbrOfOptionElements = mainTabList.size();

				boolean isGridNeeded = nbrOfOptionElements > 4;
				PopupDialogControl popupDialog =
					MegaMenuPopupDialogCreater.createPopupDialog(context, isGridNeeded,
						buttonControl.getFrameScope(), buttonControlID);

				popupDialog.setContent(new HTMLFragment() {

					@Override
					public void write(DisplayContext context1, TagWriter out) throws IOException {
						out.beginBeginTag(HTMLConstants.DIV);
						out.writeAttribute(HTMLConstants.CLASS_ATTR, "tl-mega-menu-container");
						out.beginAttribute(HTMLConstants.ONKEYDOWN_ATTR);
						out.write(
							"services.form.MegaMenuControl.handleArrowKeyNavigation(event, " + isGridNeeded + ")");
						out.endAttribute();
						out.endBeginTag();

						for (int i = 0; i < nbrOfOptionElements; i++) {
							LayoutComponent currOption = mainTabList.get(i);
							MegaMenuOptionControl option = new MegaMenuOptionControl(currOption, megaMenu, popupDialog);
							option.write(context1, out);
						}
						out.endTag(HTMLConstants.DIV);
					}
				});
				context.getWindowScope().openPopupDialog(popupDialog);
				return HandlerResult.DEFAULT_RESULT;
			}

			@Override
			public boolean isLinkActive() {
				return layoutComponent.isVisible();
			}
		};
	}

	private void addValueEventListener(SelectField megaMenu) {
		megaMenu.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				SelectField megaMenu = (SelectField) field;
				if (!CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
					LayoutComponent singleSelection = (LayoutComponent) megaMenu.getSingleSelection();
					if (ScriptingRecorder.isRecordingActive()) {
						TabComponent tabComponent =
							(TabComponent) singleSelection.getMainLayout().getComponentByName(_componentName);
						boolean before = LayoutComponentResolver
							.allowResolvingHiddenComponents(DefaultDisplayContext.getDisplayContext(), true);
						try {
							ScriptingRecorder.recordTabSwitch(tabComponent,
								tabComponent.getChildList().indexOf(singleSelection));
						} finally {
							LayoutComponentResolver
								.allowResolvingHiddenComponents(DefaultDisplayContext.getDisplayContext(), before);
						}
					}
					singleSelection.makeVisible();
				}
			}
		});
	}

	private void addVisibilityListener(TabComponent tabComponent, SelectField megaMenu, ButtonControl megaMenuButton) {
		megaMenuButton.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
			VisibilityListener _visibilityListener = new VisibilityListener() {
				@Override
				public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
					if (sender instanceof TabComponent) {
						LayoutComponent currComponent = (LayoutComponent) sender;
						// Executes when user is leaving the currently opened tab.
						if (_componentName.equals(currComponent.getName()) && !newVisibility) {
							megaMenu.setAsSingleSelection(null);
						}
					}
					megaMenuButton.requestRepaint();
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