# Imports from external libraries

# Imports from internal libraries
import torch
import torchvision

model = torchvision.models.resnet18(pretrained=True)
model.eval()
example = torch.rand(1, 3, 224, 224)
traced_script_module = torch.jit.trace(model, example)
traced_script_module.save("/home/erik/Documents/Projects/Faculty/hot_research_area/untitled/model.pt")