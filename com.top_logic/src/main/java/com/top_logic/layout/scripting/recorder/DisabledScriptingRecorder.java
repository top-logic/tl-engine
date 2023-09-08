/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.structure.Expandable.ExpansionState;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ScriptingRecorder} that ignores all events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DisabledScriptingRecorder extends ScriptingRecorder {

	/**
	 * Creates a new {@link DisabledScriptingRecorder}.
	 */
	public DisabledScriptingRecorder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean hasVetoImpl(FollowupActionRecording followupActionRecording) {
		// The DisabledScriptingRecorder should never record anything.
		return true;
	}

	@Override
	protected Event recordCommandImpl(DisplayContext context, CommandHandler command, LayoutComponent component,
			Map<String, Object> arguments) {
		return null;
	}

	@Override
	protected void recordAwaitProgressImpl(LayoutComponent component) {
		// Ignore.
	}

	@Override
	protected void recordFieldInputImpl(FormField field, Object newValue) {
		// Ignore.
	}

	@Override
	protected void recordFieldRawInputImpl(AbstractFormField field, Object newValue) {
		// Ignore.
	}

	@Override
	protected void recordCollapseToolbarImpl(ModelName toolbarOwner, ExpansionState state) {
		// Ignore.
	}

	@Override
	protected void recordSelectionImpl(NamedModel namedModel, Object target, boolean selectState,
			SelectionChangeKind changeKind) {
		// Ignore.
	}

	@Override
	protected void recordTabSwitchImpl(LayoutComponent tabComponent, int selectedIndex) {
		// Ignore.
	}

	@Override
	protected void pauseImpl() {
		// Ignore.
	}

	@Override
	protected void resumeImpl() {
		// Ignore.
	}

	@Override
	protected boolean isResourceInspectingImpl() {
		return false;
	}

	@Override
	protected void internalAllowFollowupAction() {
		// Ignore.
	}

	@Override
	protected <T, E1 extends Throwable, E2 extends Throwable> T withResourceInspectionImpl(
			ComputationEx2<T, E1, E2> computation) throws E1, E2 {
		return computation.run();
	}

	@Override
	protected void recordButtonCommandImpl(LayoutComponent component, ButtonControl buttonControl) {
		// Ignore.
	}

	@Override
	protected Event recordActionImpl(ApplicationAction action) {
		return null;
	}

	@Override
	protected Event recordActionImpl(Supplier<ApplicationAction> actionSupplier) {
		return null;
	}

	@Override
	protected void recordDownloadImpl(BinaryDataSource data, boolean showInline) {
		// Ignore.
	}

	@Override
	protected void recordTableFilterImpl(TableData tableData, String filterColumnName) {
		// Ignore.
	}

	@Override
	protected void recordOpenTreeFilterImpl(TableData tableData) {
		// Ignore.
	}

	@Override
	protected void recordSortTableColumnImpl(TableData table, List<SortConfig> sortOrder) {
		// Ignore.
	}

	@Override
	protected void recordSetTableColumnsImpl(TableData table, List<String> columnNames) {
		// Ignore.
	}

	@Override
	protected void recordExpandImpl(TreeData treeData, Object node, boolean expand) {
		// Ignore.
	}

	@Override
	public Iterator<Event> getEventPointer() {
		return Collections.<Event> emptyList().iterator();
	}

	@Override
	public boolean getEnabled() {
		return false;
	}

}
