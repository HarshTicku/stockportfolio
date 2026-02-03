import api from './api';

export interface Asset {
  assetId: string;
  portfolioId: string;
  ticker: string;
  assetName: string;
  assetType: string;
  quantity: number;
  purchasePrice: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
  gainLossPercentage: number;
  allocation: number;
  purchaseDate: string;
  notes?: string;
}

export interface CreateAssetRequest {
  portfolioId: string;
  ticker: string;
  assetName: string;
  assetType: string;
  quantity: number;
  purchasePrice: number;
  purchaseDate: string;
  notes?: string;
}

export const assetService = {
  getAll: async (): Promise<Asset[]> => {
    const response = await api.get('/assets');
    // Backend returns paginated data, extract content array
    return response.data.content || response.data;
  },

  getById: async (assetId: string): Promise<Asset> => {
    const response = await api.get(`/assets/${assetId}`);
    return response.data;
  },

  getByPortfolio: async (portfolioId: string): Promise<Asset[]> => {
    const response = await api.get(`/assets/portfolio/${portfolioId}`);
    return response.data;
  },

  create: async (data: CreateAssetRequest): Promise<Asset> => {
    const response = await api.post('/assets', data);
    return response.data;
  },

  update: async (assetId: string, data: Partial<CreateAssetRequest>): Promise<Asset> => {
    const response = await api.put(`/assets/${assetId}`, data);
    return response.data;
  },

  delete: async (assetId: string): Promise<void> => {
    await api.delete(`/assets/${assetId}`);
  },
};
