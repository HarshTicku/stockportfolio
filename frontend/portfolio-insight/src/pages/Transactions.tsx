import { useState } from 'react';
import { ArrowUpRight, ArrowDownRight, Filter, RefreshCw } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { formatCurrency } from '@/utils/formatters';
import { format } from 'date-fns';
import { cn } from '@/lib/utils';
import { useQuery } from '@tanstack/react-query';
import { transactionService, Transaction } from '@/services/transactionService';
import { usePortfolios } from '@/hooks/usePortfolios';

const getTransactionBadgeVariant = (type: string) => {
  switch (type) {
    case 'BUY':
      return 'default';
    case 'SELL':
      return 'destructive';
    case 'DEPOSIT':
    case 'DIVIDEND':
    case 'INTEREST':
      return 'secondary';
    case 'WITHDRAWAL':
    case 'FEE':
      return 'outline';
    default:
      return 'outline';
  }
};

const getTransactionIcon = (type: string) => {
  switch (type) {
    case 'BUY':
    case 'DEPOSIT':
    case 'DIVIDEND':
    case 'INTEREST':
      return <ArrowDownRight className="w-4 h-4 text-success" />;
    case 'SELL':
    case 'WITHDRAWAL':
    case 'FEE':
      return <ArrowUpRight className="w-4 h-4 text-destructive" />;
    default:
      return null;
  }
};

const isOutflow = (type: string) => {
  return ['BUY', 'WITHDRAWAL', 'FEE'].includes(type);
};

const Transactions = () => {
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string>('all');

  // Fetch portfolios for filter dropdown
  const { data: portfoliosData, isLoading: portfoliosLoading } = usePortfolios();
  const portfolios = portfoliosData?.content || [];

  // Fetch transactions for selected portfolio
  const { data: transactions, isLoading: transactionsLoading, refetch } = useQuery({
    queryKey: ['transactions', selectedPortfolioId],
    queryFn: async () => {
      if (selectedPortfolioId === 'all') {
        // Fetch transactions from all portfolios
        const allTransactions: Transaction[] = [];
        for (const portfolio of portfolios) {
          try {
            const txns = await transactionService.getByPortfolio(portfolio.portfolioId);
            allTransactions.push(...txns);
          } catch (e) {
            console.error(`Error fetching transactions for ${portfolio.portfolioId}`, e);
          }
        }
        return allTransactions.sort((a, b) => 
          new Date(b.transactionDate).getTime() - new Date(a.transactionDate).getTime()
        );
      }
      return transactionService.getByPortfolio(selectedPortfolioId);
    },
    enabled: portfolios.length > 0 || selectedPortfolioId !== 'all',
  });

  // Calculate totals
  const totalBuys = transactions?.filter(t => t.transactionType === 'BUY')
    .reduce((sum, t) => sum + t.amount, 0) || 0;
  const totalSells = transactions?.filter(t => t.transactionType === 'SELL')
    .reduce((sum, t) => sum + t.amount, 0) || 0;
  const totalDividends = transactions?.filter(t => t.transactionType === 'DIVIDEND')
    .reduce((sum, t) => sum + t.amount, 0) || 0;
  const totalDeposits = transactions?.filter(t => t.transactionType === 'DEPOSIT')
    .reduce((sum, t) => sum + t.amount, 0) || 0;

  const isLoading = portfoliosLoading || transactionsLoading;

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Transactions</h1>
          <p className="text-muted-foreground">View your transaction history</p>
        </div>
        <div className="flex items-center gap-2">
          <Select value={selectedPortfolioId} onValueChange={setSelectedPortfolioId}>
            <SelectTrigger className="w-[200px]">
              <SelectValue placeholder="Select portfolio" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Portfolios</SelectItem>
              {portfolios.map((portfolio) => (
                <SelectItem key={portfolio.portfolioId} value={portfolio.portfolioId}>
                  {portfolio.portfolioName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button variant="outline" size="icon" onClick={() => refetch()}>
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Buys</p>
            {isLoading ? (
              <Skeleton className="h-7 w-24" />
            ) : (
              <p className="text-xl font-bold text-destructive tabular-nums">
                -{formatCurrency(totalBuys)}
              </p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Sells</p>
            {isLoading ? (
              <Skeleton className="h-7 w-24" />
            ) : (
              <p className="text-xl font-bold text-success tabular-nums">
                +{formatCurrency(totalSells)}
              </p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Dividends</p>
            {isLoading ? (
              <Skeleton className="h-7 w-24" />
            ) : (
              <p className="text-xl font-bold text-success tabular-nums">
                +{formatCurrency(totalDividends)}
              </p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Transactions</p>
            {isLoading ? (
              <Skeleton className="h-7 w-16" />
            ) : (
              <p className="text-xl font-bold">{transactions?.length || 0}</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Transactions Table */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : !transactions || transactions.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <p className="mb-2">No transactions found</p>
              <p className="text-sm">Transactions will appear here when you buy or sell assets</p>
            </div>
          ) : (
            <div className="divide-y divide-border">
              {transactions.map((txn) => (
                <div key={txn.transactionId} className="flex items-center justify-between py-4">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center">
                      {getTransactionIcon(txn.transactionType)}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <p className="font-medium">{txn.ticker || txn.transactionType}</p>
                        <Badge variant={getTransactionBadgeVariant(txn.transactionType) as any}>
                          {txn.transactionType}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {txn.quantity && txn.price 
                          ? `${txn.quantity} × ${formatCurrency(txn.price)}`
                          : txn.transactionType
                        }
                      </p>
                    </div>
                  </div>

                  <div className="text-right">
                    <p className={cn(
                      "font-medium tabular-nums",
                      isOutflow(txn.transactionType) ? "text-destructive" : "text-success"
                    )}>
                      {isOutflow(txn.transactionType) ? '-' : '+'}
                      {formatCurrency(txn.amount)}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {format(new Date(txn.transactionDate), 'MMM d, yyyy h:mm a')}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Transactions;
