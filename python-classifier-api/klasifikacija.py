import torch
from transformers import AutoTokenizer
import torch.nn.functional as F

model = torch.jit.load("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert/custom-bert.pt")
model.eval()

sample_text = "Lietuvoje vyksta pavasario renginiai su muzika ir technologijomis."
tokenizer = AutoTokenizer.from_pretrained("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert")
inputs = tokenizer(sample_text, return_tensors="pt", max_length=256, padding="max_length", truncation=True)
input_ids = inputs["input_ids"]
attention_mask = inputs["attention_mask"].to(dtype=torch.float32)

classes = [
    "Ekonomika", "Istorija", "Kultūra", "Laisvalaikis", "Lietuvoje",
    "Maistas", "Mokslas", "Muzika", "Pasaulyje", "Politika",
    "Sportas", "Sveikata", "Technologijos", "Vaikams"
]

with torch.no_grad():
    logits = model(input_ids, attention_mask)
    probs = F.softmax(logits, dim=-1)
    predicted_class_idx = torch.argmax(probs, dim=-1).item()
    predicted_class = classes[predicted_class_idx]

print(f"Logits shape: {logits.shape}")
print(f"Predicted class: {predicted_class}")
print(f"Probabilities: {probs.tolist()[0]}")