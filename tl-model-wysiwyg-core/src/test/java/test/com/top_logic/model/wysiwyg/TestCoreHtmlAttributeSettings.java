/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.wysiwyg;

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
 * Test of the UI-independent {@link AttributeSettings} of the HTML types, without any classic UI
 * module on the class path.
 */
@SuppressWarnings("javadoc")
public class TestCoreHtmlAttributeSettings extends TestCase {

	private static final String HTML = """
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.RenderWholeLineAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.PDFRendererAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.layout.wysiwyg.ui.StructuredTextPDFRenderer"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <excelRenderer class="com.top_logic.layout.wysiwyg.ui.StructuredTextExcelRenderer"/>
		  <sortKeyProvider class="com.top_logic.layout.wysiwyg.ui.HTMLTextExtractor"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.io.annotation.TLExportBinding"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.wysiwyg.storage.StructuredTextAttributeValueBinding"/>
		</annotation>
		""";

	private static final String I18N_HTML = """
		<annotation config:interface="com.top_logic.model.annotate.LabelPositionAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  value="above-value"
		/>
		<annotation config:interface="com.top_logic.model.annotate.RenderWholeLineAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		/>
		<annotation config:interface="com.top_logic.model.annotate.ui.PDFRendererAnnotation"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextPDFRenderer"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.annotate.ui.TLColumnInfo"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <comparator class=""/>
		  <excelRenderer class="com.top_logic.layout.wysiwyg.ui.I18NStructuredTextExcelRenderer"/>
		  <sortKeyProvider class="com.top_logic.layout.wysiwyg.ui.i18n.I18NHTMLTextExtractor"/>
		</annotation>
		<annotation config:interface="com.top_logic.model.io.annotation.TLExportBinding"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		>
		  <impl class="com.top_logic.model.wysiwyg.storage.I18NStructuredTextAttributeValueBinding"/>
		</annotation>
		""";

	/**
	 * A class of the classic HTML editor, which must not be available to this module.
	 */
	private static final String CLASSIC_UI_CLASS = "com.top_logic.layout.wysiwyg.ui.StructuredTextControl";

	public void testHtml() throws ConfigurationException {
		assertSetting(HTML, "HTML");
	}

	public void testI18NHtml() throws ConfigurationException {
		assertSetting(I18N_HTML, "I18N_HTML");
	}

	public void testNoClassicUI() {
		try {
			Class.forName(CLASSIC_UI_CLASS);
			fail("Classic UI module on the class path: " + CLASSIC_UI_CLASS);
		} catch (ClassNotFoundException ex) {
			// Expected.
		}
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
		return ModuleLicenceTestSetup.setupModule(TestCoreHtmlAttributeSettings.class);
	}

}
