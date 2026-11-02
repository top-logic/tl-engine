// Die Verträge der Welle 5b nennen ihre Pflichtbegriffe; die neuen Specimen bestehen den Harness.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { missingInContract, specimenProblems } from './lib/component.mjs';
const datei = (d, f) => readFileSync(new URL(`../src/components/${d}/${f}`, import.meta.url), 'utf8');
const SOLL = {
  'media/media.contract.md': ['tl-media', 'tl-media__frame', 'tl-media__placeholder', 'tl-media--cover',
    'tl-media--contain', 'tl-status', '--tl-surface-document'],
  'prose/prose.contract.md': ['tl-prose', 'heading-lg', 'heading-md', 'heading-sm', 'text-link', 'border-separator'],
  'overlay/overlay.contract.md': ['tl-overlay--fill', 'tl-overlay__base', 'tl-overlay__action'],
  'file/file.contract.md': ['tl-file', 'tl-file__name', 'data-tl-state~="empty"', 'TLDownload', 'TLBinaryField'],
  'form/upload.contract.md': ['TLFileChips', 'TLAudioRecorder', 'TLPhotoCapture', 'audio', 'photo', 'tl-chip-group',
    'maxUploadSize'],
  'button/button.contract.md': ['aria-pressed', 'TLAudioRecorder'],
  'steps/steps.contract.md': ['tl-steps', 'tl-steps__item', 'tl-steps__disc', 'tl-steps__name', 'tl-steps--vertical',
    'data-tl-state~="done"', 'aria-current="step"', 'aria-hidden', '--tl-duration-loop', 'TLWizard', 'TLJobStatus'],
  'job-status/job-status.contract.md': ['tl-job-status', 'tl-job-status__header', 'tl-job-status__state',
    'tl-job-status__elapsed', 'tl-job-status__cancel', 'tl-job-status__live', 'tl-job-status__bar',
    'tl-job-status__message', 'tl-job-status__error', 'tl-job-status__result', 'role="status"', 'role="alert"',
    'tl-steps--vertical', 'tl-nums', 'tl-visually-hidden', '--tl-status-error-text'],
  'wizard/wizard.contract.md': ['tl-steps'],
};
const SPECIMEN = ['media', 'prose', 'overlay', 'file', 'steps', 'job-status'];

for (const [file, names] of Object.entries(SOLL)) test(`${file} nennt ${names.length} Pflichtbegriffe`, () => {
  const [d, f] = file.split('/');
  assert.deepEqual(missingInContract(datei(d, f), names), []);
});
for (const name of SPECIMEN)
  test(`${name}.spec.html besteht den Specimen-Harness`, () => assert.deepEqual(specimenProblems(datei(name, `${name}.spec.html`)), []));
