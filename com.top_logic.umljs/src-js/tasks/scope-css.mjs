/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/*
 * Writes the stylesheet of diagram-js, scoped to the diagrams of diagram-js, to the web
 * application: the header resources/diagram-js-header.css followed by assets/diagram-js.css of
 * the installed diagram-js, wrapped in an @scope rule (see the header for the reasons).
 */

import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';

const SCOPE = '@scope (:has(> .djs-parent)) to (:scope > :not(.djs-parent))';

const OUTPUT = '../src/main/webapp/style/umljs/diagram-js.css';

const require = createRequire(import.meta.url);
const pkgFile = require.resolve('diagram-js/package.json');
const version = JSON.parse(fs.readFileSync(pkgFile, 'utf8')).version;
const css = fs.readFileSync(path.join(path.dirname(pkgFile), 'assets', 'diagram-js.css'), 'utf8');

const header = fs.readFileSync('resources/diagram-js-header.css', 'utf8')
  .replace('{{ version }}', version);

fs.mkdirSync(path.dirname(OUTPUT), { recursive: true });
fs.writeFileSync(OUTPUT, header + '\n' + SCOPE + ' {\n\n' + css.trimEnd() + '\n\n}\n');

console.log('created ' + OUTPUT + ' (diagram-js ' + version + ')');
