import api from './api';

export interface Portfolio {
  portfolioId: string;
  portfolioName: string;
  description: string;
  baseCurrency: string;
  totalValue: number;
  cashBalance: number;
  assetsValue: number;
  assetCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioSummary {
  portfolioId: string;
  portfolioName: string;
  baseCurrency: string;
  totalValue: number;
  cashBalance: number;
  assetsValue: number;
  totalGain: number;
  totalGainPercent: number;
  assetCount: number;
  transactionCount: number;
  topHoldings: AssetHolding[];
  allocation: AllocationItem[];
}

export interface AssetHolding {
  assetId: string;
  ticker: string;
  assetName: string;
  assetType: string;
  quantity: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
  gainLossPercentage: number;
  allocation: number;
}

export interface AllocationItem {
  assetType: string;
  value: number;
  percentage: number;
}

export interface CreatePortfolioRequest {
  portfolioName: string;
  description?: string;
  baseCurrency: string;
  cashBalance: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

export const portfolioService = {
  getAll: async (): Promise<PaginatedResponse<Portfolio>> => {
    const response = await api.get('/portfolios');
    return response.data;
  },

  getById: async (portfolioId: string): Promise<Portfolio> => {
    const response = await api.get(`/portfolios/${portfolioId}`);
    return response.data;
  },

  getSummary: async (portfolioId: string): Promise<PortfolioSummary> => {
    const response = await api.get(`/portfolios/${portfolioId}/summary`);
    return response.data;
  },

  create: async (data: CreatePortfolioRequest): Promise<Portfolio> => {
    const response = await api.post('/portfolios', data);
    return response.data;
  },

  update: async (portfolioId: string, data: Partial<CreatePortfolioRequest>): Promise<Portfolio> => {
    const response = await api.put(`/portfolios/${portfolioId}`, data);
    return response.data;
  },

  delete: async (portfolioId: string): Promise<void> => {
    await api.delete(`/portfolios/${portfolioId}`);
  },
};
