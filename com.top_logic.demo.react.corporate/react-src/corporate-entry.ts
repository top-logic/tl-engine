// Attaches the example component library (react-src/example-lib) to the TopLogic React UI.
//
// Both registrations run when this bundle loads, before the controls of the page are mounted:
//
// - The root wrapper puts the library's provider above every React root of the bridge, so each
//   library component finds its brand settings.
// - The replacements render the state of the TopLogic button and checkbox controls with adapters
//   around the library's components. All other controls keep their TopLogic components.

import { React, registerRootWrapper, replace } from 'tl-react-bridge';
import { BrandProvider } from './example-lib';
import BrandButtonAdapter from './adapters/BrandButtonAdapter';
import BrandCheckboxAdapter from './adapters/BrandCheckboxAdapter';

/** The brand settings of the corporate design. */
const ACME_BRAND = { name: 'acme', accent: '#0e7c66', corners: 'pill' } as const;

/** Provides the corporate brand settings to all library components of a React root. */
function AcmeBrand({ children }: { children?: React.ReactNode }) {
  return React.createElement(BrandProvider, { ...ACME_BRAND, children });
}

registerRootWrapper(AcmeBrand);
replace('TLButton', BrandButtonAdapter);
replace('TLCheckbox', BrandCheckboxAdapter);
