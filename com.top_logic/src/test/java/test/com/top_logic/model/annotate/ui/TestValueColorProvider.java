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
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.TLColorAttribute;
import com.top_logic.model.annotate.ui.ValueColor;
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

	private static final String COLOR_ATTRIBUTE = "color";

	private static final String STATUS_ATTRIBUTE = "status";

	private TLModule _module;

	private TLClassifier _open;

	private TLClassifier _closed;

	private TLClassifier _unknown;

	private TLEnumeration _status;

	private TLPrimitive _colorType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();

		_module = TLModelUtil.addModule(model, "test");
		_colorType = TLModelUtil.addDatatype(_module, _module, "Color", Kind.CUSTOM);

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

	public void testColorAttributeHoldingAColor() {
		TLClass ticket = colorAttributeClass("Ticket", COLOR_ATTRIBUTE, _colorType);

		TLObject ticket1 = newObject(ticket);
		ticket1.tUpdateByName(COLOR_ATTRIBUTE, LITERAL);
		assertEquals(ValueColor.color(LITERAL), colorOf(ticket1));

		assertNull("An object whose color attribute has no value has no color.", colorOf(newObject(ticket)));
	}

	public void testColorAttributeHoldingAClassifier() {
		TLClass ticket = colorAttributeClass("Ticket", STATUS_ATTRIBUTE, _status);

		TLObject ticket1 = newObject(ticket);
		ticket1.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		assertEquals(ValueColor.themeToken(TOKEN), colorOf(ticket1));

		TLObject ticket2 = newObject(ticket);
		ticket2.tUpdateByName(STATUS_ATTRIBUTE, _unknown);
		assertNull("An uncolored classifier gives the object no color.", colorOf(ticket2));
	}

	public void testSpecializationInheritsColorAttribute() {
		TLClass ticket = colorAttributeClass("Ticket", STATUS_ATTRIBUTE, _status);
		TLClass bug = TLModelUtil.addClass(_module, "Bug");
		bug.getGeneralizations().add(ticket);

		TLObject bug1 = newObject(bug);
		bug1.tUpdateByName(STATUS_ATTRIBUTE, _open);
		assertEquals(ValueColor.color(LITERAL), colorOf(bug1));
	}

	public void testTypeWithoutColorAttribute() {
		TLClass plain = TLModelUtil.addClass(_module, "Plain");
		TLModelUtil.addProperty(plain, COLOR_ATTRIBUTE, _colorType);

		TLObject plain1 = newObject(plain);
		plain1.tUpdateByName(COLOR_ATTRIBUTE, LITERAL);
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

	private TLClass colorAttributeClass(String className, String attributeName, TLType type) {
		TLClass result = TLModelUtil.addClass(_module, className);
		TLModelUtil.addProperty(result, attributeName, type);
		result.setAnnotation(colorAttribute(attributeName));
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

	private static TLColorAttribute colorAttribute(String attributeName) {
		TLColorAttribute result = TypedConfiguration.newConfigItem(TLColorAttribute.class);
		result.setName(attributeName);
		return result;
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
