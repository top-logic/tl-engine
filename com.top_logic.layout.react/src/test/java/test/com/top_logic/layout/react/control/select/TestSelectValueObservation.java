/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that a {@link ReactDropdownSelectControl} follows the objects it displays as its value:
 * editing such an object elsewhere updates the label the field shows for it.
 *
 * <p>
 * The changes are committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} of the
 * test scenario and delivered by a {@link GlobalModelEventForwarder}, which is also the
 * {@link ModelScope} of the field's {@link ReactContext}: what the field shows here is therefore
 * what a session shows.
 * </p>
 */
public class TestSelectValueObservation extends AbstractDBKnowledgeBaseTest {

	/** The state key telling the client whether it holds the option list. */
	private static final String OPTIONS_LOADED_TRUE = "\"optionsLoaded\":true";

	/** The same key while the option list is outdated. */
	private static final String OPTIONS_LOADED_FALSE = "\"optionsLoaded\":false";

	/** The command loading the option list. */
	private static final String CMD_LOAD_OPTIONS = "loadOptions";

	/** The labels of the test objects, so that nothing else can produce them. */
	private static final LabelProvider NAMES = object -> ((BObj) object).getA1();

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The context the fields under test are displayed in. */
	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), updates, relevance);
		_context = new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"))) {
			@Override
			public ModelScope getModelScope() {
				return _scope;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_scope = null;

		super.tearDown();
	}

	/**
	 * Tests that renaming the displayed object updates the label the field shows for it.
	 */
	public void testRenamingTheDisplayedObjectUpdatesItsLabel() throws Exception {
		BObj b1 = create("b1");
		ReactDropdownSelectControl field = displayedField(b1);
		assertEquals(List.of("b1"), valueLabels(field));

		change(b1, "b1 renamed");

		assertEquals("The field shows the label the object now has.", List.of("b1 renamed"), valueLabels(field));
	}

	/**
	 * Tests that the option list, which carries the labels as well, is dropped so that the client
	 * loads it again.
	 */
	public void testRenamingTheDisplayedObjectDropsTheLoadedOptions() throws Exception {
		BObj b1 = create("b1");
		ReactDropdownSelectControl field = displayedField(b1);
		field.executeCommand(CMD_LOAD_OPTIONS, Map.of());
		assertTrue("The client holds the option list.", field.stateAsJSON().contains(OPTIONS_LOADED_TRUE));

		change(b1, "b1 renamed");

		assertTrue("The option list carrying the former label is dropped.",
			field.stateAsJSON().contains(OPTIONS_LOADED_FALSE));
	}

	/**
	 * Tests that a field that was never displayed is not refreshed.
	 */
	public void testAFieldNeverDisplayedIsNotRefreshed() throws Exception {
		BObj b1 = create("b1");
		ReactDropdownSelectControl field = field(b1);

		change(b1, "b1 renamed");

		assertEquals("A field nobody sees observes nothing.", List.of("b1"), valueLabels(field));
	}

	/**
	 * Tests that a field that left the display is not refreshed any more, and is refreshed again
	 * once it is displayed again.
	 */
	public void testADetachedFieldIsNotRefreshed() throws Exception {
		BObj b1 = create("b1");
		ReactDropdownSelectControl field = displayedField(b1);
		field.detach();

		change(b1, "b1 renamed");
		assertEquals("A field that left the display observes nothing.", List.of("b1"), valueLabels(field));

		field.attach();
		change(b1, "b1 renamed again");
		assertEquals("A field displayed again observes its value once more.", List.of("b1 renamed again"),
			valueLabels(field));
	}

	/**
	 * Tests that the observation follows a new field value: the object dropped from the value is
	 * not observed any more, the one taken up is.
	 */
	public void testTheObservationFollowsTheValue() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(b1, List.of(b1, b2), false);
		ReactDropdownSelectControl field = displayedField(model);

		model.setValue(b2);
		assertEquals(List.of("b2"), valueLabels(field));

		change(b1, "b1 renamed");
		assertEquals("The object dropped from the value is not observed any more.", List.of("b2"),
			valueLabels(field));

		change(b2, "b2 renamed");
		assertEquals("The object the value now names is observed.", List.of("b2 renamed"), valueLabels(field));
	}

	/**
	 * Tests that every object of a multi-selection is observed.
	 */
	public void testEveryObjectOfAMultiSelectionIsObserved() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(List.of(b1, b2), List.of(b1, b2), true);
		ReactDropdownSelectControl field = displayedField(model);

		change(b2, "b2 renamed");
		assertEquals("Renaming one of the selected objects updates its label.", List.of("b1", "b2 renamed"),
			valueLabels(field));

		change(b1, "b1 renamed");
		assertEquals("Renaming another one updates that label, too.", List.of("b1 renamed", "b2 renamed"),
			valueLabels(field));
	}

	/**
	 * A field displaying the given object, as the user sees it.
	 */
	private ReactDropdownSelectControl displayedField(BObj value) {
		ReactDropdownSelectControl field = field(value);
		field.attach();
		return field;
	}

	/**
	 * A field for the given model, as the user sees it.
	 */
	private ReactDropdownSelectControl displayedField(SimpleSelectFieldModel model) {
		ReactDropdownSelectControl field = field(model);
		field.attach();
		return field;
	}

	private ReactDropdownSelectControl field(BObj value) {
		return field(new SimpleSelectFieldModel(value, List.of(value), false));
	}

	private ReactDropdownSelectControl field(SimpleSelectFieldModel model) {
		return new ReactDropdownSelectControl(_context, model, NAMES, null, false);
	}

	/**
	 * The labels the given field shows for its selected objects, as the client reads them from the
	 * field's state.
	 */
	private static List<String> valueLabels(ReactDropdownSelectControl field) {
		String state = field.stateAsJSON();
		int start = state.indexOf("\"value\":[");
		assertTrue("The field state names the selected values.", start >= 0);
		String values = state.substring(start, state.indexOf(']', start));

		List<String> labels = new ArrayList<>();
		String marker = "\"label\":\"";
		for (int pos = values.indexOf(marker); pos >= 0; pos = values.indexOf(marker, pos)) {
			int labelStart = pos + marker.length();
			int labelEnd = values.indexOf('"', labelStart);
			labels.add(values.substring(labelStart, labelEnd));
			pos = labelEnd;
		}
		return labels;
	}

	/**
	 * Creates a committed {@link BObj} with the given name and delivers the pending events, so that
	 * the creation itself is not among the changes a later observation reports.
	 */
	private BObj create(String name) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
		return result;
	}

	/**
	 * Commits a new name for the given object and delivers the resulting change.
	 */
	private void change(BObj object, String name) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
	}

	/**
	 * Delivers the changes committed since the last call to the listeners of {@link #_scope}.
	 */
	private void deliverChanges() {
		_scope.synthesizeModelEvents();
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestSelectValueObservation.class);
	}

}
