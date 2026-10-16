/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.CommonMarkText;
import com.top_logic.layout.form.values.HtmlText;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.EditorFactory.AnnotationControlMapping;
import com.top_logic.layout.form.values.edit.EditorFactory.Config;
import com.top_logic.layout.form.values.edit.EditorFactory.TypeControlMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.form.values.edit.annotation.CodeLanguage;
import com.top_logic.layout.form.values.edit.annotation.CommonMark;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.form.values.edit.annotation.Inline;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.layout.form.values.edit.editor.ValueEditor;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test case for the control choice of {@link EditorFactory} by {@link Config#getAnnotationControls()},
 * {@link Config#getTypeControls()}, and {@link Config#getSelectionDialogThreshold()}.
 */
@SuppressWarnings("javadoc")
public class TestEditorFactoryControls extends TestCase {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface First {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface Second {
		String value();
	}

	/**
	 * {@link ControlProvider} recording where it was created.
	 */
	public static class TestControlProvider implements ControlProvider {

		private final String _origin;

		public TestControlProvider(String origin) {
			_origin = origin;
		}

		public String getOrigin() {
			return _origin;
		}

		@Override
		public Control createControl(Object model, String style) {
			throw new UnsupportedOperationException();
		}

	}

	public static class FirstProvider implements AnnotationControlProvider<First> {
		@Override
		public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property, First annotation) {
			return new TestControlProvider("first:" + annotation.value());
		}
	}

	public static class SecondProvider implements AnnotationControlProvider<Second> {
		@Override
		public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property, Second annotation) {
			return new TestControlProvider("second:" + annotation.value());
		}
	}

	public static class NumberControl extends TestControlProvider {
		public static final NumberControl INSTANCE = new NumberControl();

		private NumberControl() {
			super("number");
		}
	}

	public static class ComparableControl extends TestControlProvider {
		public static final ComparableControl INSTANCE = new ComparableControl();

		private ComparableControl() {
			super("comparable");
		}
	}

	@First("enum")
	public enum AnnotatedEnum {
		A, B;
	}

	public enum SmallEnum {
		A, B, C;
	}

	public static class FiveOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("a", "b", "c", "d", "e");
		}
	}

	public static class SmallTree extends Function0<OptionModel<DefaultMutableTLTreeNode>> {
		@Override
		public OptionModel<DefaultMutableTLTreeNode> apply() {
			DefaultMutableTLTreeModel tree = new DefaultMutableTLTreeModel("root");
			DefaultMutableTLTreeNode a = tree.getRoot().createChild("a");
			a.createChild("a1");
			tree.getRoot().createChild("b");
			return new DefaultTreeOptionModel<>(tree);
		}
	}

	public static class NodeMapping implements OptionMapping {
		@Override
		public Object toSelection(Object option) {
			return option == null ? null : ((DefaultMutableTLTreeNode) option).getBusinessObject();
		}
	}

	public interface AnnotatedProperties extends ConfigurationItem {
		@First("param")
		String getFirst();

		AnnotatedEnum getByValueType();

		@First("both")
		@Second("both")
		String getBoth();

		@First("editor")
		@PropertyEditor(ValueEditor.class)
		String getWithEditor();

		String getPlain();
	}

	public interface TypedProperties extends ConfigurationItem {
		Integer getNumber();

		String getText();

		@PropertyEditor(ValueEditor.class)
		String getTextWithEditor();

		@First("annotated")
		String getAnnotatedText();
	}

	public interface SelectProperties extends ConfigurationItem {
		@Options(fun = FiveOptions.class)
		String getLarge();

		SmallEnum getSmall();

		@Options(fun = FiveOptions.class)
		@Format(CommaSeparatedStrings.class)
		List<String> getMulti();

		@Options(fun = SmallTree.class, mapping = NodeMapping.class)
		String getTree();
	}

	public interface TextProperties extends ConfigurationItem {
		@MultiLine(rows = 8)
		String getEightLines();

		@MultiLine
		String getDefaultLines();

		@CommonMark
		String getMarkup();

		@CommonMark
		@MultiLine(rows = 3)
		String getThreeLineMarkup();

		MultiLineValue getTypeAnnotated();

		@Code(language = CodeLanguage.JSON)
		String getJson();

		@Code(language = CodeLanguage.XML)
		@MultiLine(rows = 12)
		String getTwelveLineXml();

		HTMLTemplate getHtml();

		@Html
		String getFormatted();

		@Html(features = "macro")
		@MultiLine(rows = 7)
		String getSevenLineFormatted();

		@Html
		@ItemDisplay(ItemDisplayType.MONOMORPHIC)
		ResKey getI18nFormatted();

		String getPlain();
	}

	public interface Inner extends ConfigurationItem {
		String getName();
	}

	public interface ItemProperties extends ConfigurationItem {
		@Inline
		@ItemDefault
		Inner getInlined();

		@ItemDefault
		Inner getGrouped();

		@Inline
		List<Inner> getInlinedList();
	}

	@MultiLine(rows = 2)
	public enum MultiLineValue {
		A, B;
	}

	public void testMultiLine() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(MultiLineText.class, 8, group, "eight-lines");
		assertMultiLine(MultiLineText.class, MultiLineText.DEFAULT_ROWS, group, "default-lines");
		assertNull(group.getField("plain").getControlProvider());
	}

	public void testCommonMark() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(CommonMarkText.class, MultiLineText.DEFAULT_ROWS, group, "markup");
		assertMultiLine(CommonMarkText.class, 3, group, "three-line-markup");
	}

	public void testMultiLineOnValueType() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(MultiLineText.class, 2, group, "type-annotated");
	}

	/**
	 * Without a code editor, a {@link Code} text is edited as multi-line text, with the rows of a
	 * {@link MultiLine} annotation of the same property.
	 */
	public void testCodeFallsBackToMultiLineText() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(MultiLineText.class, MultiLineText.DEFAULT_ROWS, group, "json");
		assertMultiLine(MultiLineText.class, 12, group, "twelve-line-xml");
	}

	/** An HTML template is source code by its value type. */
	public void testHtmlTemplateIsCode() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(MultiLineText.class, MultiLineText.DEFAULT_ROWS, group, "html");
	}

	/**
	 * Without a WYSIWYG editor, the source of an {@link Html} text is edited as multi-line text,
	 * with the rows of a {@link MultiLine} annotation of the same property, whatever its feature
	 * set.
	 */
	public void testHtmlFallsBackToHtmlText() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		assertMultiLine(HtmlText.class, MultiLineText.DEFAULT_ROWS, group, "formatted");
		assertMultiLine(HtmlText.class, 7, group, "seven-line-formatted");
	}

	/** Each translation of an internationalized {@link Html} text is edited as its source text. */
	public void testInternationalizedHtmlFallsBackToHtmlText() {
		FormGroup group = build(shippedConfig(), TextProperties.class);

		FormGroup translations = (FormGroup) group.getMember("i18n-formatted");
		int inputs = 0;
		for (Iterator<FormField> it = translations.getFields(); it.hasNext();) {
			FormField translation = it.next();
			if (!(translation instanceof StringField) || translation.getName().contains("_")) {
				// Not a translation, but e.g. the translation of a derived key such as a tooltip.
				continue;
			}
			ControlProvider controlProvider = translation.getControlProvider();
			assertEquals("Control for '" + translation.getName() + "'.", HtmlText.class,
				controlProvider == null ? null : controlProvider.getClass());
			inputs++;
		}
		assertTrue("No translation input.", inputs > 0);
	}

	/**
	 * An {@link Inline} item is displayed without a group: Its editor takes the inlining template
	 * instead of its default one, and the fields of the item are still created.
	 */
	public void testInline() {
		FormGroup group = build(shippedConfig(), ItemProperties.class);

		FormMember inlined = group.getMember("inlined");
		assertTrue("Inlined item is displayed without group: " + inlined.getControlProvider(),
			inlined.getControlProvider() instanceof GroupInlineControlProvider);
		assertNotNull("The fields of the inlined item are created.", findField(inlined, "name"));

		FormMember grouped = group.getMember("grouped");
		assertNotNull(grouped.getControlProvider());
		assertFalse("An item without annotation keeps its group.",
			grouped.getControlProvider() instanceof GroupInlineControlProvider);
	}

	/** {@link Inline} does not apply to a list of items, whose entries keep their groups. */
	public void testInlineIgnoredForList() {
		FormGroup group = build(shippedConfig(), ItemProperties.class);

		FormMember list = group.getMember("inlined-list");
		assertFalse("A list is not inlined.", list.getControlProvider() instanceof GroupInlineControlProvider);
	}

	private static FormField findField(FormMember member, String name) {
		if (member instanceof FormField field) {
			return name.equals(field.getName()) ? field : null;
		}
		if (member instanceof FormContainer container) {
			for (Iterator<? extends FormMember> it = container.getMembers(); it.hasNext();) {
				FormField result = findField(it.next(), name);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void testAnnotationControl() throws ConfigurationException {
		FormGroup group = build(config(First.class, FirstProvider.class), AnnotatedProperties.class);

		assertOrigin("first:param", group, "first");
		assertOrigin("first:enum", group, "by-value-type");
		assertNull(group.getField("plain").getControlProvider());
	}

	public void testAnnotationControlBeforePropertyEditor() throws ConfigurationException {
		FormGroup group = build(config(First.class, FirstProvider.class), AnnotatedProperties.class);

		assertOrigin("first:editor", group, "with-editor");
	}

	public void testConfiguredOrderDecides() throws ConfigurationException {
		Config firstFirst = config(First.class, FirstProvider.class);
		addAnnotationControl(firstFirst, Second.class, SecondProvider.class);
		assertOrigin("first:both", build(firstFirst, AnnotatedProperties.class), "both");

		Config secondFirst = config(Second.class, SecondProvider.class);
		addAnnotationControl(secondFirst, First.class, FirstProvider.class);
		assertOrigin("second:both", build(secondFirst, AnnotatedProperties.class), "both");
	}

	public void testTypeControl() throws ConfigurationException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		Map<Class<?>, TypeControlMapping> typeControls = new HashMap<>();
		typeControls.put(Number.class, typeControl(Number.class, NumberControl.class));
		typeControls.put(Comparable.class, typeControl(Comparable.class, ComparableControl.class));
		TypedConfigUtil.setProperty(config, Config.TYPE_CONTROLS, typeControls);
		addAnnotationControl(config, First.class, FirstProvider.class);

		FormGroup group = build(config, TypedProperties.class);

		assertOrigin("Superclass entry wins over an interface entry.", "number", group, "number");
		assertOrigin("Interface entry applies.", "comparable", group, "text");
		assertOrigin("The type control applies to a field created by an annotated property editor.",
			"comparable", group, "text-with-editor");
		assertOrigin("first:annotated", group, "annotated-text");
	}

	public void testSelectionDialogThreshold() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		TypedConfigUtil.setProperty(config, Config.SELECTION_DIALOG_THRESHOLD, 3);

		FormGroup group = build(config, SelectProperties.class);

		assertSame("Single select with more options than the threshold uses the selection dialog.",
			SelectionControlProvider.INSTANCE, group.getField("large").getControlProvider());
		assertNull("Single select with options up to the threshold stays a drop-down.",
			group.getField("small").getControlProvider());
		assertSame("Single select with tree options uses the selection dialog regardless of the threshold.",
			SelectionControlProvider.INSTANCE, group.getField("tree").getControlProvider());
		assertSame("Multi select keeps its selection control.",
			SelectionControlProvider.SELECTION_INSTANCE, group.getField("multi").getControlProvider());
	}

	public void testSelectionDialogThresholdDefault() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals(20, config.getSelectionDialogThreshold());

		FormGroup group = build(config, SelectProperties.class);
		assertNull(group.getField("large").getControlProvider());
		assertSame("Tree options use the selection dialog.",
			SelectionControlProvider.INSTANCE, group.getField("tree").getControlProvider());
	}

	private static void assertMultiLine(Class<?> expectedType, int expectedRows, FormGroup group, String field) {
		ControlProvider controlProvider = group.getField(field).getControlProvider();
		assertNotNull("No control for '" + field + "'.", controlProvider);
		assertEquals("Control for '" + field + "'.", expectedType, controlProvider.getClass());
		assertEquals("Rows of '" + field + "'.", expectedRows, ((MultiLineText) controlProvider).getRows());
	}

	private static Config shippedConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	private static void assertOrigin(String expected, FormGroup group, String field) {
		assertOrigin("Control for '" + field + "'.", expected, group, field);
	}

	private static void assertOrigin(String message, String expected, FormGroup group, String field) {
		ControlProvider controlProvider = group.getField(field).getControlProvider();
		assertTrue(message + " Unexpected control provider: " + controlProvider,
			controlProvider instanceof TestControlProvider);
		assertEquals(message, expected, ((TestControlProvider) controlProvider).getOrigin());
	}

	private static FormGroup build(Config config, Class<? extends ConfigurationItem> type) {
		FormGroup group = new FormGroup("group", null);
		ConfigurationItem model = TypedConfiguration.newConfigItem(type);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);
		return group;
	}

	private static Config config(Class<? extends Annotation> annotation,
			Class<? extends AnnotationControlProvider<?>> provider) throws ConfigurationException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		addAnnotationControl(config, annotation, provider);
		return config;
	}

	private static void addAnnotationControl(Config config, Class<? extends Annotation> annotation,
			Class<? extends AnnotationControlProvider<?>> provider) throws ConfigurationException {
		AnnotationControlMapping mapping = TypedConfiguration.newConfigItem(AnnotationControlMapping.class);
		TypedConfigUtil.setProperty(mapping, AnnotationControlMapping.ANNOTATION, annotation);
		TypedConfigUtil.setProperty(mapping, AnnotationControlMapping.IMPL,
			TypedConfiguration.createConfigItemForImplementationClass(provider));
		config.getAnnotationControls().add(mapping);
	}

	private static TypeControlMapping typeControl(Class<?> type, Class<? extends ControlProvider> impl)
			throws ConfigurationException {
		TypeControlMapping mapping = TypedConfiguration.newConfigItem(TypeControlMapping.class);
		TypedConfigUtil.setProperty(mapping, TypeControlMapping.TYPE, type);
		TypedConfigUtil.setProperty(mapping, TypeControlMapping.IMPL,
			TypedConfiguration.createConfigItemForImplementationClass(impl));
		return mapping;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestEditorFactoryControls.class);
	}
}
