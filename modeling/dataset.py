# Imports from external libraries
import torch
from torch.utils.data import Dataset, DataLoader
import os
# Imports from internal libraries
import config

class SpectrogramPairsDataset(Dataset):
    def __init__(self,root):
        self.root = root
        pass

    def _get_num_files_(self) -> int:
        count = 0
        for _, _, files in os.walk(self.root):
            count += 1
            if count % 1000 == 0:
                print(count)
        return count

    def __len__(self):
        pass

    def __getitem__(self, item):
        pass

if __name__ == '__main__':
    print("Testing dataset functionality")

    test_dataset = SpectrogramPairsDataset(config.TRAIN_DATA)
    print(test_dataset._get_num_files_())