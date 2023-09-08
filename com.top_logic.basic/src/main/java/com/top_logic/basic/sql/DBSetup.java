/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.XMain;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;

/**
 * Helper class to set up or reset database tables. If the tables already exist,
 * they will be dropped first. The tables to drop and the tables to create are
 * specified in separate text files.
 *
 * @author  <a href="mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DBSetup extends XMain {

	private static final String FEATURE_EXTENSION = ".sql";

	private final class CreateTables implements Computation<Exception> {
		private final ConnectionPool optionalConnectionPool;

		public CreateTables() {
			this(null);
		}
		
		public CreateTables(ConnectionPool optionalConnectionPool) {
			this.optionalConnectionPool = optionalConnectionPool;
		}

		@Override
		public Exception run() {
			if (optionalConnectionPool != null) {
				return create(optionalConnectionPool);
			} else {
				return create(ConnectionPoolRegistry.getDefaultConnectionPool());
			}
		}

		private Exception create(ConnectionPool connectionPool) {
			try {
				PooledConnection connection = connectionPool.borrowWriteConnection();
				try {
					doCreateTables(connection);
					connection.commit();
				} finally {
					connectionPool.releaseWriteConnection(connection);
				}
				return null;
			} catch (SQLException e) {
				return e;
			} catch (IOException e) {
				return e;
			}
		}
	}


	private final class DropTables implements Computation<Exception> {
		private final ConnectionPool optionalConnectionPool;

		public DropTables() {
			this(null);
		}
		
		public DropTables(ConnectionPool optionalConnectionPool) {
			this.optionalConnectionPool = optionalConnectionPool;
		}

		@Override
		public Exception run() {
			if (optionalConnectionPool != null) {
				return drop(optionalConnectionPool);
				
			} else {
				return drop(ConnectionPoolRegistry.getDefaultConnectionPool());
			}
		}

		private Exception drop(ConnectionPool connectionPool) {
			try {
				PooledConnection connection = connectionPool.borrowWriteConnection();
				try {
					doDropTables(connection);
				} finally {
					connectionPool.releaseWriteConnection(connection);
				}
				
				return null;
			} catch (SQLException e) {
				return e;
			} catch (IOException e) {
				return e;
			}
		}
	}


	/** The default number of Prepared Statements we cache. */
    public static final int  LRU_COUNT = 16;

    /** Product name extracted from the JDBC-MetaData. */
    private String product;

    /** Path where database scripts are to be found. */
    protected String scriptPath;

    /** Name of package or feature names are derive from */
    protected String feature;

    /**
     * Default Constructor
     */
    public DBSetup() {
       super(); // interactive is true;
    }

    /**
     * Constructor with interactive flag.
     */
    public DBSetup(boolean isInteractive) {
        super(isInteractive);
    }

    /**
     * Constructor with feature name and interactive flag.
     */
    public DBSetup(boolean isInteractive, String aFeature) {
        this(isInteractive);
        feature = aFeature;
    }

    /**
     * Constructor that supplies the feature name.
     */
    public DBSetup(String aFeature) {
        this(!INTERACTIVE, aFeature);
    }

    /**
     * Get the name of the file that contains the "CREATE TABLE" statements.
     * The implementing method may add the name of the database product
     * name to the file name which may cause a SQLException.
     *
     * @return the file name
     */
	protected String getCreateFileName(PooledConnection connection) throws SQLException, IOException {
        final String baseName = this.getBaseDir() + "/create" + feature + this.getProduct(connection);
		if (!IdentifierUtil.SHORT_IDS) {
			final String legacyResult = baseName + "-LegacyIds" + FEATURE_EXTENSION;
			initFileManager();
			if (FileManager.getInstance().exists(legacyResult)) {
				return legacyResult;
			}
		}
		return baseName + FEATURE_EXTENSION;
    }

    /**
     * Get the name of the file that contains the "DROP TABLE" statements.
     * The implementing method may add the name of the database product
     * name to the file name which may cause a SQLException.
     *
     * @return the file name
     */
    protected String getDropFileName(PooledConnection connection) throws SQLException {
		return (this.getBaseDir() + "/drop" + feature + this.getProduct(connection) + FEATURE_EXTENSION);
    }

    /**
     * This function is called when -h or --help is found
     */
    @Override
	protected void showHelp() {
        System.out.println("Drop or create the database tables.");
        System.out.println("Syntax: [<option>]* [<arguments>]");
        System.out.println("Options are:");
        System.out.println("\t-h | --help  show this Help");
        System.out.println("\t-x | --x[mlproperties]  <file> Use given XML Properties file");
        System.out.println("\t-r | --real <realpath> Use path to the SQL files");
        System.out.println("\t-f | --feature <feature> Name of feature to use files for");
        System.out.println("\tDefault is something like:");
        System.out.println("\t-x webapps/top-logic/WEB-INF/conf/top-logic.xml");

        System.out.println("Arguments are:");
        System.out.println("\tcreateTables     - Create Tables in database specified in top-logic.xml");
        System.out.println("\tdropTables       - Remove Tables");
    }

    /** Return the directory for the scripts will be found */
    protected String getBaseDir() {
        if (this.scriptPath == null) {
            this.scriptPath = "/WEB-INF/database/scripts";
        }

        return (this.scriptPath);
    }

    /**
     * Evaluate (and execute) the parameters.
     *
     * @param args The Original Arguments, you should use args[i]
     * @param i    The index of the current Argument
     *
     * @return next index where parsing should continue, normally i+1;
     */
    @Override
	protected int parameter(String args[], int i) {
        try {
            String arg = args[i];
            if (arg.equals("createTables")) {
                doCreateTables();
            }
            else if (arg.equals("dropTables")) {
                doDropTables();
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.err.println("\t  Please check that the missing file (and path) exists.");
            System.err.println("\t(Check usage of the -r|--real option. ");
            System.err.println("\t You may need to create a new script for new databases).");
            System.err.println();
        } catch (SQLException ex) {
            explainSQLException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return i + 1;
    }

    /**
     * Create the database tables.
     *
     * @throws SQLException in case of a database error
     * @throws IOException in case reading the file with the
     *                     "CREATE TABLE statements fails
     */
	public void doCreateTables() throws SQLException, IOException, ModuleException {
		initModules();
    	rethrow(ThreadContext.inSystemContext(this.getClass(), new CreateTables()));
    }

	public void doCreateTables(final ConnectionPool connectionPool) throws SQLException, IOException {
    	rethrow(ThreadContext.inSystemContext(this.getClass(), new CreateTables(connectionPool)));
	}

	public void doCreateTables(PooledConnection connection)
			throws SQLException, IOException {
		
		DBHelper       theHelper = DBHelper.getDBHelper(connection);
        String         theName   = this.getCreateFileName(connection);

        if (interactive)
            System.out.println("Executing: " + theName);
		try {
			try (InputStream in = FileManager.getInstance().getStream(theName)) {
				new SQLLoader(connection).executeSQL(new InputStreamReader(in));
			}
			
			// Note: MSSQL supports transactional schema modificaiton.
			connection.commit();
		}
		catch (SQLException ex) {
		    // Better close connection as it is in undefined state now
            connection.closeConnection(null);
			throw ex;
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			if (interactive)
			    ex.printStackTrace();
			else
                Logger.error("Unable to create table with script '" + theName + "'!", ex, this);

		}
	}

	/**
     * Drop the database tables
     *
     * @throws SQLException in case of a database error
     * @throws IOException in case reading the file with the
     *                     "DROP TABLE statements fails
     */
	public void doDropTables() throws SQLException, IOException, ModuleException {
		initModules();
    	rethrow(ThreadContext.inSystemContext(this.getClass(), new DropTables()));
    }

    public void doDropTables(final ConnectionPool connectionPool) throws SQLException, IOException {
    	rethrow(ThreadContext.inSystemContext(this.getClass(), new DropTables(connectionPool)));
	}

	public void doDropTables(PooledConnection connection)
			throws SQLException, IOException {
		
		String         theName   = this.getDropFileName(connection);
        if (interactive)
            System.out.println("Executing: " + theName);
    	try {
			
			
			SQLException firstProblem = null;
			try (InputStream input = FileManager.getInstance().getStream(theName)) {
				new SQLLoader(connection).setContinueOnError(true).executeSQL(new InputStreamReader(input));
			} catch (SQLException ex) {
				// Make sure to commit the transaction even if some statements
				// failed. Otherwise, successful deletions are not executed.
				firstProblem = ex;
			}
			
			// Note: MSSQL supports transactional schema modification.
			connection.commit();
			
			if (firstProblem != null) {
				throw firstProblem;
			}
    	}
		catch (SQLException ex) {
		    // Better close connection as it is in undefined state now
		    connection.closeConnection(ex);
			throw ex;
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
		    if (interactive)
		        ex.printStackTrace();
		    else
		        Logger.error("Unable to drop table with script '" + theName + "'!", ex, this);
		}
	}

    /**
     * Helper function which is called in case of an SQLException.
     *
     * It is assumed that something around the connection fails.
     */
    protected void explainSQLException(SQLException ex) {
        ex.printStackTrace();
        System.out.println();
        System.out.println("\tCheck <DefaultStmCache .. /> Entry in your Configuration");
        System.out.println();
    }

    /**
     * Get the product name of the database.
     *
     * @return the product name, e.g. DB2 or MySQL
     */
    protected String getProduct(PooledConnection statementCache) throws SQLException {

        if (product == null) {
            product = statementCache.getMetaData().getDatabaseProductName();

            // Special hack for DB2 which will return "DB2/NT"
            int slash = product.indexOf('/');
            if (slash > 0) {
                product = product.substring(0,slash);
            }
            if (product.startsWith("Microsoft")) {
                 product = "MSSQL";
            }
        }

        return product;
    }

    /**
     * Care about the "feature" and "real" oprtions.
     */
    @Override
	protected int longOption(String option, String[] args, int i) {
        if ("feature".equals(option)) {
            this.feature    = args[i++];
        } else if ("real".equals(option)) {
            this.scriptPath = args[i++];
        } else {
            return super.longOption(option, args, i);
        }
        return i;
    }

    /**
     * @see com.top_logic.basic.Main#shortOption(char, java.lang.String[], int)
     */
    @Override
	protected int shortOption(char c, String[] args, int i) {
        if (c == 'f') {
            this.feature    = args[i++];
        } else if (c == 'r') {
            this.scriptPath = args[i++];
        } else {
            return super.shortOption(c, args, i);
        }

        return i;
    }

	private void initModules() throws ModuleException {
		if (!hasModuleContext()) {
		    setupModuleContext(ConnectionPoolRegistry.Module.INSTANCE);
		}
	}

	private void rethrow(Exception problem) throws SQLException, IOException {
		if (problem != null) {
    		try {
    			throw problem;
    		} catch (SQLException ex) {
    			throw ex;
    		} catch (IOException ex) {
    			throw ex;
    		} catch (RuntimeException ex) {
    			throw ex;
    		} catch (Exception ex) {
    			throw new UnreachableAssertion("Exception cannot be thrown.", problem);
    		}
    	}
	}

    
	/**
     * Main function as entry point.
     */
    public static void main(String args[]) throws Exception {

        Logger.configureStdout();

        new DBSetup().runMainCommandLine (args);
    }

}
