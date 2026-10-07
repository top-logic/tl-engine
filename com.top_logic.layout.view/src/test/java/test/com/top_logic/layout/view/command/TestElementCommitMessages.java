/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.calendar.CalendarEvent;
import com.top_logic.layout.react.control.calendar.DefaultCalendarEvent;
import com.top_logic.layout.view.element.ExpressionCalendarModel;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.Resources;

/**
 * Tests the commit messages of the changes the calendar, the object list and the security scope of
 * the view layer perform.
 *
 * <p>
 * Each test asserts the message stored with the revision the change is committed in.
 * </p>
 *
 * @see ExpressionCalendarModel
 * @see ObjectListScope
 * @see SecurityScope
 */
public class TestElementCommitMessages extends AbstractModelAccessTest {

	/** The TL-Script literal of the name attribute of a project. */
	private static final String RENAME = "`" + MODULE + ":" + PROJECT + "#" + NAME + "`";

	/** The label of the project before a change, which the commit message names. */
	private static final String ORIGINAL = "project";

	/** Security ID of the scope the tests grant and revoke on. */
	private static final ComponentName SCOPE_ID = ComponentName.newName("test.TestElementCommitMessages");

	/** Label of the scope the tests grant and revoke on. */
	private static final String SCOPE_LABEL = "Test scope";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		becomeUser(_root);
	}

	/**
	 * Moving a calendar entry names the entry.
	 */
	public void testCalendarMove() throws Exception {
		ExpressionCalendarModel calendar =
			calendar(script("o -> s -> e -> $o.set(" + RENAME + ", 'moved')"), null, null);

		calendar.moveEvent(event(_project), new Date(), new Date());

		assertEquals("moved", _project.tValueByName(NAME));
		assertLastMessage("Moved calendar entry: " + ORIGINAL);
	}

	/**
	 * Changing the end of a calendar entry names the entry.
	 */
	public void testCalendarResize() throws Exception {
		ExpressionCalendarModel calendar =
			calendar(null, script("o -> e -> $o.set(" + RENAME + ", 'resized')"), null);

		calendar.resizeEvent(event(_project), new Date());

		assertEquals("resized", _project.tValueByName(NAME));
		assertLastMessage("Changed duration of calendar entry: " + ORIGINAL);
	}

	/**
	 * Creating a calendar entry names the created object, not the requested title.
	 */
	public void testCalendarCreate() throws Exception {
		String category = "`" + MODULE + ":" + CATEGORY + "`";
		ExpressionCalendarModel calendar = calendar(null, null,
			script("s -> e -> a -> t -> { c = new(" + category + "); $c.set(`" + MODULE + ":" + CATEGORY + "#"
				+ NAME + "`, $t + '!'); $c; }"));

		CalendarEvent created = calendar.createEvent(new Date(), new Date(), false, "entry");
		try {
			assertLastMessage("Created object: entry!");
		} finally {
			delete((TLObject) created.getBusinessObject());
		}
	}

	/**
	 * Adding an element to a list names the element.
	 */
	public void testListAdd() throws Exception {
		ObjectListScope list = list(script("x -> $x.set(" + RENAME + ", 'added')"));

		list.linkElement(_project);

		assertLastMessage("Added list element: " + ORIGINAL);
	}

	/**
	 * Removing an element from a list names the element.
	 */
	public void testListRemove() throws Exception {
		ObjectListScope list = list(script("x -> $x.set(" + RENAME + ", 'removed')"));

		list.removeElement(_project);

		assertLastMessage("Removed list element: " + ORIGINAL);
	}

	/**
	 * Granting and revoking a command group names the group, the role and the scope.
	 */
	public void testSecurityGrant() throws Exception {
		PersBoundComp persBoundComp;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			persBoundComp = PersBoundComp.createInstance(kb(), SCOPE_ID);
			tx.commit();
		}
		try {
			SecurityScope scope = new SecurityScope(SCOPE_ID, ResKey.text(SCOPE_LABEL), List.of());
			BoundCommandGroup group = SimpleBoundCommandGroup.READ;
			BoundedRole role = BoundedRole.getRoleByName(ROLE_RESPONSIBLE);
			String groupLabel = MetaLabelProvider.INSTANCE.getLabel(group);
			String roleLabel = MetaLabelProvider.INSTANCE.getLabel(role);

			scope.setGranted(group, role, true);
			assertTrue(scope.isGranted(group, role));
			assertLastMessage(
				"Granted command group \"" + groupLabel + "\" to role \"" + roleLabel + "\" on \"" + SCOPE_LABEL + "\".");

			scope.setGranted(group, role, false);
			assertFalse(scope.isGranted(group, role));
			assertLastMessage(
				"Revoked command group \"" + groupLabel + "\" from role \"" + roleLabel + "\" on \"" + SCOPE_LABEL
					+ "\".");
		} finally {
			delete(persBoundComp);
		}
	}

	private static ExpressionCalendarModel calendar(QueryExecutor onMove, QueryExecutor onResize,
			QueryExecutor onCreate) {
		ExpressionCalendarModel.EventExprs exprs =
			new ExpressionCalendarModel.EventExprs(null, null, null, null, null, null, null, null, null);
		return new ExpressionCalendarModel(List.of(), exprs, onMove, onResize, onCreate);
	}

	private static CalendarEvent event(Object businessObject) {
		return new DefaultCalendarEvent("event", new Date(), new Date()).setBusinessObject(businessObject);
	}

	private static ObjectListScope list(QueryExecutor function) {
		return new ObjectListScope(List.of(), function, function);
	}

	private static QueryExecutor script(String function) throws Exception {
		return QueryExecutor.compile(parse(function));
	}

	private static void assertLastMessage(String expected) {
		HistoryManager history = kb().getHistoryManager();
		ResKey log = history.getRevision(history.getLastRevision()).getLog();
		assertEquals(expected, Resources.getInstance(Locale.ENGLISH).getString(log));
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestElementCommitMessages.class);
	}

}
