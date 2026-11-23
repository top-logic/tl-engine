/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import static com.top_logic.model.search.expr.I18NConstants.*;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.PersistTransientAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a remove of a part from a composition checks the Delete right on the part, as the
 * TL-Script <code>delete()</code> does: the part is lost to its container.
 *
 * <p>
 * The paths: the TL-Script functions <code>set</code> (with a constant and with a computed
 * attribute) and <code>remove</code>, the save of a composition table, of a row-editing table and of
 * a row-editing tree table that delete removed rows, the save of a form with a changed composition,
 * and the persist of a new part into a single-valued composition, which removes the part it
 * replaces.
 * </p>
 *
 * <p>
 * A report decides for itself: the responsible of a project reads, writes and creates the reports
 * of the project, but no role deletes a report. A task grants its Delete right to the responsible. A
 * milestone is decided by its container, the default access parent of a composition part: its
 * Delete right is the Write right on the project. An archive entry decides for itself, and no role
 * may discover or delete it: a refusal must not name it.
 * </p>
 */
public class TestCompositionRemoveDeleteRight extends AbstractModelAccessTest {

	/** Name of the type deciding for itself whose objects no role deletes. */
	private static final String REPORT = "Report";

	/** Name of the multiple {@link #PROJECT} and {@link #TASK} composition of {@link #REPORT}s. */
	private static final String REPORTS = "reports";

	/** Name of the single-valued {@link #PROJECT} composition of a {@link #REPORT}. */
	private static final String MAIN_REPORT = "mainReport";

	/** Name of a type deciding for itself whose objects no role reads or deletes. */
	private static final String ARCHIVE_ENTRY = "ArchiveEntry";

	/** Name of the {@link #PROJECT} composition of {@link #ARCHIVE_ENTRY}s. */
	private static final String ARCHIVE = "archive";

	/** Name of the channel holding the project. */
	private static final String PROJECT_CHANNEL = "project";

	private static final String REMOVE = "p -> a -> r -> $p.remove($a, $r)";

	private static final String DYNAMIC_SET = "p -> a -> v -> $p.set($a, $v)";

	private DefaultViewContext _context;

	private ReactContext _reactContext;

	/** The only report in the {@link #REPORTS} of the project. */
	private TLObject _report;

	/** The report in the {@link #MAIN_REPORT} of the project. */
	private TLObject _mainReport;

