/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.tools;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatViewBinding;
import com.top_logic.ai.chat.view.ChatWindowService;
import com.top_logic.ai.chat.view.PageCommand;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.scripting.WindowAccess;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * The page command tools of a chat page: list the commands the page lets its agent run, and run
 * one of them in the browser window of the run.
 *
 * <p>
 * A command is an ordinary command of the page - its action chain opens a dialog, shows a view,
 * writes a channel - that a {@code <chat-conversation>} lists among its
 * {@link ChatConversationElement.Config#getAgentCommands() agent commands}. The agent names it, and
 * it runs as a click on its button would.
 * </p>
 *
 * <h2>What bounds it</h2>
 *
 * <ul>
 * <li><b>The allow list.</b> Deny by default: only a command a displayed {@code <chat-conversation>}
 * of this window lists can be run, and only the chats of the run's own account count. A name the
 * page does not list is refused with the names it does. See
 * {@link ChatViewBinding#command(com.top_logic.basic.col.TypedAnnotatable, String, Person)}.</li>
 * <li><b>The command's own checks.</b> The command's executability rules and access checks decide
 * over its inputs as they are when it is run; a command they reject is refused with their reason,
 * and its chain does not run.</li>
 * <li><b>The window.</b> The command runs in the sub-session of the browser window the run belongs
 * to and within that window's interaction, through
 * {@link ChatWindowService#inWindow(TLSubSessionContext, Runnable)}. A run without a window has no
 * page, says so and runs nothing.</li>
 * </ul>
 *
 * <p>
 * A run that has no command to run is not offered the tools ({@link #noCommands(ToolExecutionContext)}),
 * and a call that arrives anyway is answered with the same sentence.
 * </p>
 *
 * <p>
 * Both tools are {@link ToolMode#READ}: the agent changes no data through them, the page decided
 * which of its commands an agent may trigger, and an approval card would sit in the very page the
 * command acts on.
 * </p>
 */
public class ChatCommandTool {

	/** The name of the tool listing the commands of the page, {@link #pageCommands(ToolExecutionContext)}. */
	public static final String LIST_TOOL = "pageCommands";

	/** The name of the tool running a command of the page, {@link #runPageCommand(String, ToolExecutionContext)}. */
	public static final String RUN_TOOL = "runPageCommand";

	/**
	 * The argument of {@link #RUN_TOOL} naming the command: the name of the parameter of
	 * {@link #runPageCommand(String, ToolExecutionContext)}, which is what the model sends it as.
	 */
	public static final String ARG_COMMAND = "command";

	/**
	 * The answer for a run without a browser window: a fact about the run, and deliberately not the
	 * sentence a page that allows nothing is answered with.
	 */
	static final String NO_PAGE = "No page is attached to this run, so there is no command to run. "
		+ "Tell the user in text what to do instead.";

	/** The answer for a run whose page displays no element listing a command. */
	static final String NO_COMMANDS =
		"This page offers no command to run. Tell the user in text what to do instead.";

	/**
	 * Creates a {@link ChatCommandTool}.
	 */
	public ChatCommandTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Lists the commands the page beside this conversation lets the agent run.
	 *
	 * @param context
	 *        The run.
	 * @return The names and descriptions, one command per line, or why there are none.
	 */
	@ToolMethod(
		name = LIST_TOOL,
		description = "List the commands the page beside this conversation lets you run for the user, "
			+ "with what each one does - e.g. opening a form in which the user enters what must not be "
			+ "typed into the chat, such as a password or an access token. Run one with " + RUN_TOOL
			+ ". This changes nothing.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION,
		marking = AnswerMarking.NONE, unavailable = "noCommands")
	public String pageCommands(ToolExecutionContext context) {
		TLSubSessionContext window = TLContextManager.getSubSession();
		Person account = ChatPresentTool.runAccount(context);
		String unavailable = noCommands(window, account);
		if (unavailable != null) {
			return unavailable;
		}
		List<PageCommand> commands = ChatViewBinding.commands(window, account);
		return "This page offers these commands; run one with " + RUN_TOOL + ":\n"
			+ commands.stream().map(c -> "- " + c.name() + ": " + c.description()).collect(Collectors.joining("\n"));
	}

	/**
	 * Runs a command the page beside this conversation lets the agent run.
	 *
	 * @param command
	 *        The name of the command, as {@link #LIST_TOOL} lists it; sent as {@link #ARG_COMMAND}.
	 * @param context
	 *        The run.
	 * @return What became of it, phrased for the model.
	 */
	@ToolMethod(
		name = RUN_TOOL,
		description = "Run one of the commands the page beside this conversation lets you run for the "
			+ "user, by its name as " + LIST_TOOL + " lists it. The command acts in the user's page as a "
			+ "click on its button would - it opens a form, shows a view - and the user carries on there. "
			+ "Use it instead of asking the user for a password, an access token or other secret in the "
			+ "chat, and instead of describing where to click. After it, tell the user in one or two "
			+ "sentences what they now see and what to do there.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION,
		marking = AnswerMarking.NONE, unavailable = "noCommands", concurrencySafe = false)
	public String runPageCommand(
			@P("The name of the command to run, as " + LIST_TOOL + " lists it.") String command,
			ToolExecutionContext context) {
		TLSubSessionContext window = TLContextManager.getSubSession();
		Person account = ChatPresentTool.runAccount(context);
		// Asked again rather than taken from the offer: the elements of a page come and go while the
		// run works.
		String unavailable = noCommands(window, account);
		if (unavailable != null) {
			return unavailable;
		}
		String name = command == null ? "" : command.trim();
		PageCommand target = ChatViewBinding.command(window, name, account);
		if (target == null) {
			return "Nothing was run: this page offers no command '" + name + "'. It offers: "
				+ ChatViewBinding.commands(window, account).stream().map(PageCommand::name)
					.collect(Collectors.joining(", "))
				+ ".";
		}

		TopLogicException[] failure = new TopLogicException[1];
		String[] veto = new String[1];
		WindowAccess access = ChatWindowService.inWindow(window, () -> {
			try {
				target.run();
			} catch (TopLogicException ex) {
				failure[0] = ex;
			} catch (ChannelVetoException ex) {
				veto[0] = "Nothing was run: the page holds unsaved changes. Ask the user to save or "
					+ "discard them first.";
			}
		});
		switch (access) {
			case DONE:
				break;
			case BUSY:
				return "The page was busy and the command was not run. Try once more.";
			default:
				return NO_PAGE;
		}
		if (veto[0] != null) {
			return veto[0];
		}
		if (failure[0] != null) {
			return "The command '" + name + "' cannot be run now: " + reason(failure[0]);
		}
		Logger.debug("Ran page command '" + name + "' for an agent run.", ChatCommandTool.class);
		return "The command '" + name + "' ran in the user's page: " + target.description();
	}

	/**
	 * Why the given run has no command to run: no window, or a window none of whose displayed chats
	 * lists one.
	 */
	public String noCommands(ToolExecutionContext context) {
		return noCommands(TLContextManager.getSubSession(), ChatPresentTool.runAccount(context));
	}

	private static String noCommands(TLSubSessionContext window, Person account) {
		if (!ChatWindowService.hasWindow(window)) {
			return NO_PAGE;
		}
		if (ChatViewBinding.commands(window, account).isEmpty()) {
			return NO_COMMANDS;
		}
		return null;
	}

	/**
	 * The reason a refused command gives, in English like every other text a tool puts in front of
	 * a model: the details of the refusal, its message if it has none.
	 */
	private static String reason(TopLogicException failure) {
		Resources resources = Resources.getInstance(Locale.ENGLISH);
		String details = resources.getString(failure.getDetails(), null);
		if (details != null && !details.isEmpty()) {
			return details;
		}
		String message = resources.getString(failure.getErrorKey(), null);
		if (message != null && !message.isEmpty()) {
			return message;
		}
		return "it is not executable.";
	}
}
