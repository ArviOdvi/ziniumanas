import torch

# Įkeliam modelį
model = torch.jit.load("C:/Users/Admin/IdeaProjects/Ziniumanas/models/custom-distilbert/custom-bert.pt")

# Atspausdinam visą TorchScript grafą
print(model.graph)