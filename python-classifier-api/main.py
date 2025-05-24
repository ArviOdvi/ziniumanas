from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F
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

# --- Klasifikavimo API ---
@app.post("/predict")
def predict(request: TextRequest):
    try:
        inputs = tokenizer(
            request.text,
            return_tensors="pt",
            padding="max_length",
            truncation=True,
            max_length=256
        )
        with torch.no_grad():
            outputs = model(**inputs)
            probs = F.softmax(outputs.logits, dim=-1)
            pred_label_id = torch.argmax(probs, dim=1).item()

        id2label = model.config.id2label
        pred_label_id = torch.argmax(probs, dim=1).item()
        label = model.config.id2label.get(pred_label_id, "Nežinoma")
        confidence = round(probs[0][pred_label_id].item(), 3)

        return {"label": label, "confidence": confidence}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Klaida klasifikuojant tekstą: {str(e)}")