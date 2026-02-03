import { useQuery } from '@tanstack/react-query';
import { assetService, Asset } from '@/services/assetService';

export const useAssets = () => {
  return useQuery({
    queryKey: ['assets'],
    queryFn: () => assetService.getAll(),
  });
};

export const useAsset = (assetId: string) => {
  return useQuery({
    queryKey: ['asset', assetId],
    queryFn: () => assetService.getById(assetId),
    enabled: !!assetId,
  });
};

export const usePortfolioAssets = (portfolioId: string) => {
  return useQuery({
    queryKey: ['portfolioAssets', portfolioId],
    queryFn: () => assetService.getByPortfolio(portfolioId),
    enabled: !!portfolioId,
  });
};
