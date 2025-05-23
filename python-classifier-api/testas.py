import torch
from transformers import AutoTokenizer

model = torch.jit.load("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert/custom-bert.pt")
model.eval()

sample_text = "Lietuvoje vyksta pavasario renginiai su muzika ir technologijomis."
tokenizer = AutoTokenizer.from_pretrained("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert")
inputs = tokenizer(sample_text, return_tensors="pt", max_length=256, padding="max_length", truncation=True)
input_ids = inputs["input_ids"]
attention_mask = inputs["attention_mask"].to(dtype=torch.float32)

with torch.no_grad():
    logits = model(input_ids, attention_mask)
print(f"Logits shape: {logits.shape}")