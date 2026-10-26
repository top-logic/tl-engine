/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.AttributeSettings.AttributeSetting;

/**
 * Test of the configuration of <code>tl-model-search</code> without any classic UI module on the
 * class path: the UI-independent {@link AttributeSettings} of its attribute types, and that its
 * configuration references only classes available without the classic UI.
 */
@SuppressWarnings("javadoc")
public class TestCoreModelSearchConfig extends TestCase {

	private static final String CODE = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.code.CodeEditorFieldProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.model.search.code.CodeEditorTooltipRenderer"/>
		  </renderer>
		</annotation>
		""";

	private static final String JSON = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.fieldprovider.format.FormattedFieldProvider">
		    <format class="com.top_logic.element.meta.form.fieldprovider.format.JSONFormatProvider">
		      <analyzer class="com.top_logic.basic.json.JSON$DefaultValueAnalyzer"/>
		      <factory class="com.top_logic.basic.json.JSON$DefaultValueFactory"/>
		    </format>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.PDFRendererAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.layout.renderers.PDFRendererAdapter">
		    <renderer class="com.top_logic.layout.renderers.ConfiguredLabelRenderer">
		      <labels class="com.top_logic.layout.provider.label.JSONLabelProvider"
		        pretty="true"
		      >
		        <analyzer class="com.top_logic.basic.json.JSON$DefaultValueAnalyzer"/>
		      </labels>
		    </renderer>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <labelProvider class="com.top_logic.layout.provider.label.JSONLabelProvider"
		    pretty="false"
		  >
		    <analyzer class="com.top_logic.basic.json.JSON$DefaultValueAnalyzer"/>
		  </labelProvider>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider"
		      maxChars="50"
		    >
		      <fullTextProvider class="com.top_logic.layout.provider.label.JSONLabelProvider"
		        pretty="false"
		      >
		        <analyzer class="com.top_logic.basic.json.JSON$DefaultValueAnalyzer"/>
		      </fullTextProvider>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.renderers.PreformattedContentRenderer">
		      <content-renderer class="com.top_logic.layout.renderers.ConfiguredLabelRenderer">
		        <labels class="com.top_logic.layout.provider.label.JSONLabelProvider"
		          pretty="true"
		        >
		          <analyzer class="com.top_logic.basic.json.JSON$DefaultValueAnalyzer"/>
		        </labels>
		      </content-renderer>
		    </tooltipRenderer>
		  </renderer>
		</annotation>
		<annotation config:interface="com.top_logic.model.io.annotation.TLExportBinding"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.io.bindings.PrimitiveAttributeValueBinding">
		    <format class="com.top_logic.basic.config.format.JSONValueProvider"/>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.search.annotate.CodeEditorLanguage"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="ace/mode/json"
		/>
		""";

	private static final String EXPR = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.fieldprovider.SearchExpressionFieldProvider">
		    <configuration-format>
		      <impl class="com.top_logic.model.search.persistency.attribute.expr.ExprStorageMapping"/>
		    </configuration-format>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.model.search.ui.TLScriptRenderer"/>
		  </renderer>
		  <sortKeyProvider class="com.top_logic.basic.col.mapping.ToStringMapping"/>
		</annotation>
		""";

	private static final String TEMPLATE = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.fieldprovider.format.FormattedFieldProvider">
		    <configuration-format>
		      <impl class="com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping"/>
		    </configuration-format>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.tag.StringTagProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		  </renderer>
		  <sortKeyProvider class="com.top_logic.basic.col.mapping.ToStringMapping"/>
		</annotation>
		""";

	private static final String REPORT = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.fieldprovider.format.FormattedFieldProvider">
		    <configuration-format>
		      <impl class="com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping"/>
		    </configuration-format>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.persistency.attribute.report.ui.ReportTagProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		  </renderer>
		  <sortKeyProvider class="com.top_logic.basic.col.mapping.ToStringMapping"/>
		</annotation>
		""";

	private static final String MACRO = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.persistency.attribute.macro.ui.HiddenFieldProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.persistency.attribute.macro.ui.MacroTagProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.PDFRendererAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.persistency.attribute.macro.ui.MacroPDFRenderer"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <renderer class="com.top_logic.layout.form.control.ConfiguredFirstLineRenderer">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		  </renderer>
		  <sortKeyProvider class="com.top_logic.basic.col.mapping.ToStringMapping"/>
		</annotation>
		""";

