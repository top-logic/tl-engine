/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link BorderBox} that can be collapsed in a way that only the top boxes are shown.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollapsibleBorderBox extends BorderBox implements VisibilityListener, CollapsedListener {

	private Collapsible _expansionModel = defaultExpansionModel();

	private VisibilityModel _visibilityModel;

	/**
	 * Creates an always visible {@link CollapsibleBorderBox}.
	 */
	public CollapsibleBorderBox() {
		this(VisibilityModel.AlwaysVisible.INSTANCE);
	}

	/**
	 * Creates a {@link CollapsibleBorderBox} with the given {@link VisibilityModel}.
	 * 
	 * @param visibility
	 *        See {@link #getVisibilityModel()}.
	 */
	public CollapsibleBorderBox(VisibilityModel visibility) {
		_visibilityModel = visibility;
	}

	/**
	 * Whether only the boxes of the top row are shown.
	 */
	public final boolean isCollapsed() {
		return _expansionModel.isCollapsed();
	}

	/**
	 * @see #isCollapsed()
	 */
	public final void setCollapsed(boolean collapsed) {
		_expansionModel.setCollapsed(collapsed);
	}

	/**
	 * Whether this box is visible.
	 * 
	 * <p>
	 * An invisible box is not rendered at all.
	 * </p>
	 */
	public final boolean isVisible() {
		return _visibilityModel.isVisible();
	}

	/**
	 * The underlying {@link VisibilityModel}.
	 */
	public final VisibilityModel getVisibilityModel() {
		return _visibilityModel;
	}

	/**
	 * Injects a separate {@link VisibilityModel} that controls the visibility of this box.
	 * 
	 * @param visibility
	 *        The {@link VisibilityModel} to observe. <code>null</code> means that this box should
	 *        be always visible.
	 * 
	 * @see #getVisibilityModel()
	 */
	public void setVisibilityModel(VisibilityModel visibility) {
		boolean visibleBefore = isVisible();

		boolean attached = isAttached();
		if (attached) {
			detachVisibilityModel();
		}

		_visibilityModel = VisibilityModel.AlwaysVisible.ifNull(visibility);

		if (attached) {
			attachVisibilityModel();
		}

		boolean visibleAfter = isVisible();
		if (visibleAfter != visibleBefore) {
			handleVisibilityChange(_visibilityModel, visibleBefore, visibleAfter);
		}
	}

	private void attachVisibilityModel() {
		_visibilityModel.addListener(VisibilityModel.VISIBLE_PROPERTY, this);
	}

	private void detachVisibilityModel() {
		_visibilityModel.removeListener(VisibilityModel.VISIBLE_PROPERTY, this);
	}

	/**
	 * The underlying {@link Collapsible} model providing the {@link #isCollapsed()} state.
	 */
	public Collapsible getExpansionModel() {
		return _expansionModel;
	}

	/**
	 * Injects a separate {@link Collapsible} that controls the expansion state of this box.
	 * 
	 * @param expansionModel
	 *        The {@link Collapsible} to observe. <code>null</code> means that this box is expanded.
	 * 
	 * @see #getExpansionModel()
	 */
	public void setExpansionModel(Collapsible expansionModel) {
		boolean collapsedBefore = isCollapsed();

		boolean attached = isAttached();
		if (attached) {
			detachExpansionModel();
		}

		_expansionModel = expansionModel == null ? defaultExpansionModel() : expansionModel;

		if (attached) {
			attachExpansionModel();
		}

		boolean collapsedAfter = isCollapsed();
		if (collapsedAfter != collapsedBefore) {
			handleCollapsed(_expansionModel, collapsedBefore, collapsedAfter);
		}
	}

	private DefaultExpansionModel defaultExpansionModel() {
		return new DefaultExpansionModel(false);
	}

	/**
	 * Create {@link Box#LAYOUT_CHANGE} events in response to changes in the observed
	 * {@link VisibilityModel}.
	 */
	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		notifyLayoutChange();
		return Bubble.BUBBLE;
	}

	private void attachExpansionModel() {
		_expansionModel.addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	private void detachExpansionModel() {
		_expansionModel.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	/**
	 * Create {@link Box#LAYOUT_CHANGE} events in response to changes in the observed
	 * {@link Collapsible} model.
	 */
	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		notifyLayoutChange();
		return Bubble.BUBBLE;
	}

	@Override
	protected int leftColumns() {
		return super.leftColumns();
	}

	@Override
	protected int centerColumns() {
		return super.centerColumns();
	}

	@Override
	protected int rightColumns() {
		return super.rightColumns();
	}

	@Override
	protected int centerRows() {
		if (isCollapsed() || invisible()) {
			return 0;
		} else {
			return super.centerRows();
		}
	}

	private boolean invisible() {
		return !isVisible();
	}

	@Override
	protected int bottomRows() {
		if (isCollapsed() || invisible()) {
			return 0;
		} else {
			return super.bottomRows();
		}
	}

	@Override
	protected void localLayout() {
		if (invisible()) {
			setDimension(0, 0);
		} else if (isCollapsed()) {
			super.localLayout();

			setDimension(
				leftColumns() + centerColumns() + rightColumns(),
				topRows() + centerRows() + bottomRows());
		} else {
			super.localLayout();
		}
	}

	@Override
	protected void enterContent(int x, int y, int availableColumns, int availableRows, Table<ContentBox> table) {
		if (invisible()) {
			// Nothing to enter.
			return;
		}
		if (isCollapsed()) {
			int leftColumns = leftColumns();
			int rightColumns = rightColumns();
			int centerColumns = availableColumns - leftColumns - rightColumns;

			RowLayouter layouter = new RowLayouter(table);

			int topRows = topRows();
			layouter.startRow(x, y, topRows);
			layouter.add(leftColumns, getTopLeft());
			layouter.add(centerColumns, getTopBorder());
			layouter.add(rightColumns, getTopRight());
			layouter.endRow();

			int hiddenRows = availableRows - topRows;
			layouter.startRow(x, y + topRows, hiddenRows);
			layouter.add(availableColumns, Boxes.contentBox());
			layouter.endRow();
		} else {
			super.enterContent(x, y, availableColumns, availableRows, table);
		}
	}

	@Override
	protected void attach() {
		super.attach();
		attachVisibilityModel();
		attachExpansionModel();
	}

	@Override
	protected void detach() {
		super.detach();
		detachVisibilityModel();
		detachExpansionModel();
	}

}
