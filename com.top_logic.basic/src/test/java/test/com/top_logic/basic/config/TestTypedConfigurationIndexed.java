/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Test case for the {@link Indexed} annotation in {@link TypedConfiguration}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationIndexed extends AbstractTypedConfigurationTestCase {

	public interface StringIndexedCollection extends ConfigurationItem {

		String B_BY_NAME_COLLECTION_PROPERTY = "bs-by-name-collection";

		@Key(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_COLLECTION_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_COLLECTION_PROPERTY)
		B getBByName(String name);
	}

	public interface SupertypeIndexedCollection extends ConfigurationItem {

		String B_BY_NAME_SUPERTYPE_PROPERTY = "bs-by-name-collection-supertype";

		@Key(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_SUPERTYPE_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_SUPERTYPE_PROPERTY)
		B getBByName(Object name);
	}

	public interface IntIndexedCollection extends ConfigurationItem {

		String B_BY_X_COLLECTION_PROPERTY = "bs-by-x-collection";

		@Key(B.X_PROPERTY)
		@Name(B_BY_X_COLLECTION_PROPERTY)
		Collection<B> getBsByX();

		@Indexed(collection = B_BY_X_COLLECTION_PROPERTY)
		B getBByX(int x);
	}

	public static class Impl implements ConfiguredInstance<Impl.Config> {

		private final Config _config;

		public interface Config extends PolymorphicConfiguration<Impl> {

			String S = "s";

			@Name(S)
			String getS();

			void setS(String value);

		}

		/**
		 * Creates a {@link TestTypedConfigurationIndexed.Impl} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

	}

	public interface ConfiguredInstanceIndexedCollection extends ConfigurationItem {

		String IMPLS = "impls";

		@Name(IMPLS)
		@Key(Impl.Config.S)
		Collection<Impl> getImpls();

		@Indexed(collection = IMPLS)
		Impl getImplByS(String s);

	}

	public interface StringIndexedList extends ConfigurationItem {

		String B_BY_NAME_LIST_PROPERTY = "bs-by-name-list";

		@Key(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_LIST_PROPERTY)
		List<B> getBsByNameList();

		@Indexed(collection = B_BY_NAME_LIST_PROPERTY)
		B getBByName(String name);
	}

	public interface IntIndexedList extends ConfigurationItem {

		String B_BY_X_LIST_PROPERTY = "bs-by-x-list";

		@Key(B.X_PROPERTY)
		@Name(B_BY_X_LIST_PROPERTY)
		List<B> getBsByX();

		@Indexed(collection = B_BY_X_LIST_PROPERTY)
		B getBByX(int x);
	}

	public interface StringIndexedMap extends ConfigurationItem {

		String B_BY_NAME_MAP_PROPERTY = "bs-by-name-map";

		@Key(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_MAP_PROPERTY)
		Map<String, B> getBsByName();

		@Indexed(collection = B_BY_NAME_MAP_PROPERTY)
		B getBByName(String name);
	}

	public interface IntIndexedMap extends ConfigurationItem {

		String B_BY_X_MAP_PROPERTY = "bs-by-x-map";

		@Key(B.X_PROPERTY)
		@Name(B_BY_X_MAP_PROPERTY)
		Map<Integer, B> getBsByX();

		@Indexed(collection = B_BY_X_MAP_PROPERTY)
		B getBByX(int x);
	}

	public interface IndexedBase extends ConfigurationItem {

		String B_BY_X_LIST_PROPERTY = "bs-by-x-list";

		@Key(B.X_PROPERTY)
		@Name(B_BY_X_LIST_PROPERTY)
		List<B> getBsByX();

		@Indexed(collection = B_BY_X_LIST_PROPERTY)
		B getBByX(int x);
	}

	public interface IndexedSub extends IndexedBase {
		// Pure sub interface.
	}

	public interface IndexedBaseWithSubConfigProperty extends ConfigurationItem {

		String B_BY_X_LIST_PROPERTY = "bs-by-x-list";

		@Key(B.X_PROPERTY)
		@Name(B_BY_X_LIST_PROPERTY)
		List<B> getBsByX();

		@Indexed(collection = B_BY_X_LIST_PROPERTY)
		B getBByX(int x);

		IndexedSub getSubConfigProperty();
	}

	public interface IndexedSubWithSubConfigProperty extends IndexedBaseWithSubConfigProperty {
		// Nothing needed
	}

	public interface B extends NamedConfiguration {
		String X_PROPERTY = "x";

		@Name(X_PROPERTY)
		int getX();
	}

	public interface IndexedSubTree extends ConfigurationItem {

		String SUBS_BY_X_LIST_PROPERTY = "subs-by-x-list";

		@Key(IndexedSubTreeSub.X_PROPERTY)
		@Name(SUBS_BY_X_LIST_PROPERTY)
		List<IndexedSubTreeSub> getSubsByX();

		@Indexed(collection = SUBS_BY_X_LIST_PROPERTY)
		IndexedSubTreeSub getSubByX(int x);

	}

	public interface IndexedSubTreeSub extends IndexedSubTree, NamedConfiguration {

		String X_PROPERTY = "x";

		@Name(X_PROPERTY)
		int getX();

	}

	public interface IndexedTree extends NamedConfiguration {

		String CHILDREN_BY_X_LIST_PROPERTY = "children-by-x-list";

		@Key(IndexedTree.X_PROPERTY)
		@Name(CHILDREN_BY_X_LIST_PROPERTY)
		List<IndexedTree> getChildrenByX();

		@Indexed(collection = CHILDREN_BY_X_LIST_PROPERTY)
		IndexedTree getChildByX(int x);

		String X_PROPERTY = "x";

		@Name(X_PROPERTY)
		int getX();

	}

	public void testStringIndexedCollection() {
		StringIndexedCollection a = TypedConfiguration.newConfigItem(StringIndexedCollection.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByName("b1"));
		a.update(a.descriptor().getProperty(StringIndexedCollection.B_BY_NAME_COLLECTION_PROPERTY), new ArrayList<>());
		a.getBsByName().add(b1);
		assertSame(b1, a.getBByName("b1"));

		assertNull(a.getBByName("b2"));
		a.getBsByName().add(b2);
		assertSame(b2, a.getBByName("b2"));
	}

	public void testIntIndexedCollection() {
		IntIndexedCollection a = TypedConfiguration.newConfigItem(IntIndexedCollection.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByX(1));
		a.update(a.descriptor().getProperty(IntIndexedCollection.B_BY_X_COLLECTION_PROPERTY), new ArrayList<>());
		a.getBsByX().add(b1);
		assertSame(b1, a.getBByX(1));

		assertNull(a.getBByX(2));
		a.getBsByX().add(b2);
		assertSame(b2, a.getBByX(2));
	}

	public void testConfiguredInstanceIndexedCollection() {
		ConfiguredInstanceIndexedCollection a = create(ConfiguredInstanceIndexedCollection.class);

		assertNull(a.getImplByS("b1"));

		Impl.Config b1Conf = create(Impl.Config.class);
		b1Conf.setS("b1");
		Impl b1 = getInstance(b1Conf);
		a.getImpls().add(b1);

		Impl.Config b2Conf = create(Impl.Config.class);
		b2Conf.setS("b2");
		Impl b2 = getInstance(b2Conf);
		a.getImpls().add(b2);

		assertSame(b1, a.getImplByS("b1"));
		assertSame(b2, a.getImplByS("b2"));
		assertNull(a.getImplByS("foobar"));
		assertNull(a.getImplByS(null));
	}

	public void testSupertypeIndexedCollection() {
		SupertypeIndexedCollection a = TypedConfiguration.newConfigItem(SupertypeIndexedCollection.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByName("b1"));
		a.update(a.descriptor().getProperty(SupertypeIndexedCollection.B_BY_NAME_SUPERTYPE_PROPERTY),
			new ArrayList<>());
		a.getBsByName().add(b1);
		assertSame(b1, a.getBByName("b1"));

		assertNull(a.getBByName("b2"));
		a.getBsByName().add(b2);
		assertSame(b2, a.getBByName("b2"));
	}

	public void testStringIndexedList() {
		StringIndexedList a = TypedConfiguration.newConfigItem(StringIndexedList.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByName("b1"));
		a.update(a.descriptor().getProperty(StringIndexedList.B_BY_NAME_LIST_PROPERTY), new ArrayList<>());
		a.getBsByNameList().add(b1);
		assertSame(b1, a.getBByName("b1"));

		assertNull(a.getBByName("b2"));
		a.getBsByNameList().add(b2);
		assertSame(b2, a.getBByName("b2"));
	}

	public void testIntIndexedList() {
		IntIndexedList a = TypedConfiguration.newConfigItem(IntIndexedList.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByX(1));
		a.update(a.descriptor().getProperty(IntIndexedList.B_BY_X_LIST_PROPERTY), new ArrayList<>());
		a.getBsByX().add(b1);
		assertSame(b1, a.getBByX(1));

		assertNull(a.getBByX(2));
		a.getBsByX().add(b2);
		assertSame(b2, a.getBByX(2));
	}

	public void testStringIndexedMap() {
		StringIndexedMap a = TypedConfiguration.newConfigItem(StringIndexedMap.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByName("b1"));
		a.update(a.descriptor().getProperty(StringIndexedMap.B_BY_NAME_MAP_PROPERTY), new HashMap<>());
		a.getBsByName().put(b1.getName(), b1);
		assertSame(b1, a.getBByName("b1"));

		assertNull(a.getBByName("b2"));
		a.getBsByName().put(b2.getName(), b2);
		assertSame(b2, a.getBByName("b2"));
	}

	public void testIntIndexedMap() {
		IntIndexedMap a = TypedConfiguration.newConfigItem(IntIndexedMap.class);
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(TestTypedConfigurationIndexed.B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByX(1));
		a.update(a.descriptor().getProperty(IntIndexedMap.B_BY_X_MAP_PROPERTY), new HashMap<>());
		a.getBsByX().put(b1.getX(), b1);
		assertSame(b1, a.getBByX(1));

		assertNull(a.getBByX(2));
		a.getBsByX().put(b2.getX(), b2);
		assertSame(b2, a.getBByX(2));
	}

	public void testIndexedInheritance() {
		// Note: Load IndexedSub and IndexedBase in one batch (triggered by IndexedSub). This tests
		// that the indexed method implementations are created even for the sub interface.
		IndexedBase a2 = TypedConfiguration.newConfigItem(IndexedSub.class);
		doTest(a2);

		IndexedBase a1 = TypedConfiguration.newConfigItem(IndexedBase.class);
		doTest(a1);
	}

	private void doTest(IndexedBase a) {
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByX(1));
		a.update(a.descriptor().getProperty(IndexedBase.B_BY_X_LIST_PROPERTY), new ArrayList<>());
		a.getBsByX().add(b1);
		assertSame(b1, a.getBByX(1));

		assertNull(a.getBByX(2));
		a.getBsByX().add(b2);
		assertSame(b2, a.getBByX(2));
	}

	public void testIndexedInheritanceWithSubConfigProperty() {
		// Note: Load IndexedSub and IndexedBase in one batch (triggered by IndexedSub). This tests
		// that the indexed method implementations are created even for the sub interface.
		IndexedBaseWithSubConfigProperty a2 = TypedConfiguration.newConfigItem(IndexedSubWithSubConfigProperty.class);
		doTest(a2);

		IndexedBaseWithSubConfigProperty a1 = TypedConfiguration.newConfigItem(IndexedBaseWithSubConfigProperty.class);
		doTest(a1);
	}

	private void doTest(IndexedBaseWithSubConfigProperty a) {
		B b1 = TypedConfiguration.newConfigItem(B.class);
		b1.update(b1.descriptor().getProperty(B.NAME_ATTRIBUTE), "b1");
		b1.update(b1.descriptor().getProperty(B.X_PROPERTY), 1);

		B b2 = TypedConfiguration.newConfigItem(B.class);
		b2.update(b2.descriptor().getProperty(B.NAME_ATTRIBUTE), "b2");
		b2.update(b2.descriptor().getProperty(B.X_PROPERTY), 2);

		assertNull(a.getBByX(1));
		a.update(a.descriptor().getProperty(IndexedBase.B_BY_X_LIST_PROPERTY), new ArrayList<>());
		a.getBsByX().add(b1);
		assertSame(b1, a.getBByX(1));

		assertNull(a.getBByX(2));
		a.getBsByX().add(b2);
		assertSame(b2, a.getBByX(2));
	}

	public void testIndexedSubTree() {
		// Note: Load IndexedSub and IndexedBase in one batch (triggered by IndexedSub). This tests
		// that the indexed method implementations are created even for the sub interface.
		IndexedSubTree a2 = TypedConfiguration.newConfigItem(IndexedSubTreeSub.class);
		doTest(a2);

		IndexedSubTree a1 = TypedConfiguration.newConfigItem(IndexedSubTree.class);
		doTest(a1);
	}

	private void doTest(IndexedSubTree a) {
		IndexedSubTreeSub sub1 = TypedConfiguration.newConfigItem(IndexedSubTreeSub.class);
		sub1.update(sub1.descriptor().getProperty(IndexedSubTreeSub.NAME_ATTRIBUTE), "b1");
		sub1.update(sub1.descriptor().getProperty(IndexedSubTreeSub.X_PROPERTY), 1);

		IndexedSubTreeSub sub2 = TypedConfiguration.newConfigItem(IndexedSubTreeSub.class);
		sub2.update(sub2.descriptor().getProperty(IndexedSubTreeSub.NAME_ATTRIBUTE), "b2");
		sub2.update(sub2.descriptor().getProperty(IndexedSubTreeSub.X_PROPERTY), 2);

		assertNull(a.getSubByX(1));
		a.update(a.descriptor().getProperty(IndexedSubTree.SUBS_BY_X_LIST_PROPERTY), new ArrayList<>());
		a.getSubsByX().add(sub1);
		assertSame(sub1, a.getSubByX(1));

		assertNull(a.getSubByX(2));
		a.getSubsByX().add(sub2);
		assertSame(sub2, a.getSubByX(2));
	}

	public void testIndexedTree() {
		doTest(TypedConfiguration.newConfigItem(IndexedTree.class));
	}

	private void doTest(IndexedTree a) {
		IndexedTree sub1 = TypedConfiguration.newConfigItem(IndexedTree.class);
		sub1.update(sub1.descriptor().getProperty(IndexedTree.NAME_ATTRIBUTE), "b1");
		sub1.update(sub1.descriptor().getProperty(IndexedTree.X_PROPERTY), 1);

		IndexedTree sub2 = TypedConfiguration.newConfigItem(IndexedTree.class);
		sub2.update(sub2.descriptor().getProperty(IndexedTree.NAME_ATTRIBUTE), "b2");
		sub2.update(sub2.descriptor().getProperty(IndexedTree.X_PROPERTY), 2);

		assertNull(a.getChildByX(1));
		a.update(a.descriptor().getProperty(IndexedTree.CHILDREN_BY_X_LIST_PROPERTY), new ArrayList<>());
		a.getChildrenByX().add(sub1);
		assertSame(sub1, a.getChildByX(1));

		assertNull(a.getChildByX(2));
		a.getChildrenByX().add(sub2);
		assertSame(sub2, a.getChildByX(2));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigurationIndexed.class);
	}

}
