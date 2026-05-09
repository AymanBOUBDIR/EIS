#!/bin/bash

BASE_URL="http://localhost:8000/api/v1"

echo "=== 1. Health Check ==="
curl $BASE_URL/health
echo -e "\n"

echo "=== 2. Create Department ==="
DEPT=$(curl -s -X POST $BASE_URL/departments \
  -H "Content-Type: application/json" \
  -d '{"deptName":"IT","description":"IT Dept","budget":400000}')
echo $DEPT
DEPT_ID=$(echo $DEPT | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo -e "\n"

echo "=== 3. Get All Departments ==="
curl $BASE_URL/departments
echo -e "\n"

echo "=== 4. Create Employee ==="
EMP=$(curl -s -X POST $BASE_URL/employees \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Bob Smith\",\"email\":\"bob@company.com\",\"phone\":\"555-1111\",\"hireDate\":\"2024-01-01\",\"salary\":75000,\"deptId\":$DEPT_ID,\"isActive\":true}")
echo $EMP
EMP_ID=$(echo $EMP | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
echo -e "\n"

echo "=== 5. Get All Employees ==="
curl $BASE_URL/employees
echo -e "\n"

echo "=== 6. Create Performance ==="
curl -s -X POST $BASE_URL/performance \
  -H "Content-Type: application/json" \
  -d "{\"empId\":$EMP_ID,\"reviewDate\":\"2024-03-23\",\"rating\":4.2,\"comments\":\"Good\",\"status\":\"COMPLETED\"}"
echo -e "\n"

echo "=== 7. Create Attendance ==="
curl -s -X POST $BASE_URL/attendance \
  -H "Content-Type: application/json" \
  -d "{\"empId\":$EMP_ID,\"date\":\"2024-03-23\",\"status\":\"PRESENT\",\"hoursWorked\":8}"
echo -e "\n"

echo "=== Test Complete ==="