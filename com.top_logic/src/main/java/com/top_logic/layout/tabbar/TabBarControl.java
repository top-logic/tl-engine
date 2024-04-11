/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.history.BrowserHistory;
import com.top_logic.layout.history.HistoryEntry;
import com.top_logic.layout.history.HistoryException;
import com.top_logic.layout.renderers.DefaultTabBarRenderer;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.css.CssUtil;

/**
 * The class {@link TabBarControl} has a {@link TabBarModel} as Model and a Renderer to render the
 * content of the tab selector.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TabBarControl extends AbstractVisibleControl implements TabBarListener {

	private final TabBarModel tabBarModel;

	private ControlRenderer<? super TabBarControl> renderer = DefaultTabBarRenderer.INSTANCE;

	/**
	 * See {@link #getTabVisibilityFilter}.
	 */
	private Filter<? super Card> _tabVisiblityFilter = FilterFactory.falseFilter();

	private Collection<TabSwitchVetoListener> tabSwitchListeners = new ArrayList<>();

	/** Creates a {@link TabBarControl}. */
	public TabBarControl(Map<String, ? extends ControlCommand> commandsByName, TabBarModel aTabBarModel) {
		super(createCommandMap(commandsByName, new ControlCommand[] { TabSwitch.INSTANCE }));
		this.tabBarModel = aTabBarModel;
	}

	/**
	 * Creates a {@link TabBarControl} with an empty map of commands.
	 */
	public TabBarControl(TabBarModel aTabBarModel) {
		this(Collections.<String, ControlCommand>emptyMap(), aTabBarModel);
	}

	/**
	 * Method will be dispatched to the renderer given by {@link #getRenderer()}
	 * 
	 * @see com.top_logic.layout.basic.AbstractControlBase#internalWrite(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.basic.xml.TagWriter)
	 */
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		renderer.write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		renderer.appendControlCSSClasses(out, this);
	}

	/**
	 * This method returns the {@link TabBarModel} this {@link Control} based on.
	 */
	@Override
	public final TabBarModel getModel() {
		return tabBarModel;
	}

	/**
	 * JavaScript method for handling what happens after a tab was pressed.
	 * 
	 * @param aTabIndex
	 *        the index of the tab which shall be selected.
	 */
	public void writeOnClickSelectContent(TagWriter out, int aTabIndex) throws IOException {
		out.append("services.form.TabBarControl.handleClick(");
		writeIdJsString(out);
		out.append(", ");
		out.writeInt(aTabIndex);
		out.append("); return false;");
	}

	/** This method sets the renderer for writing this {@link TabBarControl}. */
	public final void setRenderer(ControlRenderer<? super TabBarControl> aRenderer) {
		assert aRenderer != null : "'aRenderer' must not be 'null'.";
		if (Utils.equals(aRenderer, this.renderer)) {
			return;
		}
		this.renderer = aRenderer;
		requestRepaint();
	}

	/** Returns the renderer which is currently responsible for rendering this control. */
	public final Renderer<? super TabBarControl> getRenderer() {
		return this.renderer;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		tabBarModel.addTabBarListener(this);
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		tabBarModel.removeTabBarListener(this);
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
		repaintOnSameModel(model);
	}

	private void repaintOnSameModel(Object model) {
		if (!Utils.equals(model, tabBarModel)) {
			return;
		}
		requestRepaint();
	}

	@Override
	public void inactiveCardChanged(TabBarModel sender, Card aCard) {
		repaintOnSameModel(sender);
	}

	@Override
	public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
		repaintOnSameModel(sender);
	}

	@Override
	public void notifyCommandsChanged(TabBarModel sender, Menu oldMenu) {
		repaintOnSameModel(sender);
	}

	/**
	 * This method performs the setting of the new tab index. Before doing this, the added
	 * {@link TabSwitchVetoListener} are called for a veto.
	 * 
	 * @param newSelection
	 *        the index of the tab to switch to
	 * @param updateHistory
	 *        whether the tab switch must change {@link BrowserHistory}
	 * @param browserHistory
	 *        The {@link BrowserHistory} to adopt when demanded.
	 * @throws VetoException
	 *         if some {@link TabSwitchVetoListener} suppresses the tab switch
	 */
	void internalSetNewSelectedIndex(int newSelection, boolean updateHistory, BrowserHistory browserHistory)
			throws VetoException {
		if (tabSwitchListeners.isEmpty()) {
			dispatchIndexUpdateHistory(newSelection, updateHistory, browserHistory);
		} else {
			for (TabSwitchVetoListener vetoListener : tabSwitchListeners) {
				vetoListener.checkVeto(tabBarModel, newSelection);
			}
			dispatchIndexUpdateHistory(newSelection, updateHistory, browserHistory);
		}
	}

	/**
	 * Dispatches the selection to the {@link TabBarModel}.
	 * 
	 * @param updateHistory
	 *        whether the tab switch must change {@link BrowserHistory}
	 * @param browserHistory
	 *        The {@link BrowserHistory} to adopt when demanded.
	 */
	void dispatchIndexUpdateHistory(int newSelection, boolean updateHistory, BrowserHistory browserHistory) {
		if (updateHistory) {
			// must catch outside entry as value can be change after construction
			int currentIndex = internalGetSelectedIndex();
			HistoryEntry entry = createHistoryEntry(currentIndex, newSelection);

			dispatchIndex(newSelection);

			browserHistory.addHistory(entry);
		} else {
			dispatchIndex(newSelection);
		}
	}

	private HistoryEntry createHistoryEntry(int undoSelection, int redoSelection) {
		final Card card;
		if (redoSelection != -1) {
			card = tabBarModel.getAllCards().get(redoSelection);
		} else {
			card = null;
		}

		return new TabModelChangeEntry(undoSelection, redoSelection, card);
	}

	/**
	 * Dispatches the selection to the {@link TabBarModel}.
	 */
	private void dispatchIndex(int newSelection) {
		tabBarModel.removeTabBarListener(this);
		tabBarModel.setSelectedIndex(newSelection);
		tabBarModel.addTabBarListener(this);
		requestRepaint();
	}
	
	/**
	 * Returns the currently selected index or -1 iff currently no tab is selected.
	 *
	 * @see TabBarModel#getSelectedIndex()
	 */
	final int internalGetSelectedIndex() {
		return tabBarModel.getSelectedIndex();
	}

	/**
	 * Adds a new {@link HTMLFragment} to this {@link TabBarControl}.
	 * 
	 * @param aName
	 *        The identifier to register the given {@link HTMLFragment}
	 * @param aView
	 *        The {@link HTMLFragment} to register under the given name. <code>aView == null</code>
	 *        deletes the {@link HTMLFragment} which was formerly registered.
	 * @return The {@link HTMLFragment} formerly registered under the given name.
	 */
	/**
	 * Adds a {@link TabSwitchVetoListener} to {@link #tabSwitchListeners} only if it currently
	 * contains none.
	 * 
	 * @param listener
	 *        Listener which this {@link TabBarControl} should receive.
	 */
	public void addTabSwitchVetoListener(TabSwitchVetoListener listener) {
		assert tabSwitchListeners.isEmpty() : "Sorry. Currently just one listener is supported";
		tabSwitchListeners.add(listener);
	}

	/**
	 * Writes a JavaScript function that makes the tab bar adjust to the size of the browser.
	 * 
	 * @param out
	 *        Writes data-resize attribute.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("dataResize")
	public void writeResizeFunction(TagWriter out) throws IOException {
		out.append("services.viewport.initScrollingDisplay(");
		writeJSONViewportReferences(out);
		out.append(")");
	}

	/**
	 * CSS class for the tab bar container.
	 * 
	 * @param out
	 *        Writes the class.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("tabbarClass")
	public void writeTabbarContainerClass(TagWriter out) throws IOException {
		DefaultTabBarRenderer defaultTabBarRenderer = (DefaultTabBarRenderer) getRenderer();
		String tabbarCSSClass = defaultTabBarRenderer.getTabbarCSSClass();
		out.append(CssUtil.joinCssClasses(tabbarCSSClass));
	}

	/**
	 * Writes the JavaScript method that will be executed when the left arrow is clicked to scroll
	 * left.
	 * 
	 * @param out
	 *        Writes the onmousedown attribute.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("scrollLeft")
	public void writeOnMouseDownLeft(TagWriter out) throws IOException {
		writeOnMouseDown(out, "scrollLeft");
	}

	/**
	 * Writes the JavaScript method that will be executed when the right arrow is being clicked to
	 * scroll right.
	 * 
	 * @param out
	 *        Writes the onmousedown attribute.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("scrollRight")
	public void writeOnMouseDownRight(TagWriter out) throws IOException {
		writeOnMouseDown(out, "scrollRight");
	}

	private void writeOnMouseDown(TagWriter out, String scrollAction) throws IOException {
		out.append("services.viewport.");
		out.append(scrollAction);
		out.append("(");
		writeJSONViewportReferences(out);
		out.append(", false)");
	}

	/**
	 * Writes the left arrow icon for scrolling to the left tabs, if not all of them have space in
	 * the browser window.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes the left arrow icon.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("scrollLeftArrowIcon")
	public void writeScrollLeftArrowIcon(DisplayContext context, TagWriter out) throws IOException {
		String scrolltLeftClass = CssUtil.joinCssClasses("scrollLeftButton", "inactive");
		String buttonId = getID() + "-scrollLeftButtonImage";
		Icons.TAB_LEFT_SCROLL_BUTTON.write(context, out, buttonId, scrolltLeftClass, null, null);
	}

	/**
	 * Writes the right arrow icon for scrolling to the right tabs, if not all of them have space in
	 * the browser window.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes the right arrow icon.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("scrollRightArrowIcon")
	public void writeScrollRightArrowIcon(DisplayContext context, TagWriter out) throws IOException {
		String scrolltRightClass = CssUtil.joinCssClasses("scrollRightButton", "inactive");
		String buttonId = getID() + "-scrollRightButtonImage";
		Icons.TAB_RIGHT_SCROLL_BUTTON.write(context, out, buttonId, scrolltRightClass, null, null);
	}

	/**
	 * Writes a button to the right edge of the tab container that contains commands. This is always
	 * the case when the <b>design mode</b> is active.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes the commands for the right side of the tabbar for instance burger menu in
	 *        design mode.
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	@TemplateVariable("additionalRightContentCommands")
	public void writeCommands(DisplayContext context, TagWriter out) throws IOException {
		Menu allCommands = tabBarModel.getBurgerMenu();
		PopupMenuButtonControl popupControl = TabbarUtil.createPopupControl(allCommands);
		if (popupControl != null) {
			popupControl.write(context, out);
		}
	}

	/**
	 * Writes all tabs.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        Writes tabs.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	@TemplateVariable("tabs")
	public void writeTabs(DisplayContext context, TagWriter out) throws IOException {
		Filter<? super Card> invisibleTabs = getTabVisibilityFilter();
		List<Card> allTabs = tabBarModel.getAllCards();
		TabContent tabContent = new TabContent();
		tabContent.setID(getID());
		for (int tabIndex = 0; tabIndex < allTabs.size(); tabIndex++) {
			Card currentTab = allTabs.get(tabIndex);
			boolean isVisibleTab = !(tabBarModel.isInactive(currentTab) || invisibleTabs.accept(currentTab));
			if (isVisibleTab) {
				writeTab(context, out, tabIndex, currentTab, tabContent);
			}
		}
	}

	private void writeTab(DisplayContext context, TagWriter out, int tabIndex, Card currentTab, TabContent tabContent)
			throws IOException {
		int currentDepth = out.getDepth();
		boolean isSelectedTab = tabIndex == tabBarModel.getSelectedIndex();
		tabContent.setContentInformation(isSelectedTab, currentTab, tabIndex);
		try {
			tabContent.write(context, out);
		} catch (Throwable throwable) {
			try {
				RuntimeException tabRenderingError = ExceptionUtil.createException(
					"Error occured during rendering of tab '" + currentTab.getName() + "'.",
					Collections.singletonList(throwable));
				out.endAll(currentDepth + 1);
				produceErrorOutput(context, out, tabRenderingError);
				out.endAll(currentDepth);
			} catch (Throwable inner) {
				// In the rare case of catastrophe better throw the original.
				throw throwable;
			}
		}
	}

	/**
	 * Writes a script that controls the visibility of the tab buttons. When the right arrow is
	 * pressed, the tabs on the right should become visible and the tabs that disappear on the left
	 * should become invisible.
	 * 
	 * @param out
	 *        Writes tabs.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	@TemplateVariable("tabUpdateScript")
	public void writeScrollButtonUpdateScript(TagWriter out) throws IOException {
		HTMLUtil.beginScriptAfterRendering(out);
		out.append("services.viewport.initScrollButtonChanger(");
		writeJSONViewportReferences(out);
		out.append(");");
		HTMLUtil.endScriptAfterRendering(out);
	}

	/**
	 * Writes a script that handles the scrolling effect when not all tabs have space in the browser
	 * window. The function handles scrolling with the mouse as well as with the arrow buttons.
	 * 
	 * @param out
	 *        Writes js function.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	@TemplateVariable("tabScrollBehaviour")
	public void writeScrollButtonScript(TagWriter out) throws IOException {
		out.append("services.viewport.tabScrollBehaviour(");
		out.append(getID());
		out.append(");");
	}

	/**
	 * Ensures that the selected tab is being displayed in the tab container and will not be hidden
	 * outside the tab bar scope.
	 * 
	 * @param out
	 *        Writes js function.
	 * @throws IOException
	 *         If an I/O error occurs.
	 */
	@TemplateVariable("ensureTabVisible")
	public void writeEnsureTabVisibleScript(TagWriter out) throws IOException {
		out.append("services.viewport.ensureTabVisible(");
		out.append(getID());
		out.append(");");
	}

	private void writeJSONViewportReferences(Appendable out) throws IOException {
		String controlID = getID();
		out.append("{");
		out.append("controlID:'");
		out.append(controlID);
		out.append("',");
		out.append("viewport:'");
		out.append(controlID);
		out.append("',");
		out.append("elementContainer:'");
		out.append(controlID + "-elementContainer");
		out.append("',");
		out.append("scrollContainer:'");
		out.append(controlID + "-scrollContainer");
		out.append("',");
		out.append("scrollLeftButton:'");
		out.append(controlID + "-scrollLeftButton");
		out.append("',");
		out.append("scrollLeftButtonImage:'");
		out.append(controlID + "-scrollLeftButtonImage");
		out.append("',");
		out.append("scrollRightButton:'");
		out.append(controlID + "-scrollRightButton");
		out.append("',");
		out.append("scrollRightButtonImage:'");
		out.append(controlID + "-scrollRightButtonImage");
		out.append("',");
		out.append("additionalRightContent:'");
		out.append(controlID + "-additionalRightContent");
		out.append("',");
		out.append("visibleElement:'");
		out.append("tab");
		out.append("',");
		out.append("activeElement:'");
		out.append("activeTab");
		out.append("'}");
	}

	/**
	 * Returns a {@link Filter} of type {@link Card} from a tab that checks whether a tab is
	 * invisible and should be rendered. It accepts all those {@link Card}s that should not be
	 * displayed for reasons other than security. If a card is set invisible, then a tab which
	 * contains this card will not be displayed. By default all cards are invisible if they are
	 * configured as invisible.
	 */
	public Filter<? super Card> getTabVisibilityFilter() {
		return _tabVisiblityFilter;
	}
	
	/**
	 * @see #getTabVisibilityFilter
	 * 
	 * @param tabVisiblityFilter
	 *        new {@link Filter} to replace current one.
	 */
	public void setTabVisibilityFilter(Filter<? super Card> tabVisiblityFilter) {
		_tabVisiblityFilter = tabVisiblityFilter;
	}

	/**
	 * {@link HistoryEntry} that sets the selection of this {@link TabBarControl}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class TabModelChangeEntry implements HistoryEntry {

		private final int _redoSelection;

		private final int _undoSelection;

		private final Card _card;

		/**
		 * Creates a new {@link TabModelChangeEntry}.
		 * @param undoSelection
		 *        selection to change to in {@link #undo(DisplayContext)}
		 * @param redoSelection
		 *        selection to change to in {@link #redo(DisplayContext)}
		 */
		TabModelChangeEntry(int undoSelection, int redoSelection, Card card) {
			_redoSelection = redoSelection;
			_undoSelection = undoSelection;
			_card = card;
		}

		@Override
		public void undo(DisplayContext context) throws HistoryException {
			changeSelection(context, _undoSelection);
		}

		private void changeSelection(DisplayContext context, int selection) throws HistoryException {
			if (selection != -1) {
				try {
					TabBarControl.this.internalSetNewSelectedIndex(selection, false, context.getWindowScope());
				} catch (VetoException ex) {
					// No not produce user feedback.

					throw new HistoryException(ex);
				}
			}
		}

		@Override
		public void redo(DisplayContext context) throws HistoryException {
			changeSelection(context, _redoSelection);
		}

		@Override
		public <T extends Appendable> T appendTitle(DisplayContext context, T out) throws IOException {
			if (_card != null) {
				try {
					_card.writeCardInfo(context, out);
				} catch (Throwable throwable) {
					out.append(Resources.getInstance().getString(getTabErrorTitleKey()));
					String errorMessage =
						"Could not create history entry for tab '" + _card.getName() + "'. Retrieving of title failed!";
					RuntimeException exception =
						ExceptionUtil.createException(errorMessage, Collections.singletonList(throwable));
					Logger.warn(exception.getMessage(), exception, TabModelChangeEntry.class);
				}
			}
			return out;
		}

		private ResKey getTabErrorTitleKey() {
			// Current translation is same than for faulty select field items.
			return com.top_logic.layout.form.control.I18NConstants.RENDERING_ERROR_SELECT_FIELD;
		}

		@Override
		public String toString() {
			return "TabBarSwitch[undo:" + _undoSelection + ",redo:" + _redoSelection + "]";
		}
	}

	/**
	 * The class {@link TabBarControl.TabSwitch} is the {@link ControlCommand} for switching to another tab.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class TabSwitch extends ControlCommand {

		private static final String COMMAND = "tabSwitch";
		static final TabSwitch INSTANCE = new TabSwitch();

		private static final String NEW_SELECTION = "newSelection";

		private TabSwitch() {
			super(COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			int newSelection = ((Integer) arguments.get(NEW_SELECTION)).intValue();
			TabBarControl tabBar = (TabBarControl) control;
			
			switchTab(newSelection, tabBar, commandContext.getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		}

		private void switchTab(final int newSelection, final TabBarControl tabBar, final WindowScope browserHistory) {
			try {
				tabBar.internalSetNewSelectedIndex(newSelection, true, browserHistory);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						switchTab(newSelection, tabBar, browserHistory);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(tabBar.getWindowScope());
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TAB_SWITCH;
		}
	}
	
}
