/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.dnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.layout.Drag;
import com.top_logic.layout.Drop;
import com.top_logic.layout.Drop.DropException;
import com.top_logic.layout.tree.TreeControl;

/**
 * {@link Drag} of trees in popup select dialogs.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DragSelectDialog implements LegacyTreeDrag {

	private Collection<Drop<? super List<?>>> _dndTargets = new ArrayList<>();

	private TreeControl _treeControl;

	/**
	 * Create a new {@link DragSelectDialog}.
	 */
	public DragSelectDialog(TreeControl treeControl) {
		_treeControl = treeControl;
	}

	@Override
	public void notifyDrag(String dropId, Object dragInfo, Object dropInfo) {
		for (Drop<? super List<?>> drop : _dndTargets) {
			if (dropId.equals(drop.getID())) {
				@SuppressWarnings("unchecked")
				List<String> elementIds = (List<String>) dragInfo;

				List<Object> elementsToMove = getElements(elementIds);
				try {
					drop.notifyDrop(elementsToMove, dropInfo);
					_treeControl.getSelectionModel().clear();
				} catch (DropException ex) {
					// Do nothing
				}
				break;
			}
		}
	}

	private List<Object> getElements(List<String> clientSideIDs) {
		ArrayList<Object> elements = new ArrayList<>();
		for (String clientSideID : clientSideIDs) {
			Object node = _treeControl.getNodeById(clientSideID);
			elements.add(node);
		}
		return elements;
	}

	@Override
	public void addDropTarget(Drop<? super List<?>> target) {
		if (target == null) {
			throw new NullPointerException("target must not be null");
		}
		_dndTargets.add(target);
	}

	@Override
	public void removeDropTarget(Drop<? super List<?>> target) {
		_dndTargets.remove(target);
	}

	@Override
	public Collection<Drop<? super List<?>>> getDropTargets() {
		return _dndTargets;
	}

}
