#!/bin/bash

# ============================================
# EIS FRONTEND - COMPLETE AUTO SETUP
# ============================================
# This script creates a complete, ready-to-run React frontend
# Usage: bash create-complete-frontend.sh

set -e

echo "========================================="
echo "EIS Frontend - Complete Auto Setup"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed!${NC}"
    echo "Please install Node.js from https://nodejs.org"
    exit 1
fi

echo -e "${GREEN}✓ Node.js version:${NC} $(node --version)"
echo -e "${GREEN}✓ npm version:${NC} $(npm --version)"
echo ""

# Step 1: Create React App
echo -e "${YELLOW}Step 1/9: Creating React application...${NC}"
npx create-react-app eis-frontend
cd eis-frontend

# Step 2: Install Tailwind CSS
echo ""
echo -e "${YELLOW}Step 2/9: Installing Tailwind CSS...${NC}"
npm install -D tailwindcss@latest postcss@latest autoprefixer@latest
npx tailwindcss init -p

# Step 3: Install additional dependencies
echo ""
echo -e "${YELLOW}Step 3/9: Installing dependencies...${NC}"
npm install \
  axios \
  react-router-dom \
  react-hook-form \
  recharts \
  lucide-react \
  @headlessui/react \
  clsx \
  date-fns

# Step 4: Create folder structure
echo ""
echo -e "${YELLOW}Step 4/9: Creating folder structure...${NC}"
mkdir -p src/{components,pages,services,hooks,context,utils,styles}
echo -e "${GREEN}✓ Folders created${NC}"

# Step 5: Create .env file
echo ""
echo -e "${YELLOW}Step 5/9: Creating .env file...${NC}"
cat > .env << 'EOF'
REACT_APP_API_URL=http://localhost:8000/api/v1
REACT_APP_APP_NAME=EIS System
REACT_APP_DEBUG=false
EOF
echo -e "${GREEN}✓ .env configured${NC}"

# Step 6: Configure Tailwind CSS
echo ""
echo -e "${YELLOW}Step 6/9: Configuring Tailwind CSS...${NC}"
cat > tailwind.config.js << 'EOF'
module.exports = {
  content: [
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        'eis-primary': '#1F4E79',
      }
    },
  },
  plugins: [],
}
EOF
echo -e "${GREEN}✓ Tailwind configured${NC}"

# Step 7: Create index.css with global styles
echo ""
echo -e "${YELLOW}Step 7/9: Creating global styles...${NC}"
cat > src/index.css << 'EOF'
@tailwind base;
@tailwind components;
@tailwind utilities;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f3f4f6;
}

code {
  font-family: source-code-pro, Menlo, Monaco, Consolas, 'Courier New',
    monospace;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

input:focus,
textarea:focus,
select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(31, 78, 121, 0.1);
  border-color: #1F4E79;
}

button:active {
  transform: scale(0.98);
}
EOF
echo -e "${GREEN}✓ Global styles created${NC}"

# Step 8: Create index.jsx
echo ""
echo -e "${YELLOW}Step 8/9: Creating index.jsx...${NC}"
cat > src/index.jsx << 'EOF'
import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
EOF
echo -e "${GREEN}✓ index.jsx created${NC}"

# Step 9: Clean up unnecessary files
echo ""
echo -e "${YELLOW}Step 9/9: Cleaning up...${NC}"
rm -f src/App.test.js src/logo.svg src/reportWebVitals.js src/setupTests.js src/App.css

# Create placeholder App.jsx
cat > src/App.jsx << 'EOF'
import React from 'react';
import { BrowserRouter } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-100">
        <div className="flex items-center justify-center h-screen">
          <div className="text-center">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">EIS Frontend</h1>
            <p className="text-gray-600 mb-8">
              Copy all component and page files to src/ folder to get started.
            </p>
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 max-w-md">
              <p className="text-sm text-blue-800">
                <strong>Next Steps:</strong>
              </p>
              <ul className="text-sm text-blue-800 mt-2 text-left">
                <li>✓ Add api.js → src/services/</li>
                <li>✓ Add page components → src/pages/</li>
                <li>✓ Add navbar/sidebar/modal → src/components/</li>
                <li>✓ Verify .env file</li>
                <li>✓ Run: npm start</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </BrowserRouter>
  );
}

export default App;
EOF

echo -e "${GREEN}✓ Setup cleaned${NC}"

# Final message
echo ""
echo "========================================="
echo -e "${GREEN}✓ Frontend setup complete!${NC}"
echo "========================================="
echo ""
echo "Project created: eis-frontend"
echo "Location: $(pwd)"
echo ""
echo -e "${YELLOW}📦 Installed packages:${NC}"
echo "  • React 18"
echo "  • React Router DOM v6"
echo "  • Tailwind CSS"
echo "  • Axios"
echo "  • Recharts"
echo "  • Lucide React"
echo "  • React Hook Form"
echo "  • And more..."
echo ""
echo -e "${YELLOW}📁 Folder structure:${NC}"
echo "  • src/components/  (Navbar, Sidebar, Modal)"
echo "  • src/pages/       (Login, Dashboard, Employees, etc.)"
echo "  • src/services/    (api.js)"
echo "  • tailwind.config.js"
echo "  • .env            (Pre-configured)"
echo ""
echo -e "${YELLOW}🚀 Next steps:${NC}"
echo "  1. Copy all component files to src/components/"
echo "  2. Copy all page files to src/pages/"
echo "  3. Copy api.js to src/services/"
echo "  4. Make sure .env has correct API URL"
echo "  5. Run: npm start"
echo ""
echo -e "${YELLOW}Backend connection:${NC}"
echo "  API URL: http://localhost:8000/api/v1"
echo ""
echo -e "${YELLOW}Login credentials:${NC}"
echo "  Email: demo@company.com"
echo "  Password: password"
echo ""
echo -e "${GREEN}Ready to code! 🎉${NC}"
echo ""
