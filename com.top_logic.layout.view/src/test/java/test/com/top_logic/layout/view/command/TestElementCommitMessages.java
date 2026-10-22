/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.calendar.CalendarEvent;
import com.top_logic.layout.react.control.calendar.DefaultCalendarEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.element.CalendarElement;
import com.top_logic.layout.view.element.ExpressionCalendarModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.job.ChunkedJobBody;
import com.top_logic.layout.view.job.JobMonitor;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.list.ElementAsConfig;
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
 * Tests the commit messages of the changes the calendar, the object list, the security scope, the
 * form and the background job of the view layer perform.
 *
 * <p>
 * Each test asserts the message stored with the revision the change is committed in.
 * </p>
 *
 * @see ExpressionCalendarModel
 * @see ObjectListScope
 * @see SecurityScope
 * @see FormControl
 * @see ChunkedJobBody
 */
public class TestElementCommitMessages extends AbstractModelAccessTest {

	/** The TL-Script literal of the name attribute of a project. */
	private static final String RENAME = "`" + MODULE + ":" + PROJECT + "#" + NAME + "`";

	/** The variable holding the calendar entry. */
	private static final String ENTRY = CalendarElement.Config.DEFAULT_ENTRY_AS;

	/** The variable holding the start of a calendar entry. */
	private static final String START = CalendarElement.Config.START_VARIABLE;

	/** The variable holding the end of a calendar entry. */
	private static final String END = CalendarElement.Config.END_VARIABLE;

	/** The variable telling whether a created calendar entry spans whole days. */
	private static final String ALL_DAY = CalendarElement.Config.ALL_DAY_VARIABLE;

	/** The variable holding the title of a created calendar entry. */
	private static final String TITLE = CalendarElement.Config.TITLE_VARIABLE;

	/** The variable holding the element of a list. */
	private static final String ELEMENT = ElementAsConfig.DEFAULT_ELEMENT_AS;

	/** The label of the project before a change, which the commit message names. */
	private static final String ORIGINAL = "project";

	/** Security ID of the scope the tests grant and revoke on. */
	private static final ComponentName SCOPE_ID = ComponentName.newName("test.TestElementCommitMessages");

	/** Label of the scope the tests grant and revoke on. */
	private static final String SCOPE_LABEL = "Test scope";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected Person initialUser() {
		return _root;
	}

	/**
	 * Moving a calendar entry names the entry.
	 */
	public void testCalendarMove() throws Exception {
		ExpressionCalendarModel calendar =
			calendar(script("$entry.set(" + RENAME + ", 'moved')", ENTRY, START, END), null, null);

		calendar.moveEvent(event(_project), new Date(), new Date());

		assertEquals("moved", _project.tValueByName(NAME));
		assertLastMessage("Moved calendar entry: " + ORIGINAL);
	}

	/**
	 * Changing the end of a calendar entry names the entry.
	 */
	public void testCalendarResize() throws Exception {
		ExpressionCalendarModel calendar =
			calendar(null, script("$entry.set(" + RENAME + ", 'resized')", ENTRY, END), null);

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
			script("{ c = new(" + category + "); $c.set(`" + MODULE + ":" + CATEGORY + "#"
				+ NAME + "`, $title + '!'); $c; }", START, END, ALL_DAY, TITLE));

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
		ObjectListScope list = list(script("$element.set(" + RENAME + ", 'added')", ELEMENT));

		list.linkElement(_project);

		assertLastMessage("Added list element: " + ORIGINAL);
	}

	/**
	 * Removing an element from a list names the element.
	 */
	public void testListRemove() throws Exception {
		ObjectListScope list = list(script("$element.set(" + RENAME + ", 'removed')", ELEMENT));

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

	/**
	 * Saving a form names the saved object.
	 */
	public void testFormSave() throws Exception {
		FormControl form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
		form.enterEditMode();
		form.getOverlay().tUpdate(part(PROJECT, NAME), "saved");

		form.executeStoreState();

		assertEquals("saved", _project.tValueByName(NAME));
		assertLastMessage("Updated object: " + ORIGINAL);
	}

	/**
	 * The commits of a background job name the step and the items they process.
	 */
	public void testJob() throws Exception {
		List<String> items = List.of("a", "b", "c", "d");
		ChunkedJobBody body = new ChunkedJobBody(2) {
			@Override
			protected boolean hasInit() {
				return true;
			}

			@Override
			protected Object init(JobMonitor job, List<Object> arguments) {
				_project.tUpdateByName(NAME, "init");
				return _project;
			}

			@Override
			protected List<?> elements(JobMonitor job, Object state) {
				return items;
			}

			@Override
			protected int stepCount() {
				return 1;
			}

			@Override
			protected void step(JobMonitor job, int index, List<?> chunk, Object state) {
				if (chunk.contains("c") && chunk.size() > 1) {
					// The second chunk fails as a whole and is retried item by item.
					throw new IllegalStateException("Cannot process c together with others.");
				}
				_project.tUpdateByName(NAME, String.join("", chunk.stream().map(String::valueOf).toList()));
			}

			@Override
			protected boolean hasFinish() {
				return true;
			}

			@Override
			protected Object finish(JobMonitor job, Object state) {
				_project.tUpdateByName(NAME, "finish");
				return null;
			}
		};

		long before = kb().getHistoryManager().getLastRevision();
		body.run(new NoMonitor(), List.of());

		assertEquals(List.of(
			"Executed step \"Preparation\" of a background job.",
			"Executed step \"Pass 1\" of a background job on items 1 to 2.",
			"Executed step \"Pass 1\" of a background job on item 3.",
			"Executed step \"Pass 1\" of a background job on item 4.",
			"Executed step \"Completion\" of a background job."), messagesSince(before));
	}

	private static List<String> messagesSince(long revision) {
		HistoryManager history = kb().getHistoryManager();
		List<String> result = new ArrayList<>();
		for (long n = revision + 1, last = history.getLastRevision(); n <= last; n++) {
			result.add(Resources.getInstance(Locale.ENGLISH).getString(history.getRevision(n).getLog()));
		}
		return result;
	}

	/**
	 * {@link JobMonitor} ignoring all reports.
	 */
	private static final class NoMonitor implements JobMonitor {
		@Override
		public void setPhases(List<JobPhase> phases) {
			// Ignored.
		}

		@Override
		public void beginPhase(String name) {
			// Ignored.
		}

		@Override
		public void progress(double done, double total) {
			// Ignored.
		}

		@Override
		public void fraction(double fraction) {
			// Ignored.
		}

		@Override
		public void indeterminate() {
			// Ignored.
		}

		@Override
		public void message(ResKey message) {
			// Ignored.
		}

		@Override
		public void checkCancelled() {
			// Never cancelled.
		}
	}

	private static ExpressionCalendarModel calendar(QueryExecutor onMove, QueryExecutor onResize,
			QueryExecutor onCreate) {
		ExpressionCalendarModel.EventExprs exprs =
			new ExpressionCalendarModel.EventExprs(null, null, null, null, null, null, null, null, null);
		return new ExpressionCalendarModel(List.of(), List.of(), exprs, onMove, onResize, onCreate);
	}

	private static CalendarEvent event(Object businessObject) {
		return new DefaultCalendarEvent("event", new Date(), new Date()).setBusinessObject(businessObject);
	}

	private static ObjectListScope list(QueryExecutor function) {
		return new ObjectListScope(List.of(), function, function);
	}

	private static QueryExecutor script(String function, String... variables) throws Exception {
		return QueryExecutor.compile(parse(function), List.of(variables));
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
