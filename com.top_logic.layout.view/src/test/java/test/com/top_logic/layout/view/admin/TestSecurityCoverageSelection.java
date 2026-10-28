/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.layout.view.admin.SecurityCoverageTable;
import com.top_logic.layout.view.admin.SecurityCoverageTable.CoverageTree;
import com.top_logic.layout.view.admin.SecurityCoverageTable.Detail;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the selection of the {@link SecurityCoverageTable}: a selected module node stands for
 * the module, a selected type node for its analysis and its marks.
 */
@SuppressWarnings("javadoc")
public class TestSecurityCoverageSelection extends TestCase {

	/** Name of an operation no command group is registered for. */
	private static final String UNKNOWN_OPERATION = "TestNoSuchOperation";

	/** The place checking {@link #UNKNOWN_OPERATION}. */
	private static final String PLACE = "test/unattached.view.xml:7";

	private TLModule _module;

	private TLClass _type;

	private TypeCoverage _row;

	private ViewChannel _selection;

	private ViewChannel _typeChannel;

	private ViewChannel _moduleChannel;

	private Detail _detail;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLModelImpl model = new TLModelImpl();
		_module = TLModelUtil.addModule(model, "test.coverage");
		_type = TLModelUtil.addClass(_module, "Covered");
		_row = new TypeCoverage(_type, null, null, null, List.of(), Set.of(), List.of(), List.of(), List.of());
		_selection = new DefaultViewChannel("selection");
		_typeChannel = new DefaultViewChannel("type");
		_moduleChannel = new DefaultViewChannel("module");
		_detail = new Detail(_selection, _typeChannel, null, null, null, null, _moduleChannel);
	}

	public void testSelectedModuleClearsTheType() {
		_detail.show(_type, rows());
		_detail.show(_module, rows());

		assertSame(_module, _moduleChannel.get());
		assertNull("No type is selected along with a module.", _selection.get());
		assertNull(_typeChannel.get());
	}

	public void testSelectedTypeClearsTheModule() {
		_detail.show(_module, rows());
		_detail.show(_type, rows());

		assertSame(_row, _selection.get());
		assertSame(_type, _typeChannel.get());
		assertNull("No module is selected along with a type.", _moduleChannel.get());
	}

	public void testNothingSelectedClearsAll() {
		_detail.show(_type, rows());
		_detail.show(null, rows());

		assertNull(_selection.get());
		assertNull(_typeChannel.get());
		assertNull(_moduleChannel.get());
	}

	public void testMarkOfTheTypeItselfCanBeDropped() {
		ViewChannel internal = new DefaultViewChannel("internal");
		Detail detail = new Detail(null, null, internal, null, null, null, null);
		TypeCoverage row =
			new TypeCoverage(_type, null, _type, null, List.of(), Set.of(), List.of(), List.of(), List.of());

		detail.show(_type, Map.of(_type, row)::get);
		assertEquals(Boolean.TRUE, internal.get());
	}

	public void testInheritedMarkNamesItsOrigin() {
		ViewChannel internal = new DefaultViewChannel("internal");
		Detail detail = new Detail(null, null, internal, null, null, null, null);
		TypeCoverage row =
			new TypeCoverage(_type, null, _module, null, List.of(), Set.of(), List.of(), List.of(), List.of());

		detail.show(_type, Map.of(_type, row)::get);
		assertSame("The module declaring the mark, which cannot be dropped for the type.", _module, internal.get());
	}

	public void testNoMark() {
		ViewChannel internal = new DefaultViewChannel("internal");
		Detail detail = new Detail(null, null, internal, null, null, null, null);

		detail.show(_type, rows());
		assertEquals(Boolean.FALSE, internal.get());
		detail.show(null, rows());
		assertNull("Nothing selected.", internal.get());
	}

	/**
	 * The findings belonging to no analyzed type follow the modules in a group of their own.
	 */
	public void testUnattachedFindingsFormTheirOwnGroup() {
		CoverageFinding finding = unattachedFinding();
		CoverageTree tree = new CoverageTree();
		tree.update(List.of(_row, finding));

		assertEquals(List.of(_module, SecurityCoverageTable.UNATTACHED), tree.roots());
		assertEquals(List.of(_type), tree.children(_module));
		assertEquals(List.of(finding), tree.children(SecurityCoverageTable.UNATTACHED));
		assertFalse(tree.isLeaf(SecurityCoverageTable.UNATTACHED));
		assertTrue(tree.isLeaf(finding));
		assertSame("A finding is the row of its own node.", finding, tree.businessObject(finding));
	}

	public void testNoGroupWithoutUnattachedFindings() {
		CoverageTree tree = new CoverageTree();
		tree.update(List.of(_row));

		assertEquals(List.of(_module), tree.roots());
	}

	/**
	 * A selected unattached finding is described by its problem and its solutions, and no type or
	 * module is selected along with it.
	 */
	public void testSelectedUnattachedFinding() {
		ViewChannel findings = new DefaultViewChannel("findings");
		Detail detail = new Detail(_selection, _typeChannel, null, null, findings, null, _moduleChannel);
		CoverageFinding finding = unattachedFinding();

		detail.show(_type, rows());
		detail.show(finding, rows());

		assertNull(_selection.get());
		assertNull(_typeChannel.get());
		assertNull(_moduleChannel.get());
		String html = (String) findings.get();
		assertNotNull("The finding is described.", html);
		assertTrue(html, html.contains(UNKNOWN_OPERATION));
		assertTrue("The place checking the operation is named: " + html, html.contains(PLACE));

		detail.show(null, rows());
		assertNull(findings.get());
	}

	private static CoverageFinding unattachedFinding() {
		OperationUse use = OperationUse.onUnknown(OperationUse.Kind.COMMAND, UNKNOWN_OPERATION, PLACE);
		return CoverageFinding.unknownOperation(null, UNKNOWN_OPERATION, List.of(use));
	}

	private Function<Object, TypeCoverage> rows() {
		return Map.of(_type, _row)::get;
	}

	/**
	 * Test suite requiring the {@link ResourcesModule}, which writes the description of a finding.
	 */
	public static Test suite() throws ModuleException {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestSecurityCoverageSelection.class,
			TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
