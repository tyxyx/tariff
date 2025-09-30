import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CalculatorPage from '../page';

jest.mock('react-datepicker', () => ({
  __esModule: true,
  default: ({ onChange, selected, ...props }) => {
    const value = selected instanceof Date && !Number.isNaN(selected.valueOf())
      ? selected.toISOString().split('T')[0]
      : '';

    return (
      <input
        data-testid="date-picker"
        type="date"
        value={value}
        onChange={(event) => onChange(new Date(event.target.value))}
        {...props}
      />
    );
  },
}));

const ORIGINAL_ENV = process.env;
const originalFetch = global.fetch;

const findSummaryItem = (summary, text) => within(summary).getByText((content, element) => {
  return element.tagName.toLowerCase() === 'li' && element.textContent === text;
});

describe('CalculatorPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...ORIGINAL_ENV };
  });

  afterEach(() => {
    if (originalFetch) {
      global.fetch = originalFetch;
    } else {
      delete global.fetch;
    }
  });

  afterAll(() => {
    process.env = ORIGINAL_ENV;
  });

  it('renders placeholder summary values on first load', () => {
    render(<CalculatorPage />);

    const summary = screen.getByRole('list');

    expect(findSummaryItem(summary, 'Product: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Import Country: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Export Country: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Tariff Rate: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Tariff Amount: -')).toBeInTheDocument();
  });

  it('fetches tariff rate and updates summary when inputs are provided', async () => {
    process.env = { ...process.env, NEXT_PUBLIC_API_URL: 'http://mock.api' };

    const mockFetch = jest.fn().mockResolvedValue({
      json: () => Promise.resolve({ tariffRate: 0.2 }),
    });
    global.fetch = mockFetch;

    render(<CalculatorPage />);
    const user = userEvent.setup();

    await user.selectOptions(screen.getByLabelText('Product Name'), 'Laptops');
    await user.selectOptions(screen.getByLabelText('Import Country'), 'China');
    await user.selectOptions(screen.getByLabelText('Export Country'), 'United States');

    const quantityInput = screen.getByLabelText('Quantity');
    await user.clear(quantityInput);
    await user.type(quantityInput, '10');

    const unitPriceInput = screen.getByLabelText('Unit Price');
    await user.clear(unitPriceInput);
    await user.type(unitPriceInput, '200');

    const dateInput = screen.getByTestId('date-picker');
    await user.type(dateInput, '2024-05-01');

    await waitFor(() => expect(mockFetch).toHaveBeenCalledTimes(1));

    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toBe('http://mock.api/api/tariffs/get-particular-tariff-rate');
    expect(options).toMatchObject({
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    });
    expect(JSON.parse(options.body)).toEqual({
      date: '2024-05-01',
      originCountry: 'China',
      destCountry: 'United States',
      productName: 'Laptops',
    });

    const summary = screen.getByRole('list');

    await waitFor(() => {
      expect(findSummaryItem(summary, 'Tariff Rate: 20.00%')).toBeInTheDocument();
    });
    expect(findSummaryItem(summary, 'Quantity: 10')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Unit Price: $200')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Tariff Amount: $400')).toBeInTheDocument();
  });
});
