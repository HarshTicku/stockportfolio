import { MoreHorizontal, Clock } from 'lucide-react';
import { formatCurrency, formatPercent, formatCompact } from '@/utils/formatters';
import { cn } from '@/lib/utils';

interface StockDetailsProps {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  previousClose: number;
  dayRange: { low: number; high: number };
  yearRange: { low: number; high: number };
  marketCap: number;
  volume: number;
  dividendYield?: number;
  peRatio?: number;
}

export const StockDetails = ({
  symbol,
  name,
  price,
  change,
  changePercent,
  previousClose,
  dayRange,
  yearRange,
  marketCap,
  volume,
  dividendYield,
  peRatio,
}: StockDetailsProps) => {
  const isPositive = change >= 0;

  const detailRows = [
    { label: 'Previous Close', value: formatCurrency(previousClose) },
    { label: 'Day Range', value: `${formatCurrency(dayRange.low)} - ${formatCurrency(dayRange.high)}` },
    { label: 'Year Range', value: `${formatCurrency(yearRange.low)} - ${formatCurrency(yearRange.high)}` },
    { label: 'Market Cap', value: `${formatCompact(marketCap)} USD` },
    { label: 'Volume', value: formatCompact(volume) },
    { label: 'Dividend Yield', value: dividendYield ? `${dividendYield.toFixed(2)}%` : 'N/A' },
    { label: 'P/E Ratio', value: peRatio ? peRatio.toFixed(2) : 'N/A' },
    { label: 'Previous Close', value: 'INDEX' },
  ];

  return (
    <div className="bg-card border border-border rounded-xl p-5">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold">Details</h3>
        <button className="text-muted-foreground hover:text-foreground">
          <MoreHorizontal className="w-5 h-5" />
        </button>
      </div>

      {/* Symbol & Time */}
      <div className="flex items-center justify-between mb-4">
        <span className="font-medium">{symbol}</span>
        <div className="flex items-center gap-1 text-xs text-muted-foreground bg-muted px-2 py-1 rounded-full">
          <Clock className="w-3 h-3" />
          <span>24h</span>
        </div>
      </div>

      {/* Details Grid */}
      <div className="space-y-3">
        {detailRows.map((row, index) => (
          <div key={index} className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">{row.label}</span>
            <span className="font-medium tabular-nums">{row.value}</span>
          </div>
        ))}
      </div>

      {/* Market Cap Card */}
      <div className="mt-6 p-4 bg-muted/50 rounded-xl flex items-center gap-4">
        <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
          <span className="text-primary text-lg font-bold">M</span>
        </div>
        <div>
          <p className="text-sm text-muted-foreground">Market Cap</p>
          <p className="text-xl font-bold">{formatCompact(marketCap)}</p>
        </div>
        {/* Mini bar chart placeholder */}
        <div className="ml-auto flex items-end gap-0.5 h-8">
          {[0.3, 0.5, 0.4, 0.7, 0.6, 0.8, 0.9].map((h, i) => (
            <div 
              key={i} 
              className="w-1.5 bg-primary/60 rounded-t"
              style={{ height: `${h * 100}%` }}
            />
          ))}
        </div>
      </div>
    </div>
  );
};
