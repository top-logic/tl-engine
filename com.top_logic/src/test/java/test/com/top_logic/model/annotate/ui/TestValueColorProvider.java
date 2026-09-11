/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.annotate.ui;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.TLDynamicColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.annotate.ui.ValueColorProvider;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test for {@link AnnotationValueColorProvider}.
 */
@SuppressWarnings("javadoc")
public class TestValueColorProvider extends BasicTestCase {

	private static final Color LITERAL = new Color(0x04, 0xA3, 0x8D);

	private static final String TOKEN = "support-success";

	static final String STATUS_ATTRIBUTE = "status";

	private TLModule _module;

	private TLClassifier _open;

	private TLClassifier _closed;

	private TLClassifier _unknown;

	private TLEnumeration _status;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();

		_module = TLModelUtil.addModule(model, "test");
		_status = TLModelUtil.addEnumeration(_module, "Status");
		_open = TLModelUtil.addClassifier(_status, "open");
		_open.setAnnotation(literalColor(LITERAL));
		_closed = TLModelUtil.addClassifier(_status, "closed");
		_closed.setAnnotation(tokenColor(TOKEN));
		_unknown = TLModelUtil.addClassifier(_status, "unknown");
	}

	public void testLiteralColorOfClassifier() {
		assertEquals(ValueColor.color(LITERAL), colorOf(_open));
		assertEquals("#04A38D", colorOf(_open).cssValue());
	}

	public void testTokenColorOfClassifier() {
		assertEquals(ValueColor.themeToken(TOKEN), colorOf(_closed));
		assertEquals("var(--" + TOKEN + ")", colorOf(_closed).cssValue());
	}

	public void testUnannotatedClassifierHasNoColor() {
		assertNull(colorOf(_unknown));
	}

	public void testDynamicColorOfObject() throws ConfigurationException {
		TLClass ticket = coloredClass("Ticket", StatusColor.class);

		TLObject ticket1 = newObject(ticket);
		ticket1.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		assertEquals(ValueColor.themeToken(TOKEN), colorOf(ticket1));

		TLObject ticket2 = newObject(ticket);
		ticket2.tUpdateByName(STATUS_ATTRIBUTE, _open);
		assertEquals(ValueColor.color(LITERAL), colorOf(ticket2));
	}

	public void testProviderAnsweringNoColor() throws ConfigurationException {
		TLClass ticket = coloredClass("Ticket", StatusColor.class);

		TLObject ticket1 = newObject(ticket);
		ticket1.tUpdateByName(STATUS_ATTRIBUTE, _unknown);
		assertNull("An uncolored classifier gives the object no color.", colorOf(ticket1));

		assertNull("An object the provider answers no color for has no color.", colorOf(newObject(ticket)));
	}

	public void testSpecializationInheritsDynamicColor() throws ConfigurationException {
		TLClass ticket = coloredClass("Ticket", StatusColor.class);
		TLClass bug = TLModelUtil.addClass(_module, "Bug");
		bug.getGeneralizations().add(ticket);

		TLObject bug1 = newObject(bug);
		bug1.tUpdateByName(STATUS_ATTRIBUTE, _open);
		assertEquals(ValueColor.color(LITERAL), colorOf(bug1));
	}

	public void testTypeWithoutDynamicColor() {
		TLClass plain = TLModelUtil.addClass(_module, "Plain");
		TLModelUtil.addProperty(plain, STATUS_ATTRIBUTE, _status);

		TLObject plain1 = newObject(plain);
		plain1.tUpdateByName(STATUS_ATTRIBUTE, _open);
		assertNull(colorOf(plain1));
	}

	public void testClassifierAnnotationSyntax() throws ConfigurationException {
		ClassifierConfig config = read(ClassifierConfig.class, "classifier",
			"<classifier name='open'>"
				+ "<annotations><color token='support-warning'/></annotations>"
				+ "</classifier>");
		assertEquals(ValueColor.themeToken("support-warning"), ValueColor.of(config.getAnnotation(TLColor.class)));

		ClassifierConfig literal = read(ClassifierConfig.class, "classifier",
			"<classifier name='closed'>"
				+ "<annotations><color value='#04A38D'/></annotations>"
				+ "</classifier>");
		assertEquals(ValueColor.color(LITERAL), ValueColor.of(literal.getAnnotation(TLColor.class)));
	}

	public void testColorValueIsItsOwnColor() {
		assertEquals(ValueColor.color(LITERAL), colorOf(LITERAL));
	}

	public void testValuesWithoutColor() {
		assertNull(colorOf(null));
		assertNull(colorOf("open"));
	}

	private TLClass coloredClass(String className, Class<? extends ValueColorProvider> providerClass)
			throws ConfigurationException {
		TLClass result = TLModelUtil.addClass(_module, className);
		TLModelUtil.addProperty(result, STATUS_ATTRIBUTE, _status);
		result.setAnnotation(dynamicColor(providerClass));
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T extends ConfigurationItem> T read(Class<T> type, String tag, String xml)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(tag, TypedConfiguration.getConfigurationDescriptor(type));
		Protocol log = new AssertProtocol();
		ConfigurationItem result = new ConfigurationReader(new DefaultInstantiationContext(log), descriptors)
			.setSource(CharacterContents.newContent(xml)).read();
		log.checkErrors();
		return (T) result;
	}

	private TLObject newObject(TLClass type) {
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	private ValueColor colorOf(Object value) {
		return AnnotationValueColorProvider.INSTANCE.colorOf(value);
	}

	private static TLColor literalColor(Color color) {
		TLColor result = TypedConfiguration.newConfigItem(TLColor.class);
		result.setValue(color);
		return result;
	}

	private static TLColor tokenColor(String token) {
		TLColor result = TypedConfiguration.newConfigItem(TLColor.class);
		result.setToken(token);
		return result;
	}

	private static TLDynamicColor dynamicColor(Class<? extends ValueColorProvider> providerClass)
			throws ConfigurationException {
		TLDynamicColor result = TypedConfiguration.newConfigItem(TLDynamicColor.class);
		result.setColorProvider(TypedConfiguration.createConfigItemForImplementationClass(providerClass));
		return result;
	}

	/**
	 * {@link ValueColorProvider} taking the color of an object from its status.
	 */
	public static class StatusColor implements ValueColorProvider {
		@Override
		public ValueColor colorOf(Object value) {
			Object status = ((TLObject) value).tValueByName(STATUS_ATTRIBUTE);
			return AnnotationValueColorProvider.INSTANCE.colorOf(status);
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestValueColorProvider.class);
		test = ServiceTestSetup.createSetup(test, CompatibilityService.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
