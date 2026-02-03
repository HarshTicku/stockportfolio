import { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { 
  Home, 
  Briefcase, 
  TrendingUp, 
  Receipt, 
  ChevronDown,
  ChevronRight,
  Wallet,
  Bitcoin,
  HelpCircle,
  Users,
  BarChart3,
  PieChart,
  DollarSign,
  Building2
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavItem {
  label: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  children?: { label: string; path: string }[];
}

const navItems: NavItem[] = [
  { label: 'Home', path: '/', icon: Home },
  { label: 'Exchange', path: '/exchange', icon: TrendingUp },
  {
    label: 'Stock & Fund',
    path: '/stocks',
    icon: BarChart3,
    children: [
      { label: 'Stock/ETF', path: '/market' },
      { label: 'Index', path: '/market/index' },
      { label: 'Currency', path: '/market/currency' },
      { label: 'Mutual Fund', path: '/market/mutual-fund' },
    ],
  },
  { label: 'Wallets', path: '/wallets', icon: Wallet },
  { label: 'Crypto', path: '/crypto', icon: Bitcoin },
];

const supportItems: NavItem[] = [
  { label: 'Community', path: '/community', icon: Users },
  { label: 'Help & Support', path: '/support', icon: HelpCircle },
];

interface WatchlistItem {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
}

const mockWatchlist: WatchlistItem[] = [
  { symbol: 'S&P 500', name: 'S&P 500', price: 4549.78, change: 13.02, changePercent: 0.30 },
  { symbol: 'S&P 500', name: 'S&P 500', price: 4549.78, change: 13.02, changePercent: 0.30 },
];

export const AppSidebar = () => {
  const location = useLocation();
  const [expandedItems, setExpandedItems] = useState<string[]>(['Stock & Fund']);

  const toggleExpand = (label: string) => {
    setExpandedItems(prev =>
      prev.includes(label)
        ? prev.filter(item => item !== label)
        : [...prev, label]
    );
  };

  const isActive = (path: string) => location.pathname === path;
  const isChildActive = (children?: { path: string }[]) =>
    children?.some(child => location.pathname === child.path);

  return (
    <aside className="w-64 h-screen bg-card border-r border-border flex flex-col overflow-hidden">
      {/* Logo */}
      <div className="p-6 flex items-center gap-3">
        <div className="w-10 h-10 rounded-full bg-primary flex items-center justify-center">
          <span className="text-primary-foreground font-bold text-sm">TP</span>
        </div>
      </div>

      {/* AI Watchlist */}
      <div className="px-4 mb-4">
        <p className="text-xs font-medium text-muted-foreground mb-3 tracking-wider">
          AI WATCHLIST
        </p>
        <div className="space-y-2">
          {mockWatchlist.map((item, idx) => (
            <div
              key={idx}
              className="p-3 rounded-lg border border-border bg-card hover:bg-muted/50 transition-colors cursor-pointer"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded bg-primary/10 flex items-center justify-center">
                    <span className="text-[10px] font-semibold text-primary">S&P<br/>500</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium">{item.symbol}</p>
                    <p className="text-xs text-muted-foreground tabular-nums">
                      {item.price.toLocaleString()}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs font-medium text-success">+{item.changePercent}%</p>
                  <p className="text-xs text-success tabular-nums">+{item.change}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Main Navigation */}
      <div className="flex-1 overflow-y-auto scrollbar-thin px-4">
        <p className="text-xs font-medium text-muted-foreground mb-3 tracking-wider">
          MAIN MENU
        </p>
        <nav className="space-y-1">
          {navItems.map((item) => (
            <div key={item.label}>
              {item.children ? (
                <>
                  <button
                    onClick={() => toggleExpand(item.label)}
                    className={cn(
                      "w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                      isChildActive(item.children)
                        ? "bg-primary/10 text-primary"
                        : "text-muted-foreground hover:bg-muted hover:text-foreground"
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <item.icon className="w-5 h-5" />
                      <span>{item.label}</span>
                    </div>
                    {expandedItems.includes(item.label) ? (
                      <ChevronDown className="w-4 h-4" />
                    ) : (
                      <ChevronRight className="w-4 h-4" />
                    )}
                  </button>
                  {expandedItems.includes(item.label) && (
                    <div className="ml-8 mt-1 space-y-1 border-l-2 border-border pl-3">
                      {item.children.map((child) => (
                        <NavLink
                          key={child.path}
                          to={child.path}
                          className={({ isActive }) =>
                            cn(
                              "block px-3 py-2 rounded-lg text-sm transition-colors",
                              isActive
                                ? "text-success font-medium"
                                : "text-muted-foreground hover:text-foreground"
                            )
                          }
                        >
                          {child.label}
                        </NavLink>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <NavLink
                  to={item.path}
                  className={({ isActive }) =>
                    cn(
                      "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                      isActive
                        ? "bg-primary/10 text-primary"
                        : "text-muted-foreground hover:bg-muted hover:text-foreground"
                    )
                  }
                >
                  <item.icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </NavLink>
              )}
            </div>
          ))}
        </nav>

        {/* Support Section */}
        <div className="mt-8">
          <p className="text-xs font-medium text-muted-foreground mb-3 tracking-wider">
            SUPPORT
          </p>
          <nav className="space-y-1">
            {supportItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary/10 text-primary"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  )
                }
              >
                <item.icon className="w-5 h-5" />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </div>
      </div>
    </aside>
  );
};
