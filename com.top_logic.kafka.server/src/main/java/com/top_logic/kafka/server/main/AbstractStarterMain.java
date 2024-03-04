/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.server.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.top_logic.basic.XMain;

/**
 * Abstract super class for external programs that must be started and stopped.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractStarterMain extends XMain {

	private static final String HOSTNAME = "localhost";

	private static final String ADMIN_WEBAPP = "/admin";

	private static final String STOP_SERVLET = "/stop";

	private String START_COMMAND = "start";

	private String STOP_COMMAND = "stop";

	private String _command;

	private int _port = 0;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if ("port".equals(option)) {
			_port = Integer.parseInt(args[i++]);
			return i;
		} else {
			return super.longOption(option, args, i);
		}
	}

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		if (START_COMMAND.equals(args[i])) {
			_command = START_COMMAND;
			return i + 1;
		} else if (STOP_COMMAND.equals(args[i])) {
			_command = STOP_COMMAND;
			return i + 1;
		} else {
			return super.parameter(args, i);
		}
	}

	/**
	 * Starts the external program.
	 */
	protected abstract void start() throws Exception;

	/**
	 * Stopsthe external program.
	 */
	protected abstract void stop() throws Exception;

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();
		if (START_COMMAND.equals(_command)) {
			start();
			try {
				startJetty();
			} finally {
				stop();
			}
		} else if (STOP_COMMAND.equals(_command)) {
			stopJetty();
		} else {
			throw new IllegalStateException("No command given");
		}

	}

	private void stopJetty() throws IOException {
		URL url = new URL("http", HOSTNAME, _port, ADMIN_WEBAPP + STOP_SERVLET);
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

	private void startJetty() throws Exception {
		final Server server = new Server();

		ServerConnector connector = new ServerConnector(server);
		connector.setHost(HOSTNAME);
		connector.setPort(_port);
		// open connector to bind port yet.
		connector.open();
		server.addConnector(connector);

		HandlerCollection handlers = new HandlerCollection();
		ServletContextHandler adminHandler = new ServletContextHandler(handlers, ADMIN_WEBAPP);
		handlers.addHandler(adminHandler);

		adminHandler.addServlet(new ServletHolder("stop",
			new HttpServlet() {
				@Override
				protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
						IOException {
					new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(500);
								server.stop();
								info("Server stopped.");
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}.start();

					resp.setStatus(HttpServletResponse.SC_OK);
					resp.setContentType("text/plain");
					resp.getWriter().println("OK");
				}
			}),
			STOP_SERVLET);

		server.setHandler(handlers);
		server.start();
		info("Server started at port " + _port + ". Waiting for stop call: http://" + HOSTNAME + ":" + _port
			+ ADMIN_WEBAPP + STOP_SERVLET);
		server.join();
	}

}
