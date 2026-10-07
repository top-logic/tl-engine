import { React } from 'tl-react-bridge';

export interface FormLayoutContextValue {
  readOnly: boolean;
  resolvedLabelPosition: 'side' | 'top';
  /** Whether the content is rendered inside a form layout. */
  insideForm: boolean;
}

const defaultContext: FormLayoutContextValue = {
  readOnly: false,
  resolvedLabelPosition: 'side',
  insideForm: false,
};

export const FormLayoutContext = React.createContext<FormLayoutContextValue>(defaultContext);
