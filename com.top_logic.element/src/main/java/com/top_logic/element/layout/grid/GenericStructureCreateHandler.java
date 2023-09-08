/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.gui.FormStructureCreation;
import com.top_logic.element.meta.schema.ElementSchema;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandHandler} that can create {@link StructuredElement}s in a {@link GridComponent} context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericStructureCreateHandler extends AbstractCommandHandler {

	/**
	 * Configuration option to choose a {@link CreateContextSelector}.
	 */
	public static final String CONTEXT_SELECTOR_PROPERTY = "contextSelector";

	/**
	 * Configuration option for the node type to create.
	 * 
	 * @see StructuredElement#createChild(String, String)
	 */
	public static final String NODE_TYPE_PROPERTY = "nodeType";
	
	public interface Config extends AbstractCommandHandler.Config {

		@Name(NODE_TYPE_PROPERTY)
		String getNodeType();

		@Name(CONTEXT_SELECTOR_PROPERTY)
		PolymorphicConfiguration<CreateContextSelector> getContextSelector();

	}

	final CreateContextSelector _contextSelector;

	final class ChildTypePossibleInCreateContext implements ExecutabilityRule {
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Object context = _contextSelector.getCreateContext((GridComponent) aComponent);
			if (! (context instanceof StructuredElement)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			StructuredElement parent = (StructuredElement) context;
			if (localNames(parent.getChildrenTypes()).contains(getNodeType())) {
				return ExecutableState.EXECUTABLE;
			} else {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
		}
	}

	private String _nodeType;

	/**
	 * Creates a {@link GenericStructureCreateHandler} from configuration.
	 */
	public GenericStructureCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
		
		_nodeType = config.getNodeType();
		_contextSelector = context.getInstance(config.getContextSelector());
	}

	/**
	 * The node type to create.
	 * 
	 * @see StructuredElement#createChild(String, String)
	 */
	String getNodeType() {
		return _nodeType;
	}
	
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		GridComponent grid = (GridComponent) aComponent;
		
		StructuredElement createContext = (StructuredElement) _contextSelector.getCreateContext(grid);
		String structureName = createContext.getStructureName();
		StructuredElementFactory factory = StructuredElementFactory.getInstanceForStructure(structureName);
		TLClass type = factory.getNodeType(createContext, getNodeType());
		grid.startCreation(
			ElementSchema.getElementType(structureName, getNodeType()), type, 
			FormStructureCreation.INSTANCE, _contextSelector.getPosition(grid, createContext), createContext, model);
		
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return new ChildTypePossibleInCreateContext();
	}

}
