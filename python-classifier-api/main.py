from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import os

# --- Konfigūracija ---
MODEL_PATH = os.path.join("model", "custom-distilbert")

# --- Modelio įkėlimas ---
try:
    tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)
    model = AutoModelForSequenceClassification.from_pretrained(MODEL_PATH)
    model.eval()
except Exception as e:
    raise RuntimeError(f"Modelio įkėlimas nepavyko: {e}")

# --- FastAPI app ---
app = FastAPI()

# --- Užklausos struktūra ---
class TextRequest(BaseModel):
    text: str

# --- Klasifikavimo funkcija ---
@app.post("/predict")
def predict(request: TextRequest):
    inputs = tokenizer(request.text, return_tensors="pt", padding=True, truncation=True, max_length=256)

    with torch.no_grad():
        outputs = model(**inputs)
        probs = torch.nn.functional.softmax(outputs.logits, dim=-1)
        pred_label_id = torch.argmax(probs, dim=1).item()

    # Gauti žmogišką kategoriją
    label = model.config.id2label[str(pred_label_id)]

    return {"label": label, "confidence": round(probs[0][pred_label_id].item(), 3)}