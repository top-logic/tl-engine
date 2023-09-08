/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.DefaultNamespaceContext;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.check.XMLCheck.XMLCheckConfig.NamespaceMapping;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ValueCheck} that applies a list of XPath expressions to a {@link BinaryData} value and
 * asserts that they evaluate to <code>true</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLCheck extends ValueCheck<XMLCheck.XMLCheckConfig> {

	/**
	 * Configuration of {@link XMLCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface XMLCheckConfig extends ValueCheckConfig {

		/**
		 * List of XPath expressions that must evaluate to <code>true</code>, if applied to the root
		 * node of the tested document.
		 */
		@ListBinding(format = StringValueProvider.class, tag = "check", attribute = "xpath")
		List<String> getChecks();
		
		/**
		 * Namespaces used in the {@link #getChecks()}.
		 */
		@EntryTag("define")
		List<NamespaceMapping> getNamespaces();

		/**
		 * Definition of a namespace prefix.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface NamespaceMapping extends ConfigurationItem {
			/**
			 * The prefix for the {@link #getNamespace()}.
			 */
			String getPrefix();

			/**
			 * The namespace bound to the {@link #getPrefix()}.
			 */
			String getNamespace();
		}
	}

	/**
	 * Create a {@link XMLCheck} from configuration.
	 */
	public XMLCheck(InstantiationContext context, XMLCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object value) throws ApplicationAssertion {
		try {
			Document document = DOMUtil.getDocumentBuilder().parse(BinaryData.cast(value).getStream());

			XPath xpathImpl = XPathFactory.newInstance().newXPath();
			DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
			
			List<NamespaceMapping> namespaces = _config.getNamespaces();
			if (namespaces != null) {
				for (NamespaceMapping mapping : namespaces) {
					nsContext.setPrefix(mapping.getPrefix(), mapping.getNamespace());
				}
			}
			
			xpathImpl.setNamespaceContext(nsContext);
			for (String xpath : _config.getChecks()) {
				XPathExpression compiledXPath = xpathImpl.compile(xpath);
				Boolean result = (Boolean) compiledXPath.evaluate(document, XPathConstants.BOOLEAN);

				ApplicationAssertions.assertTrue(_config, "XPath check failed: " + xpath, result.booleanValue());
			}
		} catch (XPathExpressionException ex) {
			ApplicationAssertions.fail(_config, "Invalid test expression.", ex);
		} catch (SAXException ex) {
			ApplicationAssertions.fail(_config, "Invalid XML document.", ex);
		} catch (IOException ex) {
			ApplicationAssertions.fail(_config, "Result document access failed.", ex);
		}

	}

}
