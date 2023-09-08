/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import java.io.StringReader;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.template.expander.TemplateExpander;
import com.top_logic.template.model.CheckResult;
import com.top_logic.template.model.ModelCheckVisitor;
import com.top_logic.template.model.SyntaxCheckVisitor;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.tree.TemplateNode;
import com.top_logic.template.writer.TemplateStringBufferWriter;

/**
 * Utility class for model checks.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ModelUtils {
	
	/** 
	 * Parses the given String and tries to expand it using the {@link TestExpansionModel}.
	 * 
	 * @param toParse the template to parse
	 * @return the expanded template string
	 */
	public static String doExpand(String toParse) throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		TemplateStringBufferWriter theWriter = new TemplateStringBufferWriter();
		TemplateExpander.expandTemplate(new TestExpansionModel(), new StringReader(toParse), theWriter);
		
		return theWriter.getBufferAsString();
	}

	public static CheckResult doCheck(String toParse) throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		TemplateNode theRoot = TemplateExpander.parseTemplate(new StringReader(toParse));
		SyntaxCheckVisitor.checkSyntax(theRoot);
		
		return ModelCheckVisitor.checkModel(theRoot, new TestExpansionModel());
	}
}
