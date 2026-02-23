// Bridge API
export { register, getComponent } from './bridge/registry';
export { connect, subscribe, unsubscribe } from './bridge/sse-client';
export {
  mount,
  mountField,
  unmount,
  useTLState,
  useTLCommand,
  useTLFieldValue,
} from './bridge/tl-react-bridge';
export type { TLCellProps } from './bridge/types';

// Register built-in controls.
import { register } from './bridge/registry';

import TLTextInput from './controls/TLTextInput';
import TLNumberInput from './controls/TLNumberInput';
import TLDatePicker from './controls/TLDatePicker';
import TLSelect from './controls/TLSelect';
import TLCheckbox from './controls/TLCheckbox';
import TLTable from './controls/TLTable';

register('TLTextInput', TLTextInput);
register('TLNumberInput', TLNumberInput);
register('TLDatePicker', TLDatePicker);
register('TLSelect', TLSelect);
register('TLCheckbox', TLCheckbox);
register('TLTable', TLTable);
