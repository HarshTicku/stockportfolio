import { useQuery } from '@tanstack/react-query';
import { stockService, StockQuote, CompanyProfile, StockPrediction } from '@/services/stockService';
import { REFRESH_INTERVAL } from '@/utils/constants';

// Tickers that have AI prediction models available
const PREDICTION_SUPPORTED_TICKERS = ['AAPL', 'GOOG', 'MSFT', 'AMZN', 'META', 'NFLX'];

export const useStockQuote = (symbol: string) => {
  return useQuery({
    queryKey: ['stockQuote', symbol],
    queryFn: () => stockService.getQuote(symbol),
    enabled: !!symbol,
    refetchInterval: REFRESH_INTERVAL,
  });
};

export const useStockProfile = (symbol: string) => {
  return useQuery({
    queryKey: ['stockProfile', symbol],
    queryFn: () => stockService.getProfile(symbol),
    enabled: !!symbol,
  });
};

export const useMultipleQuotes = (symbols: string[]) => {
  return useQuery({
    queryKey: ['multipleQuotes', symbols],
    queryFn: () => stockService.getMultipleQuotes(symbols),
    enabled: symbols.length > 0,
    refetchInterval: REFRESH_INTERVAL,
  });
};

export const useStockSearch = (query: string) => {
  return useQuery({
    queryKey: ['stockSearch', query],
    queryFn: () => stockService.search(query),
    enabled: query.length >= 2,
  });
};

export const useStockPrediction = (ticker: string) => {
  const isSupported = PREDICTION_SUPPORTED_TICKERS.includes(ticker.toUpperCase());
  
  return useQuery({
    queryKey: ['stockPrediction', ticker],
    queryFn: () => stockService.getPrediction(ticker),
    enabled: !!ticker && isSupported,
    staleTime: 5 * 60 * 1000, // Cache for 5 minutes (predictions don't change frequently)
    retry: 1, // Only retry once if prediction service is down
  });
};

export const isPredictionSupported = (ticker: string): boolean => {
  return PREDICTION_SUPPORTED_TICKERS.includes(ticker.toUpperCase());
};
