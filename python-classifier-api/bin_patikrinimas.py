from transformers import AutoModelForSequenceClassification
model = AutoModelForSequenceClassification.from_pretrained("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert")
print(model.config)