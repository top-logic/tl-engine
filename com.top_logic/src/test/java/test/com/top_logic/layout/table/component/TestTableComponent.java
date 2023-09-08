/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.renderer.TestingTableRenderer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.tool.boundsec.BoundHelper;

/**
 * Test case for {@link TableComponent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTableComponent extends AbstractLayoutTest {

	private TestingTableComponent _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_table = createTable();
	}

	@Override
	protected void tearDown() throws Exception {
		_table = null;
		super.tearDown();
	}

	public void testAcceptModel() throws IOException {
		_table.setVisible(true);
		validate();
		assertNull(_table.getSelected());
		assertEquals(-1, getTableControl().getSelectedRow());

		TestingModel m1 = new TestingModel("a", "b");
		_table.setModel(m1);
		validate();
		TestingElement firstElemen = m1.getElements().get(0);
		assertEquals(firstElemen, _table.getSelected());
		assertNotEquals(-1, getTableControl().getSelectedRow());
		assertEquals(getTableControl().getTableData().getViewModel().getDisplayedRows().indexOf(firstElemen),
			getTableControl().getSelectedRow());

		TagWriter buffer = new TagWriter();
		getTableControl().getTableData().getTableModel().getColumnDescription("foo").finalRenderer()
			.write(displayContext(), buffer, firstElemen);
		assertEquals("The configured resource provider must return the component name.", _table.getName().qualifiedName(),
			buffer.toString());
	}

	public void testConfiguredTableRenderer() {
		ITableRenderer renderer = getTableControl().getRenderer();

		assertTrue(TestingTableRenderer.class.isAssignableFrom(renderer.getClass()));
	}

	private TableControl getTableControl() {
		return _table.accessGetTableControl();
	}

	/**
	 * Test that model validation terminates, even if the model builder creates an element list that
	 * contains elements that are not supported as list elements by this model builder.
	 */
	public void testValidationWhenNoSelectionIsPossible() {
		TestingModel m2 = new TestingModel("NotSupported");
		_table.setModel(m2);
		validate();
		assertNull(_table.getSelected());
		assertEquals(-1, getTableControl().getSelectedRow());
	}

	private void validate() {
		int cnt = 0;
		while (_table.validateModel(displayContext())) {
			cnt++;
			if (cnt > 2) {
				fail("Component did not validate in a reasonable number of iteration.");
			}
		}
		assertTrue(_table.isModelValid());
	}

	private TestingTableComponent createTable() throws ConfigurationException, IOException {
		LayoutComponent result = ComponentTestUtils.createComponent(TestTableComponent.class, "testComponent.xml");
		return (TestingTableComponent) result;
	}

	public static class TestingTableComponent extends TableComponent {
		/**
		 * Creates a {@link TestTableComponent.TestingTableComponent} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestingTableComponent(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		public final TableControl accessGetTableControl() {
			return super.getTableControl();
		}
	}

	public static class TestingModel {

		private final List<TestingElement> _elements = new ArrayList<>();

		public TestingModel(String... names) {
			for (String name : names) {
				_elements.add(new TestingElement(this, name));
			}
		}

		public List<TestingElement> getElements() {
			return _elements;
		}

	}

	public static class TestingElement {

		private final TestingModel _model;

		private final String _name;

		TestingElement(TestingModel model, String name) {
			_model = model;
			_name = name;
		}

		public TestingModel getModel() {
			return _model;
		}

		public String getName() {
			return _name;
		}

		@Override
		public String toString() {
			return "element(" + getName() + ")";
		}

	}

	public static class TestingBuilder implements ListModelBuilder {

		/**
		 * Singleton {@link TestingBuilder} instance.
		 */
		public static final TestingBuilder INSTANCE = new TestingBuilder();

		private TestingBuilder() {
			// Singleton constructor.
		}

		@Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
			TestingModel componentModel = (TestingModel) businessModel;
			if (componentModel == null) {
				return Collections.emptyList();
			}
			return componentModel.getElements();
		}

		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel instanceof TestingModel;
		}

		@Override
		public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
			return listElement instanceof TestingElement
				&& Character.isLowerCase(((TestingElement) listElement).getName().charAt(0));
		}

		@Override
		public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
			return ((TestingElement) listElement).getModel();
		}

	}

	public static class TestingResources implements LabelProvider {
		LayoutComponent _table;

		@CalledByReflection
		public TestingResources(InstantiationContext context, PolymorphicConfiguration<ResourceProvider> config) {
			context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _table = c);
		}

		@Override
		public String getLabel(Object object) {
			return _table.getName().qualifiedName();
		}
	}

	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestTableComponent.class,
			BoundHelper.Module.INSTANCE,
			LayoutStorage.Module.INSTANCE);
		test = KBSetup.getSingleKBTest(test);
		return suite(test);
	}

}
