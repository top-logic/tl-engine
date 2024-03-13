/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.util.TLContext;

/**
 * A Standard List of Persons for generic usage.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PersonTableComponent extends TableComponent {

    public static final String COLUMN_AUTH_DEVICE_ID = Person.AUTHENTICATION_DEVICE_ID;
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_LAST_LOGIN = "lastLogin";

	/**
	 * Construct a PersonTable via XML.
	 */
	public PersonTableComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
		super(context, someAttr);
	}

	/**
	 * Select the {@link Person} of the current user as default.
	 */
	@Override
	protected Object getDefaultSelection(List<?> displayedRows) {
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();

		if (displayedRows.contains(currentPerson)) {
			return currentPerson;
		} else {
			return super.getDefaultSelection(displayedRows);
		}
	}

    /**
	 * Looks up the last login date for a given account.
	 */
    public static class LastLoginAccessor extends ReadOnlyAccessor<Person> {
		@Override
		public Object getValue(Person person, String aKey) {
			KnowledgeBase kb = person.getKnowledgeBase();
			RevisionQuery<KnowledgeObject> query = queryUnresolved(
				BranchParam.single,
				RangeParam.head,
				params(),
				filter(
					allOf(UserSession.OBJECT_NAME),
					eqBinary(
						attribute(UserSession.OBJECT_NAME, UserSession.USER_NAME),
						literal(person.getName()))),
				orderDesc(attribute(UserSession.OBJECT_NAME, UserSession.LOGIN)));
			try (CloseableIterator<KnowledgeObject> sessions =
				kb.searchStream(query, revisionArgs().setStopRow(1))) {
				if (sessions.hasNext()) {
					KnowledgeObject session = sessions.next();
					return session.getAttributeValue(UserSession.LOGIN);
				}
				return null;
			}
		}
    }
}
