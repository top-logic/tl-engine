/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.version;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.version.model.VersionInfo;

import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.server.io.ReaderAdapter;

/**
 * The TopLogic application version.
 */
public final class Version {

	private static Version instance;
    
	private VersionInfo _info;

	/**
	 * Creates a {@link Version} from given JSON data.
	 *
	 * @param versionData
	 *        Stream of JSON data. If value is <code>null</code> default settings apply.
	 */
	public Version(InputStream versionData) {
		try {
			if (versionData == null) {
				_info = VersionInfo.create().setName("TopLogic").setVersion("DEV-SNAPSHOT");
			} else {
				_info = VersionInfo.readVersionInfo(new JsonReader(new ReaderAdapter(
					new InputStreamReader(versionData, "utf-8"))));
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

    /** Return my complete Version */
    @Override
	public String toString() {
        String theVersion = this.getVersionString();
        String theName    = this.getName();
        int    theLength  = Math.max(2, 16 - theName.length());
        byte[] theArray   = new byte[theLength];

        Arrays.fill(theArray, (byte) ' ');
        String theSpace  = new String(theArray);

		String buildQualifier = getBuildQualifier();
		return theName + theSpace + theVersion + (buildQualifier != null ? " (" + buildQualifier + ")" : "");
    }

	/**
	 * Name of the application.
	 */
	public final String getName() {
		String name = _info.getName();
		return name == null ? _info.getArtifactId() : name;
	}
	
    /**
	 * Additional info identifying the build of the software.
	 * 
	 * <p>
	 * Might be the date of build or an identifier of the commit in a version control system exactly
	 * describing the version.
	 * </p>
	 */
	public final String getBuildQualifier() {
		return _info.getBuildQualifier();
    }
    
	/**
	 * The version number.
	 */
	public String getVersionString() {
		return _info.getVersion();
    }

	/**
	 * Prints this to the given stream.
	 */
	public void printVersions(PrintStream out) {
        out.println(this);
		out.flush();
    }

	/**
	 * The {@link Version} of this application.
	 * 
	 * <p>
	 * Note: This method must not be named <code>getInstance()</code>, because this would confuse
	 * the singleton lookup of versions of dependencies.
	 * </p>
	 */
	public static Version getApplicationVersion() {
		if (instance == null) {
			instance = new Version(defaultVersionData());
		}
		return instance;
	}

	private static InputStream defaultVersionData() {
		try {
			return FileManager.getInstance().getStreamOrNull("/license/version.json");
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * The name of {@link #getApplicationVersion()}, if initialized, <code>none</code> otherwise.
	 */
	public static String getApplicationName() {
		return (instance == null) ? "none" : instance.getName();
	}

	/**
	 * {@link VersionInfo}s describing dependencies of the running application (including the
	 * information of the current application itself).
	 */
	public List<VersionInfo> getDependencies() {
		ArrayList<VersionInfo> result = new ArrayList<>(_info.getDependencies());
		result.add(_info);
		return result;
	}

}
