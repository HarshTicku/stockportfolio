import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { formatCurrency, formatPercent } from '@/utils/formatters';
import { ASSET_TYPE_COLORS } from '@/utils/constants';

interface AllocationItem {
  assetType: string;
  value: number;
  percentage: number;
}

interface AllocationChartProps {
  data: AllocationItem[];
  size?: number;
}

const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-foreground/90 backdrop-blur-sm text-background px-3 py-2 rounded-lg shadow-lg">
        <p className="text-xs font-medium mb-1">{data.assetType}</p>
        <p className="text-sm font-semibold tabular-nums">{formatCurrency(data.value)}</p>
        <p className="text-xs text-muted">{data.percentage.toFixed(1)}%</p>
      </div>
    );
  }
  return null;
};

export const AllocationChart = ({ data, size = 200 }: AllocationChartProps) => {
  return (
    <div className="flex items-center gap-6">
      <ResponsiveContainer width={size} height={size}>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={size * 0.3}
            outerRadius={size * 0.45}
            paddingAngle={2}
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell 
                key={`cell-${index}`} 
                fill={ASSET_TYPE_COLORS[entry.assetType] || `hsl(${index * 60}, 70%, 50%)`}
                stroke="none"
              />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
        </PieChart>
      </ResponsiveContainer>

      {/* Legend */}
      <div className="space-y-2">
        {data.map((item, index) => (
          <div key={item.assetType} className="flex items-center gap-2">
            <div 
              className="w-3 h-3 rounded-full" 
              style={{ 
                backgroundColor: ASSET_TYPE_COLORS[item.assetType] || `hsl(${index * 60}, 70%, 50%)` 
              }}
            />
            <span className="text-sm text-muted-foreground">{item.assetType}</span>
            <span className="text-sm font-medium tabular-nums">{item.percentage.toFixed(1)}%</span>
          </div>
        ))}
      </div>
    </div>
  );
};
