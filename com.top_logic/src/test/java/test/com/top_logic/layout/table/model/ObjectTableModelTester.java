/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.Vector;

import com.top_logic.layout.basic.AbstractAttachable;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;

/**
 * The ObjectTableViewTester contains an {@link TableModel} and a Vector which shall be equal to the
 * rows of the {@link TableModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectTableModelTester extends AbstractAttachable implements
		TableModelListener {

	private TableModel base;
	private Vector observedRows;
	private int maxEventCount;
	private int eventCount;

	/**
	 * Creates a {@link ObjectTableModelTester}, which stops listening his model after
	 * <tt>maxEventCount</tt> events.
	 */
	public ObjectTableModelTester(TableModel base, int maxEventCount) {
		if (maxEventCount < 0)
			throw new IndexOutOfBoundsException("maxEventCount must be geq 0!");
		this.base = base;
		this.maxEventCount = maxEventCount;
		observedRows = new Vector();
	}

	public ObjectTableModelTester(TableModel base) {
		this.base = base;
		this.maxEventCount = -1;
		observedRows = new Vector();
	}

	@Override
	protected void internalAttach() {
		eventCount = 0;
		base.addTableModelListener(this);
		for (int i = 0; i < base.getRowCount(); i++) {
			observedRows.add(base.getRowObject(i));
		}
	}

	@Override
	protected void internalDetach() {
		base.removeTableModelListener(this);
		observedRows = new Vector();
	}

	/**
	 * This method copies the event onto the observed rows.
	 * 
	 * @see com.top_logic.layout.table.model.TableModelListener#handleTableModelEvent(com.top_logic.layout.table.model.TableModelEvent)
	 */
	@Override
	public void handleTableModelEvent(TableModelEvent event) {
		int type = event.getType();
		if (type == TableModelEvent.DELETE) {
			assert event.getLastRow() >= event.getFirstRow(): "First deleted row is less than last deleted row.";
			for (int i = event.getLastRow(); i >= event.getFirstRow(); i--) {
				observedRows.remove(i);
			}
		} else if (type == TableModelEvent.UPDATE) {
			assert event.getLastRow() >= event.getFirstRow(): "First updated row is less than last updated row.";
			for (int i = event.getFirstRow(); i <= event.getLastRow(); i++) {
				observedRows.setElementAt(base.getRowObject(i), i);
			}
		} else if (type == TableModelEvent.INSERT) {
			assert event.getLastRow() >= event.getFirstRow(): "First inserted row is less than last inserted row.";
			for (int i = event.getFirstRow(); i <= event.getLastRow(); i++) {
				observedRows.add(i, base.getRowObject(i));
			}
		} else if (type == TableModelEvent.INVALIDATE) {
			int newSize = base.getRowCount();
			observedRows.setSize(newSize);
			for (int i = 0; i < newSize; i++) {
				observedRows.setElementAt(base.getRowObject(i), i);
			}
		} else
			assert false : "Unknown TableModelEventType!"; // Execution should
															// never reach this
															// point!
		eventCount++;
		if (eventCount == maxEventCount) {
			detach();
		}
	}

	/**
	 * This method returns the observedRows.
	 * 
	 * @return Returns the observedRows.
	 */
	public Vector getObservedRows() {
		return (observedRows);
	}
}
