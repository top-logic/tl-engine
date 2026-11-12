/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ReactCompositeControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl.Severity;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.control.form.SliderRange;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.ScrollArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.scripting.ScriptingAction;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.SliderState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that what the headless observation says about a control matches what the user sees: the
 * value of a group in its header row, the nesting of the rows of a grouped table, the range of a
 * slider, a notice only while it is displayed, names that do not change with the displayed
 * content, and failures in the language of the user.
 */
public class TestObservationContent extends TestCase {

	private static final String NAME = "name";

	private static final String STATUS = "status";

	private static final String ROWS = "rows";

	private static final String CELLS = "cells";

	private static final String DEPTH = "depth";

	private static final String EXPANDABLE = "expandable";

	private static final String EXPANDED = "expanded";

	private static final String GROUP_COUNT = "groupCount";

	private static final String NOTICE_NAME = "sessionTimeout";

	private static final String UNKNOWN_COMMAND = "bogus";

	private ReactContext _context;

	/**
	 * Runs the test within an interaction of a session, as the naming of an object happens in one.
	 */
	@Override
	protected void runTest() throws Throwable {
		InteractionWithoutSession.inSession().runWithContext((ComputationEx2<Void, Throwable, RuntimeException>) () -> {
			super.runTest();
			return null;
		});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * The header row of a group carries the value of the group in its first cell, also where the
	 * column it is grouped by displays its values by controls, and the rows tell how deep they are
	 * nested and whether they are expanded.
	 */
	@SuppressWarnings("unchecked")
	public void testGroupedRows() {
		TableViewControl<String> table = table(true);

		List<Map<String, Object>> rows = (List<Map<String, Object>>) ScriptingTreeProjector
			.nodeState(table, ProjectionMode.AGENT).get(ROWS);
		assertEquals("Two group headers and their three rows.", 5, rows.size());

		Map<String, Object> header = rows.get(0);
		assertEquals("a", ((Map<String, Object>) header.get(CELLS)).get(NAME));
		assertEquals(Integer.valueOf(2), header.get(GROUP_COUNT));
		assertEquals(Integer.valueOf(0), header.get(DEPTH));
		assertEquals(Boolean.TRUE, header.get(EXPANDABLE));
		assertEquals(Boolean.TRUE, header.get(EXPANDED));

		Map<String, Object> member = rows.get(1);
		assertEquals("a1", ((Map<String, Object>) member.get(CELLS)).get(NAME));
		assertEquals(Integer.valueOf(1), member.get(DEPTH));
		assertEquals(Boolean.FALSE, member.get(EXPANDABLE));
		assertFalse("A leaf is neither expanded nor collapsed.", member.containsKey(EXPANDED));

		assertTrue(commands(table).contains(TableViewControl.CMD_EXPAND));
	}

	/** A flat table carries no nesting and offers no expansion. */
	@SuppressWarnings("unchecked")
	public void testFlatRows() {
		TableViewControl<String> table = table(false);

		List<Map<String, Object>> rows = (List<Map<String, Object>>) ScriptingTreeProjector
			.nodeState(table, ProjectionMode.AGENT).get(ROWS);
		assertEquals(3, rows.size());
		assertFalse(rows.get(0).containsKey(DEPTH));
		assertFalse(rows.get(0).containsKey(EXPANDABLE));
		assertFalse(commands(table).contains(TableViewControl.CMD_EXPAND));
	}

	/** A slider tells the range its handle travels, not only its value. */
	public void testSliderRange() {
		ReactSliderControl slider = new ReactSliderControl(_context, new AbstractFieldModel(Double.valueOf(12.5)),
			new DecimalFormat("#,##0.0", DecimalFormatSymbols.getInstance(Locale.GERMANY)),
			new SliderRange(0.0, 100.0, 0.5, null));

		Map<String, Object> state = ScriptingTreeProjector.nodeState(slider, ProjectionMode.AGENT);
		assertEquals(Double.valueOf(0.0), state.get(SliderState.MIN__PROP));
		assertEquals(Double.valueOf(100.0), state.get(SliderState.MAX__PROP));
		assertEquals(Double.valueOf(0.5), state.get(SliderState.STEP__PROP));
		assertEquals(Double.valueOf(12.5), state.get(FieldState.VALUE__PROP));
		assertFalse("The bounds written as text are presentation.", state.containsKey(SliderState.MIN_LABEL__PROP));
	}

	/**
	 * A notice restricted to the last moments before its deadline is seen only then, and it is
	 * addressed by its name, not by the text it displays.
	 */
	public void testNoticeWithinLeadTimeOnly() {
		ReactNoticeBarControl notice = new ReactNoticeBarControl(_context, NOTICE_NAME);
		notice.setLeadMillis(Long.valueOf(60_000));
		ScriptingSession session = ScriptingSession.forRoot(panel(notice));

		notice.show(Severity.WARNING, "Ihre Sitzung endet in", Long.valueOf(System.currentTimeMillis() + 3_600_000));
		assertEquals("The notice is not displayed while more than its lead time remains.", List.of(),
			session.observe().children().get(0).children());

		notice.show(Severity.WARNING, "Ihre Sitzung endet in", Long.valueOf(System.currentTimeMillis() + 30_000));
		assertEquals(List.of("/panel/noticeBar[" + NOTICE_NAME + "]"),
			addresses(session.observe().children().get(0).children()));
	}

	/**
	 * A control whose explicit name is empty is addressed by its role alone, whatever its model is
	 * labelled with.
	 */
	public void testEmptyNameAddressesByRole() {
		ReactCompositeControl content =
			new ReactCompositeControl(_context, "Whole content of the control", "TLGroup", List.of()) {
				@Override
				public String scriptingName() {
					return "";
				}
			};
		ScriptingSession session = ScriptingSession.forRoot(panel(content));
		assertEquals(List.of("/panel/group"), addresses(session.observe().children().get(0).children()));
		assertNull(session.observe().children().get(0).children().get(0).name());
	}

	/**
	 * A command the addressed control does not have is refused in the language of the user,
	 * naming the command and the address.
	 */
	public void testUnknownCommandIsRefused() {
		ReactButtonControl button = new ReactButtonControl(_context, "Go", context -> {});
		ScriptingSession session = ScriptingSession.forRoot(panel(button));
		String address = "/panel/button[Go]";

		TopLogicException failure = CommandFailures.failure("An unknown command is refused.",
			() -> session.act(address, UNKNOWN_COMMAND, Map.of()));
		assertEquals(I18NConstants.ERROR_UNKNOWN_COMMAND__COMMAND_ADDRESS.getKey(),
			failure.getErrorKey().plain().getKey());
		assertEquals(List.of(UNKNOWN_COMMAND, address), Arrays.asList(failure.getErrorKey().arguments()));
	}

	/**
	 * An internal failure is reported as plain text by the summary in the language of the user
	 * alone, without the technical message naming internals.
	 */
	public void testInternalFailureTextHasNoTechnicalMessage() {
		String text = CommandErrors.text(new IllegalStateException("Internal problem in some.Clazz"));
		assertEquals(Resources.getInstance().getString(I18NConstants.ERROR_COMMAND_FAILED), text);
	}

	private TableViewControl<String> table(boolean grouped) {
		List<Column<String, ?>> columns = List.<Column<String, ?>> of(
			DefaultColumn.<String, String> builder(NAME, row -> row).build(),
			DefaultColumn.<String, String> builder(STATUS, row -> row.substring(0, 1))
				.renderer(value -> new CellContent.Raw(
					(CellControlFactory) context -> new ReactTextControl(context, value)))
				.build());
		ListRowSource<String> source = new ListRowSource<>(new ArrayList<>(List.of("a1", "a2", "b1")), columns);
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.SINGLE));
		if (grouped) {
			state.setGrouping(new GroupSpec(List.of(STATUS)));
		}
		TableViewControl<String> table =
			new TableViewControl<>(_context, new DefaultTableView<>(columns, source, state), false);
		table.executeClientCommand("scroll", Map.of(ScrollArguments.START, 0, ScrollArguments.COUNT, 10));
		return table;
	}

	private static List<String> commands(TableViewControl<String> table) {
		List<String> result = new ArrayList<>();
		ScriptingNodeView node = ScriptingTreeProjector.project(table, "/table", ProjectionMode.AGENT);
		for (ScriptingAction action : node.actions()) {
			result.add(action.command());
		}
		return result;
	}

	private ReactControl panel(ReactControl... children) {
		return new ReactCompositeControl(_context, null, "TLPanel", List.of(children));
	}

	private static List<String> addresses(List<ScriptingNodeView> nodes) {
		List<String> result = new ArrayList<>();
		for (ScriptingNodeView node : nodes) {
			result.add(node.address());
		}
		return result;
	}

	/**
	 * The test suite, started with the resource bundles the controls need, the type index the
	 * commands are bound with, and the {@link ModelResolver} the rows are named with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObservationContent.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
