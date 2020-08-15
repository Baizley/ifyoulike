import React from 'react';
import { render } from '@testing-library/react';
import IfYouLike from './IfYouLike';

test('renders learn react link', () => {
  const { getByText } = render(<IfYouLike />);
  const linkElement = getByText(/learn react/i);
  expect(linkElement).toBeInTheDocument();
});
