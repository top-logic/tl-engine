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
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.layout.form.FormField;
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
		if (isOpenSelectListButtonVisible(entriesTabComponent) || entriesTabComponent.getChildCount() == 0) {
			return Fragments.empty();
		}

		List<LayoutComponent> menuEntries = getVisibleChildren(entriesTabComponent);
		if (menuEntries.isEmpty()) {
			return Fragments.empty();
		}

		SelectField selectList = FormFactory.newSelectField("navigationButton", menuEntries, false, false);
		selectList.setMandatory(true);

		entriesTabComponent.getTabBarModel().addTabBarListener(new megaMenuTabBarListener(selectList));
		selectList.addValueListener((field, oldValue, newValue) -> addValueEventListener(field, newValue));
		return instantiateMegaMenuButton(layoutComponent, entriesTabComponent, menuEntries, selectList);
	}

	private ButtonControl instantiateMegaMenuButton(LayoutComponent layoutComponent, TabComponent entriesTabComponent,
			List<LayoutComponent> menuEntries, SelectField selectList) {
		AbstractCommandModel openSelectListCommand =
			createCommandToWritePopup(layoutComponent, menuEntries, selectList);
		ButtonControl openSelectListButton = new ButtonControl(openSelectListCommand, _renderer);

		TabConfig tabInfo = entriesTabComponent.getParent().getConfig().getTabInfo();
		ThemeImage tabImage = tabInfo.getImage();
		openSelectListCommand.setImage(tabImage);
		openSelectListCommand.setLabel(Resources.getInstance().getString(tabInfo.getLabel(), null));
		addVisibilityListener(entriesTabComponent, selectList, openSelectListButton);
		return openSelectListButton;
	}

	private void addVisibilityListener(TabComponent tabComponent, SelectField selectList,
			ButtonControl openSelectListButton) {
		openSelectListButton.addListener(AbstractControlBase.ATTACHED_PROPERTY, (sender, oldValue,
				newValue) -> handleAttachEvent(sender, newValue, tabComponent, selectList, openSelectListButton));
	}

	private boolean isOpenSelectListButtonVisible(TabComponent entriesTabComponent) {
		LayoutComponent currentChild = entriesTabComponent;
		LayoutComponent current = entriesTabComponent.getParent();

		while (!(current instanceof TabComponent)) {
			currentChild = current;
			current = current.getParent();
		}

		return !((TabComponent) current).canBeVisible(currentChild);
	}

	private List<LayoutComponent> getVisibleChildren(TabComponent entriesTabComponent) {
		List<LayoutComponent> tabList = new ArrayList<>();
		for (int i = 0; i < entriesTabComponent.getChildCount(); i++) {
			LayoutComponent child = entriesTabComponent.getChild(i);
			if (entriesTabComponent.canBeVisible(child)) {
				tabList.add(child);
			}
		}
		return tabList;
	}

	private AbstractCommandModel createCommandToWritePopup(LayoutComponent layoutComponent,
			List<LayoutComponent> tabList, SelectField selectList) {
		return new AbstractCommandModel() {

			{
				/**
				 * This code ensures that the act of pressing the button is not recorded by the
				 * script recorder. This is necessary because the desired recording is the selection
				 * made within the selection list and the subsequent opening of the selected view,
				 * not the button press itself. The script recorder captures only the initial
				 * action, ignoring subsequent actions triggered by the button press. Therefore,
				 * this implementation aims to record the view that gets opened as a result of
				 * making a selection from the selection list, bypassing the button press action
				 * that merely shows the selection list popup. This approach ensures that the actual
				 * user interaction of interest, which is the selection and view opening, is
				 * captured for script recording purposes.
				 */
				ScriptingRecorder.annotateAsDontRecord(this);
			}

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				ButtonControl openSelectListButton = context.get(Command.EXECUTING_CONTROL);
				String openSelectListButtonID = openSelectListButton.getID();

				int nbrOfOptionElements = tabList.size();

				boolean isGridNeeded = nbrOfOptionElements > 4;
				PopupDialogControl popupDialog =
					MegaMenuPopupDialogCreater.createPopupDialog(context, isGridNeeded,
						openSelectListButton.getFrameScope(), openSelectListButtonID);

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
							LayoutComponent currOption = tabList.get(i);
							MegaMenuOptionControl option =
								new MegaMenuOptionControl(currOption, selectList, popupDialog);
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

	private void addValueEventListener(FormField field, Object newValue) {
		SelectField selectList = (SelectField) field;
		if (!CollectionUtil.isEmptyOrNull((Collection<?>) newValue)) {
			LayoutComponent singleSelection = (LayoutComponent) selectList.getSingleSelection();
			handleScriptRecordingEvents(singleSelection);
			singleSelection.makeVisible();
		}
	}

	private void handleScriptRecordingEvents(LayoutComponent singleSelection) {
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
	}

	private void handleAttachEvent(AbstractControlBase sender, Boolean newValue, TabComponent tabComponent,
			SelectField selectList, ButtonControl openSelectListButton) {
		VisibilityListener visibilityListener = createVisibilityListener(selectList, openSelectListButton);
		if (newValue) {
			tabComponent.addListener(LayoutComponent.VISIBILITY_EVENT, visibilityListener);
		} else {
			tabComponent.removeListener(LayoutComponent.VISIBILITY_EVENT, visibilityListener);
		}
	}

	private VisibilityListener createVisibilityListener(SelectField selectList, ButtonControl openSelectListButton) {
		return (sender, oldVisibility, newVisibility) -> {
			if (sender instanceof TabComponent) {
				LayoutComponent currComponent = (LayoutComponent) sender;
				// Executes when user is leaving the currently opened tab.
				if (_componentName.equals(currComponent.getName()) && !newVisibility) {
					selectList.setAsSingleSelection(null);
				}
			}
			openSelectListButton.requestRepaint();
			return Bubble.BUBBLE;
		};
	}

	private static class megaMenuTabBarListener implements TabBarListener {
		private final SelectField selectList;

		public megaMenuTabBarListener(SelectField selectList) {
			this.selectList = selectList;
		}

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
				Object selectedObject) {
			if (selectedObject == null) {
				selectList.setAsSingleSelection(null);
			} else {
				LayoutComponent component = ((TabbedLayoutComponent) selectedObject).getContent();
				selectList.setAsSingleSelection(component);
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
	}

}