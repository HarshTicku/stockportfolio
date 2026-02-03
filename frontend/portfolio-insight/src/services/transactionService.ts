import api from './api';

export interface Transaction {
  transactionId: string;
  portfolioId: string;
  ticker: string;
  transactionType: string;
  quantity: number;
  price: number;
  amount: number;
  currency: string;
  status: string;
  transactionDate: string;
}

export interface CreateTransactionRequest {
  portfolioId: string;
  ticker: string;
  transactionType: string;
  quantity: number;
  price: number;
  amount: number;
}

export const transactionService = {
  getByPortfolio: async (portfolioId: string): Promise<Transaction[]> => {
    const response = await api.get(`/transactions/portfolio/${portfolioId}`);
    return response.data;
  },

  create: async (data: CreateTransactionRequest): Promise<Transaction> => {
    const response = await api.post('/transactions', data);
    return response.data;
  },
};
