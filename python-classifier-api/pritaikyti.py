from transformers import AutoModelForSequenceClassification, AutoConfig, AutoTokenizer
import os

model_name = "distilbert-base-multilingual-cased"
local_dir = "/python-classifier-api/model/custom-distilbert"
os.makedirs(local_dir, exist_ok=True)

# Nustatome klasifikacijos kategorijas
config = AutoConfig.from_pretrained(
    model_name,
    num_labels=14,  # Jūsų kategorijų skaičius
    id2label={
        0: "Ekonomika",
        1: "Istorija",
        2: "Politika",
        3: "Kultūra",
        4: "Sportas",
        5: "Technologijos",
        6: "Sveikata",
        7: "Mokslas",
        8: "Gamta",
        9: "Kelionės",
        10: "Maistas",
        11: "Mada",
        12: "Pramogos",
        13: "Vaikams"
    },
    label2id={
        "Ekonomika": 0,
        "Istorija": 1,
        "Politika": 2,
        "Kultūra": 3,
        "Sportas": 4,
        "Technologijos": 5,
        "Sveikata": 6,
        "Mokslas": 7,
        "Gamta": 8,
        "Kelionės": 9,
        "Maistas": 10,
        "Mada": 11,
        "Pramogos": 12,
        "Vaikams": 13
    }
)

# Įkeliame modelį klasifikacijai
model = AutoModelForSequenceClassification.from_pretrained(model_name, config=config)

# Išsaugome modelį ir tokenizer'į
model.save_pretrained(local_dir, safe_serialization=False)  # Išsaugo pytorch_model.bin
tokenizer = AutoTokenizer.from_pretrained(model_name)
tokenizer.save_pretrained(local_dir)