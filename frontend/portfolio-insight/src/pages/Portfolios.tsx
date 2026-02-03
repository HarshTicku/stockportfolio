import { useState } from 'react';
import { Plus, MoreVertical, TrendingUp, TrendingDown, Trash2, Edit, Eye } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency, formatPercent } from '@/utils/formatters';
import { CURRENCIES } from '@/utils/constants';
import { cn } from '@/lib/utils';
import { usePortfolios } from '@/hooks/usePortfolios';
import { portfolioService, CreatePortfolioRequest } from '@/services/portfolioService';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';

const Portfolios = () => {
  const { toast } = useToast();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [formData, setFormData] = useState<CreatePortfolioRequest>({
    portfolioName: '',
    description: '',
    baseCurrency: 'USD',
    cashBalance: 0,
  });

  // Fetch portfolios
  const { data: portfoliosData, isLoading } = usePortfolios();
  const portfolios = portfoliosData?.content || [];

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: CreatePortfolioRequest) => portfolioService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios'] });
      setIsCreateOpen(false);
      setFormData({ portfolioName: '', description: '', baseCurrency: 'USD', cashBalance: 0 });
      toast({ title: 'Success', description: 'Portfolio created successfully' });
    },
    onError: (error: any) => {
      toast({ 
        title: 'Error', 
        description: error.response?.data?.message || 'Failed to create portfolio',
        variant: 'destructive'
      });
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (portfolioId: string) => portfolioService.delete(portfolioId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['portfolios'] });
      toast({ title: 'Success', description: 'Portfolio deleted successfully' });
    },
    onError: (error: any) => {
      toast({ 
        title: 'Error', 
        description: error.response?.data?.message || 'Failed to delete portfolio',
        variant: 'destructive'
      });
    },
  });

  // Calculate totals
  const totalValue = portfolios.reduce((sum, p) => sum + (p.totalValue || 0), 0);
  const totalAssets = portfolios.reduce((sum, p) => sum + (p.assetCount || 0), 0);

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.portfolioName.trim()) {
      toast({ title: 'Error', description: 'Portfolio name is required', variant: 'destructive' });
      return;
    }
    createMutation.mutate(formData);
  };

  const handleDelete = (portfolioId: string, portfolioName: string) => {
    if (window.confirm(`Are you sure you want to delete "${portfolioName}"? This will also delete all assets in this portfolio.`)) {
      deleteMutation.mutate(portfolioId);
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">My Portfolios</h1>
          <p className="text-muted-foreground">Manage your investment portfolios</p>
        </div>
        
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2">
              <Plus className="w-4 h-4" />
              Create Portfolio
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Portfolio</DialogTitle>
            </DialogHeader>
            <form onSubmit={handleCreate} className="space-y-4 mt-4">
              <div className="space-y-2">
                <Label htmlFor="portfolioName">Portfolio Name *</Label>
                <Input
                  id="portfolioName"
                  value={formData.portfolioName}
                  onChange={(e) => setFormData({ ...formData, portfolioName: e.target.value })}
                  placeholder="e.g., Growth Portfolio"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Brief description of your investment strategy"
                />
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="currency">Currency</Label>
                  <Select 
                    value={formData.baseCurrency} 
                    onValueChange={(value) => setFormData({ ...formData, baseCurrency: value })}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {CURRENCIES.map((currency) => (
                        <SelectItem key={currency} value={currency}>{currency}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="cashBalance">Initial Cash Balance</Label>
                  <Input
                    id="cashBalance"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formData.cashBalance}
                    onChange={(e) => setFormData({ ...formData, cashBalance: parseFloat(e.target.value) || 0 })}
                    placeholder="0.00"
                  />
                </div>
              </div>
              
              <div className="flex justify-end gap-2 pt-4">
                <Button type="button" variant="outline" onClick={() => setIsCreateOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={createMutation.isPending}>
                  {createMutation.isPending ? 'Creating...' : 'Create Portfolio'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Value</p>
            {isLoading ? (
              <Skeleton className="h-8 w-32" />
            ) : (
              <p className="text-2xl font-bold tabular-nums">{formatCurrency(totalValue)}</p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Portfolios</p>
            {isLoading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <p className="text-2xl font-bold">{portfolios.length}</p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-sm text-muted-foreground mb-1">Total Assets</p>
            {isLoading ? (
              <Skeleton className="h-8 w-16" />
            ) : (
              <p className="text-2xl font-bold">{totalAssets}</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Portfolio Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-[200px] rounded-xl" />
          ))}
        </div>
      ) : portfolios.length === 0 ? (
        <Card className="p-12 text-center">
          <div className="max-w-md mx-auto">
            <h3 className="text-lg font-semibold mb-2">No portfolios yet</h3>
            <p className="text-muted-foreground mb-4">
              Create your first portfolio to start tracking your investments.
            </p>
            <Button onClick={() => setIsCreateOpen(true)} className="gap-2">
              <Plus className="w-4 h-4" />
              Create Your First Portfolio
            </Button>
          </div>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {portfolios.map((portfolio) => {
            const gainPercent = portfolio.totalValue > 0 
              ? ((portfolio.assetsValue || 0) / portfolio.totalValue) * 100 - 100
              : 0;
            const isPositive = gainPercent >= 0;
            
            return (
              <Card 
                key={portfolio.portfolioId} 
                className="cursor-pointer hover:shadow-card-hover hover:-translate-y-0.5 transition-all"
              >
                <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
                  <div className="flex-1" onClick={() => navigate(`/portfolios/${portfolio.portfolioId}`)}>
                    <CardTitle className="text-base">{portfolio.portfolioName}</CardTitle>
                    <p className="text-xs text-muted-foreground mt-1 line-clamp-1">
                      {portfolio.description || 'No description'}
                    </p>
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <button className="text-muted-foreground hover:text-foreground p-1">
                        <MoreVertical className="w-4 h-4" />
                      </button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => navigate(`/portfolios/${portfolio.portfolioId}`)}>
                        <Eye className="w-4 h-4 mr-2" />
                        View Details
                      </DropdownMenuItem>
                      <DropdownMenuItem>
                        <Edit className="w-4 h-4 mr-2" />
                        Edit Portfolio
                      </DropdownMenuItem>
                      <DropdownMenuItem 
                        className="text-destructive"
                        onClick={() => handleDelete(portfolio.portfolioId, portfolio.portfolioName)}
                      >
                        <Trash2 className="w-4 h-4 mr-2" />
                        Delete Portfolio
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </CardHeader>
                <CardContent onClick={() => navigate(`/portfolios/${portfolio.portfolioId}`)}>
                  <div className="space-y-4">
                    <div>
                      <p className="text-2xl font-bold tabular-nums">
                        {formatCurrency(portfolio.totalValue)}
                      </p>
                      <div className={cn(
                        "flex items-center gap-1 text-sm font-medium mt-1",
                        isPositive ? "text-success" : "text-destructive"
                      )}>
                        {isPositive ? (
                          <TrendingUp className="w-4 h-4" />
                        ) : (
                          <TrendingDown className="w-4 h-4" />
                        )}
                        <span>{formatPercent(Math.abs(gainPercent))}</span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Assets</span>
                      <span className="font-medium">{portfolio.assetCount}</span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Cash Balance</span>
                      <span className="font-medium tabular-nums">
                        {formatCurrency(portfolio.cashBalance)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-muted-foreground">Currency</span>
                      <span className="font-medium">{portfolio.baseCurrency}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Portfolios;
