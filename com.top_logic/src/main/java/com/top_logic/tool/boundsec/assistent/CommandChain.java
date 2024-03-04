/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandScriptWriter;
import com.top_logic.tool.boundsec.CommandSequence;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * To perform more than one command during a step switch in the
 * assistentComponent. e.g. the step forward and a addional command.
 * 
 * @see CommandSequence Building command sequences that are executed on the same component.
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class CommandChain extends AbstractCommandHandler {

	private static String		ID			= "commandChain";

	private List<CommandHolder> commandList = new ArrayList<>(2);
	
	/**
	 * Chains sometimes target other components that the original one.
	 * 
	 * @see AssistentComponent#reinitButton(CommandHolder,CommandHandler)
	 */
	protected LayoutComponent	target;

	public CommandChain(InstantiationContext context, Config config) {
		super(context, config);
	}

	public static CommandChain newInstance(String aCommandID, BoundCommandGroup aGroup, List someCommandHolders,
			LayoutComponent aTarget) {
		return newInstance(aCommandID, aGroup, someCommandHolders, aTarget, null, null);
	}

	public static CommandChain newInstance(String aCommandID, BoundCommandGroup aGroup,
			List<CommandHolder> someCommandHolders,
			LayoutComponent aTarget, ExecutabilityRule rule, ResKey i18nKey) {
		CommandChain result = newInstance(
			updateGroup(
				AbstractCommandHandler.<Config> createConfig(CommandChain.class, aCommandID),
				aGroup));
		result.commandList = someCommandHolders;
		result.target = aTarget;
		result.setRule(getCustomExecutabilityRule(rule, someCommandHolders));
		ResKey customI18NKey = getCustomI18NKey(i18nKey, someCommandHolders);
		if (customI18NKey != null) {
			result.setResourceKey(customI18NKey);
		}
		return result;
	}

	/**
	 * This method generates an ID using the {@link CommandHolder#getHandler() handler} of the
	 * {@link CommandHolder command holder} in the given list, can be used as id for
	 * {@link CommandChain}s.
	 * 
	 * @param someCommandHolders
	 *            a list of not <code>null</code> {@link CommandHolder}s
	 * @return an ID
	 * @deprecated please provide a not generic command ID
	 */
	@Deprecated
	public static String generateID(List someCommandHolders) {
		StringBuffer theID = new StringBuffer(ID);
		for (int i = 0; i < someCommandHolders.size(); i++) {
			theID.append('_');
			theID.append(((CommandHolder) someCommandHolders.get(i)).getHandler().getID());
		}
		return theID.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.top_logic.tool.boundsec.CommandHandler#handleCommand(jakarta.servlet.ServletContext,
	 *      jakarta.servlet.http.HttpServletRequest,
	 *      jakarta.servlet.http.HttpServletResponse,
	 *      com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		HandlerResult theResult = null;
		Iterator<CommandHolder> iter = this.commandList.iterator();
		boolean closeDialog = false;
		while (iter.hasNext() && (theResult == null || theResult.isSuccess())) {
			CommandHolder theHolder = iter.next();
			CommandHandler theHandler = theHolder.getHandler();
			LayoutComponent theTarget = theHolder.getTargetComponent();

			try {
				/*
				 * Intuitively the handlers must be executed using CommandDispatcher, but this can
				 * produce inconsistent security checking: It is possible that this CommandChain has
				 * a BoundCommandGroup which the current user is allowed to execute, but some inner
				 * handler has an BoundCommandGroup which the current user is not allowed to
				 * execute. This is the was to mark a chain as executable, whereas the single
				 * commands are not executable.
				 */
				theResult = theHandler.handleCommand(aContext, theTarget, model, someArguments);
				if (!theResult.isSuccess()) {
					return theResult;
				}
			} catch (Exception ex) {
				if (theResult == null) {
					theResult = new HandlerResult(/* closeDialog */false);
				}
				if (!(ex instanceof TopLogicException)) {
					ex = new TopLogicException(CommandChain.class, "error.base.attributes", ex);
				}
				if (theResult instanceof DefaultHandlerResult) {
					theResult = new HandlerResult();
				}
				theResult.setException((TopLogicException) ex);
			}
			/**
			 * TODO SKR/KHA what about this if (theResult.stopProcessing()) {
			 * break; }
			 */

			/*
			 * TODO SKR/KHA/TSA this causes problems when using the
			 * FileUploadHandler, -Component. It returns closeDialog == true,
			 * but the assistent may want to continue. The correct handling must
			 * be specified!
			 * 
			 * TSA -> KHA: i commentet this out so the demo example works again
			 */
			// if (theResult.shallCloseDialog()) {
			// closeDialog = true;
			// }
		}
		if (closeDialog && !theResult.shallCloseDialog())
			theResult.setCloseDialog(closeDialog);

		return theResult;
	}

	/**
	 * Return the (optional) Target for the chained command(s).
	 */
	public LayoutComponent getTarget() {
		return (target);
	}

	/**
	 * Return the last CommandHandler of this chain.
	 * 
	 * @return null in the (unrealistic) case of an empty {@link #commandList}.
	 */
	public CommandHandler getLastCommand() {
		return getLastCommand(commandList);
	}

	private static CommandHandler getLastCommand(List<CommandHolder> commandList) {
		CommandHolder lastHolder = getLastHolder(commandList);
		if (lastHolder == null) {
			return null;
		}
		return lastHolder.getHandler();
	}

	private static CommandHolder getLastHolder(List<CommandHolder> commandList) {
		int pos = commandList.size() - 1;
		if (pos > 0) {
			CommandHolder theHolder = commandList.get(pos);
			return theHolder;
		}
		return null;
	}

	@Override
	public CommandScriptWriter getCommandScriptWriter(LayoutComponent component) {
		return commandList.get(0).getHandler().getCommandScriptWriter(component);
	}

	/**
	 * I18N key for this chain.
	 */
	public static ResKey getCustomI18NKey(ResKey i18nKey, List<CommandHolder> commandList) {
		if (i18nKey != null) {
			return i18nKey;
		}
		CommandHolder last = getLastHolder(commandList);
		if (last != null) {
			return last.getHandler().getResourceKey(last.getTargetComponent());
		}
		return null;
	}
	
	private static ExecutabilityRule getCustomExecutabilityRule(ExecutabilityRule rule, List commandList) {
		if (rule != null) {
			return rule;
		}
		return new MergedExecutabilityRule(commandList);
	}
	
	@Override
	public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments) {
		ResKey result = super.getConfirmKey(component, arguments);
		if (result == null) {
			for (CommandHolder cmd : commandList) {
				ResKey subResult = cmd.getHandler().getConfirmKey(component, arguments);
				if (subResult != null) {
					return subResult;
				}
			}
		}
		return result;
	}

	/**
	 * The MergedExecutabilityRule expects a list of {@link CommandHolder}s.
	 * The executability of this rule depends on the executability of the
	 * handler of the holder in the list.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class MergedExecutabilityRule implements ExecutabilityRule {

		List	listOfCommandHolders;

		public MergedExecutabilityRule(List listOfCommandHolders) {
			this.listOfCommandHolders = listOfCommandHolders;
		}

		/**
		 * This method returns the "largest" {@link ExecutableState} in the
		 * list.
		 * 
		 * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
		 */
		@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
            ExecutableState state = ExecutableState.EXECUTABLE;
			if (listOfCommandHolders != null) {
				for (int index = 0, cnt = listOfCommandHolders.size(); index < cnt; index++) {
					CommandHandler theHandler = ((CommandHolder) listOfCommandHolders.get(index))
                            .getHandler();
					ExecutableState stateToCompare = theHandler.isExecutable(component, model, someValues);
    				if (state.compareTo(stateToCompare) > 0)
    					state = stateToCompare;
    			}
			}
			return state;
		}
	}
}
