from transformers import DistilBertTokenizerFast

# Įvesk tikslų kelią į tavo modelio katalogą
model_dir = "/python-classifier-api/model/custom-distilbert"

# Įkeliame slow tokenizer ir konvertuojame į fast
tokenizer = DistilBertTokenizerFast.from_pretrained(model_dir, from_slow=True)

# Išsaugome tokenizer.json į tą patį katalogą
tokenizer.save_pretrained(model_dir)