import os
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

# 🔧 Kelias iki modelio aplanko
model_path = "/python-classifier-api/model/custom-distilbert"
onnx_output_path = os.path.join(model_path, "custom-bert.onnx")

# 📦 Įkeliam tokenizerį ir modelį
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModelForSequenceClassification.from_pretrained(model_path)
model.eval()  # labai svarbu!

# 🧪 Testinis įvesties pavyzdys
sample_text = "Lietuvoje vyksta pavasario renginiai su muzika ir technologijomis."

# ✂️ Tokenizuojam su padding ir truncation
inputs = tokenizer(
    sample_text,
    return_tensors="pt",
    max_length=256,
    padding="max_length",
    truncation=True
)

# 📤 Export to ONNX
torch.onnx.export(
    model,
    (inputs["input_ids"], inputs["attention_mask"]),
    onnx_output_path,
    input_names=["input_ids", "attention_mask"],
    output_names=["logits"],
    dynamic_axes={
        "input_ids": {0: "batch_size"},
        "attention_mask": {0: "batch_size"},
        "logits": {0: "batch_size"}
    },
    opset_version=14,  # ← čia pakeista
    do_constant_folding=True
)

print(f"✅ Modelis sėkmingai konvertuotas į ONNX: {onnx_output_path}")