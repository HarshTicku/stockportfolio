import { ChevronRight } from 'lucide-react';
import { SparklineChart } from '@/components/charts/SparklineChart';
import { formatCurrency, formatPercent } from '@/utils/formatters';
import { cn } from '@/lib/utils';

interface PortfolioCardProps {
  ticker: string;
  name: string;
  logo?: string;
  price: number;
  change: number;
  changePercent: number;
  portfolioValue: number;
  sparklineData: number[];
  onClick?: () => void;
}

export const PortfolioCard = ({
  ticker,
  name,
  logo,
  price,
  change,
  changePercent,
  portfolioValue,
  sparklineData,
  onClick,
}: PortfolioCardProps) => {
  const isPositive = changePercent >= 0;

  return (
    <div
      onClick={onClick}
      className="bg-card border border-border rounded-xl p-4 hover:shadow-card-hover hover:-translate-y-0.5 transition-all cursor-pointer"
    >
      <div className="flex items-start justify-between mb-4">
        {/* Stock Badge */}
        <div className="flex items-center gap-2">
          <div className="bg-foreground text-background px-3 py-1.5 rounded-full flex items-center gap-2">
            {logo ? (
              <img src={logo} alt={name} className="w-4 h-4 rounded" />
            ) : (
              <div className="w-4 h-4 bg-background/20 rounded flex items-center justify-center">
                <span className="text-[8px] font-bold">{ticker.slice(0, 2)}</span>
              </div>
            )}
            <span className="text-xs font-medium">{name}</span>
          </div>
        </div>

        {/* Price Info */}
        <div className="text-right">
          <p className="text-sm font-semibold">{ticker}</p>
          <p className={cn(
            "text-xs font-medium",
            isPositive ? "text-success" : "text-destructive"
          )}>
            {formatPercent(changePercent)}
          </p>
        </div>
      </div>

      {/* Sparkline */}
      <div className="h-10 mb-3">
        <SparklineChart data={sparklineData} positive={isPositive} />
      </div>

      {/* Portfolio Value */}
      <div className="flex items-center justify-between">
        <span className="text-xs text-muted-foreground">Portfolio</span>
        <span className="text-sm font-semibold tabular-nums">
          {formatCurrency(portfolioValue)}
        </span>
      </div>
    </div>
  );
};
