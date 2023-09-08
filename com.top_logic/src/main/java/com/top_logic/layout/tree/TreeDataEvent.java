/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * Event sending by a {@link TreeData} when inner state changed.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TreeDataEvent {

	private final TreeData _source;

	TreeDataEvent(TreeData source) {
		_source = source;
	}

	/**
	 * Returns the {@link TreeData} sending this event.
	 */
	public TreeData getSource() {
		return _source;
	}
	
	/**
	 * Inspects the specific change send with this {@link TreeDataEvent}.
	 * 
	 * @param v
	 *        Callback handle specific change.
	 */
	public abstract void visit(TreeDataEventVisitor v);

	/**
	 * {@link TreeDataEvent} informing about the change of the {@link TreeData#getTreeModel()} of
	 * the sending {@link TreeData}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class TreeUIModelChange extends TreeDataEvent{

		private final TreeUIModel _oldModel;

		private final TreeUIModel _newModel;

		/**
		 * Creates a new {@link TreeUIModelChange}.
		 * 
		 * @param source
		 *        The sending {@link TreeData}.
		 * @param oldModel
		 *        The old {@link TreeUIModel}.
		 * @param newModel
		 *        The new {@link TreeUIModel}.
		 */
		public TreeUIModelChange(TreeData source, TreeUIModel oldModel, TreeUIModel newModel) {
			super(source);
			_oldModel = oldModel;
			_newModel = newModel;
		}

		@Override
		public void visit(TreeDataEventVisitor v) {
			v.notifyTreeModelChange(getSource(), _newModel, _oldModel);
		}
		
	}

	/**
	 * {@link TreeDataEvent} informing about the change of the {@link TreeData#getSelectionModel()}
	 * of the sending {@link TreeData}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SelectionModelChange extends TreeDataEvent {

		private SelectionModel _oldSelection;

		private SelectionModel _newSelection;

		/**
		 * Creates a new {@link SelectionModelChange}.
		 * 
		 * @param source
		 *        The sending {@link TreeData}.
		 * @param oldSelection
		 *        The old {@link SelectionModel}.
		 * @param newSelection
		 *        The new {@link SelectionModel}.
		 */
		public SelectionModelChange(TreeData source, SelectionModel oldSelection, SelectionModel newSelection) {
			super(source);
			_oldSelection = oldSelection;
			_newSelection = newSelection;
		}

		@Override
		public void visit(TreeDataEventVisitor v) {
			v.notifySelectionModelChange(getSource(), _newSelection, _oldSelection);
		}
	}

	/**
	 * {@link TreeDataEvent} informing about the change of the {@link TreeData#getTreeRenderer()} of
	 * the sending {@link TreeData}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class RendererChange extends TreeDataEvent {

		private TreeRenderer _oldRenderer;

		private TreeRenderer _newRenderer;

		/**
		 * Creates a new {@link RendererChange}.
		 * 
		 * @param source
		 *        The sending {@link TreeData}.
		 * @param oldRenderer
		 *        The old {@link TreeRenderer}.
		 * @param newRenderer
		 *        The new {@link TreeRenderer}.
		 */
		public RendererChange(TreeData source, TreeRenderer oldRenderer, TreeRenderer newRenderer) {
			super(source);
			_oldRenderer = oldRenderer;
			_newRenderer = newRenderer;
		}

		@Override
		public void visit(TreeDataEventVisitor v) {
			v.notifyRendererChange(getSource(), _newRenderer, _oldRenderer);
		}

	}

}

