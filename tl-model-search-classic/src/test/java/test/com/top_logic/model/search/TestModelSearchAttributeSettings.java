/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search;

import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.AttributeSettings.AttributeSetting;

/**
 * Test of the effective {@link AttributeSettings} of the attribute types of
 * <code>tl-model-search</code>, in which the classic UI settings of this module (tag providers and
 * edit controls with the Ace code editor) are merged into the UI-independent settings of
 * <code>tl-model-search</code>.
 */
@SuppressWarnings("javadoc")
public class TestModelSearchAttributeSettings extends TestCase {

	private static final String CODE = """
		<annotation config:interface="com.top_logic.element.meta.form.FieldProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.search.code.CodeEditorFieldProvider"/>
		</annotation>
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.tag.CustomInputTagProvider">
		    <controlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.model.search.code.CodeEditorTooltipRenderer"/>
		    <editControlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </editControlProvider>
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
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.tag.CustomInputTagProvider">
		    <controlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
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
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
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
		    <editControlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </editControlProvider>
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
		<annotation config:interface="com.top_logic.element.meta.form.tag.TagProviderAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.element.meta.form.tag.CustomInputTagProvider">
		    <controlProvider class="com.top_logic.model.search.ui.TLScriptCodeEditorControl$CP"/>
		  </impl>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.model.search.ui.TLScriptRenderer"/>
		    <editControlProvider class="com.top_logic.model.search.ui.TLScriptCodeEditorControl$CP"/>
		  </editControlProvider>
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
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		    <editControlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </editControlProvider>
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
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		    <editControlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </editControlProvider>
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
		  <editControlProvider class="com.top_logic.layout.form.control.PopupEditControl$CP">
		    <labelProvider class="com.top_logic.layout.form.control.FirstLineLabelProvider">
		      <fullTextProvider class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </labelProvider>
		    <tooltipRenderer class="com.top_logic.layout.form.control.WikiTextRenderer">
		      <text-extractor class="com.top_logic.layout.provider.MetaLabelProvider"/>
		    </tooltipRenderer>
		    <editControlProvider class="com.top_logic.model.search.code.CodeEditorControlProvider"/>
		  </editControlProvider>
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
		return ModuleLicenceTestSetup.setupModule(TestModelSearchAttributeSettings.class);
	}

}
