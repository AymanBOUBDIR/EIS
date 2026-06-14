from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import numpy as np
import os

app = FastAPI(title="EIS Attrition Predictor API", version="1.0.0")

model_path = 'attrition_model.joblib'
if not os.path.exists(model_path):
    import subprocess
    subprocess.run(['python', 'train_model.py'])

model = joblib.load(model_path)

class EmployeePredictRequest(BaseModel):
    attendance_rate: float
    performance_rating: float
    salary_ratio: float

class PredictionResponse(BaseModel):
    attrition_probability: float
    attrition_risk_class: str
    recommendation: str

@app.post("/predict", response_model=PredictionResponse)
def predict_attrition(req: EmployeePredictRequest):
    try:
        features = np.array([[req.attendance_rate, req.performance_rating, req.salary_ratio]])
        
        probs = model.predict_proba(features)[0]
        attrition_prob = float(probs[1]) * 100 

        if attrition_prob >= 60:
            risk_class = "CRITICAL"
            recommendation = "Alerte Critique : Taux de presence bas ou salaire en dessous de la moyenne. Planifier un entretien de retention immediat."
        elif attrition_prob >= 40:
            risk_class = "WARNING"
            recommendation = "Attention : Profil instable. Envisager un ajustement salarial ou des jours de teletravail."
        else:
            risk_class = "STABLE"
            recommendation = "Profil Stable : Maintenir le suivi regulier."

        return PredictionResponse(
            attrition_probability=attrition_prob,
            attrition_risk_class=risk_class,
            recommendation=recommendation
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health():
    return {"status": "healthy", "model_loaded": model is not None}
