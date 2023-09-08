/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CompoundSecurityLayoutConfigDescender} collection all
 * {@link LayoutConfigTreeNode#getCommandGroups()} in the visited layout tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandGroupCollector extends CompoundSecurityLayoutConfigDescender {

	private final Collection<? super BoundCommandGroup> _out;

	/**
	 * Creates a new {@link CommandGroupCollector}.
	 * 
	 * @param out
	 *        Where to add {@link BoundCommandGroup}s to.
	 */
	public CommandGroupCollector(Collection<? super BoundCommandGroup> out) {
		_out = out;
	}

	@Override
	protected void visit(LayoutConfigTreeNode configNode) {
		_out.addAll(configNode.getCommandGroups());
	}

	/**
	 * Collects all {@link BoundCommandGroup} except {@link SimpleBoundCommandGroup#SYSTEM}.
	 */
	public static List<BoundCommandGroup> collectNonSystemGroups(LayoutConfigTreeNode node) {
		Set<BoundCommandGroup> commandGroups = new HashSet<>();
		CommandGroupCollector cgCollector = new CommandGroupCollector(commandGroups);
		cgCollector.descend(node);
		commandGroups.remove(SimpleBoundCommandGroup.SYSTEM);
		int numberCommandGroups = commandGroups.size();
		switch (numberCommandGroups) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList(commandGroups.iterator().next());
			default:
				return Arrays.asList(commandGroups.toArray(new BoundCommandGroup[numberCommandGroups]));
		}
	}

}

