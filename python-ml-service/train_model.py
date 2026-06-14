import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
import joblib

# Generate synthetic training data
np.random.seed(42)
n_samples = 1000

# Features: attendance_rate (0-100), performance_rating (1-5), salary_ratio_to_avg (0.2-2.0)
attendance_rate = np.random.uniform(30, 100, n_samples)
performance_rating = np.random.randint(1, 6, n_samples)
salary_ratio = np.random.uniform(0.3, 2.0, n_samples)

# Target: attrition (0 = stays, 1 = leaves)
risk_score = (
    (100 - attendance_rate) * 0.4 +
    (5 - performance_rating) * 5 * 0.3 +
    (2.0 - salary_ratio) * 10 * 0.3
)
attrition = (risk_score + np.random.normal(0, 5, n_samples) > 35).astype(int)

df = pd.DataFrame({
    'attendance_rate': attendance_rate,
    'performance_rating': performance_rating,
    'salary_ratio': salary_ratio,
    'attrition': attrition
})

X = df[['attendance_rate', 'performance_rating', 'salary_ratio']]
y = df['attrition']

model = RandomForestClassifier(n_estimators=50, random_state=42)
model.fit(X, y)

# Save the model
joblib.dump(model, 'attrition_model.joblib')
print("Machine learning attrition model trained and saved successfully!")
