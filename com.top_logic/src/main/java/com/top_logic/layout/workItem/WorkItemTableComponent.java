/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.workItem;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.base.workItem.WorkItemComparator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableComponent;

/**
 * Table displaying {@link WorkItem work items}.
 *
 * @author     <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class WorkItemTableComponent extends TableComponent {

    /**
     * Create a new instance of this class.
     */
    public WorkItemTableComponent(InstantiationContext context, Config attr) throws ConfigurationException {
        super(context, attr);
    }

    @Override
	protected boolean receiveModelCreatedEvent(Object aModel, Object changedBy) {
        if (! this.getListBuilder().supportsListElement(this, aModel)) {
            return false;
        }
        this.invalidate();
        return true;
    }

    @Override
	protected boolean receiveGlobalRefreshEvent(Object model, Object changedBy) {
        invalidate();
        return true;
    }

    @Override
	protected void initTableViewModel(TableViewModel newViewModel) {
		super.initTableViewModel(newViewModel);

		newViewModel.setColumnComparator(0, WorkItemComparator.INSTANCE);
        newViewModel.setDescendingColumnComparator(0, WorkItemComparator.INSTANCE_DESCENDING);
    }

}