	/**
	 * The configuration file of this module.
	 */
	private static final String CONFIG_FILE = "/WEB-INF/conf/modelSearchConf.config.xml";

	/**
	 * A fully qualified class name in a configuration file.
	 */
	private static final Pattern CLASS_NAME = Pattern.compile("\\b(?:com|test)\\.top_logic(?:\\.[a-z_0-9]+)*\\.[A-Z][\\w$]*");

	/**
	 * Classes of the classic UI modules <code>tl-model-search-classic</code>,
	 * <code>tl-layout-codeedit</code> and <code>tl-reporting-flex</code>, which must not be
	 * available to this module.
	 */
	private static final String[] CLASSIC_UI_CLASSES = {
		"com.top_logic.model.search.ui.TLScriptCodeEditorControl",
		"com.top_logic.model.search.ui.selector.SearchSelectorComponent",
		"com.top_logic.layout.codeedit.control.CodeEditorControl",
		"com.top_logic.reporting.flex.chart.config.ChartConfig",
	};

	public void testCode() throws ConfigurationException {
		assertSetting(CODE, "CODE");
	}

	public void testJson() throws ConfigurationException {
		assertSetting(JSON, "JSON");
	}

	public void testExpr() throws ConfigurationException {
		assertSetting(EXPR, "EXPR");
	}

	public void testTemplate() throws ConfigurationException {
		assertSetting(TEMPLATE, "TEMPLATE");
	}

	public void testReport() throws ConfigurationException {
		assertSetting(REPORT, "REPORT");
	}

	public void testMacro() throws ConfigurationException {
		assertSetting(MACRO, "MACRO");
	}

	public void testNoClassicUI() {
		for (String className : CLASSIC_UI_CLASSES) {
			try {
				Class.forName(className);
				fail("Classic UI module on the class path: " + className);
			} catch (ClassNotFoundException ex) {
				// Expected.
			}
		}
	}

	/**
	 * Every class named in the configuration of this module can be loaded, so the configuration
	 * does not reference a class of a classic UI module.
	 */
	public void testConfigClassesAvailable() throws IOException {
		String config;
		try (InputStream in = FileManager.getInstance().getStream(CONFIG_FILE)) {
			config = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		TreeSet<String> classNames = new TreeSet<>();
		Matcher matcher = CLASS_NAME.matcher(config);
		while (matcher.find()) {
			classNames.add(matcher.group());
		}
		assertFalse("No class names found in " + CONFIG_FILE + ".", classNames.isEmpty());

		List<String> missing = new ArrayList<>();
		for (String className : classNames) {
			try {
				Class.forName(className);
			} catch (ClassNotFoundException ex) {
				missing.add(className);
			}
		}
		assertEquals("Classes not available in " + CONFIG_FILE + ".", List.of(), missing);
	}

	private static void assertSetting(String expectedAnnotations, String configName)
			throws ConfigurationException {
		AttributeSetting setting = settings().getAttributeSettings().get(configName);
		assertNotNull("No attribute setting '" + configName + "'.", setting);
		assertEquals(Integer.valueOf(1), setting.getLegacyTypeCode());
		assertEquals(expectedAnnotations.strip(), annotations(setting));
	}

	/**
	 * The annotations of the given setting as XML, ordered by their serialization (which starts
	 * with the annotation interface name), since the order of the annotations is irrelevant.
	 */
	private static String annotations(AttributeSetting setting) {
		return setting.getAnnotations().stream()
			.map(annotation -> TypedConfiguration.toString("annotation", annotation).strip().replace("\r\n", "\n"))
			.sorted()
			.collect(Collectors.joining("\n"));
	}

	private static AttributeSettings.Config settings() throws ConfigurationException {
		return (AttributeSettings.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(com.top_logic.model.annotate.util.AttributeSettings.class);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestCoreModelSearchConfig.class);
	}

}
