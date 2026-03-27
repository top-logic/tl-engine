/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Confirmation dialog for unsaved changes.
 *
 * Displays the list of dirty handlers and offers Save (if no errors), Discard, and Cancel buttons.
 *
 * State from server:
 * - descriptions: string[] - handler descriptions
 * - canSave: boolean - whether save is available
 */
const TLDirtyConfirmDialog: React.FC<TLCellProps> = () => {
	const state = useTLState();
	const sendCommand = useTLCommand();

	const descriptions = (state.descriptions as string[]) ?? [];
	const canSave = state.canSave as boolean;

	const handleSave = React.useCallback(() => {
		sendCommand('save');
	}, [sendCommand]);

	const handleDiscard = React.useCallback(() => {
		sendCommand('discard');
	}, [sendCommand]);

	const handleCancel = React.useCallback(() => {
		sendCommand('cancel');
	}, [sendCommand]);

	return (
		<div className="tlDirtyConfirmDialog">
			<h3 className="tlDirtyConfirmDialog__title">Unsaved Changes</h3>
			<p className="tlDirtyConfirmDialog__message">The following forms have unsaved changes:</p>
			{descriptions.length > 0 && (
				<ul className="tlDirtyConfirmDialog__list">
					{descriptions.map((desc, idx) => (
						<li key={idx}>{desc}</li>
					))}
				</ul>
			)}
			<div className="tlDirtyConfirmDialog__actions">
				{canSave && (
					<button className="tlButton tlButton--primary" onClick={handleSave}>
						Save
					</button>
				)}
				<button className="tlButton tlButton--danger" onClick={handleDiscard}>
					Discard
				</button>
				<button className="tlButton" onClick={handleCancel}>
					Cancel
				</button>
			</div>
		</div>
	);
};

export default TLDirtyConfirmDialog;
