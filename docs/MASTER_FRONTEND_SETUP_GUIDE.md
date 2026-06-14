# COMPLETE EIS FRONTEND - MASTER SETUP GUIDE

## 🎯 TWO SETUP OPTIONS

---

## ✅ OPTION 1: AUTOMATED SETUP (RECOMMENDED - 5 Minutes)

**Best for:** Want everything auto-installed with single command

### Step 1: Run Setup Script
```bash
bash create-complete-frontend.sh
```

This script will:
- ✅ Create React app
- ✅ Install all dependencies
- ✅ Setup Tailwind CSS
- ✅ Configure .env
- ✅ Create folder structure
- ✅ Create global styles
- ✅ Remove unnecessary files

**Time:** ~5-10 minutes (depends on internet speed)

### Step 2: Copy Component Files

After script completes, copy these files:

**To src/services/**
```
api.js
```

**To src/pages/**
```
Login.jsx
Dashboard.jsx
Employees.jsx
Analytics.jsx
Attendance.jsx
Departments.jsx
Agent.jsx
Profile.jsx
```

**To src/components/**
```
Navbar.jsx
Sidebar.jsx
Modal.jsx
```

**To src/**
```
App.jsx
index.jsx
index.css (overwrite if exists)
```

### Step 3: Start Development
```bash
cd eis-frontend
npm start
```

**Opens:** http://localhost:3000

---

## 🚀 OPTION 2: MANUAL SETUP (DIY - 10 Minutes)

**Best for:** Want to understand each step

### Step 1: Create React App
```bash
npx create-react-app eis-frontend
cd eis-frontend
```

### Step 2: Install Tailwind CSS
```bash
npm install -D tailwindcss@latest postcss@latest autoprefixer@latest
npx tailwindcss init -p
```

### Step 3: Install Dependencies
```bash
npm install \
  axios \
  react-router-dom \
  react-hook-form \
  recharts \
  lucide-react \
  @headlessui/react \
  clsx \
  date-fns
```

### Step 4: Create Folder Structure
```bash
mkdir -p src/{components,pages,services,hooks,context,utils}
```

### Step 5: Create .env
```bash
cat > .env << 'EOF'
REACT_APP_API_URL=http://localhost:8000/api/v1
REACT_APP_APP_NAME=EIS System
REACT_APP_DEBUG=false
EOF
```

### Step 6: Configure Tailwind
Edit `tailwind.config.js`:
```javascript
module.exports = {
  content: [
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

### Step 7: Update index.css
Copy content from provided `index.css` file to `src/index.css`

### Step 8: Create index.jsx
Copy provided `index.jsx` to `src/index.jsx`

### Step 9: Copy All Component Files
(Same as Option 1, Step 2)

### Step 10: Start Development
```bash
npm start
```

---

## 📦 ALL FILES PROVIDED

You have all 17 files ready to copy:

### Core Files
1. ✅ **api.js** - API service with all endpoints
2. ✅ **App.jsx** - Main app with routing
3. ✅ **index.jsx** - React entry point
4. ✅ **index.css** - Global styles

### Page Components (8 Pages)
5. ✅ **Login.jsx** - Authentication
6. ✅ **Dashboard.jsx** - Main dashboard
7. ✅ **Employees.jsx** - Employee CRUD
8. ✅ **Analytics.jsx** - Analytics dashboard
9. ✅ **Attendance.jsx** - Attendance tracking
10. ✅ **Departments.jsx** - Department management
11. ✅ **Agent.jsx** - AI agent interface
12. ✅ **Profile.jsx** - User profile

### Reusable Components
13. ✅ **Navbar.jsx** - Top navigation
14. ✅ **Sidebar.jsx** - Left sidebar
15. ✅ **Modal.jsx** - Dialog modal

### Setup Scripts
16. ✅ **create-complete-frontend.sh** - Automated setup
17. ✅ **setup-frontend.sh** - Alternative setup

---

## 🎯 QUICK REFERENCE

### Command Summary

```bash
# Option 1: Automated (ONE COMMAND!)
bash create-complete-frontend.sh

# Option 2: Manual commands
npx create-react-app eis-frontend
cd eis-frontend
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install axios react-router-dom react-hook-form recharts lucide-react @headlessui/react clsx date-fns
mkdir -p src/{components,pages,services}
npm start
```

---

## 📋 FINAL STRUCTURE

After setup, you'll have:

```
eis-frontend/
├── node_modules/              ✓ All packages installed
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── Navbar.jsx        ✓ Provided
│   │   ├── Sidebar.jsx       ✓ Provided
│   │   └── Modal.jsx         ✓ Provided
│   ├── pages/
│   │   ├── Login.jsx         ✓ Provided
│   │   ├── Dashboard.jsx     ✓ Provided
│   │   ├── Employees.jsx     ✓ Provided
│   │   ├── Analytics.jsx     ✓ Provided
│   │   ├── Attendance.jsx    ✓ Provided
│   │   ├── Departments.jsx   ✓ Provided
│   │   ├── Agent.jsx         ✓ Provided
│   │   └── Profile.jsx       ✓ Provided
│   ├── services/
│   │   └── api.js            ✓ Provided
│   ├── App.jsx               ✓ Provided
│   ├── index.jsx             ✓ Provided
│   └── index.css             ✓ Provided
├── .env                       ✓ Created automatically
├── package.json               ✓ Created automatically
├── tailwind.config.js         ✓ Created automatically
└── README.md                  ✓ Created automatically
```

---

## ✅ VERIFICATION CHECKLIST

After setup, verify:

```
□ node_modules folder exists (300-500MB)
□ .env file created with API URL
□ tailwind.config.js exists
□ postcss.config.js exists
□ src/index.css has @tailwind imports
□ All component files copied to correct locations
□ package.json has all dependencies
```

---

## 🚀 START COMMAND

Once everything is set up:

```bash
npm start
```

**Opens automatically:** http://localhost:3000

**Login with:**
- Email: demo@company.com
- Password: password

---

## 🎯 COMPLETE BACKEND STACK

For full system, run all three in separate terminals:

**Terminal 1 - Database & Cache:**
```bash
docker-compose up -d
```

**Terminal 2 - Backend API:**
```bash
cd backend
java -jar target/eis-backend-1.0.0.jar
```

**Terminal 3 - Frontend:**
```bash
cd eis-frontend
npm start
```

---

## 🆘 TROUBLESHOOTING

### npm install takes forever
- Normal! First time can take 5-10 minutes
- This is when node_modules is being downloaded

### Port 3000 already in use
```bash
PORT=3001 npm start  # Use different port
```

### Node.js not installed
- Download from: https://nodejs.org
- Install LTS version (18 or higher)
- Restart terminal after install

### API connection fails
1. Verify backend running: `http://localhost:8000/api/v1/health`
2. Check .env file has correct URL
3. Restart frontend: Ctrl+C and `npm start` again

---

## 📊 DEPENDENCIES INSTALLED

```json
{
  "react": "^18.2.0",
  "react-dom": "^18.2.0",
  "react-router-dom": "^6.16.0",
  "axios": "^1.6.0",
  "react-hook-form": "^7.48.0",
  "recharts": "^2.10.0",
  "lucide-react": "^0.292.0",
  "@headlessui/react": "^1.7.16",
  "clsx": "^2.0.0",
  "date-fns": "^2.30.0",
  "tailwindcss": "latest",
  "postcss": "latest",
  "autoprefixer": "latest"
}
```

---

## ✨ WHAT YOU GET

After following this guide, you'll have:

✅ Complete React frontend (8 pages)
✅ All components and services
✅ Tailwind CSS styling
✅ Responsive design
✅ API integration
✅ Authentication system
✅ Data visualization
✅ Form handling
✅ Error handling
✅ Ready to connect to backend

---

## 🎉 YOU'RE READY!

**Choose your setup method:**

1. **AUTOMATED:** `bash create-complete-frontend.sh` (5 min)
2. **MANUAL:** Follow Option 2 steps (10 min)

Then copy files and run `npm start`!

---

## 📞 SUPPORT

Everything is provided and tested. If you get stuck:

1. Check internet connection (first npm install downloads ~300MB)
2. Verify Node.js installed: `node --version`
3. Verify backend running: `http://localhost:8000/api/v1/health`
4. Check .env file exists with API URL
5. Try: `rm -rf node_modules && npm install`

---

## 🚀 LET'S GO!

```bash
# Start here:
bash create-complete-frontend.sh

# Then:
npm start

# Enjoy! 🎉
```
