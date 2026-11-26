import {
  React, useTLState, useTLCommand, TLChild, FillBarrier, rootClassName, ThemeIcon, StateBadge, ICON_EXPAND,
} from 'tl-react-bridge';
import type { TLCellProps, SideRailSectionsStateJson } from 'tl-react-bridge';

/** Command of a click on the header of a section; the server's `ReactSideRailControl.TOGGLE_SECTION_COMMAND`. */
export const CMD_TOGGLE_SECTION = 'toggleSection';

/** Argument naming the section; the server's `ReactSideRailControl.NAME_ARG`. */
export const ARG_NAME = 'name';

/**
 * Encodes a section name into a fragment of an element ID: letters, digits and `-` are kept, every
 * other character is written as `_<hex code>_`.
 */
function idFragment(name: string): string {
  return name.replace(/[^A-Za-z0-9-]/g, ch => '_' + ch.codePointAt(0)!.toString(16) + '_');
}

/**
 * The column of a side rail: its sections stacked, each with a header that expands and collapses
 * it (design system: tl-side-rail). The state is described by SideRailSectionsStateJson.
 *
 * The header shows the label and the badge of the section. The content of a collapsed section stays
 * mounted and hidden, so that it keeps what the user did there and keeps reporting its badge. A
 * `headless` section has no header and is always expanded: its content brings a title bar of its
 * own, the section is named by its label.
 */
const TLSideRailSections: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<SideRailSectionsStateJson>>();
  const command = useTLCommand();
  const sections = (state.sections ?? []).filter((section) => !section.hidden);

  return (
    <div id={controlId} className={rootClassName(state, 'tl-side-rail__sections')}>
      {sections.map((section) => {
        const headless = section.headless === true;
        const expanded = headless || section.expanded === true;
        const id = `${controlId}-${idFragment(section.name ?? '')}`;
        if (headless) {
          // The content brings a title bar of its own; the section is always expanded.
          return (
            <section key={section.name} className="tl-side-rail__section" data-tl-state="headless" aria-label={section.label}>
              <div id={`${id}-body`} className="tl-side-rail__body tl-region">
                {section.content ? (
                  <FillBarrier>
                    <TLChild control={section.content} />
                  </FillBarrier>
                ) : null}
              </div>
            </section>
          );
        }
        return (
          <section key={section.name} className="tl-side-rail__section">
            <button
              type="button"
              id={`${id}-header`}
              className="tl-side-rail__header tl-type-body-strong"
              aria-expanded={expanded}
              aria-controls={`${id}-body`}
              onClick={() => void command(CMD_TOGGLE_SECTION, { [ARG_NAME]: section.name })}
            >
              <ThemeIcon encoded={ICON_EXPAND} className="tl-icon-sm tl-side-rail__chevron" />
              <span className="tl-side-rail__label">{section.label}</span>
              {section.badge ? <>{' '}<StateBadge badge={section.badge} alone={false} /></> : null}
            </button>
            <div
              id={`${id}-body`}
              className="tl-side-rail__body tl-region"
              role="region"
              aria-labelledby={`${id}-header`}
              hidden={!expanded}
            >
              {section.content ? (
                <FillBarrier>
                  <TLChild control={section.content} />
                </FillBarrier>
              ) : null}
            </div>
          </section>
        );
      })}
    </div>
  );
};

export default TLSideRailSections;
