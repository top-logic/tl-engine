/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UserMonitorListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link UserMonitorListModelBuilder} instance.
	 */
	public static final UserMonitorListModelBuilder INSTANCE = new UserMonitorListModelBuilder();

	private UserMonitorListModelBuilder() {
		// Singleton constructor.
	}

    @Override
	public Collection<UserSession> getModel(Object businessModel, LayoutComponent aComponent) {
		Map theMap = (Map) businessModel;

        if (theMap != null) {
        	Date theStart = (Date) theMap.get(SessionSearchComponent.START_DATE_FIELD);
        	Date theEnd   = (Date) theMap.get(SessionSearchComponent.END_DATE_FIELD);

			return UserMonitor.userSessions(theStart, theEnd);
        }
        else {
			return Collections.emptyList();
        }
    }

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		if (!(listElement instanceof UserSession)) {
			return false;
		}
		Map theMap = (Map) contextComponent.getModel();

		if (theMap != null) {
			Date start = (Date) theMap.get(SessionSearchComponent.START_DATE_FIELD);
			Date end = (Date) theMap.get(SessionSearchComponent.END_DATE_FIELD);
			Date date = (Date) ((UserSession) listElement).getValue(UserSession.LOGIN);
			return date != null && !start.after(date) && !end.before(date);
		} else {
			return false;
		}
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        if (aModel instanceof Map) {
            Map theMap = (Map) aModel;

            return theMap.containsKey(SessionSearchComponent.END_DATE_FIELD) && 
                   theMap.containsKey(SessionSearchComponent.START_DATE_FIELD) &&
                   theMap.containsKey(SessionSearchComponent.RELATIVE_RANGES_FIELD) &&
                   theMap.containsKey(SessionSearchComponent.TIME_RANGE_FIELD);
        }
        else {
            return aModel == null;
        }
    }
}

