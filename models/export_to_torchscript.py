import os
import torch
from transformers import DistilBertForSequenceClassification, DistilBertTokenizer

model_path = "C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert"
output_path = os.path.join(model_path, "custom-bert.pt")

class TraceableModel(torch.nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, input_ids, attention_mask):
        outputs = self.model(input_ids=input_ids, attention_mask=attention_mask)
        return outputs.logits

try:
    print(f"Įkeliamas modelis iš {model_path}")
    model = DistilBertForSequenceClassification.from_pretrained(
        model_path,
        attn_implementation="eager"
    )
    tokenizer = DistilBertTokenizer.from_pretrained(model_path)

    traceable_model = TraceableModel(model)
    traceable_model.eval()

    sample_texts = [
        "Lietuvos krepšinio rinktinė laimėjo auksą",
        "AVINAS. Nelengva diena, nes jums bus primintos pareigos.",
        "Ekonomika auga, bet infliacija kelia iššūkius",
        "Apie šį sprendimą buvo pranešta Maskvos bendrosios konferencijos metu, kur dalyvavo įvairių šalių atstovai.",
        "Tačiau galiausiai taikos procesas vyksta būtent taip, kaip ir buvo planuota, todėl visi džiaugiasi rezultatais.",
        "Seimas patvirtino naują įstatymą dėl mokesčių reformos, kuri paveiks daugelį gyventojų ir įmonių. Šis sprendimas buvo ilgai svarstomas ir sulaukė įvairių reakcijų."
    ]
    inputs = tokenizer(
        sample_texts,
        return_tensors="pt",
        truncation=True,
        padding=True,
        max_length=128
    )

    input_ids = inputs["input_ids"]
    attention_mask = inputs["attention_mask"]

    print("Konvertuojama į TorchScript...")
    with torch.no_grad():
        traced_model = torch.jit.trace(
            traceable_model,
            (input_ids, attention_mask),
            strict=False,
            check_trace=True
        )

    print(f"Išsaugoma custom-bert.pt į {output_path}")
    traced_model.save(output_path)
    print("Konvertavimas sėkmingas")
except Exception as e:
    print(f"Klaida konvertuojant modelį: {e}")