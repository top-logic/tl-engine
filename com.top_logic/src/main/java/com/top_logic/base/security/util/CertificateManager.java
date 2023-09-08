/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;

/**
 * Import a server certificate with its private key as a KeyEntry
 * and the certificate chain into a .keystore file suitable
 * for use with tomcat webserver for SSL connections.
 * For usage details cf. {@link #main(String[])} method.
 * For details on certificates cf. {@link com.top_logic.base.security.util.KeyEntryManager}
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buuch</a>
 */
public class CertificateManager {

	// Private fields

	/* Fields to store parameters. */
	private String workingDir;
	private String keyStoreFileName;
	private String keyStorePass;
	private String certFilePattern;
	private String certAliasPattern;
	private String keyEntryAlias;
	private String keyEntryPass;
	private String keyFileName;
	
	private List   certFileNames;
	private List   certAliasNames;
	
	private Boolean useFilePattern;
	private Boolean useAliasPattern;

	/**
	 * Default constructor.
	 * Init fields with default values.
	 */
	public CertificateManager () {
		this.workingDir       = ".";
		this.keyStoreFileName = ".keystore";
		this.keyFileName      = "privkey.key";
		this.keyStorePass     = "changeit";
		this.certFilePattern  = "chain";
		this.certAliasPattern = "cert";
		this.keyEntryAlias    = "serverkeyentry";
		this.keyEntryPass     = "changeit";
		this.useFilePattern   = null;
		this.useAliasPattern  = null;	
	}

	/**
	 * Print usage info.
	 */
	private void usage() {
		System.err.println("CertificateManager usage:\n");
		System.err.println("\t     [-path <workingdir>]");
		System.err.println("\t     [-certfilepattern <filenamepattern>]");
		System.err.println("\t     | [-certfilenames <certfile_1> .. <certfile_n>]");
		System.err.println("\t     [-keystorename <keystorename>]");
		System.err.println("\t     [-keyfilename <keyfilename>]");
		System.err.println("\t     [-certaliaspattern <filenamepattern>]");
		System.err.println("\t     | [-certaliasnames <alias_1> .. <alias_n>]");
		System.err.println("\t     [-keyentryalias <keyentalias>]");
		System.err.println("\t     [-keystorepass <password>]");
		System.err.println("\t     [-keyentrypass <password>]");
		System.err.println();
		System.exit(1);
	}

	/**
	 * Parse the cmd line args
	 * 
	 * @param aParams the cmd line args
	 */
	private void parseArgs(String aParams[]) {
		// Parameter parsing
		for (int i = 0; i < aParams.length && StringServices.startsWithChar(aParams[i], '-'); i++) {
			String theParam = aParams[i];
			if(theParam.equalsIgnoreCase("-path")) {
				if(++i == aParams.length)
					usage();
				this.workingDir = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-keystorepass")) {
				if(++i == aParams.length)
					usage();
				this.keyStorePass = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-keyentrypass")) {
				if(++i == aParams.length)
					usage();
				this.keyEntryPass = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-keystorename")) {
				if(++i == aParams.length)
					usage();
				this.keyStoreFileName = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-keyfilename")) {
				if(++i == aParams.length)
					usage();
				this.keyStoreFileName = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-keyentryalias")) {
				if(++i == aParams.length)
					usage();
				this.keyEntryAlias = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-certfilepattern")) {
				if((this.useFilePattern != null) && !this.useFilePattern.booleanValue() || (++i == aParams.length))
					usage();
				this.useFilePattern = Boolean.TRUE;
				this.certFilePattern = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-certaliaspattern")) {
				if((this.useAliasPattern != null) && !this.useAliasPattern.booleanValue() || (++i == aParams.length))
					usage();
				this.useAliasPattern = Boolean.TRUE;
				this.certFilePattern = aParams[i];
				continue;
			}
			if(theParam.equalsIgnoreCase("-certfilenames")) {
				if((this.useFilePattern != null) && this.useFilePattern.booleanValue() || (++i == aParams.length))
					usage();
				this.useFilePattern = Boolean.FALSE;
				this.certFileNames = new ArrayList();
				for (; i < aParams.length && !StringServices.startsWithChar(aParams[i], '-'); i++) {
					this.certFileNames.add(aParams[i]);
				}
				if (i < aParams.length) {	// Because surrounding for will do i++
					i--;
				}
				continue;
			}
			if(theParam.equalsIgnoreCase("-certaliasnames")) {
				if((this.useAliasPattern != null) && this.useAliasPattern.booleanValue() || (++i == aParams.length))
					usage();
				this.useAliasPattern = Boolean.FALSE;
				this.certAliasNames = new ArrayList();
				for (; i < aParams.length && !StringServices.startsWithChar(aParams[i], '-'); i++) {
					this.certAliasNames.add(aParams[i]);
				}
				if (i < aParams.length) {	// Because surrounding for will do i++
					i--;
				}
				continue;
			}
		}
		
		// Pattern field inits
		if (this.useFilePattern == null) {
			this.useFilePattern = Boolean.TRUE;
		}
		if (this.useAliasPattern == null) {
			this.useAliasPattern = Boolean.TRUE;
		}

		// Alias names must not be given when file patterns are used
		if (!this.useAliasPattern.booleanValue() && this.useFilePattern.booleanValue()) {
			this.usage();
		}

		// Pattern counts must be equal
		if (!this.useFilePattern.booleanValue() && !this.useAliasPattern.booleanValue()) {
			if (this.certAliasNames.size() != this.certFileNames.size()) {
				this.usage();
			}
		}
		else {
			// Init the file names and aliases
			try {
				FilenameFilter theFilter = new CertificateFilenameFilter (this.certFilePattern, ".crt");
				File theDir = new File (this.workingDir);
				String[] theFileNames = FileUtilities.list(theDir, theFilter);
				if (theFileNames.length == 0) {
					this.usage();
				}
				this.certFileNames = new ArrayList ();
				this.certAliasNames = new ArrayList ();
				for (int i=0; i < theFileNames.length; i++) {
					this.certFileNames.add(theFileNames[i]);
					this.certAliasNames.add (this.certAliasPattern + i);
				}
				Collections.sort (this.certFileNames, new CertFilenameComparator (this.certFilePattern, ".crt"));
			}
			catch (Exception ex) {
				System.err.println ("Failed to get certificate files: " + ex);
				this.usage();
			}
		}
	}

