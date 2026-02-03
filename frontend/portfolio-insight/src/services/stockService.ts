import api from './api';

export interface StockQuote {
  symbol: string;
  currentPrice: number;
  change: number;
  changePercent: number;
  highPrice: number;
  lowPrice: number;
  openPrice: number;
  previousClose: number;
  timestamp: number;
}

export interface CompanyProfile {
  symbol: string;
  name: string;
  country: string;
  currency: string;
  exchange: string;
  industry: string;
  logo: string;
  marketCap: number;
  weburl: string;
}

export interface MarketData {
  ticker: string;
  date: string;
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
  change: number;
  changePercent: number;
}

export interface ForecastPoint {
  date: string;
  price: number;
}

export interface StockPrediction {
  ticker: string;
  lastObservedDate: string;
  lastObservedPrice: number;
  forecast: ForecastPoint[];
  confidenceScore: number;
}

export const stockService = {
  getQuote: async (symbol: string): Promise<StockQuote> => {
    const response = await api.get(`/stocks/${symbol}/quote`);
    return response.data;
  },

  getProfile: async (symbol: string): Promise<CompanyProfile> => {
    const response = await api.get(`/stocks/${symbol}/profile`);
    return response.data;
  },

  getMultipleQuotes: async (symbols: string[]): Promise<StockQuote[]> => {
    const response = await api.get(`/stocks/quotes?symbols=${symbols.join(',')}`);
    return response.data;
  },

  search: async (query: string): Promise<{ symbol: string; description: string }[]> => {
    const response = await api.get(`/stocks/search?query=${query}`);
    return response.data;
  },

  getTickers: async (): Promise<string[]> => {
    const response = await api.get('/market-data/tickers');
    return response.data;
  },

  getLatestPrice: async (ticker: string): Promise<MarketData> => {
    const response = await api.get(`/market-data/${ticker}/latest`);
    return response.data;
  },

  getHistory: async (ticker: string, startDate: string, endDate: string): Promise<MarketData[]> => {
    const response = await api.get(`/market-data/${ticker}/history?startDate=${startDate}&endDate=${endDate}`);
    return response.data;
  },

  getPrediction: async (ticker: string): Promise<StockPrediction> => {
    const response = await api.get(`/predictions/${ticker}`);
    return response.data;
  },

  getSupportedPredictionTickers: async (): Promise<{ supportedTickers: string[]; count: number }> => {
    const response = await api.get('/predictions/supported-tickers');
    return response.data;
  },

  checkPredictionSupport: async (ticker: string): Promise<{ ticker: string; supported: boolean }> => {
    const response = await api.get(`/predictions/check/${ticker}`);
    return response.data;
  },
};
