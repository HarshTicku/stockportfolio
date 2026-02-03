import { 
  Area, 
  AreaChart, 
  ResponsiveContainer, 
  XAxis, 
  YAxis, 
  Tooltip,
  CartesianGrid,
  ReferenceLine
} from 'recharts';
import { format } from 'date-fns';
import { formatCurrency } from '@/utils/formatters';

interface PerformanceChartProps {
  data: { date: string; value: number }[];
  height?: number;
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-foreground/90 backdrop-blur-sm text-background px-3 py-2 rounded-lg shadow-lg">
        <p className="text-xs text-muted mb-1">{label}</p>
        <p className="text-sm font-semibold tabular-nums">
          {formatCurrency(payload[0].value)}
        </p>
      </div>
    );
  }
  return null;
};

export const PerformanceChart = ({ data, height = 300 }: PerformanceChartProps) => {
  const minValue = Math.min(...data.map(d => d.value));
  const maxValue = Math.max(...data.map(d => d.value));
  const padding = (maxValue - minValue) * 0.1;

  return (
    <ResponsiveContainer width="100%" height={height}>
      <AreaChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="performanceGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="hsl(var(--success))" stopOpacity={0.3} />
            <stop offset="50%" stopColor="hsl(var(--success))" stopOpacity={0.1} />
            <stop offset="100%" stopColor="hsl(var(--success))" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid 
          strokeDasharray="3 3" 
          vertical={false} 
          stroke="hsl(var(--border))" 
        />
        <XAxis
          dataKey="date"
          axisLine={false}
          tickLine={false}
          tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 11 }}
          dy={10}
        />
        <YAxis
          domain={[minValue - padding, maxValue + padding]}
          axisLine={false}
          tickLine={false}
          tick={{ fill: 'hsl(var(--muted-foreground))', fontSize: 11 }}
          tickFormatter={(value) => value.toLocaleString()}
          dx={-10}
        />
        <Tooltip content={<CustomTooltip />} />
        <Area
          type="monotone"
          dataKey="value"
          stroke="hsl(var(--success))"
          strokeWidth={2}
          fill="url(#performanceGradient)"
          dot={false}
          activeDot={{
            r: 6,
            fill: 'hsl(var(--success))',
            stroke: 'hsl(var(--background))',
            strokeWidth: 2,
          }}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
};
