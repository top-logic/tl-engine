/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.handler.structured;

import java.util.Map;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.importer.base.ImportValueProvider;
import com.top_logic.importer.base.StructuredDataImportPerformer.StructureImportResult;
import com.top_logic.importer.xml.XMLFileImportParser;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * Handle an import operation on the given kind of object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractDOImportHandler<C extends XMLFileImportParser.MappingConfig, O extends Object> implements ConfiguredInstance<C> {

	/** Key for identifying the current object in the set. */
    public static final String CURRENT_OBJECT_KEY = "_current";

    private final C config;

    private final String type;

	/** 
	 * Creates a {@link AbstractDOImportHandler}.
	 */
	public AbstractDOImportHandler(InstantiationContext aContext, C aConfig) {
	    this.config = aConfig;
        this.type   = aConfig.getType();
	}

	/**
	 * Execute the current command. Dispatched to the command according to the command key.
	 * 
	 * @param someObjects	the current business objects. Must not be <code>null</code>.
	 * @param valueProvider provides imported values for the attributes of the Knowlegde Model. 
     * @param aResult       import result to be filled by this method.
	 * @return Message about the operation performed in the execute. 
	 */
	public abstract ResKey execute(Map<String, Object> someObjects, ImportValueProvider valueProvider, StructureImportResult aResult);

	@Override
	public C getConfig() {
	    return this.config;
	}

	@Override
	public String toString() {
	    return this.getClass().getSimpleName() + " [name: '" + this.config.getName() + "', type: '" + this.type + "']";
	}

	/** 
	 * Return the meta element type this handler operates on.
	 * 
	 * @return   The requested ME type.
	 * @see      #getCurrentObject(Map)
	 */
	public String getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    protected O getCurrentObject(Map<String, Object> someObjects) {
        return (O) someObjects.get(this.getType());
    }

    @SuppressWarnings("unchecked")
    protected <T extends CommandHandler> T getCommandHandler(String aCommandID) {
        return (T) CommandHandlerFactory.getInstance().getHandler(aCommandID);
    }
}
