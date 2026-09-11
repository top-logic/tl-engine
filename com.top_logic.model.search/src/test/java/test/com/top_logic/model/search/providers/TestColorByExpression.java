/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.TLDynamicColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.annotate.ui.ValueColorProvider;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.providers.ColorByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link ColorByExpression}.
 */
@SuppressWarnings("javadoc")
public class TestColorByExpression extends AbstractSearchExpressionTest {

	private static final Color LITERAL = new Color(0x04, 0xA3, 0x8D);

	private static final String TOKEN = "support-success";

	public void testColorValueResult() throws Exception {
		assertEquals(ValueColor.color(LITERAL), colorOf("x -> color('#04A38D')", "some object"));
	}

	public void testClassifierResult() throws Exception {
		assertEquals(ValueColor.themeToken(TOKEN), colorOf("x -> $x", coloredClassifier()));
	}

	public void testUncoloredClassifierResult() throws Exception {
		assertNull(colorOf("x -> $x", uncoloredClassifier()));
	}

	public void testNullResult() throws Exception {
		assertNull(colorOf("x -> null", "some object"));
	}

	public void testResultThatIsNoColor() throws Exception {
		assertNull("A result that is neither a color nor a classifier has no color.",
			colorOf("x -> 'open'", "some object"));
	}

	public void testAnnotationSyntax() throws Exception {
		TLDynamicColor annotation = read(TLDynamicColor.class, TLDynamicColor.TAG_NAME,
			"<" + TLDynamicColor.TAG_NAME + ">"
				+ "<" + ColorByExpression.Config.TAG_NAME + " color='t -&gt; $t'/>"
				+ "</" + TLDynamicColor.TAG_NAME + ">");

		ValueColorProvider provider = TypedConfigUtil.createInstance(annotation.getColorProvider());
		assertInstanceof(provider, ColorByExpression.class);
		assertEquals(ValueColor.themeToken(TOKEN), provider.colorOf(coloredClassifier()));
	}

	@SuppressWarnings("unchecked")
	private static <T extends ConfigurationItem> T read(Class<T> type, String tag, String xml) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(tag, TypedConfiguration.getConfigurationDescriptor(type));
		Protocol log = new AssertProtocol();
		ConfigurationItem result = new ConfigurationReader(new DefaultInstantiationContext(log), descriptors)
			.setSource(CharacterContents.newContent(xml)).read();
		log.checkErrors();
		return (T) result;
	}

	private ValueColor colorOf(String colorExpr, Object value) throws Exception {
		return provider(colorExpr).colorOf(value);
	}

	private ValueColorProvider provider(String colorExpr) throws Exception {
		ColorByExpression.Config config = TypedConfiguration.newConfigItem(ColorByExpression.Config.class);
		TypedConfigUtil.setProperty(config, ColorByExpression.Config.COLOR, parse(colorExpr));
		return TypedConfigUtil.createInstance(config);
	}

	private TLClassifier coloredClassifier() {
		TLColor color = TypedConfiguration.newConfigItem(TLColor.class);
		color.setToken(TOKEN);

		TLClassifier result = uncoloredClassifier();
		result.setAnnotation(color);
		return result;
	}

	private TLClassifier uncoloredClassifier() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		TLEnumeration status = TLModelUtil.addEnumeration(module, "Status");
		return TLModelUtil.addClassifier(status, "closed");
	}

	public static Test suite() {
		return suite(TestColorByExpression.class);
	}

}
