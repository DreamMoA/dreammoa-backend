import os
import torch
import torch.nn as nn
from models.config import FEATURES, DEVICE, MODEL_PATH  # 🔥 상대 경로 유지

# ✅ Residual GRU 모델 정의
class ResidualGRU(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, num_classes):
        super(ResidualGRU, self).__init__()
        self.gru = nn.GRU(input_size, hidden_size, num_layers, batch_first=True, dropout=0.5, bidirectional=True)
        self.residual_fc = nn.Linear(input_size, hidden_size * 2)  # 🔥 잔차 연결 추가
        self.fc = nn.Sequential(
            nn.Linear(hidden_size * 2, 256),
            nn.ReLU(),
            nn.Dropout(0.5),
            nn.Linear(256, num_classes)
        )

    def forward(self, x):
        residual = self.residual_fc(x[:, -1, :])  # 🔥 원본 입력을 잔차 연결로 사용
        out, _ = self.gru(x)
        out = out[:, -1, :] + residual  # 🔥 GRU 출력과 원본 입력을 더함
        return self.fc(out)

# ✅ 모델 로드 함수
def load_model(model_path=None):
    if model_path is None:
        model_path = MODEL_PATH  # ✅ 기본값 설정

    if not os.path.exists(model_path):
        raise FileNotFoundError(f"❌ 모델 파일을 찾을 수 없습니다: {model_path}")

    try:
        model = ResidualGRU(len(FEATURES), 512, 2, 2).to(DEVICE)
        model.load_state_dict(torch.load(model_path, map_location=DEVICE), strict=False)  # ✅ strict=False 추가
        model.eval()
        print(f"✅ 모델 로드 완료: {model_path}")
        return model
    except Exception as e:
        raise RuntimeError(f"❌ 모델 로드 실패: {e}")
