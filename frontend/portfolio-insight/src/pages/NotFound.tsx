import { Link } from 'react-router-dom';
import { Home, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';

const NotFound = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
      <div className="w-20 h-20 rounded-full bg-muted flex items-center justify-center mb-6">
        <span className="text-4xl font-bold text-muted-foreground">404</span>
      </div>
      <h1 className="text-2xl font-bold mb-2">Page Not Found</h1>
      <p className="text-muted-foreground mb-6 max-w-md">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <div className="flex gap-3">
        <Button variant="outline" asChild>
          <Link to="/" className="gap-2">
            <ArrowLeft className="w-4 h-4" />
            Go Back
          </Link>
        </Button>
        <Button asChild>
          <Link to="/" className="gap-2">
            <Home className="w-4 h-4" />
            Home
          </Link>
        </Button>
      </div>
    </div>
  );
};

export default NotFound;
