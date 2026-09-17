/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.recorder.RecorderAccess;

/**
 * Turns the state entries selected in the UI inspector into an assertion step of the running script
 * recording.
 *
 * <p>
 * The action's input is the selection of the state table - the paths the user ticked - and the
 * configured channel holds the inspected element they were taken from. What is recorded is the part
 * of that element's state the paths address, so the assertion checks the observations the user chose
 * and stays indifferent to everything else the element displays. The step goes to the recorder of
 * the inspected window, the same recorder the user's own interactions are captured in, so the check
 * lands between the steps it belongs between. Without a running recording nothing is recorded, since
 * a script the assertion could be part of is what makes it a step.
 * </p>
 *
 * <p>
 * App-specific action, referenced by {@code class=} in the inspector view rather than claiming a
 * global {@code @TagName}.
 * </p>
 */
public class RecordAssertionAction implements ViewAction {

	/**
	 * Configuration for {@link RecordAssertionAction}.
	 */
	public interface Config extends PolymorphicConfiguration<RecordAssertionAction> {

		/** Configuration name for {@link #getNode()}. */
		String NODE = "node";

		@Override
		@ClassDefault(RecordAssertionAction.class)
		Class<? extends RecordAssertionAction> getImplementationClass();

		/**
		 * The channel holding the {@link InspectedNode} the assertion is recorded for.
		 */
		@Name(NODE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getNode();
	}

	private final ChannelRef _nodeRef;

	/**
	 * Creates a new {@link RecordAssertionAction} from configuration.
	 */
	@CalledByReflection
	public RecordAssertionAction(InstantiationContext context, Config config) {
		_nodeRef = config.getNode();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return input;
		}
		ViewChannel nodeChannel = viewContext.resolveChannel(_nodeRef);
		if (!(nodeChannel.get() instanceof InspectedNode node)) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_NODE);
			return input;
		}

		Collection<String> paths = selectedPaths(input);
		if (paths.isEmpty()) {
			ViewMessages.info(context, I18NConstants.ERROR_NO_STATE_SELECTED);
			return input;
		}

		ScriptRecorder recorder = RecorderAccess.openerRecorder(context);
		if (recorder == null || !recorder.isRecording()) {
			ViewMessages.info(context, I18NConstants.ERROR_NOT_RECORDING);
			return input;
		}

		Map<String, Object> expectedState = node.stateSubset(paths);
		if (expectedState.isEmpty()) {
			ViewMessages.info(context, I18NConstants.ERROR_STATE_GONE);
			return input;
		}

		recorder.recordAssertion(node.address(), expectedState);
		int entries = InspectedNode.stateEntries(expectedState).size();
		ViewMessages.info(context,
			I18NConstants.ASSERTION_RECORDED__ADDRESS_ENTRIES.fill(node.address(), Integer.valueOf(entries)));
		return input;
	}

	/**
	 * The state paths the selection input names: one selected row as that path, several as the set
	 * of them, nothing selected as no path at all.
	 */
	private static Collection<String> selectedPaths(Object input) {
		if (input == null) {
			return List.of();
		}
		if (input instanceof Collection<?> selection) {
			return selection.stream().filter(path -> path != null).map(Object::toString).toList();
		}
		return List.of(input.toString());
	}

}
