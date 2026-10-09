/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.admin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.layout.view.admin.SecurityCoverageTable;
import com.top_logic.layout.view.admin.SecurityCoverageTable.Detail;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.GroupKey;

/**
 * Test for the selection of the {@link SecurityCoverageTable}: a selected group header stands for
 * its module, a selected row for its type and its marks.
 */
@SuppressWarnings("javadoc")
public class TestSecurityCoverageSelection extends TestCase {

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

	public void testGroupOfAModuleStandsForTheModule() {
		assertSame(_module, SecurityCoverageTable.moduleOf(new GroupKey(List.of(_module))));
	}

	public void testOtherKeysStandForNoModule() {
		assertNull("A type row.", SecurityCoverageTable.moduleOf(_type));
		assertNull("Nothing selected.", SecurityCoverageTable.moduleOf(null));
		assertNull("A group of another column.", SecurityCoverageTable.moduleOf(new GroupKey(List.of("other"))));
		assertNull("A group of several columns.",
			SecurityCoverageTable.moduleOf(new GroupKey(List.of(_module, "other"))));
	}

	public void testSelectedModuleClearsTheType() {
		_detail.show(_type, rows());
		_detail.show(new GroupKey(List.of(_module)), rows());

		assertSame(_module, _moduleChannel.get());
		assertNull("No type is selected along with a module.", _selection.get());
		assertNull(_typeChannel.get());
	}

	public void testSelectedTypeClearsTheModule() {
		_detail.show(new GroupKey(List.of(_module)), rows());
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

		detail.show(_type, Map.of(_type, row));
		assertEquals(Boolean.TRUE, internal.get());
	}

	public void testInheritedMarkNamesItsOrigin() {
		ViewChannel internal = new DefaultViewChannel("internal");
		Detail detail = new Detail(null, null, internal, null, null, null, null);
		TypeCoverage row =
			new TypeCoverage(_type, null, _module, null, List.of(), Set.of(), List.of(), List.of(), List.of());

		detail.show(_type, Map.of(_type, row));
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

	private Map<Object, TypeCoverage> rows() {
		return Map.of(_type, _row);
	}

}
