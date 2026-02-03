import { useState, useEffect } from 'react';
import { Search, TrendingUp, TrendingDown, Star, Plus, RefreshCw, Loader2 } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { PerformanceChart } from '@/components/charts/PerformanceChart';
import { StockPredictionCard } from '@/components/market/StockPredictionCard';
import { formatCurrency, formatPercent, formatCompact } from '@/utils/formatters';
import { REFRESH_INTERVAL } from '@/utils/constants';
import { cn } from '@/lib/utils';
import { useQuery } from '@tanstack/react-query';
import { stockService, StockQuote, CompanyProfile } from '@/services/stockService';

// Default watchlist tickers
const DEFAULT_TICKERS = ['AAPL', 'GOOG', 'MSFT', 'AMZN', 'META', 'NFLX'];

const Market = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [watchlist, setWatchlist] = useState<string[]>(['AAPL', 'GOOG', 'MSFT']);

  // Fetch all tickers from backend
  const { data: availableTickers } = useQuery({
    queryKey: ['tickers'],
    queryFn: () => stockService.getTickers(),
  });

  // Fetch quotes for watchlist
  const { data: stockQuotes, isLoading: quotesLoading, refetch: refetchQuotes } = useQuery({
    queryKey: ['stockQuotes', DEFAULT_TICKERS],
    queryFn: () => stockService.getMultipleQuotes(DEFAULT_TICKERS),
    refetchInterval: REFRESH_INTERVAL,
  });

  // Fetch selected stock quote
  const { data: selectedQuote, isLoading: quoteLoading } = useQuery({
    queryKey: ['stockQuote', selectedSymbol],
    queryFn: () => stockService.getQuote(selectedSymbol),
    enabled: !!selectedSymbol,
    refetchInterval: REFRESH_INTERVAL,
  });

  // Fetch selected stock profile
  const { data: selectedProfile, isLoading: profileLoading } = useQuery({
    queryKey: ['stockProfile', selectedSymbol],
    queryFn: () => stockService.getProfile(selectedSymbol),
    enabled: !!selectedSymbol,
  });

  // Search stocks
  const { data: searchResults, isLoading: searchLoading } = useQuery({
    queryKey: ['stockSearch', searchQuery],
    queryFn: () => stockService.search(searchQuery),
    enabled: searchQuery.length >= 2,
  });

  // Filter stocks based on search
  const displayStocks = searchQuery.length >= 2 && searchResults?.length 
    ? searchResults.slice(0, 10)
    : stockQuotes || [];

  // Generate mock chart data (in real app, this would come from historical API)
  const chartData = selectedQuote ? [
    { date: 'Open', value: selectedQuote.openPrice },
    { date: 'Low', value: selectedQuote.lowPrice },
    { date: 'High', value: selectedQuote.highPrice },
    { date: 'Current', value: selectedQuote.currentPrice },
  ] : [];

  const toggleWatchlist = (symbol: string) => {
    setWatchlist(prev => 
      prev.includes(symbol) 
        ? prev.filter(s => s !== symbol)
        : [...prev, symbol]
    );
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Market</h1>
          <p className="text-muted-foreground">Real-time stock data from Finnhub</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => refetchQuotes()} className="gap-2">
          <RefreshCw className="w-4 h-4" />
          Refresh
        </Button>
      </div>

      {/* Search */}
      <div className="relative max-w-xl">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search stocks by symbol or name..."
          className="pl-10"
        />
        {searchLoading && (
          <Loader2 className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 animate-spin text-muted-foreground" />
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Stock List */}
        <div className="lg:col-span-2 space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>
                {searchQuery.length >= 2 ? 'Search Results' : 'Popular Stocks'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              {quotesLoading ? (
                <div className="space-y-4">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <Skeleton key={i} className="h-16 w-full" />
                  ))}
                </div>
              ) : displayStocks.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  {searchQuery.length >= 2 ? 'No stocks found' : 'No stock data available'}
                </div>
              ) : (
                <div className="divide-y divide-border">
                  {displayStocks.map((stock: any) => {
                    // Handle both quote objects and search result objects
                    const symbol = stock.symbol;
                    const name = stock.description || stock.name || symbol;
                    const price = stock.currentPrice || 0;
                    const change = stock.change || 0;
                    const changePercent = stock.changePercent || 0;
                    const isPositive = changePercent >= 0;
                    const isWatched = watchlist.includes(symbol);

                    return (
                      <div
                        key={symbol}
                        onClick={() => setSelectedSymbol(symbol)}
                        className={cn(
                          "flex items-center justify-between py-4 cursor-pointer hover:bg-muted/50 -mx-4 px-4 transition-colors",
                          selectedSymbol === symbol && "bg-muted/50"
                        )}
                      >
                        <div className="flex items-center gap-4">
                          <div className="w-10 h-10 rounded-full bg-muted flex items-center justify-center">
                            <span className="text-xs font-bold">{symbol.slice(0, 2)}</span>
                          </div>
                          <div>
                            <p className="font-medium">{symbol}</p>
                            <p className="text-sm text-muted-foreground line-clamp-1">{name}</p>
                          </div>
                        </div>

                        <div className="flex items-center gap-6">
                          {price > 0 && (
                            <div className="text-right">
                              <p className="font-medium tabular-nums">{formatCurrency(price)}</p>
                              <p className={cn(
                                "text-sm font-medium",
                                isPositive ? "text-success" : "text-destructive"
                              )}>
                                {isPositive ? '+' : ''}{formatCurrency(change)} ({formatPercent(changePercent)})
                              </p>
                            </div>
                          )}
                          <button 
                            onClick={(e) => {
                              e.stopPropagation();
                              toggleWatchlist(symbol);
                            }}
                            className={cn(
                              "p-2 rounded-full hover:bg-muted transition-colors",
                              isWatched && "text-warning"
                            )}
                          >
                            <Star className={cn("w-4 h-4", isWatched && "fill-current")} />
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Selected Stock Detail */}
        <div className="space-y-4">
          {quoteLoading ? (
            <Card>
              <CardContent className="pt-6">
                <Skeleton className="h-12 w-full mb-4" />
                <Skeleton className="h-8 w-32 mb-2" />
                <Skeleton className="h-4 w-24 mb-4" />
                <Skeleton className="h-[150px] w-full mb-4" />
                <Skeleton className="h-10 w-full" />
              </CardContent>
            </Card>
          ) : selectedQuote ? (
            <>
              <Card>
                <CardContent className="pt-6">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                      {selectedProfile?.logo ? (
                        <img 
                          src={selectedProfile.logo} 
                          alt={selectedSymbol}
                          className="w-12 h-12 rounded-full object-cover"
                          onError={(e) => {
                            (e.target as HTMLImageElement).style.display = 'none';
                          }}
                        />
                      ) : (
                        <div className="w-12 h-12 rounded-full bg-muted flex items-center justify-center">
                          <span className="font-bold">{selectedSymbol.slice(0, 2)}</span>
                        </div>
                      )}
                      <div>
                        <p className="font-semibold">{selectedSymbol}</p>
                        <p className="text-sm text-muted-foreground">
                          {selectedProfile?.name || selectedSymbol}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="mb-4">
                    <p className="text-3xl font-bold tabular-nums">
                      {formatCurrency(selectedQuote.currentPrice)}
                    </p>
                    <p className={cn(
                      "text-sm font-medium mt-1 flex items-center gap-1",
                      selectedQuote.changePercent >= 0 ? "text-success" : "text-destructive"
                    )}>
                      {selectedQuote.changePercent >= 0 ? (
                        <TrendingUp className="w-4 h-4" />
                      ) : (
                        <TrendingDown className="w-4 h-4" />
                      )}
                      {selectedQuote.changePercent >= 0 ? '+' : ''}
                      {formatCurrency(selectedQuote.change)} ({formatPercent(selectedQuote.changePercent)})
                    </p>
                  </div>

                  <PerformanceChart data={chartData} height={150} />

                  <Button className="w-full mt-4 gap-2">
                    <Plus className="w-4 h-4" />
                    Add to Portfolio
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardContent className="pt-6 space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Open</span>
                    <span className="font-medium tabular-nums">{formatCurrency(selectedQuote.openPrice)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Previous Close</span>
                    <span className="font-medium tabular-nums">{formatCurrency(selectedQuote.previousClose)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Day High</span>
                    <span className="font-medium tabular-nums">{formatCurrency(selectedQuote.highPrice)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Day Low</span>
                    <span className="font-medium tabular-nums">{formatCurrency(selectedQuote.lowPrice)}</span>
                  </div>
                  {selectedProfile && (
                    <>
                      <div className="border-t border-border pt-3 mt-3">
                        <div className="flex justify-between text-sm">
                          <span className="text-muted-foreground">Market Cap</span>
                          <span className="font-medium">{formatCompact(selectedProfile.marketCap * 1000000)}</span>
                        </div>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Industry</span>
                        <span className="font-medium">{selectedProfile.industry}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Exchange</span>
                        <span className="font-medium">{selectedProfile.exchange}</span>
                      </div>
                      {selectedProfile.weburl && (
                        <a 
                          href={selectedProfile.weburl} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-sm text-primary hover:underline block"
                        >
                          Visit Website →
                        </a>
                      )}
                    </>
                  )}
                </CardContent>
              </Card>

              {/* AI Prediction Card */}
              <StockPredictionCard 
                ticker={selectedSymbol} 
                currentPrice={selectedQuote.currentPrice} 
              />
            </>
          ) : (
            <Card>
              <CardContent className="pt-6 text-center text-muted-foreground">
                Select a stock to view details
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default Market;
