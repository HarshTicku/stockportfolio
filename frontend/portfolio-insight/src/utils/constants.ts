export const API_BASE_URL = 'http://localhost:8080/api/v1';

export const ASSET_TYPES = {
  STOCK: 'STOCK',
  BOND: 'BOND',
  CASH: 'CASH',
  ETF: 'ETF',
  CRYPTO: 'CRYPTO',
  COMMODITY: 'COMMODITY',
} as const;

export const TRANSACTION_TYPES = {
  BUY: 'BUY',
  SELL: 'SELL',
  DEPOSIT: 'DEPOSIT',
  WITHDRAWAL: 'WITHDRAWAL',
  DIVIDEND: 'DIVIDEND',
  INTEREST: 'INTEREST',
  FEE: 'FEE',
} as const;

export const CHART_PERIODS = [
  { label: '1d', value: '1D' },
  { label: '5d', value: '5D' },
  { label: '1m', value: '1M' },
  { label: '6m', value: '6M' },
  { label: '1y', value: '1Y' },
  { label: '5d', value: '5Y' },
  { label: 'Max', value: 'MAX' },
] as const;

export const CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CAD'] as const;

export const ASSET_TYPE_COLORS: Record<string, string> = {
  STOCK: 'hsl(var(--chart-1))',
  ETF: 'hsl(var(--chart-2))',
  BOND: 'hsl(var(--chart-3))',
  CRYPTO: 'hsl(var(--chart-4))',
  CASH: 'hsl(var(--chart-5))',
  COMMODITY: 'hsl(var(--primary))',
};

export const REFRESH_INTERVAL = 30000; // 30 seconds for real-time updates
