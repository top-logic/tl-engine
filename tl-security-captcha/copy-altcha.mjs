// Copies the ALTCHA web component (npm package altcha) into the webapp of tl-security-captcha.
//
// The file defines the custom element <altcha-widget> and the global registry globalThis.$altcha.
// The classic captcha field loads it as module script through the JSFileCompiler, the React
// captcha of tl-security-captcha-view imports it under the specifier "altcha" of the import map.
// The license of the package precedes the minified code.

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';

const pkg = 'node_modules/altcha';
const version = JSON.parse(readFileSync(`${pkg}/package.json`, 'utf8')).version;
const license = readFileSync(`${pkg}/LICENSE.txt`, 'utf8').trim();
const code = readFileSync(`${pkg}/dist/main/altcha.min.js`, 'utf8');

const target = 'src/main/webapp/script';
mkdirSync(target, { recursive: true });
writeFileSync(`${target}/altcha.js`,
  `/*! altcha ${version} (https://altcha.org)\n${license.replaceAll('*/', '* /')}\n*/\n${code}`);
