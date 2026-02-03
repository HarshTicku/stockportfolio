import { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, MoreHorizontal, TrendingUp, TrendingDown, RefreshCw } from 'lucide-react';
import { PortfolioCard } from '@/components/portfolio/PortfolioCard';
import { PerformanceChart } from '@/components/charts/PerformanceChart';
import { AllocationChart } from '@/components/charts/AllocationChart';
import { StockDetails } from '@/components/market/StockDetails';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { CHART_PERIODS, REFRESH_INTERVAL } from '@/utils/constants';
import { formatCurrency, formatPercent } from '@/utils/formatters';
import { cn } from '@/lib/utils';
import { usePortfolios, usePortfolioSummary } from '@/hooks/usePortfolios';
import { useQuery } from '@tanstack/react-query';
import { stockService } from '@/services/stockService';

const Dashboard = () => {
  const [selectedPeriod, setSelectedPeriod] = useState('6m');
  const [selectedPortfolioId, setSelectedPortfolioId] = useState<string | null>(null);
  
  // Fetch portfolios
  const { data: portfoliosData, isLoading: portfoliosLoading, refetch: refetchPortfolios } = usePortfolios();
  const portfolios = portfoliosData?.content || [];
  
  // Set first portfolio as selected by default
  useEffect(() => {
    if (portfolios.length > 0 && !selectedPortfolioId) {
      setSelectedPortfolioId(portfolios[0].portfolioId);
    }
  }, [portfolios, selectedPortfolioId]);
  
  // Fetch selected portfolio summary
  const { data: portfolioSummary, isLoading: summaryLoading } = usePortfolioSummary(selectedPortfolioId || '');
  
  // Fetch real-time stock quotes for holdings
  const tickers = portfolioSummary?.topHoldings?.map(h => h.ticker) || [];
  const { data: stockQuotes, isLoading: quotesLoading } = useQuery({
    queryKey: ['stockQuotes', tickers],
    queryFn: () => stockService.getMultipleQuotes(tickers),
    enabled: tickers.length > 0,
    refetchInterval: REFRESH_INTERVAL,
  });
  
  // Calculate totals across all portfolios
  const totalValue = portfolios.reduce((sum, p) => sum + (p.totalValue || 0), 0);
  const totalCash = portfolios.reduce((sum, p) => sum + (p.cashBalance || 0), 0);
  const totalAssets = portfolios.reduce((sum, p) => sum + (p.assetCount || 0), 0);
  
  // Create chart data from holdings
  const chartData = portfolioSummary?.topHoldings?.map((holding, index) => ({
    date: holding.ticker,
    value: holding.totalValue || 0,
  })) || [];
  
  // Allocation data for pie chart
  const allocationData = portfolioSummary?.allocation || [];
  
  const isLoading = portfoliosLoading || summaryLoading;

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Summary Cards */}
      <section className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Portfolio Value</p>
            {isLoading ? (
              <Skeleton className="h-8 w-32" />
            ) : (
              <>
                <p className="text-2xl font-bold tabular-nums">{formatCurrency(totalValue)}</p>
                <p className="text-xs text-muted-foreground mt-1">{portfolios.length} portfolios</p>
              </>
            )}
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Gain/Loss</p>
            {summaryLoading ? (
              <Skeleton className="h-8 w-32" />
            ) : (
              <>
                <p className={cn(
                  "text-2xl font-bold tabular-nums",
                  (portfolioSummary?.totalGain || 0) >= 0 ? "text-success" : "text-destructive"
                )}>
                  {formatCurrency(portfolioSummary?.totalGain || 0)}
                </p>
                <p className={cn(
                  "text-sm font-medium",
                  (portfolioSummary?.totalGainPercent || 0) >= 0 ? "text-success" : "text-destructive"
                )}>
                  {formatPercent(portfolioSummary?.totalGainPercent || 0)}
                </p>
              </>
            )}
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Cash Balance</p>
            {isLoading ? (
              <Skeleton className="h-8 w-32" />
            ) : (
              <>
                <p className="text-2xl font-bold tabular-nums">{formatCurrency(totalCash)}</p>
                <p className="text-xs text-muted-foreground mt-1">Available to invest</p>
              </>
            )}
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Holdings</p>
            {isLoading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <>
                <p className="text-2xl font-bold">{totalAssets}</p>
                <p className="text-xs text-muted-foreground mt-1">Across all portfolios</p>
              </>
            )}
          </CardContent>
        </Card>
      </section>

      {/* Portfolio Cards Carousel */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">Your Portfolios</h2>
          <button 
            onClick={() => refetchPortfolios()}
            className="text-muted-foreground hover:text-foreground flex items-center gap-1 text-sm"
          >
            <RefreshCw className="w-4 h-4" />
            Refresh
          </button>
        </div>

        {portfoliosLoading ? (
          <div className="flex gap-4">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="w-[260px] h-[180px] rounded-xl" />
            ))}
          </div>
        ) : portfolios.length === 0 ? (
          <Card className="p-8 text-center">
            <p className="text-muted-foreground">No portfolios found. Create your first portfolio!</p>
          </Card>
        ) : (
          <div className="relative">
            <div className="flex gap-4 overflow-x-auto pb-2">
              {portfolios.map((portfolio) => {
                // Find matching stock quote for sparkline
                const quote = stockQuotes?.find(q => 
                  portfolioSummary?.topHoldings?.some(h => h.ticker === q.symbol)
                );
                
                return (
                  <div 
                    key={portfolio.portfolioId} 
                    className={cn(
                      "flex-shrink-0 w-[260px] cursor-pointer transition-all",
                      selectedPortfolioId === portfolio.portfolioId && "ring-2 ring-primary rounded-xl"
                    )}
                    onClick={() => setSelectedPortfolioId(portfolio.portfolioId)}
                  >
                    <PortfolioCard
                      ticker={portfolio.portfolioName.substring(0, 4).toUpperCase()}
                      name={portfolio.portfolioName}
                      price={portfolio.totalValue}
                      change={portfolio.assetsValue || 0}
                      changePercent={portfolio.assetCount > 0 ? (portfolio.assetsValue / portfolio.totalValue) * 100 : 0}
                      portfolioValue={portfolio.totalValue}
                      sparklineData={[100, 102, 98, 105, 103, 108, 110, 112, 108, 115]}
                    />
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </section>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Holdings Chart */}
        <div className="lg:col-span-2 bg-card border border-border rounded-xl p-5">
          <div className="flex items-center justify-between mb-6">
            <h3 className="font-semibold">Portfolio Holdings</h3>
            <div className="flex gap-1 bg-muted/50 rounded-lg p-1">
              {CHART_PERIODS.slice(0, 5).map((period) => (
                <button
                  key={period.value}
                  onClick={() => setSelectedPeriod(period.value.toLowerCase())}
                  className={cn(
                    "px-3 py-1.5 text-xs font-medium rounded-md transition-colors",
                    selectedPeriod === period.value.toLowerCase()
                      ? "bg-card text-foreground shadow-sm"
                      : "text-muted-foreground hover:text-foreground"
                  )}
                >
                  {period.label}
                </button>
              ))}
            </div>
          </div>

          {summaryLoading ? (
            <Skeleton className="h-[300px] w-full" />
          ) : (
            <>
              <div className="mb-6">
                <div className="flex items-baseline gap-3">
                  <span className="text-3xl font-bold tabular-nums">
                    {formatCurrency(portfolioSummary?.totalValue || 0)}
                  </span>
                  <span className={cn(
                    "text-sm font-medium",
                    (portfolioSummary?.totalGainPercent || 0) >= 0 ? "text-success" : "text-destructive"
                  )}>
                    {formatPercent(portfolioSummary?.totalGainPercent || 0)}
                  </span>
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {portfolioSummary?.portfolioName || 'Select a portfolio'}
                </p>
              </div>
              <PerformanceChart data={chartData} height={280} />
            </>
          )}
        </div>

        {/* Allocation Chart */}
        <div className="lg:col-span-1 bg-card border border-border rounded-xl p-5">
          <h3 className="font-semibold mb-4">Asset Allocation</h3>
          {summaryLoading ? (
            <Skeleton className="h-[300px] w-full" />
          ) : allocationData.length > 0 ? (
            <AllocationChart data={allocationData} />
          ) : (
            <div className="h-[300px] flex items-center justify-center text-muted-foreground">
              No allocation data
            </div>
          )}
        </div>
      </div>

      {/* Holdings Table */}
      <section className="bg-card border border-border rounded-xl p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold">Top Holdings</h3>
          <a href="/portfolios" className="text-sm text-primary hover:underline">
            View All →
          </a>
        </div>

        {summaryLoading ? (
          <div className="space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        ) : portfolioSummary?.topHoldings && portfolioSummary.topHoldings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-2 text-sm font-medium text-muted-foreground">Symbol</th>
                  <th className="text-left py-3 px-2 text-sm font-medium text-muted-foreground">Name</th>
                  <th className="text-right py-3 px-2 text-sm font-medium text-muted-foreground">Quantity</th>
                  <th className="text-right py-3 px-2 text-sm font-medium text-muted-foreground">Price</th>
                  <th className="text-right py-3 px-2 text-sm font-medium text-muted-foreground">Value</th>
                  <th className="text-right py-3 px-2 text-sm font-medium text-muted-foreground">Gain/Loss</th>
                  <th className="text-right py-3 px-2 text-sm font-medium text-muted-foreground">Allocation</th>
                </tr>
              </thead>
              <tbody>
                {portfolioSummary.topHoldings.map((holding) => {
                  const quote = stockQuotes?.find(q => q.symbol === holding.ticker);
                  const livePrice = quote?.currentPrice || holding.currentPrice;
                  const isPositive = holding.gainLossPercentage >= 0;
                  
                  return (
                    <tr key={holding.assetId} className="border-b border-border/50 hover:bg-muted/50">
                      <td className="py-3 px-2">
                        <span className="font-semibold">{holding.ticker}</span>
                      </td>
                      <td className="py-3 px-2 text-muted-foreground">{holding.assetName}</td>
                      <td className="py-3 px-2 text-right tabular-nums">{holding.quantity}</td>
                      <td className="py-3 px-2 text-right tabular-nums">{formatCurrency(livePrice)}</td>
                      <td className="py-3 px-2 text-right tabular-nums font-medium">
                        {formatCurrency(holding.totalValue)}
                      </td>
                      <td className={cn(
                        "py-3 px-2 text-right tabular-nums font-medium",
                        isPositive ? "text-success" : "text-destructive"
                      )}>
                        <div className="flex items-center justify-end gap-1">
                          {isPositive ? <TrendingUp className="w-3 h-3" /> : <TrendingDown className="w-3 h-3" />}
                          {formatPercent(holding.gainLossPercentage)}
                        </div>
                      </td>
                      <td className="py-3 px-2 text-right tabular-nums">
                        {holding.allocation?.toFixed(1)}%
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-8 text-muted-foreground">
            No holdings in this portfolio
          </div>
        )}
      </section>
    </div>
  );
};

export default Dashboard;
