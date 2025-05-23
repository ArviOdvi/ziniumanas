import torch
from transformers import AutoModelForSequenceClassification, AutoTokenizer

class ClassificationWrapper(torch.nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, input_ids, attention_mask):
        # Grąžiname tik logits, ne visą dict
        return self.model(input_ids=input_ids, attention_mask=attention_mask).logits