import { render, screen, waitFor, within, fireEvent } from '@testing-library/react';
import CalculatorPage from '../page';

jest.mock('@/components/ui/PageHeader', () => () => <div data-testid="page-header" />);

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
    // Mock fetch for countries and products
    global.fetch = jest.fn((url) => {
      if (url.includes('/api/countries')) {
        return Promise.resolve({
          json: () => Promise.resolve([{ name: 'China' }, { name: 'United States' }]),
        });
      }
      if (url.includes('/api/products')) {
        return Promise.resolve({
          json: () => Promise.resolve([{ name: 'Laptops' }]),
        });
      }
      return Promise.reject(new Error('Unknown URL'));
    });

    render(<CalculatorPage />);

    const summary = screen.getByRole('list');

    expect(findSummaryItem(summary, 'Product: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Import Country: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Export Country: -')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Calculation Date: -')).toBeInTheDocument();
  });

  it('fetches tariff rate and updates summary when inputs are provided', async () => {
    process.env = { ...process.env, NEXT_PUBLIC_API_URL: 'http://mock.api' };

    const mockFetch = jest.fn((url) => {
      if (url.includes('/api/countries')) {
        return Promise.resolve({
          json: () => Promise.resolve([{ name: 'China' }, { name: 'United States' }]),
        });
      }
      if (url.includes('/api/products')) {
        return Promise.resolve({
          json: () => Promise.resolve([{ name: 'Laptops' }]),
        });
      }
      if (url.includes('/api/tariffs/particular-tariff-rate')) {
        return Promise.resolve({
          json: () => Promise.resolve({ adValoremRate: 0.2, specificRate: 0 }),
        });
      }
      return Promise.reject(new Error('Unknown URL'));
    });
    global.fetch = mockFetch;

    render(<CalculatorPage />);
    
    // Wait for initial fetches to complete
    await waitFor(() => expect(mockFetch).toHaveBeenCalledTimes(2));
    
    const selects = screen.getAllByRole('combobox');
    fireEvent.change(selects[0], { target: { value: 'Laptops' } });
    fireEvent.change(selects[1], { target: { value: 'China' } });
    fireEvent.change(selects[2], { target: { value: 'United States' } });

    const numberInputs = screen.getAllByRole('spinbutton');
    fireEvent.change(numberInputs[0], { target: { value: '10' } });
    fireEvent.change(numberInputs[1], { target: { value: '200' } });

    const dateInput = screen.getByTestId('date-picker');
    fireEvent.change(dateInput, { target: { value: '2024-05-01' } });

    // Wait for the tariff rate fetch (3rd call: countries, products, then tariff)
    await waitFor(
      () => {
        const tariffCalls = mockFetch.mock.calls.filter(call => 
          call[0].includes('/api/tariffs/particular-tariff-rate')
        );
        expect(tariffCalls.length).toBeGreaterThanOrEqual(1);
      },
      { timeout: 3000 }
    );

    // Find the tariff API call
    const tariffCall = mockFetch.mock.calls.find(call => 
      call[0].includes('/api/tariffs/particular-tariff-rate')
    );
    const [url, options] = tariffCall;
    expect(url).toBe(`http://${process.env.NEXT_PUBLIC_BACKEND_EC2_HOST}:8080/api/tariffs/particular-tariff-rate`);
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
      expect(findSummaryItem(summary, 'Ad Valorem Rate (%): 0.2')).toBeInTheDocument();
    });
    expect(findSummaryItem(summary, 'Quantity: 10')).toBeInTheDocument();
    expect(findSummaryItem(summary, 'Unit Price: $200')).toBeInTheDocument();
    // Ad Valorem Duty Amount = (0.2 / 100) * (200 * 10) = 0.002 * 2000 = 4
    expect(findSummaryItem(summary, 'Ad Valorem Duty Amount: 4')).toBeInTheDocument();
  });
});