	/**
	 * Import the certificates and create the KeyEntry
	 * 
	 * @param aPrintstream	the PrintStream messages are written to
	 */
	private void doImport (PrintStream aPrintstream) throws Exception {
		// Import the certificates via KeyTool
		int theSize = this.certFileNames.size();
		String theKeyStoreFileName = this.workingDir + "/" + this.keyStoreFileName;
		String theTempKeyStoreFileName = this.workingDir + "/" + this.keyStoreFileName + ".temp";
		for (int i=0; i < theSize; i++) {
			String theCertFileName = this.workingDir + "/" + (String) this.certFileNames.get(i);
			String theCertAlias    = (String) this.certAliasNames.get(i);
			String[] theParams     = new String[] {
											"-import",
											"-v",
											"-noprompt",
											"-trustcacerts",
											"-alias",
											theCertAlias,
											"-file",
											theCertFileName,
											"-keystore",
											theTempKeyStoreFileName,
											"-storepass",
											this.keyStorePass
										};
			
			Class<?>[] paramTypes = new Class<?>[]{String[].class};
			Class.forName("sun.security.tools.KeyTool").getDeclaredMethod("main", paramTypes).invoke(null, (Object)theParams);						
		}

		// Generate KeyEntry via KeyEntryManager
		try {
			String theKeyFileName  = this.workingDir + "/" + this.keyFileName;
			theSize = this.certAliasNames.size();
			String[] theCertAliases = new String[theSize];
			for (int i=0; i < theSize; i++) {
				theCertAliases[i] = (String) this.certAliasNames.get(i);
			}
			KeyEntryManager theKEM = new KeyEntryManager ();
			boolean success;
			final FileInputStream aKey = new FileInputStream (theKeyFileName);
			try {
				final FileInputStream aKeyStore = new FileInputStream(theTempKeyStoreFileName);
				try {
					final FileOutputStream aNewKeyStore = new FileOutputStream(theKeyStoreFileName);
					try {
						success = theKEM.importKeyEntry(
							aKeyStore,
							this.keyStorePass.toCharArray(),
							theCertAliases,
							aKey,
							this.keyEntryAlias,
							this.keyEntryPass.toCharArray(),
							aNewKeyStore,
							this.keyStorePass.toCharArray());
					} finally {
						aNewKeyStore.close();
					}
				} finally {
					aKeyStore.close();
				}
			} finally {
				aKey.close();
			}
			aPrintstream.println ((success) ? "Success!" : "Errors occured");	   
		} catch (Throwable ex) {
			aPrintstream.println ("Sorry, couldn't create KeyEntry!");
			ex.printStackTrace ();
		}

	}

	/**
	 * Parse parameters and execute given commands.
	 *  
	 * @param aParams          the cmd line args
	 * @param aPrintstream     where to write the info output
	 */
	public void run(String[] aParams, PrintStream aPrintstream) {
		try {
			parseArgs(aParams);
			doImport(aPrintstream);
		}
		catch(Exception exception) {
			System.out.println("CertificateManager error: " + exception);
			System.exit(1);
		}
	}

