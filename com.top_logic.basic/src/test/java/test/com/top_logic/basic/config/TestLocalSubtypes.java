/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.ConfigurationSchemaConstants.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Test case for the {@link Subtypes} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLocalSubtypes extends AbstractSubtypesTest {

	public interface A extends ConfigurationItem {

		/**
		 * @see #getId()
		 */
		String ID_PROPERTY = "id";

		/**
		 * @see #getCommon()
		 */
		String COMMON = "common";

		@Name(ID_PROPERTY)
		String getId();

		@Name(COMMON)
		String getCommon();
	}

	public interface B extends A {
		String getB1();
	}

	public interface C extends A {
		String getC1();
	}

	public interface D extends C {
		String getD1();
	}

	public interface X extends ConfigurationItem {

		@EntryTag("a")
		@Subtypes({
			@Subtype(tag = "b", type = B.class),
			@Subtype(tag = "c", type = C.class),
			@Subtype(tag = "d", type = D.class) })
		List<A> getListA();

		@EntryTag("a")
		@Subtypes({
			@Subtype(tag = "b", type = B.class),
			@Subtype(tag = "c", type = C.class),
			@Subtype(tag = "d", type = D.class) })
		@Key(A.ID_PROPERTY)
		Map<String, A> getMapA();

	}

	@Override
	protected String aClass() {
		return A.class.getName();
	}

	@Override
	protected String bClass() {
		return B.class.getName();
	}

	@Override
	protected String cClass() {
		return C.class.getName();
	}

	@Override
	protected String dClass() {
		return D.class.getName();
	}

	@Override
	protected Class<? extends ConfigurationItem> containerClass() {
		return X.class;
	}

	public void testOverrideWithCompatibleType() throws ConfigurationException {
		doTestOverrideWithCompatibleType(false);
	}

	public void testOverrideWithCompatibleTypeExplicit() throws ConfigurationException {
		doTestOverrideWithCompatibleType(true);
	}

	private void doTestOverrideWithCompatibleType(boolean explicit) throws ConfigurationException {
		String xml0 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<d id='a1' c1='c1' d1='d1'/>"
			+ "</map-a>"
			+ "</x>";

		X x1 = (X) readX(xml0);
		D d1 = (D) x1.getMapA().get("a1");
		assertEquals("a1", d1.getId());
		assertEquals("c1", d1.getC1());
		assertEquals("d1", d1.getD1());

		String typeAnnotation = explicit ? "config:interface='" + D.class.getName() + "' " : "";
		String xml1 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<c id='a1' c1='c1'/>"
			+ "</map-a>"
			+ "</x>";
		String xml2 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<d " + typeAnnotation + "id='a1' d1='d1'/>"
			+ "</map-a>"
			+ "</x>";

		X x2 = (X) readX(xml1, xml2);
		D d2 = (D) x2.getMapA().get("a1");
		assertEquals("a1", d2.getId());
		assertEquals("c1", d2.getC1());
		assertEquals("d1", d2.getD1());
	}

	public void testOverrideWithIncompatibleType() throws ConfigurationException {
		doTestOverrideWithIncompatibleType(false);
	}

	public void testOverrideWithIncompatibleTypeExplicit() throws ConfigurationException {
		doTestOverrideWithIncompatibleType(true);
	}

	private void doTestOverrideWithIncompatibleType(boolean explicit) throws ConfigurationException {
		String xml0 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<c id='a1' common='common' c1='c1'/>"
			+ "</map-a>"
			+ "</x>";

		X x1 = (X) readX(xml0);
		C d1 = (C) x1.getMapA().get("a1");
		assertEquals("a1", d1.getId());
		assertEquals("common", d1.getCommon());
		assertEquals("c1", d1.getC1());

		String typeAnnotation = explicit ? "config:interface='" + C.class.getName() + "' " : "";

		String xml1 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<b id='a1' common='common' b1='dont-care' />"
			+ "</map-a>"
			+ "</x>";
		String xml2 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<c " + typeAnnotation + "id='a1' c1='c1'/>"
			+ "</map-a>"
			+ "</x>";

		X x2 = (X) readX(xml1, xml2);
		C b2 = (C) x2.getMapA().get("a1");
		assertEquals("a1", b2.getId());
		assertEquals("common", b2.getCommon());
		assertEquals("c1", b2.getC1());
	}

	public interface ScenarioTypeMap extends ConfigurationItem {

		@Name("entries")
		@Key(ScenarioTypeEntry.PROPERTY_NAME_KEY)
		@Subtypes({
			@Subtype(tag = "special-a", type = ScenarioTypeSpecialEntryA.class),
			@Subtype(tag = "special-b", type = ScenarioTypeSpecialEntryB.class),
			@Subtype(tag = "self-specializing", type = ScenarioTypeSelfSpecializing.class),
		})
		Map<String, ScenarioTypeEntry> getMap();

	}

	public interface ScenarioTypeList extends ConfigurationItem {

		@Name("entries")
		@Key(ScenarioTypeEntry.PROPERTY_NAME_KEY)
		@Subtypes({
			@Subtype(tag = "special-a", type = ScenarioTypeSpecialEntryA.class),
			@Subtype(tag = "special-b", type = ScenarioTypeSpecialEntryB.class),
			@Subtype(tag = "self-specializing", type = ScenarioTypeSelfSpecializing.class),
		})
		List<ScenarioTypeEntry> getList();

	}

	public interface ScenarioTypeEntry extends ConfigurationItem {

		String PROPERTY_NAME_KEY = "key";

		@Name(PROPERTY_NAME_KEY)
		String getKey();

		int getCommon();

	}

	public interface ScenarioTypeSpecialEntryA extends ScenarioTypeEntry {

		/** Conflicts with {@link ScenarioTypeSpecialEntryB#getConflict()} */
		int getConflict();

	}

	public interface ScenarioTypeVerySpecialEntryA extends ScenarioTypeSpecialEntryA {

		int getVerySpecial();

	}

	public interface ScenarioTypeSpecialEntryB extends ScenarioTypeEntry {

		/** Conflicts with {@link ScenarioTypeSpecialEntryA#getConflict()} */
		int getConflict();

	}

	public interface ScenarioTypeSelfSpecializing extends
			ScenarioTypeSpecialEntryA, PolymorphicConfiguration<ScenarioTypeSelfSpecializingImpl> {

		@Override
		@ClassDefault(ScenarioTypeSelfSpecializeResultImpl.class)
		public Class<? extends ScenarioTypeSelfSpecializingImpl> getImplementationClass();

	}

	public static class ScenarioTypeSelfSpecializingImpl implements ConfiguredInstance<ScenarioTypeSelfSpecializing> {

		private final ScenarioTypeSelfSpecializing _config;

		@SuppressWarnings("unused")
		public ScenarioTypeSelfSpecializingImpl(InstantiationContext context, ScenarioTypeSelfSpecializing config) {
			_config = config;
		}

		@Override
		public ScenarioTypeSelfSpecializing getConfig() {
			return _config;
		}

	}

	public interface ScenarioTypeSelfSpecializeResult extends ScenarioTypeSelfSpecializing {

		int getVerySpecial();

	}

	public static class ScenarioTypeSelfSpecializeResultImpl extends ScenarioTypeSelfSpecializingImpl {

		public ScenarioTypeSelfSpecializeResultImpl(
				InstantiationContext context, ScenarioTypeSelfSpecializeResult config) {
			super(context, config);
		}

	}

	@SuppressWarnings("unchecked")
	public void testMapStandardTag() throws ConfigurationException {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		ScenarioTypeMap item = read(createDescriptorMap(ScenarioTypeMap.class), configXml);
		ScenarioTypeEntry innerConfig = item.getMap().get("example");
		assertEquals(ScenarioTypeEntry.class, innerConfig.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testListStandardTag() throws ConfigurationException {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		ScenarioTypeList item = read(createDescriptorMap(ScenarioTypeList.class), configXml);
		ScenarioTypeEntry innerConfig = item.getList().get(0);
		assertEquals(ScenarioTypeEntry.class, innerConfig.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testMapTypeSpecializationViaDefaultImplClass() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <self-specializing key='example' very-special='2' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		ScenarioTypeMap item = read(createDescriptorMap(ScenarioTypeMap.class), baseConfig, incrementConfig);
		ScenarioTypeEntry innerConfig = item.getMap().get("example");
		assertEquals(ScenarioTypeSelfSpecializeResult.class, innerConfig.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testListTypeSpecializationViaDefaultImplClass() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <self-specializing key='example' very-special='2' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		ScenarioTypeList item = read(createDescriptorMap(ScenarioTypeList.class), baseConfig, incrementConfig);
		ScenarioTypeEntry innerConfig = item.getList().get(0);
		assertEquals(ScenarioTypeSelfSpecializeResult.class, innerConfig.getConfigurationInterface());
	}

	public static class ScenarioTypeSpecialImpl extends ScenarioTypeSelfSpecializingImpl {

		public ScenarioTypeSpecialImpl(InstantiationContext context, ScenarioTypeSpecialImplConfig config) {
			super(context, config);
		}

	}

	public interface ScenarioTypeSpecialImplConfig extends ScenarioTypeSelfSpecializing {

		int getOtherSpecial();

	}

	@SuppressWarnings("unchecked")
	public void testMapTypeSpecializationViaExplicitImplClass() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <self-specializing key='example' other-special='2' class='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeSpecialImpl' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		ScenarioTypeMap item = read(createDescriptorMap(ScenarioTypeMap.class), baseConfig, incrementConfig);
		ScenarioTypeEntry innerConfig = item.getMap().get("example");
		assertEquals(ScenarioTypeSpecialImplConfig.class, innerConfig.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testListTypeSpecializationViaExplicitImplClass() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <entry key='example' common='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <self-specializing key='example' other-special='2' class='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeSpecialImpl' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		ScenarioTypeList item = read(createDescriptorMap(ScenarioTypeList.class), baseConfig, incrementConfig);
		ScenarioTypeEntry innerConfig = item.getList().get(0);
		assertEquals(ScenarioTypeSpecialImplConfig.class, innerConfig.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testMapDropConflictingPropertiesOnTypeChange() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' conflict='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-b key='example' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeMap.class);
		ScenarioTypeMap item = read(globalDescriptors, baseConfig, incrementConfig);
		ScenarioTypeSpecialEntryB innerConfig = (ScenarioTypeSpecialEntryB) item.getMap().get("example");
		assertEquals(0, innerConfig.getConflict());
	}

	@SuppressWarnings("unchecked")
	public void testListDropConflictingPropertiesOnTypeChange() throws ConfigurationException {
		String baseConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' conflict='1' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		String incrementConfig = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-b key='example' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeList.class);
		ScenarioTypeList item = read(globalDescriptors, baseConfig, incrementConfig);
		ScenarioTypeSpecialEntryB innerConfig = (ScenarioTypeSpecialEntryB) item.getList().get(0);
		assertEquals(0, innerConfig.getConflict());
	}

	@SuppressWarnings("unchecked")
	public void testMapTagAndConfigInterfaceAttributeConflict() {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' config:interface='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeSpecialEntryB' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeMap.class);
		try {
			read(globalDescriptors, configXml);
		} catch (Throwable ex) {
			String message =
				"Conflict of special tag name and 'config:interface' attribute failed for the wrong reason.";
			BasicTestCase.assertErrorMessage(message, Pattern.compile("is not a subtype of"), ex);
			// Good
			return;
		}
		fail("Conflict of special tag name and 'config:interface' attribute did not cause an error.");
	}

	@SuppressWarnings("unchecked")
	public void testListTagAndConfigInterfaceAttributeConflict() {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' config:interface='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeSpecialEntryB' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeList.class);
		try {
			read(globalDescriptors, configXml);
		} catch (Throwable ex) {
			String message =
				"Conflict of special tag name and 'config:interface' attribute failed for the wrong reason.";
			BasicTestCase.assertErrorMessage(message, Pattern.compile("is not a subtype of"), ex);
			// Good
			return;
		}
		fail("Conflict of special tag name and 'config:interface' attribute did not cause an error.");
	}

	@SuppressWarnings("unchecked")
	public void testMapConfigInterfaceAttributeSpecializesTag() throws ConfigurationException {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeMap xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' config:interface='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeVerySpecialEntryA' very-special='3' />"
			+ "  </entries>"
			+ "</ScenarioTypeMap>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeMap.class);
		ScenarioTypeMap outerItem = read(globalDescriptors, configXml);
		ScenarioTypeEntry entry = outerItem.getMap().get("example");
		assertEquals(ScenarioTypeVerySpecialEntryA.class, entry.getConfigurationInterface());
	}

	@SuppressWarnings("unchecked")
	public void testListConfigInterfaceAttributeSpecializesTag() throws ConfigurationException {
		String configXml = "<?xml version='1.0' encoding='utf-8'?>"
			+ "<ScenarioTypeList xmlns:config='http://www.top-logic.com/ns/config/6.0'>"
			+ "  <entries>"
			+ "    <special-a key='example' config:interface='"
			+ "test.com.top_logic.basic.config.TestLocalSubtypes$ScenarioTypeVerySpecialEntryA' very-special='3' />"
			+ "  </entries>"
			+ "</ScenarioTypeList>";
		Map<String, ConfigurationDescriptor> globalDescriptors = createDescriptorMap(ScenarioTypeList.class);
		ScenarioTypeList outerItem = read(globalDescriptors, configXml);
		ScenarioTypeEntry entry = outerItem.getList().get(0);
		assertEquals(ScenarioTypeVerySpecialEntryA.class, entry.getConfigurationInterface());
	}

}
