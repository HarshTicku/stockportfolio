import { 
  Area, 
  AreaChart, 
  ResponsiveContainer, 
  XAxis, 
  YAxis, 
  Tooltip,
  CartesianGrid,
  ReferenceLine,
  Line,
  ComposedChart
} from 'recharts';
import { formatCurrency } from '@/utils/formatters';
import { ForecastPoint } from '@/services/stockService';

interface PredictionChartProps {
  currentPrice: number;
  currentDate: string;
  forecast: ForecastPoint[];
  height?: number;
}

interface ChartDataPoint {
  date: string;
  displayDate: string;
  actual?: number;
  predicted?: number;
  isPrediction: boolean;
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    const dataPoint = payload[0]?.payload;
    const isPrediction = dataPoint?.isPrediction;
    const value = dataPoint?.predicted ?? dataPoint?.actual;
    
    return (
      <div className="bg-foreground/90 backdrop-blur-sm text-background px-3 py-2 rounded-lg shadow-lg">
        <p className="text-xs text-muted mb-1">{label}</p>
        <p className="text-sm font-semibold tabular-nums">
          {formatCurrency(value)}
        </p>
        <p className="text-xs mt-1 opacity-70">
          {isPrediction ? 'AI Prediction' : 'Last Price'}
        </p>
      </div>
    );
  }
  return null;
};

export const PredictionChart = ({ 
  currentPrice, 
  currentDate, 
  forecast, 
  height = 200 
}: PredictionChartProps) => {
  // Format date for display (e.g., "Jan 15")
  const formatDate = (dateStr: string): string => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  // Build chart data: current price point + forecast points
  const chartData: ChartDataPoint[] = [
    {
      date: currentDate,
      displayDate: formatDate(currentDate),
      actual: currentPrice,
      predicted: currentPrice, // Connect the line
      isPrediction: false,
    },
    ...forecast.map((point, index) => ({
      date: point.date,
      displayDate: formatDate(point.date),
      predicted: point.price,
      isPrediction: true,
    })),
  ];

  // Calculate Y-axis domain with padding
  const allPrices = [currentPrice, ...forecast.map(f => f.price)];
  const minValue = Math.min(...allPrices);
  const maxValue = Math.max(...allPrices);
  const padding = (maxValue - minValue) * 0.15;

  return (
    <ResponsiveContainer width="100%" height={height}>
      <ComposedChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="predictionGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity={0.3} />
            <stop offset="50%" stopColor="hsl(var(--primary))" stopOpacity={0.1} />
            <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid 
          strokeDasharray="3 3" 
          vertical={false} 
          stroke="hsl(var(--border))" 
        />
        <XAxis
          dataKey="displayDate"
          axisLine={false}
          tickLine={false}
          tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 10 }}
          dy={10}
          interval={0}
        />
        <YAxis
          domain={[minValue - padding, maxValue + padding]}
          axisLine={false}
          tickLine={false}
          tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 10 }}
          tickFormatter={(value) => `$${value.toFixed(0)}`}
          dx={-5}
          width={50}
        />
        <Tooltip content={<CustomTooltip />} />
        <ReferenceLine 
          y={currentPrice} 
          stroke="hsl(var(--muted-foreground))" 
          strokeDasharray="3 3" 
          strokeOpacity={0.5}
        />
        {/* Prediction area fill */}
        <Area
          type="monotone"
          dataKey="predicted"
          stroke="hsl(var(--primary))"
          strokeWidth={2}
          strokeDasharray="5 5"
          fill="url(#predictionGradient)"
          dot={(props: any) => {
            const { cx, cy, payload } = props;
            if (payload.isPrediction) {
              return (
                <circle 
                  cx={cx} 
                  cy={cy} 
                  r={4} 
                  fill="hsl(var(--primary))" 
                  stroke="hsl(var(--background))"
                  strokeWidth={2}
                />
              );
            }
            return (
              <circle 
                cx={cx} 
                cy={cy} 
                r={5} 
                fill="hsl(var(--foreground))" 
                stroke="hsl(var(--background))"
                strokeWidth={2}
              />
            );
          }}
          activeDot={{
            r: 6,
            fill: 'hsl(var(--primary))',
            stroke: 'hsl(var(--background))',
            strokeWidth: 2,
          }}
        />
      </ComposedChart>
    </ResponsiveContainer>
  );
};
