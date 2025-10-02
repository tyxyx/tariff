import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CalculatorPage from '../page';

document.createRange = () => ({
  setStart: () => {},
  setEnd: () => {},
  commonAncestorContainer: {
    nodeName: 'BODY',
    ownerDocument: document,
  },
});

document.execCommand = () => {};

jest.mock('@/components/ui/PageHeader', () => () => <div data-testid="page-header" />);

const ORIGINAL_ENV = { ...process.env };
const originalFetch = global.fetch;

const findSummaryItem = (summary, text) => within(summary).getByText((content, element) => {
  return element.tagName.toLowerCase() === 'li' && element.textContent === text;
});

describe('CalculatorPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    process.env = { ...ORIGINAL_ENV };
    localStorage.setItem('userEmail', 'test@example.com');
  });

  afterEach(() => {
    localStorage.clear();
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
      json: () => Promise.resolve({ tariffRate: 0.2, rate: 0.2 }),
    });
    global.fetch = mockFetch;

    render(<CalculatorPage />);
    const user = userEvent.setup();

    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'Laptops');
    await user.selectOptions(selects[1], 'China');
    await user.selectOptions(selects[2], 'United States');

    const numberInputs = screen.getAllByRole('spinbutton');
    await user.clear(numberInputs[0]);
    await user.type(numberInputs[0], '10');
    await user.clear(numberInputs[1]);
    await user.type(numberInputs[1], '200');

    const dateInput = screen.getByTestId('date-picker');
    await user.type(dateInput, '2024-05-01');

    await waitFor(() => expect(mockFetch).toHaveBeenCalledTimes(1));

    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toBe('http://18.139.89.63:8080/api/tariffs/particular-tariff-rate');
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