	/** The only milestone of the project. */
	private TLObject _milestone;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_context = new DefaultViewContext(_reactContext);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_report = newNamed(REPORT, "report");
			_mainReport = newNamed(REPORT, "main report");
			_milestone = newNamed(MILESTONE, "milestone");
			_project.tUpdateByName(REPORTS, List.of(_report));
			_project.tUpdateByName(MAIN_REPORT, _mainReport);
			_project.tUpdateByName(MILESTONES, List.of(_milestone));
			tx.commit();
		}
		DefaultViewChannel project = new DefaultViewChannel(PROJECT_CHANNEL);
		project.set(_project);
		_context.registerChannel(PROJECT_CHANNEL, project);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_reactContext = null;
		_report = null;
		_mainReport = null;
		_milestone = null;
		super.tearDown();
	}

	/**
	 * TL-Script <code>remove</code> of a part the user may not delete is refused.
	 */
	public void testScriptRemoveRefused() {
		becomeUser(_responsible);
		assertRemoveRefused(_report, () -> script(REMOVE, _project, part(PROJECT, REPORTS), _report));
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * TL-Script <code>set</code> of a composition without a part the user may not delete is
	 * refused.
	 */
	public void testScriptSetRefused() {
		becomeUser(_responsible);
		assertRemoveRefused(_report, () -> script(setScript(REPORTS), _project, List.of()));
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * TL-Script <code>set</code> with a computed attribute is checked as with a constant one.
	 */
	public void testScriptDynamicSetRefused() {
		becomeUser(_responsible);
		assertRemoveRefused(_report, () -> script(DYNAMIC_SET, _project, part(PROJECT, REPORTS), List.of()));
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * A new target of a single-valued composition removes the old part, which the user may not
	 * delete.
	 */
	public void testScriptSetSingleRefused() {
		becomeUser(_responsible);
		assertRemoveRefused(_mainReport, () -> {
			try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				TLObject replacement = newNamed(REPORT, "replacement");
				execute(search(setScript(MAIN_REPORT)), _project, replacement);
				tx.commit();
			} catch (ParseException ex) {
				throw new AssertionError(ex);
			}
		});
		assertSame(_mainReport, _project.tValueByName(MAIN_REPORT));
	}

	/**
	 * A remove of a part the user may neither delete nor discover is refused with a message that
	 * names the container and the composition, but not the part.
	 */
	public void testHiddenPartNotNamed() {
		TLObject entry;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			entry = DynamicModelService.getFactoryFor(MODULE).createObject(type(ARCHIVE_ENTRY));
			_project.tUpdateByName(ARCHIVE, List.of(entry));
			tx.commit();
		}
		becomeUser(_responsible);
		assertFalse(ModelAccessRights.getInstance().isDiscoverAllowed(entry));

		TopLogicException refusal = refusal(entry, () -> script(setScript(ARCHIVE), _project, List.of()));
		assertEquals(REMOVE_PERMISSION_DENIED__OBJECT_ATTRIBUTE.getKey(), refusal.getErrorKey().getKey());
		assertEquals(List.of(_project, part(PROJECT, ARCHIVE)), List.of(refusal.getErrorKey().arguments()));
		assertEquals(List.of(entry), _project.tValueByName(ARCHIVE));
	}

	/**
	 * The save of a composition table without a row the user may not delete is refused, and the
	 * form stays in edit mode.
	 */
	public void testCompositionTableRefused() {
		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl reports = new CompositionControl(form, REPORTS);
		reports.init();
		assertTrue(form.enterEditMode());
		reports.removeRow(reports.session().findRow(_report));

		assertRemoveRefused(_report, form::executeSave);
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
		assertTrue(form.isEditMode());
	}

	/**
	 * The save of a form whose composition lost a part the user may not delete is refused.
	 */
	public void testFormSaveRefused() {
		becomeUser(_responsible);
		FormControl form = newForm();
		assertTrue(form.enterEditMode());
		form.getOverlay().tUpdateByName(REPORTS, List.of());

		assertRemoveRefused(_report, form::executeSave);
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * The save of a row-editing table deleting its removed rows is refused for a row the user may
	 * not delete.
	 */
	public void testTableOnRemoveDeleteRefused() throws Exception {
		becomeUser(_responsible);
		FormControl form = newForm();
		RowSetTableControl table = (RowSetTableControl) ((TableElement) parse(TableElement.Config.class,
			"<table inputs='" + PROJECT_CHANNEL + "' rows='$" + PROJECT_CHANNEL + ".get(`" + qualified(PROJECT)
				+ "#" + REPORTS + "`)' row-edit='ALL' on-remove='DELETE'>"
				+ "<columns><column attribute='" + NAME + "'/></columns>"
				+ "</table>"))
			.createControl(_context);
		assertTrue(form.enterEditMode());
		table.removeRow(table.session().findRow(_report));

		assertRemoveRefused(_report, form::executeSave);
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * The save of a row-editing tree table deleting its removed rows is refused for a row the user
	 * may not delete.
	 */
	public void testTreeTableOnRemoveDeleteRefused() throws Exception {
		becomeUser(_responsible);
		FormControl form = newForm();
		RowSetTableControl table = editedTreeTable(form, qualified(REPORT),
			"if($node.instanceOf(`" + qualified(PROJECT) + "`), $node.get(`" + qualified(PROJECT) + "#" + REPORTS
				+ "`), [])");
		table.removeRow(table.session().findRow(_report));

		assertRemoveRefused(_report, form::executeSave);
		assertEquals(List.of(_report), _project.tValueByName(REPORTS));
	}

	/**
	 * A removed row the user may delete is deleted with the rows that are its parts: they are not
	 * checked on their own, as a delete of the row checks the row only.
	 */
	public void testPartsOfRemovedRowNotChecked() throws Exception {
		TLObject taskReport;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			taskReport = newNamed(REPORT, "task report");
			_task.tUpdateByName(REPORTS, List.of(taskReport));
			tx.commit();
		}
		becomeUser(_responsible);
		FormControl form = newForm();
		RowSetTableControl table = editedTreeTable(form, qualified(TASK) + "," + qualified(REPORT),
			"if($node.instanceOf(`" + qualified(PROJECT) + "`), $node.get(`" + qualified(PROJECT) + "#" + TASKS
				+ "`), if($node.instanceOf(`" + qualified(TASK) + "`), $node.get(`" + qualified(TASK) + "#" + REPORTS
				+ "`), []))");
		assertNotNull("The report of the task is a row.", table.session().findRow(taskReport));
		table.removeRow(table.session().findRow(_task));

		form.executeSave();
		assertFalse(_task.tValid());
		assertFalse(taskReport.tValid());
	}

	/**
	 * A row-editing tree table over the project that deletes removed rows, in the given form, which
	 * enters edit mode.
	 *
	 * @param form
	 *        The form of the {@link #_context}.
	 * @param types
	 *        The types of the rows.
	 * @param children
	 *        The function computing the children of a node.
	 */
	private RowSetTableControl editedTreeTable(FormControl form, String types, String children)
			throws ConfigurationException {
		RowSetTableControl table = (RowSetTableControl) ((TreeTableElement) parse(TreeTableElement.Config.class,
			"<tree-table inputs='" + PROJECT_CHANNEL + "' root='$" + PROJECT_CHANNEL + "'"
				+ " types='" + types + "' row-edit='ALL' on-remove='DELETE'>"
				+ "<children>" + children + "</children>"
				+ "<columns><column attribute='" + NAME + "'/></columns>"
				+ "</tree-table>"))
			.createControl(_context);
		assertTrue(form.enterEditMode());
		return table;
	}

	/**
	 * The persist of a new part into a single-valued composition is refused when the user may not
	 * delete the part it replaces.
	 */
	public void testPersistIntoSingleCompositionRefused() throws Exception {
		ViewAction persist = (ViewAction) parse(PersistTransientAction.Config.class,
			"<persist-transient container='" + PROJECT_CHANNEL + "' reference='" + MAIN_REPORT + "'/>");
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(REPORT));
		draft.tUpdateByName(NAME, "replacement");

		becomeUser(_responsible);
		assertRemoveRefused(_mainReport, () -> persist.execute(_context, draft));
		assertSame(_mainReport, _project.tValueByName(MAIN_REPORT));
	}

	/**
	 * A user holding the Delete right on a part removes it on each path, as before.
	 */
	public void testRemoveWithDeleteRight() throws Exception {
		becomeUser(_responsible);
		script(REMOVE, _project, part(PROJECT, TASKS), _task);
		assertEquals(List.of(), _project.tValueByName(TASKS));

		TLObject other = newPart(_project, TASKS, TASK);
		FormControl form = newForm();
		CompositionControl tasks = new CompositionControl(form, TASKS);
		tasks.init();
		assertTrue(form.enterEditMode());
		tasks.removeRow(tasks.session().findRow(other));
		form.executeSave();
		assertFalse(other.tValid());
		assertEquals(List.of(), _project.tValueByName(TASKS));
	}

	/**
	 * A part decided by its container, the default access parent, is removed by a user who may
	 * write the container, as before.
	 */
	public void testDefaultAccessParentUnchanged() throws Exception {
		becomeUser(_responsible);
		script(setScript(MILESTONES), _project, List.of());
		assertEquals(List.of(), _project.tValueByName(MILESTONES));

		TLObject other = newPart(_project, MILESTONES, MILESTONE);
		FormControl form = newForm();
		CompositionControl milestones = new CompositionControl(form, MILESTONES);
		milestones.init();
		assertTrue(form.enterEditMode());
		milestones.removeRow(milestones.session().findRow(other));
		form.executeSave();
		assertFalse(other.tValid());
		assertEquals(List.of(), _project.tValueByName(MILESTONES));
	}

	/**
	 * Asserts that the given remove of the given part is refused with the message of the TL-Script
	 * <code>delete()</code>, and that the part stays.
	 */
	private static void assertRemoveRefused(TLObject part, Runnable remove) {
		TopLogicException refusal = refusal(part, remove);
		assertEquals(DELETE_PERMISSION_DENIED__OBJECT.getKey(), refusal.getErrorKey().getKey());
		assertTrue("A refused remove keeps the part.", part.tValid());
	}

	/**
	 * The refusal of the given remove of the given part.
	 */
	private static TopLogicException refusal(TLObject part, Runnable remove) {
		try {
			remove.run();
		} catch (RuntimeException ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause instanceof TopLogicException refusal) {
					return refusal;
				}
			}
			throw ex;
		}
		throw new AssertionError("The remove was not refused: " + state(part));
	}

	/**
	 * Describes whether the given part was deleted or stays without its container.
	 */
	private static String state(TLObject part) {
		if (!part.tValid()) {
			return "the part is deleted.";
		}
		return "the part is not deleted, its container is now " + part.tContainer() + ".";
	}

	/**
	 * The script setting the given attribute of the given project to the given value.
	 */
	private static String setScript(String attribute) {
		return "p -> v -> $p.set(`" + qualified(PROJECT) + "#" + attribute + "`, $v)";
	}

	/**
	 * Executes the given script with the given arguments in a transaction of its own.
	 */
	private static void script(String script, Object... args) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			execute(search(script), args);
			tx.commit();
		} catch (ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	private FormControl newForm() {
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		return form;
	}

	private static TLObject newNamed(String typeName, String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(typeName));
		result.tUpdateByName(NAME, name);
		return result;
	}

	/**
	 * Instantiates the configuration of the given type in the given XML.
	 */
	private static Object parse(Class<? extends PolymorphicConfiguration<?>> configType, String xml)
			throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestCompositionRemoveDeleteRight.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configType);
		String tag = xml.substring(1, xml.indexOf(' '));
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(tag, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		Object result = context.getInstance((PolymorphicConfiguration<?>) reader.read());
		context.checkErrors();
		return result;
	}

	/**
	 * {@link AbstractCompositionControl} over a composition of the form object, without a
	 * presentation.
	 */
	private class CompositionControl extends AbstractCompositionControl {

		CompositionControl(FormControl form, String attribute) {
			super(_reactContext, form, attribute, "TLPanel");
		}

		@Override
		protected void buildContent(List<? extends TLObject> rows, boolean editMode) {
			// No presentation.
		}

		@Override
		protected void refreshRows() {
			// No presentation.
		}
	}

	/**
	 * The test suite, starting the theme and resources the tables need for their icons, the
	 * services building their columns and cells, and the scripting their rows are computed with.
	 */
	public static Test suite() {
		return suiteWith(TestCompositionRemoveDeleteRight.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
