import { React, useTLState, useTLCommand, TLChild, FillBarrier, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, AccordionStateJson } from 'tl-react-bridge';

/** A section as the server describes it, always with its ID and label. */
type SectionInfo = Partial<AccordionStateJson.Section> & Required<Pick<AccordionStateJson.Section, 'id' | 'label'>>;

const { useCallback, useRef } = React;

/** The command expanding or collapsing a section. */
const TOGGLE_SECTION = 'toggleSection';

/** Argument of {@link TOGGLE_SECTION}: the ID of the section. */
const ARG_SECTION_ID = 'sectionId';

/** Argument of {@link TOGGLE_SECTION}: whether the section is to be expanded. */
const ARG_EXPANDED = 'expanded';

/** Points right; the accordion's stylesheet rotates it when the section is expanded. */
const CHEVRON = 'css:fa-solid fa-chevron-right';

/**
 * Encodes a section ID into a fragment of an element ID: letters, digits and `-` are kept, every
 * other character is written as `_<hex code>_`, so that distinct section IDs give distinct element
 * IDs that contain no white space.
 */
function idFragment(sectionId: string): string {
  return sectionId.replace(/[^A-Za-z0-9-]/g, ch => '_' + ch.codePointAt(0)!.toString(16) + '_');
}

/**
 * A stack of sections, each with a header that expands or collapses its body (tl-accordion).
 *
 * The component is controlled: which sections are expanded is decided by the server, a click on a
 * header only asks for the change. The body of a section that was expanded once stays mounted
 * while the section is collapsed, hidden, so that its content keeps its local state.
 *
 * Follows the WAI-ARIA accordion pattern: each header is a heading holding a button that controls
 * the section's region. Enter and Space toggle a section (the native button), the arrow keys move
 * the focus to the next and previous header (wrapping around), Home and End to the first and last.
 * The actions of a header sit beside its button and take no part in either.
 */
const TLAccordion: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AccordionStateJson>>();
  const sendCommand = useTLCommand();
  const sections = (state.sections ?? []) as SectionInfo[];
  const triggers = useRef<(HTMLButtonElement | null)[]>([]);

  const triggerId = (sectionId: string) => `${controlId}-header-${idFragment(sectionId)}`;
  const panelId = (sectionId: string) => `${controlId}-panel-${idFragment(sectionId)}`;

  const handleToggle = useCallback((section: SectionInfo) => {
    sendCommand(TOGGLE_SECTION, { [ARG_SECTION_ID]: section.id, [ARG_EXPANDED]: !section.expanded });
  }, [sendCommand]);

  const focusTrigger = (index: number) => {
    const count = sections.length;
    if (count === 0) return;
    triggers.current[(index + count) % count]?.focus();
  };

  const handleKeyDown = (e: React.KeyboardEvent, index: number) => {
    if (e.key === 'ArrowDown') focusTrigger(index + 1);
    else if (e.key === 'ArrowUp') focusTrigger(index - 1);
    else if (e.key === 'Home') focusTrigger(0);
    else if (e.key === 'End') focusTrigger(sections.length - 1);
    else return;
    e.preventDefault();
  };

  triggers.current.length = sections.length;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-accordion')}>
      {sections.map((section, index) => {
        const expanded = section.expanded === true;
        return (
          <div
            key={section.id}
            className={'tl-accordion__section' + (expanded ? ' tl-accordion__section--expanded' : '')}
          >
            <div className="tl-accordion__header">
              <div className="tl-accordion__heading" role="heading" aria-level={3}>
                <button
                  ref={el => { triggers.current[index] = el; }}
                  type="button"
                  id={triggerId(section.id)}
                  className="tl-accordion__trigger tl-type-body-strong"
                  aria-expanded={expanded}
                  aria-controls={panelId(section.id)}
                  onClick={() => handleToggle(section)}
                  onKeyDown={e => handleKeyDown(e, index)}
                >
                  <ThemeIcon encoded={CHEVRON} className="tl-icon-sm tl-accordion__chevron" />
                  {section.icon && <ThemeIcon encoded={section.icon} className="tl-icon-sm tl-accordion__icon" />}
                  <span className="tl-accordion__label">{section.label}</span>
                </button>
              </div>
              {section.actions && (
                <div className="tl-accordion__actions">
                  <TLChild control={section.actions} />
                </div>
              )}
            </div>
            <div
              id={panelId(section.id)}
              className="tl-accordion__panel"
              role="region"
              aria-labelledby={triggerId(section.id)}
              hidden={!expanded}
            >
              {section.content && (
                <FillBarrier>
                  <TLChild control={section.content} />
                </FillBarrier>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default TLAccordion;
