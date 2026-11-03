/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/*
 * Writes stylesheets of an npm package, restricted by an @scope rule, into one stylesheet of a web
 * application. Used by the npm builds of modules that ship stylesheets of a diagram library, so
 * that several such libraries in one application do not style each other's diagrams.
 *
 * Usage:
 *
 *   node scope-css.mjs --package <package dir> --scope <scope prelude> --header <header file>
 *       --out <output file> <stylesheet>...
 *
 * The output consists of the header, followed by the given stylesheets (relative to the package
 * dir) inside "@scope <scope prelude> { ... }". In the header, {{ name }} and {{ version }} are
 * replaced with name and version of the package, {{ scope }} with the scope prelude.
 */

import fs from 'node:fs';
import path from 'node:path';
import { parseArgs } from 'node:util';

const { values: options, positionals: stylesheets } = parseArgs({
  options: {
    package: { type: 'string' },
    scope: { type: 'string' },
    header: { type: 'string' },
    out: { type: 'string' }
  },
  allowPositionals: true
});

for (const option of [ 'package', 'scope', 'header', 'out' ]) {
  if (!options[option]) {
    throw new Error('Missing option --' + option + '.');
  }
}
if (stylesheets.length === 0) {
  throw new Error('No stylesheet given.');
}

const pkg = JSON.parse(fs.readFileSync(path.join(options.package, 'package.json'), 'utf8'));

const header = fs.readFileSync(options.header, 'utf8')
  .replaceAll('{{ name }}', pkg.name)
  .replaceAll('{{ version }}', pkg.version)
  .replaceAll('{{ scope }}', options.scope);

const body = stylesheets
  .map(file => fs.readFileSync(path.join(options.package, file), 'utf8').trimEnd())
  .join('\n\n');

fs.mkdirSync(path.dirname(options.out), { recursive: true });
fs.writeFileSync(options.out, header.trimEnd() + '\n\n@scope ' + options.scope + ' {\n\n' + body + '\n\n}\n');

console.log('created ' + options.out + ' (' + pkg.name + ' ' + pkg.version + ')');
