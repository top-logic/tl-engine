/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.top_logic.knowledge.monitor.FailedLogin;
import com.top_logic.knowledge.monitor.I18NConstants;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Holder for things used for table component for failed logins.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class FailedLoginTableComponent {

    /**
     * ResourceProvider to show dates inclusive timestamp.
     */
    public static class FailedLoginDateResourceProvider extends MetaResourceProvider {

        @Override
        public String getLabel(Object anObject) {
            if (anObject instanceof Date) {
                return HTMLFormatter.getInstance().getDateTimeFormat().format(anObject);
            }
            return super.getLabel(anObject);
        }

    }


    /**
     * Accessor to get reasons i18Ned.
     */
    public static class FailedLoginsAccessor extends ReadOnlyAccessor<FailedLogin> {

    	private static final String COLUMN_REASON = "reason";

        private static final I18NResourceProvider RES_PROVIDER = new I18NResourceProvider(I18NConstants.FAILED_LOGIN_REASONS);

        @Override
		public Object getValue(FailedLogin object, String property) {
            Object value = WrapperAccessor.INSTANCE.getValue(object, property);
            if (value == null) {
            	return null;
            }
			if (COLUMN_REASON.equals(property) ||
				FailedLogin.NO_CLIENT_IP.equals(value) ||
				FailedLogin.EMPTY_USER_NAME.equals(value)) {
                return RES_PROVIDER.getLabel(value);
            }
            return value;
        }

    }


    /**
     * ModelBuilder searching the failed logins for the given date range.
     */
	public static class FailedLoginsModelBuilder implements ListModelBuilder {

		/**
		 * Singleton {@link FailedLoginsModelBuilder} instance.
		 */
		public static final FailedLoginsModelBuilder INSTANCE = new FailedLoginsModelBuilder();

		private FailedLoginsModelBuilder() {
			// Singleton constructor.
		}

        @Override
		public Collection<FailedLogin> getModel(Object businessModel, LayoutComponent aComponent) {
			Map theMap = (Map) businessModel;
            if (theMap == null) return Collections.emptyList();
            Date start = (Date) theMap.get(SessionSearchComponent.START_DATE_FIELD);
            Date end = (Date) theMap.get(SessionSearchComponent.END_DATE_FIELD);
            return FailedLogin.getFailedLogins(start, end);
        }

		@Override
		public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
			if (!(listElement instanceof FailedLogin)) {
				return false;
			}
			Map theMap = (Map) contextComponent.getModel();
			if (theMap == null) {
				return false;
			}
			Date start = (Date) theMap.get(SessionSearchComponent.START_DATE_FIELD);
			Date end = (Date) theMap.get(SessionSearchComponent.END_DATE_FIELD);
			Date date = (Date) ((FailedLogin) listElement).getValue(FailedLogin.DATE);
			return date != null && !start.after(date) && !end.before(date);
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
            return aModel == null;
        }

    }

}
