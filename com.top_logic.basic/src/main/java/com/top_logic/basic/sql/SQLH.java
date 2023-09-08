/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Types;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.format.EnumFormat;

/** Various static helper functions for SQL.
 *
 * @author  Klaus Halfmann
 */
public abstract class SQLH {

	/**
	 * Configuration of {@link SQLH}
	 * 
	 * @since 5.7.4
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {

		/**
		 * Whether mangling database names should contain unnecessary underscores, e.g. "userID" is
		 * transformed to "USER_I_D".
		 * 
		 * @see SQLH#mangleDBName(String)
		 */
		boolean shouldMangleWithUnnecessaryUnderscores();
	}

	static private Boolean _unnecessaryUnderscores;

	/**
	 * Configuration option giving the datasource implementation class.
	 */
    public static final String DATA_SOURCE_PROPERTY = "dataSource";

	/**
	 * Configuration option for refering to the datasource by JNDI.
	 */
	public static final String JNDI_NAME_PROPERTY = "jndiName";

    /** Catch this strange "Unknown Error" only once.
     *  Has been seen on customer sites only.
     */
    static boolean foundUnknown = false;

    /** CREATE a LIKE Condition using the common * and ? wildcards replacing
     * them with the SQL Specific ones.
     *  createLIKE("a*%b?") -&gt; "LIKE 'a%\%b_' ESCAPE '\'"
     */
    public static String createLIKE(String search) {
        int l = search.length();
        StringBuffer result = new StringBuffer(l + 7);
        boolean escape = false;
        result.append("LIKE '");
        for (int i=0; i < l; i++) {
            char c = search.charAt(i);
            switch (c) {
                case '*'  : c = '%';  break;
                case '?'  : c = '_';  break;
                case '%'  : result.append('\\'); escape=true; break;
                case '_'  : result.append('\\'); escape=true; break;
            }
            result.append(c);
        }
        result.append('\'');
        if (escape) {
            result.append(" ESCAPE '\\'");
        }

        return result.toString();
    }

    /** CREATE a LIKE Condition for a Prepared Statement.
     *  It uses the common * and ? wildcards and replaces
     *  them with the SQL Specific ones.
     *  createLIKEParam("a*%b?") -&gt; "a%\%b_".
     *  be aware that the % and _ will still work.
     *
     *  @see #createLIKE for an alternative Solution
     */
    public static String createLIKEParam(String search) {
        int l = search.length();
        StringBuffer result = new StringBuffer(l);
        for (int i=0; i < l; i++) {
            char c = search.charAt(i);
            switch (c) {
                case '*'  : c = '%';  break;
                case '?'  : c = '_';  break;
            }
            result.append(c);
        }

        return result.toString();
    }

    /** Create a String usable for a WHERE xx = statement. When the String
     * is <code>null</code>, <code>IS NULL</code> will be returned. The string
     * will be surrounded by quotes otherwise. aString must not contain
     * single quotes.
     */
    public static String createWhereString(String aString) {
        final String theReturnValue;
        if (aString == null) {
            theReturnValue = "IS NULL";
        }
        else {
            theReturnValue = "='" + aString + '\'';
        }

        return theReturnValue;
    }

    /** 
     * Create a Name for a Columns, Tables, Indexes from a "normal" name.
     *<p>
     *    This function is not designed to be foolproof, but fast.
     *    e.g. <code>ThisIsAName -&gt; THIS_IS_A_NAME</code>
     *</p>
     */
    public static String mangleDBName(String name) {
        int len = name.length();
        StringBuffer dbname = new StringBuffer(len+10);
		boolean useUnnecessaryUnderscores = useUnnecessaryUnderscores();
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (i > 0 && Character.isUpperCase(c))  {
				if (useUnnecessaryUnderscores) {
					dbname.append('_');
				} else if (dbname.charAt(dbname.length() - 1) == '_') {
					// no double '_'
				} else {
					boolean lastLowerCase = i > 0 && Character.isLowerCase(name.charAt(i - 1));
					boolean nextLowerCase = i + 1 < len && Character.isLowerCase(name.charAt(i + 1));
					if (lastLowerCase || nextLowerCase) {
						dbname.append('_');
					}
				}
            }
            if (Character.isLetterOrDigit(c)) {
                dbname.append(Character.toUpperCase(c));
			} else {
				// anything else is folded to _ ...
                dbname.append('_');
            }
        }
        String result = dbname.toString();

        if (result.length() > 32) { // ORA allow 32 MAX, Knowledgebase will derive _SX and _DX
            Logger.warn("Name '" + result + "' will be to long (" + result.length() + ") for some DBs", SQLH.class);
        }
        return result;

    }

	private static boolean useUnnecessaryUnderscores() {
		if (_unnecessaryUnderscores == null) {
			if (ApplicationConfig.Module.INSTANCE.isActive()) {
				Config config = ApplicationConfig.getInstance().getConfig(Config.class);
				_unnecessaryUnderscores = Boolean.valueOf(config.shouldMangleWithUnnecessaryUnderscores());
			} else {
				Logger.warn(
					"Application configuration not started. Can not determine value of 'useUnnecessaryUnderscores'",
					SQLH.class);
				return false;
			}
		}
		return _unnecessaryUnderscores.booleanValue();
	}

    /**
     * This is called by quote on the first char that actually needs quoting.
     *
     * @param    start  The index where encoding should start.
     * @param    len    s.length()
     * @param    s      The string to be encoded, mus not be null
     * @return   The quoted string.
     */
    public static String helpQuote(int start, int len, String s) {

        StringBuffer str = new StringBuffer(len + 8);

        str.append(s);
        str.setLength(start);

        for ( int i = start; i < len; i++ ) {
            char ch = s.charAt(i);
            if ( '\'' == ch )
                str.append("''");
            else
                str.append(ch);
        }
        return str.toString();
    }

    /**
     * Care for single quotes in given String, replace by double ''
     */
    public static String quote(String s) {
        int len = (s != null) ? s.length() : 0;
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            if ( '\'' == ch )
                return helpQuote(i, len, s);
        }

        return s;   // return unmodified String.
    }

    /** Create a String useable for an INSERT statement. When the String
     * is <code>null</code>, <code>NULL</code> will be returned. The string
     * will be surrounded by quotes (and inner quotes will be quoted)
     * otherwise. (Checked for MySQL and Oracle only)
     */
    public static String createInsertString(String aString) {
        final String theReturnValue;
        if (aString == null) {
            theReturnValue = "NULL";
        }
        else {
            theReturnValue = '\'' + quote(aString) + '\'';
        }

        return theReturnValue;
    }

    /** Append a String useable for an INSERT statement to a StringBuffer.
     *
     * When the String
     * is <code>null</code>, <code>NULL</code> will be returned. The String
     * will be surrounded by quotes otherwise. aString must not contain
     * single quotes.
     */
    public static void appendInsertString(String aString, StringBuilder out) {
        if (aString == null) {
            out.append("NULL");
            return;
        }
        out.append('\'');
        out.append(aString);
        out.append('\'');
    }
    
    /** When the returned String is null return "" instead.
     */
    public static String getEmptyString(ResultSet res, int index)
        throws SQLException {
        String result = res.getString(index);
        if (result == null) {
            result = "";
        }
        return result;
    }


    /** Same as getEmptyString but with additional trim();
     */
    public static String getEmptyCHAR(ResultSet res, int index)
        throws SQLException {
        String result = res.getString(index);
        if (result == null) {
            result = "";
        }
        else {
            result = result.trim();
        }

        return result;
    }

    /** When the returned String is null return "" instead.
     */
    public static String getEmptyString(ResultSet res, String colname)
        throws SQLException {
        String result = res.getString(colname);
        if (result == null) {
            result = "";
        }
        return result;
    }

    /** Same as getEmptyString but with additional trim();
     */
      public static String getEmptyCHAR(ResultSet res, String colname)
          throws SQLException {
          String result = res.getString(colname);
          if (result == null) {
              result = "";
          }
          else {
              result = result.trim();
          }

          return result;
      }

    /** Set a key, when value is 0 , NULL is set instead.       */
    public static void setNullKey(PreparedStatement stm, int index, int value)
        throws SQLException
    {
        if (value == 0) {
            stm.setNull(index, Types.INTEGER);
        }
        else {
            stm.setInt(index, value);
        }
    }

    /** Set a String (varchar) , when value is null or empty , NULL is set instead.       */
    public static void setNullString(PreparedStatement stm, int index, String value)
        throws SQLException {
        if (value == null || value.length() == 0) {
            stm.setNull(index, Types.VARCHAR);
        }
        else {
            stm.setString(index, value);
        }
    }

    /** Create INSERT INTO &lt;name&gt; VALUES (?,...,?)
     *
     * @param count must be &gt; 0 othwise an invalid
     *              statement will be generated.
     */
	public static String createInsert(DBHelper sqlDialect, String tableName, int count) {
        StringBuilder buf = new StringBuilder(
            32 + tableName.length() + count << 1);
        buf.append("INSERT INTO ");
		buf.append(sqlDialect.tableRef(tableName));
        buf.append(" VALUES (?");
        for (int i=1;i<count;i++)
            buf.append(",?");
        buf.append(')');
        return buf.toString();
    }

	public static DataSource createDataSource(String driver, String url, String user, String passwd) throws SQLException {
		Properties properties = new Properties();
		properties.setProperty("url", url);
		properties.setProperty("user", user);
		properties.setProperty("password", passwd);
	    return createDataSource(driver, properties);
	}

    /** 
     * Create a DataSource for <em>any</em> Database by using Introspection.
     *
     * It is assumed that the given className implements the
     * {@link DataSource} Interface, any Property accessible in
     * the class AND in the Property will be set (well only String, Integer and Boolean
     * Parameters). As the DataSource interface itself has no setter methods per se
     * the properties are highly Database specific.  
     */
    public static DataSource createDataSource(String aClassName, Properties props) throws SQLException {
        try {
			Class<? extends DataSource> dsClass = 
				ConfigUtil.getClassForNameMandatory(DataSource.class, (String) null, aClassName);
            if (ConfigUtil.hasConfigConstructor(dsClass)) {
                return ConfigUtil.newConfiguredInstance(dsClass, props);
            } else {
            	DataSource ds = ConfigUtil.newInstance(dsClass);
            	injectConfiguration(ds, props);
            	return ds;
            }
        } catch (ConfigurationException ex) {
            throw (SQLException) new SQLException(ex.getMessage()).initCause(ex);
		}
    }

	private static void injectConfiguration(Object instance, Properties configuration) throws ConfigurationException {
		Class<?> concreteType = instance.getClass();
		try {
			BeanInfo   info     = Introspector.getBeanInfo(concreteType);
			PropertyDescriptor descs[] = info.getPropertyDescriptors();
			int len = descs.length;
			for (int i=0; i < len; i++) {
				PropertyDescriptor desc = descs[i];
				String name = desc.getName();
				String value = configuration.getProperty(name);
				if (value != null) {
					Method putter = desc.getWriteMethod();
					if (putter != null) {
						Class<?> firstParam = putter.getParameterTypes()[0];
						if (Enum.class.isAssignableFrom(firstParam)) {
							putter.invoke(instance, EnumFormat.INSTANCE.getValue(name, value));
						} else if (String.class.isAssignableFrom(firstParam))
							putter.invoke(instance, new Object[] {value});
						else if (Integer.class.isAssignableFrom(firstParam)
								|| Integer.TYPE .isAssignableFrom(firstParam))
							putter.invoke(instance, new Object[] { Integer.valueOf(value) });
						else if (Boolean.class.isAssignableFrom(firstParam)
								|| Boolean.TYPE .isAssignableFrom(firstParam))
							putter.invoke(instance, new Object[]{ Boolean.valueOf(value) });
						else
							Logger.error("Don know how to set parameter of type "
									+ firstParam, SQLH.class);
					}
				}
			}
		} catch (IntrospectionException ex) {
			throw new ConfigurationException("Configured class '" + concreteType.getName() + "' is not inspectable.", ex);
		} catch (IllegalAccessException ex) {
			throw new ConfigurationException("Configuration cannot be applied to configured class '" + concreteType.getName() + "'.", ex);
		} catch (InvocationTargetException ex) {
			throw new ConfigurationException("Configuration cannot be applied to configured class '" + concreteType.getName() + "'.", ex);
		}
	}

    /** Fetch a DataSource from some JNDI context (the J2EE Way) */
    public static DataSource fetchJNDIDataSource(String jndiName) {

        try {
            InitialContext ctx = new InitialContext();
            Object res = ctx.lookup(jndiName);
            if(res instanceof javax.sql.DataSource){
                return (javax.sql.DataSource)res;  // e.G. "jdbc/OracleDatasource"
            }
            if(res!=null){
                Logger.error("DataSource has wrong class."+res+" class="+res.getClass(),SQLH.class);
            }
            Logger.info("No datasource for name "+jndiName+" found",SQLH.class);
        } catch (Exception e) {
            Logger.warn("Failed to fetch dataSource from JNDI: " + jndiName, e, SQLH.class);
        }
        return null;
	}


    /** 
     * Create a DataSource for <em>any</em> Database by using Introspection.
     *
     * First it will try to fetch a DS from JNDI using a <code>jndiName</code>
     * property. In case the jndiName is null or empty or fetching from JNDI fails,
     * it will use an entry named "datasource"  containing the name of a class 
     * implementing the DataSource interface. This is done by {@link #createDataSource(String, Properties)}
     *
     * @return null when no DataSource was configured in the properties.
     */
    public static DataSource createDataSource(Properties props)
        throws SQLException
    {
		String jndiName = props.getProperty(JNDI_NAME_PROPERTY);
		String className = props.getProperty(DATA_SOURCE_PROPERTY);

		DataSource result = createDataSource(jndiName, className, props);
		if (result == null) {
			throw new IllegalArgumentException("No database configured, make sure that at least on of the properties '"
				+ JNDI_NAME_PROPERTY + "' or '" + DATA_SOURCE_PROPERTY + "' are given: " + props);
		}
		return result;
	}

	/**
	 * Create a DataSource for <em>any</em> Database by using Introspection.
	 * 
	 * @param jndiName
	 *        The JNDI name to look up.
	 * @param className
	 *        The driver class name to directly instantiate, if no jndiName was given.
	 * @param driverConfig
	 *        The driver properties to initialize the {@link DataSource} with.
	 * @return The created {@link DataSource}, or <code>null</code>, if neither jndiName nor
	 *         className are given.
	 */
	public static DataSource createDataSource(String jndiName, String className, Properties driverConfig) throws SQLException {
		if (!StringServices.isEmpty(jndiName)) {
			DataSource result = fetchJNDIDataSource(jndiName);
			if (result != null) {
				return result;
			}
		}
		if (!StringServices.isEmpty(className)) {
			return createDataSource(className, driverConfig);
		}

		return null;
	}

    // Log SQL Warnings as Logger Warnings.

    /**
     * Log a Chain of Warnings.
     *
     * Used as utility function by the subsequent logXXX fucntions.
     */
    public static void logWarnings(SQLWarning aWarning) {
        boolean debug = Logger.isDebugEnabled(SQLH.class);
        while (aWarning != null) {
            String theMessage = aWarning.getMessage();
            if (!"Unknown error".equals(theMessage)) {
                Logger.warn(theMessage, SQLH.class);
            } else {
                if (!foundUnknown) { // Warn only once ...
                    Logger.warn(theMessage, new Exception("show Stacktrace"), SQLH.class);
                    foundUnknown = true;
                } else if (debug) {
                    Logger.debug(theMessage, new Exception("show Stactrace"), SQLH.class);
                }
            }

            aWarning = aWarning.getNextWarning();
        }
    }

    /**
     * Log the warnings of a Connection.
     *
     * This avoids accumulating the Warnings at the JBC-Connection.
     */
    public static void logWarnings(Connection con) {
        try {
            SQLWarning warn = con.getWarnings();
            con.clearWarnings();
            logWarnings(warn);
        }
        catch (Exception sqlx) {
            logException(sqlx);
        }
   }


    /**
     * Special handling for Exceptions that happen during logWarning.
     */
    static void logException(Exception sqlx) {
        if (Logger.isDebugEnabled(SQLH.class)) {
            Logger.info("Failed to log DB warnings.", sqlx, SQLH.class);
        } else {
        	String message = "Failed to log DB warnings: " + sqlx;
        	// Remove multiLine Exceptions as these are triggered
        	// during retry handling and pollute the logs.
        	int nlIdx = message.indexOf('\n');
        	if (nlIdx > 0) {
        		message = message.substring(0, nlIdx);
        	}
        	Logger.info(message, SQLH.class);
        }
    }

    /** Cleanup a SQL Cnnection, usually in some finally block.
     *
     * @return always null to allow seeting the cnnection to null.
     */
    public static Connection cleanup(Connection con) {
        try {
            if (con != null) {
                logWarnings(con);
                con.close();
            }
        }
        catch (SQLException e) {
            Logger.error("failed to cleanup() SQL Connection", e, SQLH.class);
        }
        return null;
    }

	/**
	 * Adds the given additional information to a wrapped {@link SQLException}.
	 * 
	 * @param ex
	 *        The original {@link SQLException}.
	 * @param additionalMessage
	 *        The additional information to add.
	 * @return The new {@link SQLException} to re-throw.
	 */
	public static SQLException enhanceMessage(SQLException ex, String additionalMessage) {
		return new SQLException(ex.getMessage() + ": " + additionalMessage, ex.getSQLState(), ex.getErrorCode(), ex);
	}

	/** Playground for Testing
	public static void main(String args[]) throws SQLException {
		OracleConnectionPoolDataSource ocpds
			= new OracleConnectionPoolDataSource();
		ocpds.setUser      ("kha");
		ocpds.setPassword  ("bup");
		ocpds.setDriverType("oci");
		ocpds.setServerName("10.49.8.152");
		ocpds.setPortNumber(1521);
		ocpds.setDatabaseName("TL");
		// OracleLog.TRACE = true;
		// ocpds.setLogWriter(new PrintWriter(new OutputStreamWriter(System.out)));
		PooledConnection  con  = ocpds.getPooledConnection();
		DatabaseMetaData meta = con.getConnection().getMetaData();
		System.out.println(ocpds.getURL());
		System.out.println(meta.getDriverName());
		System.out.println(meta.getDriverVersion());
		con.close();
	}
	*/
}