/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.authorisation.symbols.file;

import com.top_logic.base.security.authorisation.symbols.AbstractAuthorisation;
import com.top_logic.base.security.authorisation.symbols.Symbol;
import com.top_logic.base.security.authorisation.symbols.SymbolException;
import com.top_logic.base.security.authorisation.symbols.SymbolFactory;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Implementation of the Authorisation used for File system access. The methods
 * of this class can be used to access symbols information from a file defined
 * in the "top-logic.xml".
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler </a>
 */
public class FileAuthorisation extends AbstractAuthorisation {

	/**
	 * Configuration for the {@link FileAuthorisation}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<FileAuthorisation> {

		/**
		 * Defines the location where the symbols file may be found.
		 */
		@Mandatory
		ResourceDeclaration getSymbols();

	}

	private String symbolPath;

	/**
	 * Creates a new {@link FileAuthorisation} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FileAuthorisation}.
	 */
	public FileAuthorisation(InstantiationContext context, Config config) {
		symbolPath = config.getSymbols().getResource();
	}

	/**
	 * Return the symbol for the given name. If there is no symbol, this method
	 * returns null.
	 * 
	 * @param aName
	 *            The name of the symbol.
	 * @return The requested symbol.
	 */
	@Override
	public Symbol getSymbol(String aName) throws SymbolException {
		Symbol theSymbol = this.getSymbolFactory().loadSymbol(aName);

		if (theSymbol == null) {
			throw new SymbolException("Symbol '" + aName + "' is unknown!");
		}

		return (theSymbol);
	}

	@Override
	public SymbolFactory getSymbolFactory(){
		FileSymbolFactory newInstance = FileSymbolFactory.getInstance(this.symbolPath);
        // TODO: This class is not actually reloadable.
        //
        // newInstance.registerForReload();
		
		return newInstance;
	}
	
    /** TODO: Provide a name, after making this class "really" {@link Reloadable}. */
    @Override
	public String getName () {
        return null;
    }

    /** TODO: Provide a description, after making this class "really" {@link Reloadable}. */
    @Override
	public String getDescription () {
        return null;
    }

}