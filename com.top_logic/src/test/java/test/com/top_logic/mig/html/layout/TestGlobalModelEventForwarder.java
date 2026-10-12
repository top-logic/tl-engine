/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html.layout;

import static com.top_logic.mig.html.layout.ModelEventListener.*;
import static test.com.top_logic.mig.html.layout.TestGlobalModelEventForwarder.Evt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.EnabledConfiguration;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.listener.Registration;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder.LinkRelevanceConfig;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.util.Utils;

/**
 * Test case for {@link GlobalModelEventForwarder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestGlobalModelEventForwarder extends AbstractDBKnowledgeBaseClusterTest {

	private UpdateChain _updates1;

	private UpdateChain _updates2;

	static class EventTester implements ModelListener {
		final Set<?>[] expectedEvents;

		int currentSetIndex;

		public EventTester(Set<?>... expectedEvents) {
			this.expectedEvents = expectedEvents;
		}

		@Override
		public void notifyChange(ModelChangeEvent changes) {
			Set<?> expected = getNextExpectedEvents();
			Set<Evt> actual = toEvents(changes);
			assertEquals(expected, actual);
			expected.removeAll(actual);
		}

		protected Set<Evt> toEvents(ModelChangeEvent changes) {
			Stream<Evt> creates = changes.getCreated().map(change -> new Evt(change, MODEL_CREATED));
			Stream<Evt> updates = changes.getUpdated().map(change -> new Evt(change, MODEL_MODIFIED));
			Stream<Evt> deletes = changes.getDeleted().map(change -> new Evt(change, MODEL_DELETED));
			return StreamUtilities.concat(creates, updates, deletes).collect(Collectors.toSet());
		}

		private Set<?> getNextExpectedEvents() {
			while (true) {
				if (currentSetIndex >= expectedEvents.length) {
					return Collections.emptySet();
				}
				Set<?> expected = expectedEvents[currentSetIndex];
				if (!expected.isEmpty()) {
					return expected;
				}

				currentSetIndex++;
			}
		}

		public Collection<?> getPendingEvents() {
			return getNextExpectedEvents();
		}
	}

	static class Evt {

		private final Object _model;

		private final int _eventType;

		Evt(Object model, int eventType) {
			_model = model;
			_eventType = eventType;
		}

		@Override
		public String toString() {
			return "Evt(" + typeName(_eventType) + ", " + _model + ")";
		}

		private String typeName(int eventType) {
			switch (eventType) {
				case MODEL_CREATED:
					return "created";
				case MODEL_MODIFIED:
					return "modified";
				case MODEL_DELETED:
					return "deleted";
				default:
					return "unknown(" + eventType + ")";
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Evt)) {
				return false;
			}
			return equalsEvt((Evt) obj);
		}

		private boolean equalsEvt(Evt obj) {
			return Utils.equals(_model, obj._model) && _eventType == obj._eventType;
		}

		@Override
		public int hashCode() {
			return hashCode(_model) + 16519 * _eventType;
		}

		public static TestGlobalModelEventForwarder.Evt evt1(Wrapper obj, int eventType) {
			return new TestGlobalModelEventForwarder.Evt(obj, eventType);
		}

		public static TestGlobalModelEventForwarder.Evt evt2(Wrapper obj, int eventType) throws UnknownTypeException {
			return new TestGlobalModelEventForwarder.Evt(onNode2(obj), eventType);
		}

		private static int hashCode(Object model) {
			return model != null ? model.hashCode() : 0;
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_updates1 = kb().getUpdateChain();
		_updates2 = kbNode2().getUpdateChain();
	}

	@Override
	protected void tearDown() throws Exception {
		_updates1 = null;
		_updates2 = null;

		super.tearDown();
	}

	/**
	 * Single create event.
	 */
	public void testCreate() throws KnowledgeBaseException, RefetchTimeout, UnknownTypeException {
		BObj b1;
		Transaction tx = begin();
		{
			b1 = BObj.newBObj("b1");
			tx.commit();
		}

		assertEvents1(set(evt1(b1, MODEL_CREATED)));

		kbNode2().refetch();
		assertEvents2(set(evt2(b1, MODEL_CREATED)));
	}

	/**
	 * Updating an object by modifying links.
	 */
	public void testLinkUpdate() throws KnowledgeBaseException, UnknownTypeException, RefetchTimeout {
		BObj b1;
		BObj b2;
		BObj b3;
		BObj b4;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			b4 = BObj.newBObj("b4");
			tx1.commit();
			kbNode2().refetch();
		}

		assertEvents1(set(evt1(b1, MODEL_CREATED), evt1(b2, MODEL_CREATED), evt1(b3, MODEL_CREATED),
			evt1(b4, MODEL_CREATED)));

		assertEvents2(set(evt2(b1, MODEL_CREATED), evt2(b2, MODEL_CREATED), evt2(b3, MODEL_CREATED),
			evt2(b4, MODEL_CREATED)));

		// Adding associations.

		Transaction tx2 = begin();
		{
			b1.addAB(b2);
			b3.addBC(b4);
			tx2.commit();
			kbNode2().refetch();
		}

		Map<String, LinkRelevanceConfig> relevance1 =
			new MapBuilder<String, LinkRelevanceConfig>()
				.putAll(defaultRelevance())
				.put(BC_NAME, destination(BC_NAME))
				.toMap();

		assertEvents1(relevance1, set(evt1(b1, MODEL_MODIFIED), evt1(b4, MODEL_MODIFIED)));
		assertEvents2(relevance1, set(evt2(b1, MODEL_MODIFIED), evt2(b4, MODEL_MODIFIED)));

		Transaction tx3 = begin();
		{
			b1.addAB(b3);
			b2.addBC(b4);
			tx3.commit();
			kbNode2().refetch();
		}

		Map<String, LinkRelevanceConfig> relevance2 =
			new MapBuilder<String, LinkRelevanceConfig>()
				.putAll(defaultRelevance())
				.put(AB_NAME, both(AB_NAME))
				.put(BC_NAME, none(BC_NAME))
				.toMap();

		assertEvents1(relevance2, set(evt1(b1, MODEL_MODIFIED), evt1(b3, MODEL_MODIFIED)));
		assertEvents2(relevance2, set(evt2(b1, MODEL_MODIFIED), evt2(b3, MODEL_MODIFIED)));

		// Removing associations.

		Transaction tx4 = begin();
		{
			b1.removeAB(b2);
			b3.removeBC(b4);
			tx4.commit();
			kbNode2().refetch();
		}

		assertEvents1(relevance1, set(evt1(b1, MODEL_MODIFIED), evt1(b4, MODEL_MODIFIED)));
		assertEvents2(relevance1, set(evt2(b1, MODEL_MODIFIED), evt2(b4, MODEL_MODIFIED)));

		Transaction tx5 = begin();
		{
			b1.removeAB(b3);
			b2.removeBC(b4);
			tx5.commit();
			kbNode2().refetch();
		}

		assertEvents1(relevance2, set(evt1(b1, MODEL_MODIFIED), evt1(b3, MODEL_MODIFIED)));
		assertEvents2(relevance2, set(evt2(b1, MODEL_MODIFIED), evt2(b3, MODEL_MODIFIED)));
	}

	private LinkRelevanceConfig destination(String associationType) {
		return newRelevanceConfig(associationType, false, true);
	}

	private Map<String, LinkRelevanceConfig> defaultRelevance() {
		String associationTypeName = BasicTypes.ASSOCIATION_TYPE_NAME;
		return Collections.singletonMap(associationTypeName, source(associationTypeName));
	}

	private LinkRelevanceConfig source(String associationType) {
		return newRelevanceConfig(associationType, true, false);
	}

	private LinkRelevanceConfig both(String associationType) {
		return newRelevanceConfig(associationType, true, true);
	}

	private LinkRelevanceConfig none(String associationType) {
		return newRelevanceConfig(associationType, false, false);
	}

	private LinkRelevanceConfig newRelevanceConfig(String associationType, boolean source, boolean dest) {
		Map<String, Boolean> enabledByReference =
			new MapBuilder<String, Boolean>()
				.put(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, source)
				.put(DBKnowledgeAssociation.REFERENCE_DEST_NAME, dest)
				.toMap();
		AssertProtocol protocol = new AssertProtocol(getName());
		InstantiationContext instantiationContext = new DefaultInstantiationContext(protocol);
		LinkRelevanceConfig config = newRelevanceConfig(associationType, enabledByReference, instantiationContext);
		protocol.checkErrors();
		return config;
	}

	/**
	 * Creation of linked objects.
	 */
	public void testCreateLinked() throws KnowledgeBaseException, RefetchTimeout, UnknownTypeException {
		BObj b1;
		BObj b2;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b1.addAB(b2);
			tx1.commit();
		}

		assertEvents1(set(evt1(b1, MODEL_CREATED), evt1(b2, MODEL_CREATED)));

		Transaction tx2 = begin();
		{
			b1.addBC(b2);
			tx2.commit();
		}

		assertEvents1(set(evt1(b1, MODEL_MODIFIED)));

		Transaction tx3 = begin();
		{
			b1.removeBC(b2);
			tx3.commit();
		}

		assertEvents1(set(evt1(b1, MODEL_MODIFIED)));

		kbNode2().refetch();
		assertEvents2(set(evt2(b1, MODEL_CREATED), evt2(b2, MODEL_CREATED)));
	}

	/**
	 * Joining multiple events into a single update.
	 */
	public void testJoinUpdate() throws KnowledgeBaseException, RefetchTimeout, UnknownTypeException {
		BObj b1;
		BObj b2;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx1.commit();
		}

		Transaction tx2 = begin();
		{
			b1.addAB(b2);
			tx2.commit();
		}

		assertEvents1(set(evt1(b1, MODEL_CREATED), evt1(b2, MODEL_CREATED)));

		kbNode2().refetch();

		assertEvents2(set(evt2(b1, MODEL_CREATED), evt2(b2, MODEL_CREATED)));
	}

	/**
	 * A listener whose registration is disposed by another listener during the delivery of an
	 * event is not notified anymore.
	 */
	public void testDisposedDuringDeliveryNotNotified() throws KnowledgeBaseException {
		BObj b1;
		BObj b2;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx1.commit();
		}

		GlobalModelEventForwarder forwarder = newForwarder();
		forwarder.synthesizeModelEvents();

		List<String> calls = new ArrayList<>();
		Registration[] second = new Registration[1];
		forwarder.addModelListener(b1, change -> {
			calls.add("first");
			second[0].dispose();
		});
		second[0] = forwarder.addModelListener(b1, change -> calls.add("second"));

		Registration[] globalSecond = new Registration[1];
		forwarder.addModelListener(change -> {
			calls.add("global");
			globalSecond[0].dispose();
		});
		globalSecond[0] = forwarder.addModelListener(b1, change -> calls.add("globalSecond"));

		Transaction tx2 = begin();
		{
			b1.addAB(b2);
			tx2.commit();
		}
		forwarder.synthesizeModelEvents();

		assertEquals(List.of("global", "first"), calls);
		assertFalse(second[0].isActive());
		assertFalse(globalSecond[0].isActive());
	}

	/**
	 * A listener registered for multiple changed objects is notified once.
	 */
	public void testNotifiedOnceForMultipleRegistrations() throws KnowledgeBaseException {
		BObj b1;
		BObj b2;
		BObj b3;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			tx1.commit();
		}

		GlobalModelEventForwarder forwarder = newForwarder();
		forwarder.synthesizeModelEvents();

		List<ModelChangeEvent> events = new ArrayList<>();
		ModelListener listener = events::add;
		forwarder.addModelListener(b1, listener);
		forwarder.addModelListener(b2, listener);
		forwarder.addModelListener(listener);

		Transaction tx2 = begin();
		{
			b1.addAB(b3);
			b2.addAB(b3);
			tx2.commit();
		}
		forwarder.synthesizeModelEvents();

		assertEquals(1, events.size());
		assertEquals(Set.of(b1, b2), events.get(0).getUpdated().collect(Collectors.toSet()));
	}

	/**
	 * Disposing the last registration of an object ends the observation, a later registration
	 * observes the object again.
	 */
	public void testReRegisterAfterDispose() throws KnowledgeBaseException {
		BObj b1;
		BObj b2;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx1.commit();
		}

		GlobalModelEventForwarder forwarder = newForwarder();
		forwarder.synthesizeModelEvents();

		List<String> calls = new ArrayList<>();
		Registration registration = forwarder.addModelListener(b1, change -> calls.add("disposed"));
		assertTrue(registration.isActive());
		registration.dispose();
		assertFalse(registration.isActive());

		// Disposing again has no effect.
		registration.dispose();

		Transaction tx2 = begin();
		{
			b1.addAB(b2);
			tx2.commit();
		}
		forwarder.synthesizeModelEvents();
		assertEquals(List.of(), calls);

		Registration reRegistration = forwarder.addModelListener(b1, change -> calls.add("re-registered"));

		// Disposing an outdated registration does not affect the registration made later on.
		registration.dispose();
		assertTrue(reRegistration.isActive());

		Transaction tx3 = begin();
		{
			b1.removeAB(b2);
			tx3.commit();
		}
		forwarder.synthesizeModelEvents();
		assertEquals(List.of("re-registered"), calls);
	}

	/**
	 * Removing a listener ends all its registrations for the removed object.
	 */
	@SuppressWarnings("deprecation")
	public void testRemoveListener() throws KnowledgeBaseException {
		BObj b1;
		BObj b2;
		Transaction tx1 = begin();
		{
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			tx1.commit();
		}

		GlobalModelEventForwarder forwarder = newForwarder();
		forwarder.synthesizeModelEvents();

		List<String> calls = new ArrayList<>();
		ModelListener listener = change -> calls.add("removed");
		Registration first = forwarder.addModelListener(b1, listener);
		Registration second = forwarder.addModelListener(b1, listener);
		assertTrue(forwarder.removeModelListener(b1, listener));
		assertFalse(first.isActive());
		assertFalse(second.isActive());
		assertFalse(forwarder.removeModelListener(b1, listener));

		Transaction tx2 = begin();
		{
			b1.addAB(b2);
			tx2.commit();
		}
		forwarder.synthesizeModelEvents();
		assertEquals(List.of(), calls);
	}

	private GlobalModelEventForwarder newForwarder() {
		Protocol log = new AssertProtocol();
		AssociationEndRelevance endRelevance =
			MapBasedAssociationEndRelevance.newAssociationEndRelevance(log, defaultRelevance(),
				kb().getMORepository());
		log.checkErrors();
		return new GlobalModelEventForwarder(kb(), _updates1, endRelevance);
	}

	void assertEvents1(Set<?>... events) {
		assertEvents(kb(), _updates1, defaultRelevance(), events);
	}

	void assertEvents1(Map<String, LinkRelevanceConfig> relevance, Set<?>... events) {
		assertEvents(kb(), _updates1, relevance, events);
	}

	void assertEvents2(Set<?>... events) {
		assertEvents(kbNode2(), _updates2, defaultRelevance(), events);
	}

	void assertEvents2(Map<String, LinkRelevanceConfig> relevance, Set<?>... events) {
		assertEvents(kbNode2(), _updates2, relevance, events);
	}

	private static void assertEvents(KnowledgeBase kb, UpdateChain updates,
			Map<String, LinkRelevanceConfig> relevance, Set<?>... events) {
		EventTester tester = new EventTester(events);
		Protocol log = new AssertProtocol();
		AssociationEndRelevance endRelevance =
			MapBasedAssociationEndRelevance.newAssociationEndRelevance(log, relevance, kb.getMORepository());
		log.checkErrors();
		GlobalModelEventForwarder forwarder = new GlobalModelEventForwarder(kb, updates, endRelevance);
		forwarder.addModelListener(tester);
		try {
			forwarder.synthesizeModelEvents();
			assertEmpty("Missing events.", true, tester.getPendingEvents());
		} finally {
			forwarder.removeModelListener(tester);
		}
	}

	static void assertContains(String message, Collection<?> expected, Object obj) {
		if (!expected.contains(obj)) {
			String prefix = message != null ? message + " " : "";
			fail(prefix + "The object '" + obj + "' is not among the expected ones: " + expected);
		}
	}

	static Wrapper onNode2(Wrapper obj) throws UnknownTypeException {
		ObjectKey key = (ObjectKey) KBUtils.getWrappedObjectKey(obj);
		DefaultObjectKey keyNode2 =
			new DefaultObjectKey(
				key.getBranchContext(),
				key.getHistoryContext(),
				kbNode2().getMORepository().getMetaObject(key.getObjectType().getName()),
				key.getObjectName());
		KnowledgeObject koNode2 = (KnowledgeObject) kbNode2().resolveObjectKey(keyNode2);
		return WrapperFactory.getWrapper(koNode2);
	}

	private static LinkRelevanceConfig newRelevanceConfig(String type, Map<String, Boolean> enabledByReference,
			InstantiationContext instantiationContext) {
		ConfigurationDescriptor configurationDescriptor =
			TypedConfiguration.getConfigurationDescriptor(LinkRelevanceConfig.class);
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(LinkRelevanceConfig.class);
		builder.initValue(configurationDescriptor.getProperty(LinkRelevanceConfig.TYPE_NAME), type);
		Map<String, EnabledConfiguration> enabledConfigurations = new HashMap<>();
		for (Entry<String, Boolean> entry : enabledByReference.entrySet()) {
			EnabledConfiguration enabled = newEnabled(entry, instantiationContext);
			enabledConfigurations.put(entry.getKey(), enabled);
		}
		builder.initValue(configurationDescriptor.getProperty(LinkRelevanceConfig.ATTRIBUTES_NAME),
			enabledConfigurations);
		ConfigurationItem createConfig = builder.createConfig(instantiationContext);
		return (LinkRelevanceConfig) createConfig;
	}

	private static EnabledConfiguration newEnabled(Entry<String, Boolean> entry,
			InstantiationContext instantiationContext) {
		ConfigurationDescriptor enabledDescr =
			TypedConfiguration.getConfigurationDescriptor(EnabledConfiguration.class);
		ConfigBuilder enabledBuilder = TypedConfiguration.createConfigBuilder(enabledDescr);
		enabledBuilder.initValue(enabledDescr.getProperty(EnabledConfiguration.NAME_ATTRIBUTE), entry.getKey());
		enabledBuilder.initValue(enabledDescr.getProperty(EnabledConfiguration.ENABLED_VALUE), entry.getValue());
		EnabledConfiguration enabled = (EnabledConfiguration) enabledBuilder.createConfig(instantiationContext);
		return enabled;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestGlobalModelEventForwarder.class);
	}

}
