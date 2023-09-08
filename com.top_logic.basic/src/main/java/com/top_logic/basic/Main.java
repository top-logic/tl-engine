/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;




/** 
 * Common base class for command line tools.
 * 
 * <p>
 * Example invocation: <code>java com.top_logic.basic.Main [-h | --help] [parameters]</code>
 * </p>
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class Main extends ProtocolAdaptor
{
    /** use this when calling the CTor with boolean. */
    public static final boolean INTERACTIVE = true;

    /**
     * @see #isInteractive()
     */
    protected boolean interactive;
    
	/**
	 * Interactive usage with error output to the console.
	 */
    public Main() {
        this(true);
    }

    /** CTor for other, internal usages */
    public Main(boolean isInteractive) {
    	this(isInteractive, new SyserrProtocol());
    }

	/**
	 * Creates a {@link Main} routine for a tool.
	 * 
	 * @param isInteractive
	 *        See {@link #isInteractive()}.
	 * @param protocol
	 *        See {@link #getProtocol()}.
	 */
    public Main(boolean isInteractive, Protocol protocol) {
		super(protocol);
		if (isInteractive) {
			Logger.configureStdout("WARN");
		}
        this.interactive = isInteractive; 
        setProtocol(protocol);
	}
    
    /**
     *  Whether the tool was started from the command line.
     */
    public boolean isInteractive() {
		return interactive;
	}
    
    /**
     * The destination for log and error output of this tool.
     */
    public Protocol getProtocol() {
		return this;
	}
    
    /**
     * Sets the {@link #getProtocol()} property.
     */
    // Workaround for not modifying thousands of constructors of subclasses.
    public final void setProtocol(Protocol protocol) {
		setProtocolImplementation(protocol);
	}

	/**
	 * Sets verbosity to {@link Protocol#DEBUG}.
	 */
	public final void setDebug() {
		setVerbosity(Protocol.DEBUG);
	}

	/**
	 * Sets verbosity to {@link Protocol#VERBOSE}.
	 */
	public final void setVerbose() {
		setVerbosity(Protocol.VERBOSE);
	}

	/**
	 * Sets verbosity to {@link SyserrProtocol#QUIET}.
	 */
	public final void setQuiet() {
		setVerbosity(SyserrProtocol.QUIET);
	}

	private void setVerbosity(int verbosity) {
		final Protocol protocolImplementation = getProtocolImplementation();
		if (protocolImplementation instanceof AbstractPrintProtocol) {
			AbstractPrintProtocol console = (AbstractPrintProtocol) protocolImplementation;
			console.setVerbosity(verbosity);
		} else {
			error("Verbosity cannot be adjusted if no connected to the console.");
		}
	}

    /** This is called by showHelp to show the possible options */
    protected void showHelpOptions() {
        info("\t-h | --help             show this Help");
    }

    /** This is called by showHelp to show the possible arguments */
    protected void showHelpArguments() {
        // by defult we dont have any Arguments
    }

    /** This function is called when -h or --help is found */
    protected void showHelp() {
        info("Syntax: [<option>* ,<argument>*]");
        info("Options are:");
        showHelpOptions();
        info("Arguments are:");
        showHelpArguments();
    }
    
    /** Override this function to fetch long Options (Arguments with --).
     *<p>
     *  Please call <code>super.shortOption()</code> to support 
     *  "--help" and a Standard "Unknown Option" message.
     *</p>
     * @param option Text after the '--', may be empty but not null.
     * @param args   The Original Arguments, you should use args[i]
     * @param i      The index after the current Option
     *
     * @return next index where parsing should continue, usually i or i+1;
     */
    protected int longOption(String option, String args[], int i) {
        if (option.equals("help")) {
            showHelp();
        }
        else if ("debug".equals(option)) {
    		setDebug();
    	} 
    	else if ("verbose".equals(option)) {
			setVerbose();
    	} 
    	else if ("quiet".equals(option)) {
    		setQuiet();
    	} 
    	else if ("showArguments".equals(option)) {
        	showArguments(args);
    	} 
        else {
            error("Unknown Option \t'--" + option + "' ignored. (Use -h for Help)");
        }

        return i;
    }

    /** Override this function to fetch short Options (Arguments with -).
     *<p>
     *  Please call <code>super.shortOption()</code> since 
     *  <code>longOption()</code> wont work otherwise.
     *</p>
     * @param c     Character after the '-', 0 for a '-' only.
     * @param args  The Original Arguments, you should use args[i]
     * @param i     The index after the current Option
     *
     * @return next index where parsing should continue, usually i or i+1;
     */
    protected int shortOption(char c, String args[], int i) {
        switch (c) {
        
            case 'h': showHelp(); break;
            case '-': // Starts a long option with --
                String option = args[i-1].substring(2);
                i = longOption(option, args, i);
                break;
            case 0: 
            	error("A Single - is not an option, ignored. (Use -h for Help)");
                break; 
            default:
            	error("Unknown Option \t'-" + c + "' ignored. (Use -h for Help)"); 
        
        }
        return i;
    }

    /** Override this function to fetch parameters (Arguments without -).
     *
     * @param args The Original Arguments, you should use args[i]
     * @param i    The index of the current Argument
     *
     * @return next index where parsing should continue, normally i+1;
     * 
     * @throws Exception if processing of parameter fails.
     */
    protected int parameter(String args[], int i) throws Exception {
		String arg = args[i];
		if (arg.equals("verbose")) {
			Logger.configureStdout("INFO");
		} else if (arg.equals("debug")) {
			Logger.configureStdout("DEBUG");
		} else {
			error("Unknown parameter \t\t'" + args[i] + "' ignored. (Use -h for Help)");
		}
        return i + 1;
    }
    
    
    /** call this functions to parse the command line */
    protected void parseArgs(String args[]) throws Exception {
        int len = args.length;

        if (len == 0 && this.argumentsRequired ()) {
            this.showHelp ();
            throw fatal("No arguments given");
        }

        int i = 0;
        while (i < len) {
            String arg = args[i];
            int l = 0;
            if (arg == null || (l = arg.length()) == 0) {
                error("Unexpected null/empty argument, ignored");
                i++;
                continue;
            }
            if (arg.charAt(0) == '-') {
                char c = 0;
                if (l > 1) {
                    c = arg.charAt(1);
                }
                i = shortOption(c, args, i+1);
            }
            else
                i = parameter(args, i);
        }
    }

    /**
     * A call to {@link #doActualPerformance()} surrounded by appropriate
     * actions to ensure a correct instance state
     * before and after the call.
     *
     * @param args the command line arguments
     *
     * @see #doActualPerformance
     */
    public final void runMain (String[] args) throws Exception {
        try {
            this.setUp (args);
			checkErrors();
			beforeActualPerformance();
			checkErrors();
            this.doActualPerformance ();
        } finally {
            this.tearDown ();
        }
    }

    /**
	 * Called immediately before {@link #doActualPerformance()}.
	 * 
	 * <p>
	 * Specialized base classes for tools may do some general setup before the actual tool
	 * implementation starts.
	 * </p>
	 */
	protected void beforeActualPerformance() {
		// Hook for subclasses.
	}

	/**
	 * Called at the beginning of runMain. Override this method if you have to make instance changes
	 * after running doActualPerformance in runMain.
	 * <p>
	 * <em>Do not forget the super call.</em>
	 * </p>
	 * 
	 * @see #doActualPerformance
	 * @see #runMain
	 * @see #tearDown #author Michael Eriksson
	 */
    protected void setUp (String[] args) throws Exception {
        this.parseArgs(args);
    }

    /**
     * Here you do whatever it is that you want to do.
     * <p>
     * This is only needed when you do not work directly on
     * arguments, but only when all are parsed. 
     * <em>You would normally not call super() here.</em>
     * </p>
     *
     * #author Michael Eriksson
     */
    protected void doActualPerformance () throws Exception {
        if (interactive) {
        	info("This tool does nothing");
        }
    }

    /**
     * Called at the end of runMain.
     * Override this method if you have to make instance changes
     * before running doActualPerformance in runMain.
     * <p>
     * <em>Do not forget the super call.</em>
     * </p>
     *
     * @see #doActualPerformance
     * @see #runMain
     * @see #setUp
     * #author Michael Eriksson
     */
    protected void tearDown () throws Exception {
        // Currently empty.
    }

    /**
     * Called at the beginning of runMainCommandLine.
     * Override this method if you have to make global changes
     * after running runMain in runMainCommandLine.
     * <p>
     * <em>Do not forget the super call.</em>
     * </p>
     *
     * @see #runMain
     * @see #runMainCommandLine
     * @see #postRunMainCommandLine
     * #author Michael Eriksson
     */
    protected void preRunMainCommandLine () throws Exception {
        //Currently empty.
    }

    /**
     * A call to runMain surrounded by appropriate
     * actions to ensure a correct system state
     * before and after the call.
     * <p>
     * This should be your entry point when using
     * (or simulating) the command line.
     * </p>
     *
     * @param args the command line arguments
     *
     * @see #runMain
     * #author Michael Eriksson
     */
    public final void runMainCommandLine (String[] args) throws Exception {
		try {
    		this.preRunMainCommandLine ();
			checkErrors();
    		
    		this.runMain (args);
			checkErrors();
    		
    		this.postRunMainCommandLine ();
    	} catch (RuntimeException ex) {
    		throw ex;
    	} catch (Exception ex) {
    		error("Internal error", ex);
    	}

		checkErrors();
    }

	private void showArguments(String[] args) {
	    StringBuilder line = new StringBuilder(128 + (args.length << 6));
		line.append("java ");
		line.append(this.getClass().getName());
		for (int n = 0, cnt = args.length; n < cnt; n++) {
			line.append(' ');
			line.append('"' + args[n] + '"');
		}
		
		info(line.toString());
	}

	/**
     * Called at the end of runMainCommandLine.
     * Override this method if you want to do something after all other
     * processing.
     * <p>
     * <em>Do not forget the super call.</em>
     * </p>
     *
     * @see #runMain
     * @see #runMainCommandLine
     * @see #preRunMainCommandLine
     * #author Michael Eriksson
     */
    protected void postRunMainCommandLine () throws Exception {
        //Currently empty.
    }

    /**
     * Does this instance require arguments?
     * <p>
     * This implementation always returns false.
     * Sub-classes may override it.
     * </p>
     *
     * @return true if arguments are required, else false
     *
     * #author Michael Eriksson
     */
    protected boolean argumentsRequired () {
        return false;
    }

	/**
	 * Reports a non-fatal error.
	 * 
	 * @param ex the exception that occurred.
	 */
	protected final void error(Exception ex) {
		error(null, ex);
	}

	/**
	 * Reports a fatal error that immediately stopps processing.
	 * 
	 * @param ex the exception that occurred.
	 */
	protected final RuntimeException fatal(Exception ex) {
		return fatal(null, ex);
	}
	
    /**
     * Main function as entry point.
     * <p>
     * This method should never be called directly.
     * Instead use {@link #runMain(String[])}.
     * </p>
     * <p>
     * This method should never do anything but creating
     * a new instance and calling {@link #runMainCommandLine(String[])}.
     * </p>
     * <p>
     * Note: <em>When sub-classing you will have to declare the main method
     * anew.</em>
     * </p>
     *
     * @see #runMain(String[])
     * @see #runMainCommandLine(String[])
     */
    public static void main(String args[]) throws Exception {
        new Main ().runMainCommandLine (args);
    }

}
