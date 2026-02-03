import { Area, AreaChart, ResponsiveContainer } from 'recharts';

interface SparklineChartProps {
  data: number[];
  color?: string;
  positive?: boolean;
  height?: number;
}

export const SparklineChart = ({ 
  data, 
  color,
  positive = true,
  height = 40 
}: SparklineChartProps) => {
  const chartData = data.map((value, index) => ({ value, index }));
  const strokeColor = color || (positive ? 'hsl(var(--success))' : 'hsl(var(--destructive))');

  return (
    <ResponsiveContainer width="100%" height={height}>
      <AreaChart data={chartData} margin={{ top: 0, right: 0, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id={`gradient-${positive ? 'positive' : 'negative'}`} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={strokeColor} stopOpacity={0.3} />
            <stop offset="100%" stopColor={strokeColor} stopOpacity={0} />
          </linearGradient>
        </defs>
        <Area
          type="monotone"
          dataKey="value"
          stroke={strokeColor}
          strokeWidth={1.5}
          fill={`url(#gradient-${positive ? 'positive' : 'negative'})`}
          dot={false}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
};
