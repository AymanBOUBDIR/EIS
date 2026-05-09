#!/bin/bash

# EIS FRONTEND - COMPLETE AUTOMATED SETUP
# This script creates a complete React frontend with all dependencies
# Run: bash setup-frontend.sh

set -e  # Exit on error

echo "=================================================="
echo "EIS Frontend - Complete Automated Setup"
echo "=================================================="
echo ""

# Step 1: Create React App
echo "Step 1/8: Creating React application..."
npx create-react-app eis-frontend

cd eis-frontend

# Step 2: Install Dependencies
echo "Step 2/8: Installing dependencies..."
npm install --legacy-peer-deps \
  axios@^1.6.0 \
  react-router-dom@^6.16.0 \
  react-hook-form@^7.48.0 \
  recharts@^2.10.0 \
  lucide-react@^0.292.0 \
  @headlessui/react@^1.7.16 \
  clsx@^2.0.0 \
  date-fns@^2.30.0

# Step 3: Install Tailwind CSS
echo "Step 3/8: Installing Tailwind CSS..."
npm install -D tailwindcss@latest postcss@latest autoprefixer@latest

# Initialize Tailwind
npx tailwindcss init -p

# Step 4: Create folder structure
echo "Step 4/8: Creating folder structure..."
mkdir -p src/{components,pages,services,hooks,context,utils,styles}

# Step 5: Create .env file
echo "Step 5/8: Creating .env file..."
cat > .env << 'ENVFILE'
REACT_APP_API_URL=http://localhost:8000/api/v1
REACT_APP_APP_NAME=EIS System
REACT_APP_DEBUG=false
ENVFILE

# Step 6: Configure Tailwind
echo "Step 6/8: Configuring Tailwind CSS..."
cat > tailwind.config.js << 'TAILWINDFILE'
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
TAILWINDFILE

# Step 7: Create index.css
echo "Step 7/8: Creating global styles..."
cat > src/index.css << 'CSSFILE'
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

::-webkit-scrollbar {
  width: 8px;
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
  to { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(31, 78, 121, 0.1);
  border-color: #1F4E79;
}

button:active {
  transform: scale(0.98);
}
CSSFILE

# Step 8: Delete default src files and create new structure
echo "Step 8/8: Setting up application files..."

# Remove unnecessary files
rm -f src/App.test.js src/logo.svg src/reportWebVitals.js src/setupTests.js

# Create index.jsx
cat > src/index.jsx << 'INDEXFILE'
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
INDEXFILE

# Create package.json scripts
npm pkg set scripts.dev="react-scripts start"
npm pkg set scripts.build="react-scripts build"
npm pkg set scripts.start="react-scripts start"

echo ""
echo "=================================================="
echo "✓ Frontend setup complete!"
echo "=================================================="
echo ""
echo "Project: eis-frontend"
echo "Location: $(pwd)"
echo ""
echo "Next steps:"
echo "1. Copy all component and page files to src/"
echo "2. Create .env file with: REACT_APP_API_URL=http://localhost:8000/api/v1"
echo "3. Run: npm start"
echo ""
echo "Frontend will open at: http://localhost:3000"
echo ""
