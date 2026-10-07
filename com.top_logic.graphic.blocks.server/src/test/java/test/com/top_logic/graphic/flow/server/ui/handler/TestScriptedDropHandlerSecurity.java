/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.ui.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.providers.AbstractDropSecurityTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graphic.flow.data.DropRegion;
import com.top_logic.graphic.flow.server.ui.handler.ScriptedDropHandler;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DragSourceSPI;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * Test of the security check of the {@link ScriptedDropHandler}.
 * 
 * <p>
 * The drop handlers are configured in the layout {@code TestScriptedDropHandlerSecurity_layout.xml}.
 * Their scripts throw an exception, when they are executed.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestScriptedDropHandlerSecurity
		extends AbstractDropSecurityTest<TestScriptedDropHandlerSecurity.DropTestComponent> {

	private static final String CONFIG_FILE = "TestScriptedDropHandlerSecurity.xml";

	@Override
	protected TLObject newTarget(String name) {
		return TestPerson.createPerson("dropHandlerSec_" + name);
	}

	/**
	 * The drop onto a diagram element is checked on the element's business object.
	 */
	public void testDropOntoElement() {
		ScriptedDropHandler handler = _component.handler("script");

		assertScriptExecuted(() -> handler.onDrop(region(_allowed), dndData(_denied)));
		assertRefused(() -> handler.onDrop(region(_denied), dndData(_allowed)));
	}

	/**
	 * A technical tree node as business object of the diagram element is checked on its business
	 * object.
	 */
	public void testDropOntoElementUnwrapsTreeNode() {
		ScriptedDropHandler handler = _component.handler("script");

		assertScriptExecuted(() -> handler.onDrop(region(node(_allowed)), dndData(_denied)));
		assertRefused(() -> handler.onDrop(region(node(_denied)), dndData(_allowed)));
	}

	/**
	 * Without a business object of the diagram element, the security is checked on the model of the
	 * component.
	 */
	public void testDropWithoutBusinessObjectUsesComponentModel() {
		ScriptedDropHandler handler = _component.handler("script");

		_component.setModel(_allowed);
		assertScriptExecuted(() -> handler.onDrop(region(null), dndData(_denied)));

		_component.setModel(_denied);
		assertRefused(() -> handler.onDrop(region(null), dndData(_allowed)));
	}

	/**
	 * A handler without script is checked as well, since its post-drop actions may modify.
	 */
	public void testDropWithoutScript() {
		ScriptedDropHandler handler = _component.handler("no-script");

		handler.onDrop(region(_allowed), dndData(_denied));
		assertRefused(() -> handler.onDrop(region(_denied), dndData(_allowed)));
	}

	private static DropRegion region(Object businessObject) {
		return DropRegion.create().setUserObject(businessObject);
	}

	private static DndData dndData(Object dragged) {
		return new DndData(new TestDragSource(), List.of(dragged), x -> null);
	}

	/**
	 * {@link DragSourceSPI} without model.
	 */
	private static final class TestDragSource implements DragSourceSPI {

		@Override
		public Object getDragSourceModel() {
			return null;
		}

		@Override
		public java.util.Collection<?> getDragData(String dataId) {
			return List.of();
		}

		@Override
		public com.top_logic.basic.col.Maybe<? extends com.top_logic.layout.scripting.recorder.ref.ModelName> getDragDataName(
				Object dragSource, String dataId) {
			return com.top_logic.basic.col.Maybe.none();
		}

	}

	/**
	 * {@link BoundComponent} providing the drop handlers under test.
	 */
	public static class DropTestComponent extends BoundComponent {

		/**
		 * Configuration options for {@link DropTestComponent}.
		 */
		public interface Config extends BoundComponent.Config {

			@Name("handlers")
			@EntryTag("handler")
			@Key(NamedConfiguration.NAME_ATTRIBUTE)
			Map<String, ScriptedDropHandler.Config<?>> getHandlers();

		}

		private final Map<String, ScriptedDropHandler> _handlers = new HashMap<>();

		/**
		 * Creates a {@link DropTestComponent} from configuration.
		 */
		public DropTestComponent(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
			for (ScriptedDropHandler.Config<?> handler : config.getHandlers().values()) {
				_handlers.put(handler.getName(), context.getInstance(handler));
			}
		}

		@Override
		protected boolean supportsInternalModel(Object object) {
			return true;
		}

		ScriptedDropHandler handler(String name) {
			return _handlers.get(name);
		}

	}

	public static Test suite() {
		return suite(TestScriptedDropHandlerSecurity.class, CONFIG_FILE);
	}

}
