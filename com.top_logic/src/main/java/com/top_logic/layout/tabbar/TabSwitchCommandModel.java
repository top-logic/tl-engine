/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * The class {@link TabSwitchCommandModel} is a {@link CommandModel} which switches the
 * {@link TabComponent} to a tab identified by a card name. The model is visible if the desired card
 * exists and the card is currently not inactive. The model is executable if the desired card is
 * currently not selected.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TabSwitchCommandModel extends ComponentCommandModel {

	/** The {@link TabBarModel} whose {@link Card}s will be switched by this {@link CommandModel} */
	private TabBarModel model;

	/** The name of the card to switch to */
	private String tabName;

	/**
	 * The {@link Card} of the {@link TabBarModel} to which this {@link CommandModel} switch to.
	 * This {@link Card} is <code>null</code> if there is no {@link Card} to switch to. Must be in
	 * sync with the {@link TabBarModel}, i.e. if the {@link Card}s of the model change it is
	 * necessary to verify that this {@link Card} is still the card in the deck with the name
	 * {@link #tabName}.
	 */
	private Card cardToSelect;

	/**
	 * The {@link com.top_logic.layout.tabbar.TabBarModel.TabBarListener} which reacts on
	 * changes of the {@link TabBarModel} and updates this {@link CommandModel}. This listener will
	 * only be added to the {@link TabBarModel} if this {@link CommandModel} has
	 * {@link GenericPropertyListener} which observe it.
	 */
	private final TabBarListener tabBarListener = new TabBarListener() {
		@Override
		public void inactiveCardChanged(TabBarModel sender, Card aCard) {
			update();
		}

		@Override
		public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
			update();

		}

		@Override
		public void notifySelectionChanged(SingleSelectionModel aModel, Object formerlySelectedObject, Object selectedObject) {
			update();
		}
	};

	private TabSwitchCommandModel(CommandHandler handler, TabComponent component, String tabName, ResKey label) {
		super(handler, component, args(tabName), label);
		this.model = component.getTabBarModel();
		this.tabName = tabName;
	}

	private static Map<String, Object> args(String tabName) {
		return Collections.<String, Object> singletonMap(AbstractTabSwitchVetoCommand.TAB_NAME, tabName);
	}

	/** 
	 * creates the command to execute with a {@link DefaultTabSwitchVetoListener} as veto listener.
	 */
	private static CommandHandler createCommand(LayoutComponent component) {
		CommandHandler command = DefaultTabSwitchVetoCommand.INSTANCE;
		if (component.getCommandById(command.getID()) == null) {
			component.registerCommand(command);
		}
		return command;
	}

	/**
	 * Creates the command to execute with the given {@link TabSwitchVetoListener}.
	 */
	private static CommandHandler createCommand(LayoutComponent component, TabSwitchVetoListener vetoListener) {
		CommandHandler command = ConstantTabSwitchVetoCommand.newInstance(vetoListener);
		if (component.getCommandById(command.getID()) == null) {
			component.registerCommand(command);
		}
		return command;
	}

	/**
	 * Creates a {@link ConstantTabSwitchVetoCommand} which has a
	 * {@link DefaultTabSwitchVetoListener} as veto listener.
	 * 
	 * @see #newInstance(TabComponent, String, TabSwitchVetoListener)
	 */
	public static TabSwitchCommandModel newInstance(TabComponent component, String tabName) {
		CommandHandler handler = createCommand(component);
		return newInstance(component, tabName, handler);
	}

	/**
	 * Creates a {@link TabSwitchCommandModel}. Execution of the command
	 * switches the {@link TabComponent} to the tab whose name is the given name.
	 * 
	 * @param component
	 *        The {@link TabComponent} whose tabs this {@link CommandModel} shall be switch.
	 * @param tabName
	 *        the name of the tab to switch to.
	 * @param vetoListener
	 *        a listener which is asked for a veto before switching the tab. May be
	 *        <code>null</code> if no veto handling is needed.
	 */
	public static TabSwitchCommandModel newInstance(TabComponent component, String tabName,
			TabSwitchVetoListener vetoListener) {
		CommandHandler handler = createCommand(component, vetoListener);
		return newInstance(component, tabName, handler);
	}

	private static TabSwitchCommandModel newInstance(TabComponent component, String tabName, CommandHandler handler) {
		ResKey label = handler.getResourceKey(component);
		return new TabSwitchCommandModel(handler, component, tabName, label);
	}

	private void update() {
		updateCardToSelect();
		updateVisibility();
		updateExecutability();
	}

	/**
	 * This method sets this {@link CommandModel} executable iff there is a {@link Card} to select
	 * and the card to select is currently not selected.
	 */
	private void updateExecutability() {
		if (cardToSelect == null) {
			setNotExecutable(I18NConstants.TAB_NOT_FOUND);
		} else if (model.getSelectedIndex() == -1) {
			setExecutable();
		} else {
			boolean cardSelected = model.getAllCards().get(model.getSelectedIndex()).equals(cardToSelect);
			if (cardSelected) {
				setNotExecutable(I18NConstants.NOT_EXECUTABLE_TAB_SELECTED);
			} else {
				setExecutable();
			}
		}
	}

	@Override
	public boolean isLinkActive() {
		return I18NConstants.NOT_EXECUTABLE_TAB_SELECTED.equals(getNotExecutableReasonKey());
	}

	/**
	 * This method sets this model visible if there is a card to select and the card to select is
	 * not inactive.
	 */
	private void updateVisibility() {
		if (cardToSelect == null) {
			setVisible(false);
		} else {
			setVisible(!model.isInactive(cardToSelect));
		}
	}

	/**
	 * This method select the {@link Card} with name {@link #tabName} in the {@link TabBarModel} and
	 * updated the {@link Card} {@link #cardToSelect}.
	 */
	private void updateCardToSelect() {
		List<Card> allCards = model.getAllCards();
		this.cardToSelect = null;
		for (int index = 0, size = allCards.size(); index < size; index++) {
			Card currentCard = allCards.get(index);
			if (Utils.equals(currentCard.getName(), tabName)) {
				this.cardToSelect = currentCard;
				return;
			}
		}
		Logger.warn("There is no card with name '" + tabName + "' in the model '" + model + "'.", this);
	}

	/**
	 * If this model has some {@link GenericPropertyListener} after the call of this method, it starts to
	 * observe its {@link TabBarModel}.
	 */
	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		boolean firstListener = !hasListeners();
		boolean result = super.addListener(type, listener);
		if (firstListener) {
			boolean succeded = model.addTabBarListener(tabBarListener);
			if (succeded) {
				update();
			}
		}
		return result;
	}

	/**
	 * If the last {@link GenericPropertyListener} is removed, this {@link CommandModel} stops observing
	 * its {@link TabBarModel}
	 */
	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		boolean result = super.removeListener(type, listener);
		if (!hasListeners()) {
			model.removeTabBarListener(tabBarListener);
		}
		return result;
	}

	/**
	 * The class {@link ConstantTabSwitchVetoCommand} always uses a given
	 * {@link TabSwitchVetoListener}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class ConstantTabSwitchVetoCommand extends AbstractTabSwitchVetoCommand {

		/**
		 * Name of the command.
		 */
		public static final String COMMAND = "tabSwitch";
		
		private TabSwitchVetoListener vetoListener;

		/**
		 * Creates a {@link ConstantTabSwitchVetoCommand}.
		 */
		public ConstantTabSwitchVetoCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		private void setVetoListener(TabSwitchVetoListener vetoListener) {
			this.vetoListener = vetoListener;
		}

		/**
		 * Creates the command to execute with the given {@link TabSwitchVetoListener}.
		 */
		public static CommandHandler newInstance(TabSwitchVetoListener vetoListener) {
			ConstantTabSwitchVetoCommand result = newInstance(ConstantTabSwitchVetoCommand.class, COMMAND);
			result.setVetoListener(vetoListener);
			return result;
		}

		@Override
		protected TabSwitchVetoListener getVetoListener(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return vetoListener;
		}

	}

	/**
	 * The class {@link DefaultTabSwitchVetoCommand} is an
	 * {@link AbstractTabSwitchVetoCommand} whose {@link TabSwitchVetoListener}
	 * is a {@link DefaultTabSwitchVetoListener} which opens the dialog in the
	 * window in which the command is executed.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class DefaultTabSwitchVetoCommand extends AbstractTabSwitchVetoCommand {
		
		/**
		 * Name of the command.
		 */
		public static final String COMMAND = "defaultTabSwitch";
		
		/**
		 * {@link DefaultTabSwitchVetoCommand} instance.
		 */
		public static DefaultTabSwitchVetoCommand INSTANCE = newInstance(DefaultTabSwitchVetoCommand.class, COMMAND);
		
		/**
		 * Creates a {@link DefaultTabSwitchVetoCommand}.
		 */
		public DefaultTabSwitchVetoCommand(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		protected TabSwitchVetoListener getVetoListener(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return DefaultTabSwitchVetoListener.INSTANCE;
		}
	}
	
	/**
	 * The class {@link AbstractTabSwitchVetoCommand} is a command which switches a
	 * {@link TabComponent} to a card whose name is stored in the argument map under the key
	 * {@link #TAB_NAME}. Before switching the tab, a {@link TabSwitchVetoListener} is consulted.
	 * 
	 * @see #getVetoListener(DisplayContext, LayoutComponent, Map)
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	protected static abstract class AbstractTabSwitchVetoCommand extends AbstractCommandHandler {

		/**
		 * Key for the argument map.
		 */
		public static final String TAB_NAME = "tabName";

		/**
		 * Creates a {@link AbstractTabSwitchVetoCommand}.
		 */
		public AbstractTabSwitchVetoCommand(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public final HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
				Object model, Map<String, Object> someArguments) {
			TabSwitchVetoListener vetoListener = getVetoListener(context, component, someArguments);
			String tabName = (String) someArguments.get(TAB_NAME);

			switchTab(context, component, vetoListener, tabName);
			return HandlerResult.DEFAULT_RESULT;
		}

		void switchTab(DisplayContext context, final LayoutComponent component,
				final TabSwitchVetoListener vetoListener, final String tabName) {
			try {
				((TabComponent) component).dispatchSelection(tabName, vetoListener);
			}  catch(VetoException ex) {
				ex.setContinuationCommand(new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext callbackContext) {
						switchTab(callbackContext, component, vetoListener, tabName);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(context.getWindowScope());
			}
		}

		@Override
		protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments) {
			return true;
		}


		/**
		 * Returns the {@link TabSwitchVetoListener} which is consulted before
		 * the actual tab switch is performed.
		 * 
		 * @param context
		 *        the context in which the command is executed.
		 * @param component
		 *        the component on which the command is executed.
		 * @param someArguments
		 *        the arguments for the command.
		 * 
		 * @return the {@link TabSwitchVetoListener} which is consulted before
		 *         the actual tab switch is performed.
		 */
		protected abstract TabSwitchVetoListener getVetoListener(DisplayContext context, LayoutComponent component,
				Map<String, Object> someArguments);

	}

}
