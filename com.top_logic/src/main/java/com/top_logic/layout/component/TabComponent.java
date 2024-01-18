/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.commands.AddDialogCommand;
import com.top_logic.layout.editor.commands.AddTabCommand;
import com.top_logic.layout.editor.commands.ConfigureTabsCommand;
import com.top_logic.layout.editor.commands.DeleteComponentCommand;
import com.top_logic.layout.editor.commands.EditComponentCommand;
import com.top_logic.layout.editor.commands.EditLayoutCommand;
import com.top_logic.layout.editor.commands.ResetLayoutConfiguration;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.layout.renderers.DefaultTabBarRenderer;
import com.top_logic.layout.renderers.TabBarRenderer;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tabbar.DefaultTabBarModel;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.layout.tabbar.TabInfo;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.layout.tabbar.TabSwitchVetoListener;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutList;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.mig.html.layout.SubComponentConfig;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;
import com.top_logic.tool.boundsec.BoundCheckerLayoutConfig;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * The class {@link TabComponent} is a {@link LayoutContainer} whose children are displayed as tabs.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class TabComponent extends LayoutList implements BoundCheckerDelegate, TabBarListener,
		DecorationValueListener {
    
	/**
	 * Property determining whether the tab list of {@link #getTabBar()} must be updated when the
	 * {@link #getChildList() children} changed.
	 */
	private static final Property<Boolean> UPDATE_TAB_LIST =
		TypedAnnotatable.property(Boolean.class, "must tab list be updated", Boolean.TRUE);
    
	private static final String XML_TAG_RENDERER_NAME = "renderer";
	
	/**
	 * Group name used in templates to mark it as a template to create tabs.
	 */
	public static final String TABBAR_TEMPLATE_GROUP = "tabbar";

	/**
	 * Configuration options for {@link TabComponent}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends LayoutList.Config, BoundCheckerLayoutConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * @see SubComponentConfig#getComponents()
		 */
		String TAG_NAME = "tabbar";

		@Override
		@ClassDefault(TabComponent.class)
		public Class<? extends LayoutComponent> getImplementationClass();

		@Name(XML_TAG_RENDERER_NAME)
		@InstanceFormat
		TabBarRenderer getRenderer();

		/**
		 * Whenever the number of visible tabs change, select the first one that can be selected.
		 * 
		 * <p>
		 * With this option in combination with a hidden tab bar, one can realize a dynamic layout
		 * that displays the first alternative that can show the current model.
		 * </p>
		 */
		@Name("onChangeSelectFirstTab")
		boolean getOnChangeSelectFirstTab();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			LayoutList.Config.super.modifyIntrinsicCommands(registry);
			registry.registerIfExists(ConfigureTabsCommand.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(ResetLayoutConfiguration.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(AddTabCommand.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(AddDialogCommand.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(EditComponentCommand.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(DeleteComponentCommand.DEFAULT_COMMAND_ID, true);
			registry.registerIfExists(EditLayoutCommand.DEFAULT_COMMAND_ID, true);
		}

	}

	private transient Map<ComponentName, TabInfo> decorators;

	/**
	 * This variable indicates whether the allowness state of the children is consistent to the
	 * state of the model.
	 */
	private boolean modelValid;

	/** A renderer which can be used to render the tab bar of this {@link TabComponent} */
	private final TabBarRenderer renderer;
	
	/**
	 * See {@link #getTabVisibilityFilter}.
	 */
	private Filter<? super Card> _tabVisiblityFilter = new Filter<>() {

		@Override
		public boolean accept(Card anObject) {
			if (!(anObject instanceof TabbedLayoutComponent)) {
				return false;
			}
			return !((TabbedLayoutComponent) anObject).getCardInfo().getConfig().isRendered();
		}

	};

	private final TabComponentTabBarModel _tabBar = new TabComponentTabBarModel(Collections.emptyList(),
		Collections.emptySet());

	private final BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

	private boolean _onChangeSelectFirstTab;

	/**
	 * Creates a {@link TabComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TabComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		
		getTabBar().addTabBarListener(this);

		TabBarRenderer configuredRenderer = config.getRenderer();
		if (configuredRenderer != null) {
			this.renderer = configuredRenderer;
		} else {
			this.renderer = DefaultTabBarRenderer.INSTANCE;
		}

		_onChangeSelectFirstTab = config.getOnChangeSelectFirstTab();
	}

	/**
	 * If caller for security change is a child of myself, we have to invalidate and redraw.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent#receiveModelSecurityChangedEvent(java.lang.Object)
	 */
	@Override
	protected boolean receiveModelSecurityChangedEvent(Object aCaller) {
		int childIndex = getChildIndex(aCaller);

		// Is one of my children responsible for the security event?
		if (childIndex == -1) {
			return false;
		}

		if (canBeVisible(childIndex)) {
			getTabBar().removeInactiveCard(getCard(childIndex));
		} else {
			getTabBar().addInactiveCard(getCard(childIndex));
		}

		int theSelectedIndex = getTabBar().getSelectedIndex();

		if (theSelectedIndex >= 0) {
			if (!this.canBeVisible(theSelectedIndex)) {
				fireModelEvent(null, ModelEventListener.SECURITY_CHANGED);
				this.modelValid = false;
			} else if (!this.isVisible()) {
				fireModelEvent(null, ModelEventListener.SECURITY_CHANGED);
				this.modelValid = false;
			}
		} else {
			fireModelEvent(null, ModelEventListener.SECURITY_CHANGED);
			this.modelValid = false;
		}

		if (_onChangeSelectFirstTab) {
			setSelectedIndex(findNextVisible(-1));
		}
		return true;
	}

	/**
	 * The {@link TabbedLayoutComponent} of the child at the given index.
	 * 
	 * @throws IndexOutOfBoundsException
	 *         If the index is < 0 or >= {@link #getChildCount()}.
	 */
	public TabbedLayoutComponent getCard(int index) {
		return (TabbedLayoutComponent) getCards().get(index);
	}

	/**
	 * All {@link Card}s in the tab bar.
	 */
	public List<Card> getCards() {
		return getTabBar().getAllCards();
	}

	private int getChildIndex(Object aCaller) {
		if (aCaller instanceof LayoutComponent) {
			LayoutComponent theCaller = (LayoutComponent) aCaller;
			LayoutComponent thePotentialChild = theCaller;
			while (thePotentialChild.getParent() != null && !(thePotentialChild.getParent() instanceof TabComponent)) {
				thePotentialChild = thePotentialChild.getParent();
			}
			if (thePotentialChild.getParent() == null) {
				return -1;
			} else {
				return getChildList().indexOf(thePotentialChild);
			}
		} else {
			return -1;
		}
	}

	/**
	 * The name of the tab with the given index.
	 */
	public String getTabName(int tabIndex) {
		return getCard(tabIndex).getName();
	}

	@Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return this.getTabBar().getSingleSelection() != null ?
			Collections.singletonList(this.getCurrentChild())
			: Collections.<LayoutComponent> emptyList();
	}

	@Override
	public boolean isOuterFrameset() {
		return true;
	}
	
	/**
	 * The {@link TabComponent} sets the visibility of the current selected child.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutContainer#setChildVisibility(boolean)
	 */
	@Override
	protected void setChildVisibility(boolean visible) {
    	int theIndex = getTabBar().getSelectedIndex();
    	
    	/* here we switch the visibility of childs */
		List childList = this.getChildList();
		int size = childList.size();
		for (int i = 0; i < size; i++) {
			LayoutComponent child = (LayoutComponent) childList.get(i);
			child.setVisible(visible && (theIndex == i));
		}
	}

	/**
	 * Try to make the given child component visible. Since we dont hide anything, we just try to
	 * make ourself visible.
	 * 
	 * @return true when child component is visible after the call.
	 */
	@Override
	public boolean makeVisible(LayoutComponent aChild) {

		boolean superVisible = makeVisible();

		if (superVisible) { // No need to do this if super failed me
			int index = getChildList().indexOf(aChild);
			if (index < 0)
				return false; // not my child

			if (!canBeVisible(index))
				return false; // cannot allow this.

			setSelectedIndex(index);
			setVisible(true);
		}

		return superVisible; // This should do it.
	}

	public void setSelectedIndex(int index) {
		getTabBar().setSelectedIndex(index);
	}

	/** The index of the currently selected child, or -1 if no child is selected. */
	public int getSelectedIndex() {
		return getTabBar().getSelectedIndex();
	}

	/**
	 * Tries to select the tab with the given <code>name</code>. This method is designed to dispatch
	 * a selection coming from the client. It first checks whether the given
	 * <code>vetoListener</code> accepts the selection before actually change it.
	 * 
	 * @param name
	 *        the name of the tab to select.
	 * @param vetoListener
	 *        a listener to ask whether it is currently possible to change the selection. May be
	 *        <code>null</code>. In this case the selection change occurs unconditionally.
	 * 
	 * @throws VetoException
	 *         if the given <code>vetoListener</code> is not <code>null</code> and does not accept
	 *         the new selection.
	 */
	public void dispatchSelection(String name, TabSwitchVetoListener vetoListener) throws VetoException {
		List allCards = getCards();
		for (int index = 0, size = allCards.size(); index < size; index++) {
			String cardName = ((Card) allCards.get(index)).getName();
			if (Utils.equals(cardName, name)) {
				if (vetoListener != null) {
					vetoListener.checkVeto(getTabBar(), index);
				}
				setSelectedIndex(index);
				return;
			}
		}
		Logger.warn("Tried to select a card with name '" + name + "', but there is no card with this name.", this);

	}

	@Override
	protected void onRemove(int index, LayoutComponent removed) {
		super.onRemove(index, removed);

		TabbedLayoutComponent currentTab = getCard(index);
		assert Utils.equals(currentTab.getContent(), removed);
		getTabBar().removeSelectableCard(currentTab);
	}

	/**
	 * Return the currently selected child component.
	 * 
	 * @return The currently selected child component.
	 */
	private LayoutComponent getCurrentChild() {
		return this.getChildList().get(getTabBar().getSelectedIndex());
	}

	@Override
	public void inactiveCardChanged(TabBarModel sender, Card aCard) {
		// nothing to do here
	}

	@Override
	public void notifyCardsChanged(TabBarModel sender, List oldAllCards) {
		List<Card> allCards = getCards();
		List<LayoutComponent> newChildren = new ArrayList<>(allCards.size());
		for (int index = 0, size = allCards.size(); index < size; index++) {
			TabbedLayoutComponent child = (TabbedLayoutComponent) allCards.get(index);
			newChildren.add(child.getContent());
		}
		set(UPDATE_TAB_LIST, Boolean.FALSE);
		try {
			/* No need to update the tab list, because the tab list is newly set. */
			setChildren(newChildren);
		} finally {
			reset(UPDATE_TAB_LIST);
		}
	}

	@Override
	protected void onSet(List<LayoutComponent> oldChildren) {
		super.onSet(oldChildren);
		if (!get(UPDATE_TAB_LIST).booleanValue()) {
			return;
		}
		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		List<LayoutComponent> children = getChildList();
		List<Card> cards = new ArrayList<>(children.size());
		for (LayoutComponent child : children) {
			cards.add(createCard(context, child));
		}
		notifyErrors(log);

		if (_onChangeSelectFirstTab) {
			setTabs(cards);
			setSelectedIndex(findNextVisible(-1));
		} else {
			Object singleSelection = getTabBar().getSingleSelection();
			int formerSelectedIndex;
			if (singleSelection != null) {
				formerSelectedIndex = oldChildren.indexOf(((TabbedLayoutComponent) singleSelection).getContent());
			} else {
				formerSelectedIndex = -1;
			}
			setTabs(cards);
			if (formerSelectedIndex != -1) {
				getTabBar().setSelectedIndex(formerSelectedIndex);
			}
		}

		modelValid = false;
	}

	private TabbedLayoutComponent createCard(InstantiationContext context, LayoutComponent child) {
		return new TabbedLayoutComponent(child, createTabInfo(context, child));
	}

	private void notifyErrors(BufferingProtocol log) {
		if (log.hasErrors()) {
			InfoService.showError(ResKey.text(log.getError()));
		}
	}

	private TabInfo createTabInfo(InstantiationContext context, LayoutComponent child) {
		TabConfig tabInfo = child.getConfig().getTabInfo();
		if (tabInfo == null) {
			context.error("No tab info set in component '" + child + "' as part of TabComponent '" + this + "'.");
			return TabInfo.newTabInfo(ResKey.text(child.getName().qualifiedName()));
		} else {
			return new TabInfo(context, tabInfo);
		}
	}

	@Override
	protected void onAdd(int index, LayoutComponent newChild) {
		super.onAdd(index, newChild);

		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		Card newCard = createCard(context, newChild);
		notifyErrors(log);
		getTabBar().addSelectableCard(index, newCard);
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel aModel, Object formerlySelectedObject, Object selectedObject) {
		ComponentName theID = this.getName();
		if (theID != null) {
			if (formerlySelectedObject != null) { // is null while login
				Card formerlySelectedCard = (Card) formerlySelectedObject;
				LayoutComponent formerlySelectedChild = (LayoutComponent) formerlySelectedCard.getContent();
				formerlySelectedChild.setVisible(false);
			}
			if (selectedObject != null    // may be null if the visible Tab become not allowed
		     && isVisible())              // No need to make child visible, now
		    {
				Card selectedCard = (Card) selectedObject;
				LayoutComponent selectedChild = (LayoutComponent) selectedCard.getContent();
				
				if (ScriptingRecorder.isRecordingActive()) {
					ScriptingRecorder.recordTabSwitch(this, this.getTabBar().getSelectedIndex());
				}
				
				selectedChild.setVisible(true);
			}
		}
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean nonLocalChange = super.validateModel(context);
		if (!modelValid) {
			updateAllowness(getTabBar());
			modelValid = true;
			if (_onChangeSelectFirstTab) {
				setSelectedIndex(findNextVisible(-1));
			} else {
				int theSelectedIndex = this.getTabBar().getSelectedIndex();
				if (theSelectedIndex < 0) {
					setSelectedIndex(findNextVisible(-1));
				} else if (!this.getTabBar().isSelectable(this.getTabBar().getSingleSelection())) {
					setSelectedIndex(findNextVisible(theSelectedIndex));
				}
			}
            return true;
		}
		return nonLocalChange;
	}
	
	@Override
	public boolean isModelValid() {
		return modelValid && super.isModelValid();
	}
	
	/**
	 * This method inspects all cards and updates the inactive state due to the new
	 * {@link BoundChecker#hideReason(Object)} value
	 */
	private static void updateAllowness(DefaultTabBarModel aDefaultTabBarModel) {
		List allCards = aDefaultTabBarModel.getAllCards();
		for (int index = 0, size = allCards.size(); index < size; index++) {
			TabbedLayoutComponent tabbedLayoutComponent = (TabbedLayoutComponent) allCards.get(index);
			LayoutComponent layoutComponent = tabbedLayoutComponent.getContent();
			if (layoutComponent instanceof BoundCheckerComponent) {
				BoundCheckerComponent checker = (BoundCheckerComponent) layoutComponent;
				boolean allow = checker.allow();
                boolean      inactive = aDefaultTabBarModel.isInactive(tabbedLayoutComponent);
                if (allow && inactive) {
					aDefaultTabBarModel.removeInactiveCard(tabbedLayoutComponent);
				} else if (!allow && !inactive) {
					aDefaultTabBarModel.addInactiveCard(tabbedLayoutComponent);
				}
			}
		}
	}

	@Override
	protected void becomingVisible() {
		if (this.getTabBar().getSingleSelection() == null) { // set initial selection
			this.modelValid = false; 
		}
		super.becomingVisible();
	}

	/**
	 * @param anIndex
	 *            The index to start from. The first checked position is <code>anIndex + 1</code>.
	 * @return The index of the component to be displayed or -1, if no valid component found.
	 */
	private int findNextVisible(int anIndex) {
		int theValue;
		int theResult = -1;
		int theCount = this.getChildCount();
		int theCurrent = anIndex + 1;

		for (int thePos = 0; (theResult == -1) && (thePos < theCount); thePos++) {
			theValue = (theCurrent + thePos) % theCount;
			if (canBeVisible(theValue)) {
				theResult = theValue;
			}
		}

		return theResult;
	}

	/**
	 * This method returns <code>true</code> iff the child with the given index can be turned
	 * visible, i.e. it is allowed and not accepted by
	 * {@link #getTabVisibilityFilter() the invisible filter}.
	 * 
	 * TODO TSA/DBU: Semantic of rendered, invisible, and cnBeVisible must be clarified
	 */
	private boolean canBeVisible(int index) {
		if (index < 0 || index >= getChildCount()) {
			return false;
		}
		return canBeVisible(getChild(index)); // && !invisibleCards.accept(getCard(index));
	}

	/**
	 * We write the framese on our own, so
	 * 
	 * @return always <code>true</code>
	 */
	@Override
	public boolean isCompleteRenderer() {
		return true;
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
	 * Setter method for {@link #getTabVisibilityFilter}.
	 * 
	 * @param tabVisiblityFilter
	 *        new {@link Filter} to replace current one.
	 */
	public void setTabVisibilityFilter(Filter<? super Card> tabVisiblityFilter) {
		_tabVisiblityFilter = tabVisiblityFilter;
	}

	/**
	 * By default we do not support any kind of object.
	 * 
	 * @return always false here.
	 */
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null;
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	/**
	 * Apply the BoundSecurity by supressing disallowed children Return true when tabber with given
	 * index shoud be drawn.
	 * 
	 * @return ask the BoundChecker child for allow().
	 */
	public boolean canBeVisible(LayoutComponent aChild) {
		if (aChild instanceof BoundCheckerComponent) {
			// long start = System.currentTimeMillis();
			boolean result = ((BoundCheckerComponent) aChild).allow();
			/*
			 * long delta = System.currentTimeMillis() - start; if (delta > 1000) Logger.warn
			 * ("allow needed " + DebugHelper.getTime (delta) + " for " + aChild , this);
			 */
			return result;
		}
		// else
		return true;
	}

	public void setTabs(List<Card> tabs) {
		getTabBar().setSelectableObjects(tabs);
	}

	public TabBarRenderer getRenderer() {
		return this.renderer;
	}

	/**
	 * Invalidate myself in case a {@link DecorationValueProvider} demands a redraw.
	 */
    @Override
	protected boolean receiveAnyModelEvent(Object aModel, Object changedBy, int aType) {
        boolean theResult = super.receiveAnyModelEvent(aModel, changedBy, aType);
        // if (this.isVisible()) { TODO KHA Visible handling ?
        if (!this.isInvalid() && decorators != null && changedBy instanceof DecorationValueProvider) {
			ComponentName changersName = ((LayoutComponent) changedBy).getName();
			TabInfo theDRTI = decorators.get(changersName);
            if (theDRTI != null) {
                theResult = true;
                this.invalidate();
            }
        }
        return theResult;
    }

    /**
     * Overriden to call connectDecorations when resolving is complete.
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
        super.componentsResolved(context);
		decorators = connectDecorations(getMainLayout(), this, this.getTabBar().getAllCards());
		
		DefaultToolBar toolbar = new DefaultToolBar(this, null);
		onSetToolBar(null, toolbar);
		Menu menu = ToolbarControl.createMenu(toolbar, false);
		getTabBar().setBurgerMenu(menu);
	}

    /**
	 * Connect the {@link DecorationValueProvider}s.
	 * 
	 * @return Map of names to {@link TabInfo}s.
	 */
	public static Map<ComponentName, TabInfo> connectDecorations(MainLayout theMain, DecorationValueListener aListener,
			List someTabbers) {
		Map<ComponentName, TabInfo> result = null;
        for (Iterator theIt = someTabbers.iterator(); theIt.hasNext();) {
            Card    theCard    = (Card)    theIt.next();
            TabInfo theTabInfo = (TabInfo) theCard.getCardInfo();
			{
				TabInfo theDRTI = theTabInfo;
				ComponentName theDecoratedName = theDRTI.getDecorator();
				if (theDecoratedName != null) {
                    LayoutComponent theComponent = theMain.getComponentByName(theDecoratedName);
                    if (theComponent != null && theComponent.getName().equals(theDecoratedName) 
                            && (theComponent instanceof DecorationValueProvider)) {
                        DecorationValueProvider theDVP = (DecorationValueProvider) theComponent;
                        theDRTI.setDecorated(theDVP);
                        theDVP.registerDecorationValueListener(aListener);
                        if (result == null) {
							result = new HashMap<>(someTabbers.size());
                        }
                        result.put(theDecoratedName, theDRTI);
                    } else {
                    	if (theComponent != null) {
                    		Logger.warn("Invalid decorater '" + theComponent.getClass().getName()
								+ "' for decorated tab info '" + theDRTI.getLabelKey() + "', ignored.",
                    			TabComponent.class);
                    	} else {
                    		Logger.warn("Decorator reference not found for decorated tab info '"
                    			+ theDRTI.getId() + "', ignored.",
                    			TabComponent.class);
                    	}
                    }
                }
            }
        }
        return result;
    }

    @Override
	public void receiveDecorationValueChanged(DecorationValueProvider aProvider) {
        this.invalidate();
    }

	/**
	 * Access to the {@link TabBarModel}.
	 */
	public final TabBarModel getTabBarModel() {
		return getTabBar();
	}

	/**
	 * Access to the {@link DeckPaneModel}.
	 */
	public final DeckPaneModel getDeckPaneModel() {
		return getTabBar();
	}

	/**
	 * Access to the default {@link TabBarModel}.
	 */
	public final TabComponentTabBarModel getTabBar() {
		return _tabBar;
	}

	/**
	 * Adapts the children resp. cards of this {@link TabComponent} with the new tabs.
	 * 
	 * <p>
	 * The given sequence of tabs must not be the same as the tabs returned by
	 * {@link #getTabBarModel()}, but may contain the same contents. Components that are not longer
	 * contained in the tabs are removed, new tabs are created.
	 * </p>
	 * 
	 * @param newCards
	 *        The new list of cards.
	 */
	public void adaptTabs(List<? extends Card> newCards) throws ConfigurationException {
		ObjectFlag<LayoutComponent.Config> storedConfig = new ObjectFlag<>(null);
		ObjectFlag<String> layoutName = new ObjectFlag<>(null);
		LayoutComponent.Config tabComponentConf = copyLayoutConfig(this, storedConfig, layoutName);

		adaptTabConfig((TabComponent.Config) tabComponentConf, newCards);

		String layoutKey = layoutName.get();
		try {
			getMainLayout().replaceComponent(layoutKey,
				LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(), TLContext.getContext().getPerson(),
					layoutKey));
		} catch (ConfigurationException exception) {
			throw new ConfigurationException(I18NConstants.CAN_NOT_REPLACE_TABS__LAYOUT_NAME.fill(layoutKey),
				exception.getPropertyName(), exception.getPropertyValue(), exception);
		}
	}

	private void adaptTabConfig(TabComponent.Config tabConfig, List<? extends Card> newCards) {
		List<LayoutComponent.Config> childConfigs = tabConfig.getComponents();
		Map<LayoutComponent, LayoutComponent.Config> configByComponent = new HashMap<>();
		for (int index = 0; index < getChildCount(); index++) {
			configByComponent.put(getChild(index), childConfigs.get(index));
		}
		// Update child configurations due to new card order.
		childConfigs.clear();
		newCards.forEach(card -> {
			LayoutComponent.Config config;
			if (card instanceof TabbedLayoutComponent) {
				// reordered
				config = configByComponent.get(card.getContent());
			} else {
				// new tab
				config = (LayoutComponent.Config) card.getContent();
			}
			childConfigs.add(config);
		});
	}

	/**
	 * Copies the configuration of the next ancestor of the given component that has an layoutName
	 * in the {@link MainLayout}.
	 * 
	 * @param component
	 *        The component to copy.
	 * @param storedConfig
	 *        Sink that is filled with the configuration copy the is stored in the
	 *        {@link MainLayout}.
	 * @param layoutName
	 *        Sink that is filled with the name under which the configuration is stored.
	 * @return A copy of the configuration of the given component.
	 */
	private LayoutComponent.Config copyLayoutConfig(LayoutComponent component,
			ObjectFlag<LayoutComponent.Config> storedConfig,
			ObjectFlag<String> layoutName) {
		String layoutKey = LayoutTemplateUtils.getDirectNameScope(component);
		if (layoutKey != null) {
			LayoutComponent.Config copy = TypedConfiguration.copy(component.getConfig());
			storedConfig.set(copy);
			layoutName.set(layoutKey);
			return copy;
		} else {
			LayoutComponent parent = component.getParent();
			LayoutComponent.Config parentCopy = copyLayoutConfig(parent, storedConfig, layoutName);
			if (component.getDialogTopLayout() == component) {
				int indexOfDialog = parent.getDialogs().indexOf(component);
				return parentCopy.getDialogs().get(indexOfDialog);
			}
			if (!(parent instanceof LayoutList)) {
				throw new TopLogicException(
					I18NConstants.CAN_NOT_REORGANIZE_TABS__TAB_NAME.fill(this.getName()));
			}
			int childIndex = ((LayoutList) parent).getIndexOfChild(component);
			return ((SubComponentConfig) parentCopy).getComponents().get(childIndex);
		}

	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

	/**
	 * Default {@link TabBarModel} for the {@link TabComponent}.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class TabComponentTabBarModel extends DefaultTabBarModel {

		/**
		 * Creates a {@link TabComponentTabBarModel} for the given cards.
		 */
		public TabComponentTabBarModel(List<? extends TabbedLayoutComponent> someCards,
				Set<? extends TabbedLayoutComponent> someInactiveCards) {
			super(someCards, someInactiveCards);
		}

		@Override
		public boolean isInactive(Card card) {
			if (getListeners().get(TabBarListener.class).isEmpty()) {
				if (!(card instanceof TabbedLayoutComponent)) {
					return true;
				}
				return !canBeVisible(((TabbedLayoutComponent) card).getContent());
			}
			return super.isInactive(card);
		}
		
		@Override
		public ModelName getModelName() {
			return ModelResolver.buildModelName(this);
		}

		/**
		 * Returns the {@link TabComponent} corresponding to this {@link TabComponentTabBarModel}.
		 */
		public TabComponent getComponent() {
			return TabComponent.this;
		}
	}

	/**
	 * The class {@link TabComponent.TabbedLayoutComponent} is the {@link Card} implementation which
	 * is used for the {@link TabBarModel} of this {@link TabComponent}. Each
	 * {@link TabComponent.TabbedLayoutComponent} is immutable. The content of the card is a child
	 * of this {@link TabComponent} and the info is the corresponding {@link TabInfo}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class TabbedLayoutComponent implements Card {

		private LayoutComponent businessComponent;
		private TabInfo tabInfo;

		public TabbedLayoutComponent(LayoutComponent businessComponent, TabInfo tabInfo) {
			if (tabInfo == null) {
				throw new IllegalArgumentException("'tabInfo' must not be 'null'.");
			}
			if (businessComponent == null) {
				throw new IllegalArgumentException("'businessComponent' must not be 'null'.");
			}
			this.businessComponent = businessComponent;
			this.tabInfo = tabInfo;
		}

		@Override
		public LayoutComponent getContent() {
			return businessComponent;
		}

		@Override
		public TabInfo getCardInfo() {
			return tabInfo;
		}

		@Override
		public void writeCardInfo(DisplayContext context, Appendable out) throws IOException {
			out.append(tabInfo.getLabel());
		}

		@Override
		public String getName() {
			return tabInfo.getId();
		}

		@Override
		public String toString() {
			return "[componentName: " + businessComponent.getName() + "], [TabName: " + tabInfo.getLabel() + "]";
		}

	}
}
