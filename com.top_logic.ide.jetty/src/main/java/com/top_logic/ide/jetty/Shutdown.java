/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ide.jetty;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Gracefully shuts down a <i>TopLogic</i> application started in development mode.
 * 
 * @see Bootstrap
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Shutdown {

	/**
	 * Main entry point of the tool.
	 * 
	 * @param args
	 *        Supported arguments are <code>-port</code>.
	 */
	public static void main(String[] args) throws Exception {
		Shutdown main = new Shutdown();
		main.run(args);
	}

	private int _port = 8080;

	private void run(String[] args) throws Exception {
		for (int n = 0, length = args.length; n < length; n++) {
			String arg = args[n];
			if (arg.equals("-port")) {
				_port = Integer.parseInt(args[++n]);
			}
			else {
				throw new IllegalArgumentException("Unknown option '" + arg + "'.");
			}
		}

		start();
	}

	private void start() throws Exception {
		URL url = new URL("http", Bootstrap.HOSTNAME, _port, Bootstrap.ADMIN_WEBAPP + Bootstrap.STOP_SERVLET);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "*/*");
		con.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
		con.connect();
		if (con.getResponseCode() != 200) {
			System.err.println("Got response: " + con.getResponseCode());
		} else {
			try (InputStream in = con.getInputStream()) {
				int ch;
				while ((ch = in.read()) > 0) {
					System.out.print((char) ch);
				}
			}
		}
	}
}