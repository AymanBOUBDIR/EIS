async function test() {
  try {
    const loginRes = await fetch('http://localhost:8000/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: 'demo@company.com',
        password: 'password'
      })
    });
    const loginData = await loginRes.json();
    const token = loginData.accessToken;
    console.log('Token received:', token.substring(0, 15) + '...');

    // 1. Fetch all employees first to trigger risk calculations and persistence
    console.log('Fetching all employees to trigger calculations...');
    const allRes = await fetch('http://localhost:8000/api/v1/employees', {
      headers: { Authorization: `Bearer ${token}` }
    });
    const allData = await allRes.json();
    console.log(`Fetched ${allData.length} employees.`);

    // 2. Fetch at-risk employees
    console.log('Fetching at-risk employees...');
    const riskRes = await fetch('http://localhost:8000/api/v1/employees/attrition-risk?threshold=40', {
      headers: { Authorization: `Bearer ${token}` }
    });
    const riskData = await riskRes.json();
    console.log('At Risk Employees Response:', JSON.stringify(riskData, null, 2));
  } catch (err) {
    console.error('Error:', err.message);
  }
}

test();
