from transformers import AutoModelForSequenceClassification

# Nurodyk savo modelio aplanką
model = AutoModelForSequenceClassification.from_pretrained("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert", trust_remote_code=True)

# Išsaugok kaip .bin
model.save_pretrained("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert", safe_serialization=False)