import os
import pickle
from dotenv import load_dotenv
import torch

# ✅ 환경 변수 로드
load_dotenv()
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # ✅ backend 폴더 기준

# ✅ 모델 경로 설정 (상대 경로 유지)
MODEL_PATH = os.path.join(BASE_DIR, "models", "residual_gru_model.pth")
FEATURES_PATH = os.path.join(BASE_DIR, "models", "features.pkl")
SCALER_PATH = os.path.join(BASE_DIR, "models", "standard_scaler.pkl")
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

# ✅ Feature 리스트 (자동 로드)
if os.path.exists(FEATURES_PATH):
    with open(FEATURES_PATH, "rb") as f:
        FEATURES = pickle.load(f)
    print(f"✅ Features 로드 완료: {len(FEATURES)}개")
else:
    raise FileNotFoundError(f"❌ Feature 파일을 찾을 수 없습니다: {FEATURES_PATH}")

# ✅ 디버깅용 출력
print(f"✅ MODEL_PATH: {MODEL_PATH}")
print(f"✅ SCALER_PATH: {SCALER_PATH}")
print(f"✅ DEVICE: {DEVICE}")
print(f"✅ FEATURES: {FEATURES}")