	/**
	 * Import a certificate chain and a server certificate.
	 * 
	 * The method accepts the following parameters:
	 *		-path				working directory, default is &quot;.&quot;
	 *		-certfilepattern	file name pattern to read certificates, default is &quot;chain&quot;
	 *							all files in the working directory matching the pattern&lt;number&gt;.crt
	 *							will be read and imported in the order of their numbers
	 *		-certfilenames		alternatively all certificate file names may be given
	 *							(full file path and name relative to working directory)
	 *		-keystorename		name of the keystore file to be created, default is &quot;.keystore&quot;
	 *		-keyfilename		name of the private key file, default is &quot;privkey.key&quot;
	 *		-certaliaspattern	alias patter for the Certificates and the KeyEntry in the keystore,
	 *							default is &quot;certificate&lt;number&gt;&quot; and &quot;keyentry&quot;.
	 *		-certaliasnames		alternatively all aliases may be given. Their number and order
	 *							must match that of the certificate file names and can only be used
	 *							if the parameter -certfilenames is specified
	 *		-keyentryalias		the alias of the KeyEntry, default is &quot;serverkeyentry&quot;
	 *		-keystorepass		password for the keystore, default is &quot;changeit&quot;
	 *		-keyentrypass		password for the KeyEntry, default is &quot;changeit&quot;
	 *
	 * @param aParams the cmd line args
	 */
	public static void main (String[] aParams) {
		CertificateManager theManager = new CertificateManager();
		theManager.run (aParams, System.out);
	}

	/**
	 * FilenameFilter for certificates.
	 * This class is generic enough to be used with any filename
	 * prefix and/or suffix patterns.
	 * 
	 * @author    <a href="mailto:kbu@top-logic.com"></a>
	 */
	private class CertificateFilenameFilter implements FilenameFilter {
		// Private fields
		private String prefix;
		private String suffix;
		
		// Constructors
		
		/**
		 * Set the prefix and suffix that have to be matched in the
		 * file name to be accepted.
		 * 
		 * @param aPrefix	the prefix
		 * @param aSuffix	the suffix
		 */
		public CertificateFilenameFilter (String aPrefix, String aSuffix) {
			this.prefix = aPrefix;
			if (this.prefix == null) {
				this.prefix = "";
			}

			this.suffix = aSuffix;
			if (this.suffix == null) {
				this.suffix = "";
			}
		}

		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String name) {
			return name.startsWith(this.prefix) && name.endsWith(this.suffix);
		}
		
	}
	
	/**
	 * Comparator for certificate file names.
	 * This class is generic enough to be used with any filename
	 * prefix and/or suffix patterns.
	 * 
	 * @author    <a href="mailto:kbu@top-logic.com"></a>
	 */
	private class CertFilenameComparator implements Comparator {
		// Private fields
		private String prefix;
		private String suffix;
		
		// Constructors
		
		/**
		 * Set the prefix and suffix that have to be matched in the
		 * file name.
		 * 
		 * @param aPrefix	the prefix
		 * @param aSuffix	the suffix
		 */
		public CertFilenameComparator (String aPrefix, String aSuffix) {
			this.prefix = aPrefix;
			if (this.prefix == null) {
				this.prefix = "";
			}

			this.suffix = aSuffix;
			if (this.suffix == null) {
				this.suffix = "";
			}
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				else {
					return -1;
				}
			}
			else if (o2 == null) {
				return 1;
			}
			else {
				int theName1 = this.getNumberFrom ((String) o1);
				int theName2 = this.getNumberFrom ((String) o2);
				return theName1 - theName2;
				
			}
		}
		
		/**
		 * Get the number encoded in the file name between
		 * the prefix and suffix defined for the instance.
		 * 
		 * @param aName	the file name
		 * @return	the number encoded in the file name or -1 if an error occurs
		 * (no number encoded or number format has an error).
		 */
		private int getNumberFrom (String aName) {
			if ((aName == null) || (aName.length() == 0)) {
				return -1;
			}
			else {
				if (!aName.startsWith(this.prefix)) {
					return -1;
				}
				else {
					aName = aName.substring(0, this.prefix.length());
					if (!aName.endsWith(this.suffix)) {
						return -1;
					}
					else {
						aName = aName.substring(0, aName.length() - this.suffix.length());
						
						// Parse cert number
						try {
							return Integer.parseInt(aName);
						}
						catch (NumberFormatException nfx) {
							return -1;
						}
					}
				}
			}
		}
	}

}
