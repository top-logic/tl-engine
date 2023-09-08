/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Helper class in case you reall MUST access this thing..
 * 
 * @author    <a href=mailto:kha@top-logic.com">Klaus HAlfmann</a>
 */
public class MSAccessHelper extends DBHelper {

	/**
	 * Configuration options for {@link MSAccessHelper}.
	 */
	public interface Config extends DBHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.
	}

	/**
	 * Creates a {@link MSAccessHelper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MSAccessHelper(InstantiationContext context, Config config) {
		super(context, config);
	}

    /** 
     * The JDOC/ODBC Bridge is quite stupid so we must help a bit :-/.
     * 
     * @param pstm      Insert the value into this statement
     * @param val       The Value to set
     * @param col       The column to set the vlaue in.
     * @param dbtype    as found in {@link java.sql.Types}.
     */
    @Override
	public void internalSetFromJava(PreparedStatement pstm, Object val, int col,
                                    DBType dbtype) throws SQLException {
        switch (dbtype) {
			case LONG: {
                setLongFromJava(pstm, val, col);
                } break;
                // Optionales Feature wurde nicht implementiert.
           default:             
				super.internalSetFromJava(pstm, val, col, dbtype);
        }
    }
    
    /** 
     * Map a DB specific type to a Java Type.
     * 
     * This implemtantion currently was tested with MySQL only.
     * 
     * @param res       The resultSet to extract the value from.
     * @param col       The column to extract it from
     * @param dbtype    the DBType for the derires JAVA-type.
     * 
     * @return          An Object apropriate for the given DBType.
     */
    @Override
	public Object mapToJava(ResultSet res, int col, DBType dbtype) throws SQLException {
        Object result;        
        switch (dbtype) {
            
            /* Access returns Double when asked for a Float */
			case FLOAT:
                float f = res.getFloat(col);
                if (res.wasNull())
                    return null;
                return Float.valueOf(f);
            default:
                result = super.mapToJava(res, col, dbtype);
        }
        return result;
    }
}
