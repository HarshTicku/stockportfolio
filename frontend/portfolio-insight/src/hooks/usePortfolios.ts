import { useQuery } from '@tanstack/react-query';
import { portfolioService, Portfolio, PortfolioSummary } from '@/services/portfolioService';

export const usePortfolios = () => {
  return useQuery({
    queryKey: ['portfolios'],
    queryFn: () => portfolioService.getAll(),
  });
};

export const usePortfolio = (portfolioId: string) => {
  return useQuery({
    queryKey: ['portfolio', portfolioId],
    queryFn: () => portfolioService.getById(portfolioId),
    enabled: !!portfolioId,
  });
};

export const usePortfolioSummary = (portfolioId: string) => {
  return useQuery({
    queryKey: ['portfolioSummary', portfolioId],
    queryFn: () => portfolioService.getSummary(portfolioId),
    enabled: !!portfolioId,
  });
};
