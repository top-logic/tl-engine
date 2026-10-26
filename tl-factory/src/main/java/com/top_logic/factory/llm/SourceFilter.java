/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.filter.CIDRFilter;
import com.top_logic.factory.workspace.WorkspaceProvider;

/**
 * The client addresses a service accepts requests from: address ranges and the network of the
 * workspace containers.
 *
 * <p>
 * An entry is a range in CIDR notation, IPv4 ({@code 172.18.0.0/16}) or IPv6 ({@code fd00::/8}), a
 * single address, or {@link #WORKSPACE_NETWORK}: the ranges of the network the workspace containers
 * are attached to, as the {@link WorkspaceProvider#networkSubnets() container platform} reports
 * them. These are read on the first check and again when an address matches no range, at most
 * every {@link #REFRESH_INTERVAL_MILLIS} milliseconds, so that a network created or changed after
 * the start is picked up without a request per check.
 * </p>
 *
 * <p>
 * Without entries, every address is accepted.
 * </p>
 */
public class SourceFilter {

	/**
	 * Entry standing for the ranges of the network of the workspace containers.
	 */
	public static final String WORKSPACE_NETWORK = "workspace-network";

	/**
	 * Minimum time between two reads of the ranges of the {@link #WORKSPACE_NETWORK}.
	 */
	public static final long REFRESH_INTERVAL_MILLIS = 30_000;

	private final List<CIDRFilter> _ranges = new ArrayList<>();

	private final boolean _workspaceNetwork;

	private final boolean _acceptAll;

	private final Supplier<List<String>> _workspaceSubnets;

	private List<CIDRFilter> _networkRanges;

	private long _networkRangesRead;

	/**
	 * Creates a {@link SourceFilter}.
	 *
	 * @param entries
	 *        The accepted ranges and addresses, see the class comment.
	 * @param workspaceSubnets
	 *        Supplier of the ranges of the network of the workspace containers, asked only if
	 *        {@link #WORKSPACE_NETWORK} is among the entries.
	 * @throws ParseException
	 *         If an entry is neither a range nor {@link #WORKSPACE_NETWORK}.
	 */
	public SourceFilter(List<String> entries, Supplier<List<String>> workspaceSubnets) throws ParseException {
		boolean workspaceNetwork = false;
		boolean any = false;
		for (String entry : entries) {
			String trimmed = entry.strip();
			if (trimmed.isEmpty()) {
				continue;
			}
			any = true;
			if (trimmed.equals(WORKSPACE_NETWORK)) {
				workspaceNetwork = true;
			} else {
				_ranges.add(CIDRFilter.fromString(trimmed));
			}
		}
		_workspaceNetwork = workspaceNetwork;
		_acceptAll = !any;
		_workspaceSubnets = workspaceSubnets;
	}

	/**
	 * Whether a request from the given address is accepted.
	 *
	 * @param address
	 *        The textual IPv4 or IPv6 address of the client, as the servlet container reports it;
	 *        {@code null} or something that is no address is refused unless every address is
	 *        accepted.
	 */
	public boolean allows(String address) {
		if (_acceptAll) {
			return true;
		}
		byte[] bytes = address == null ? null : CIDRFilter.getByteArray(address);
		if (bytes == null) {
			return false;
		}
		if (matches(_ranges, bytes)) {
			return true;
		}
		if (!_workspaceNetwork) {
			return false;
		}
		if (matches(networkRanges(false), bytes)) {
			return true;
		}
		return matches(networkRanges(true), bytes);
	}

	private static boolean matches(List<CIDRFilter> ranges, byte[] address) {
		for (CIDRFilter range : ranges) {
			if (range.matches(address)) {
				return true;
			}
		}
		return false;
	}

	private synchronized List<CIDRFilter> networkRanges(boolean refresh) {
		long now = System.currentTimeMillis();
		if (_networkRanges == null || (refresh && now - _networkRangesRead >= REFRESH_INTERVAL_MILLIS)) {
			_networkRangesRead = now;
			List<CIDRFilter> ranges = new ArrayList<>();
			try {
				for (String subnet : _workspaceSubnets.get()) {
					try {
						ranges.add(CIDRFilter.fromString(subnet));
					} catch (ParseException ex) {
						Logger.warn("Ignoring the range '" + subnet + "' of the workspace network: " + ex.getMessage(),
							SourceFilter.class);
					}
				}
				_networkRanges = ranges;
			} catch (RuntimeException ex) {
				Logger.warn("Cannot read the ranges of the workspace network.", ex, SourceFilter.class);
				if (_networkRanges == null) {
					_networkRanges = ranges;
				}
			}
		}
		return _networkRanges;
	}

}
