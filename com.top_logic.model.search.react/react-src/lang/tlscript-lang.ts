import { LRLanguage, LanguageSupport } from '@codemirror/language';
import { parser } from './tlscript.grammar';
import { highlighting } from './tlscript-highlight';

/**
 * CodeMirror 6 language definition for TL-Script.
 */
export const tlscriptLanguage = LRLanguage.define({
  parser: parser.configure({
    props: [highlighting],
  }),
  languageData: {
    commentTokens: { line: '//', block: { open: '/*', close: '*/' } },
    closeBrackets: { brackets: ['(', '[', '{', "'", '"', '`'] },
  },
});

/**
 * Returns a CM6 LanguageSupport instance for TL-Script.
 */
export function tlscript(): LanguageSupport {
  return new LanguageSupport(tlscriptLanguage);
}
