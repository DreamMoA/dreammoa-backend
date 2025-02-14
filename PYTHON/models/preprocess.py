import os
import numpy as np
import pandas as pd
import torch
import pickle
from models.config import FEATURES, DEVICE, MODEL_PATH  # ✅ 상대 경로 유지

# ✅ 프로젝트의 루트 디렉토리를 `backend` 기준으로 설정
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # `backend` 폴더 기준

# ✅ StandardScaler 로드 (상대 경로 유지)
SCALER_PATH = os.path.join(BASE_DIR, "models", "standard_scaler.pkl")

try:
    with open(SCALER_PATH, "rb") as f:
        scaler = pickle.load(f)
    print(f"✅ StandardScaler 로드 완료: {SCALER_PATH}")
except FileNotFoundError:
    raise FileNotFoundError(f"❌ StandardScaler 파일을 찾을 수 없습니다: {SCALER_PATH}")

# ✅ Feature 개수 검증
EXPECTED_FEATURE_COUNT = scaler.n_features_in_

# ✅ 데이터 전처리 함수 (테스트 데이터)
def preprocess_input(data):
    frame_data = data.get("frame_data", [])

    if not frame_data:
        print("❌ 입력 데이터가 없습니다.")
        return None  # 🔥 데이터가 없으면 처리 X

    df = pd.DataFrame(frame_data)

    # ✅ Feature 정렬 및 누락 값 처리
    for feature in FEATURES:
        if feature not in df.columns:
            df[feature] = 0  # 🔥 누락된 Feature를 0으로 채움

    df = df[FEATURES]  # ✅ Feature 순서 고정

    # ✅ Feature 개수 검증 (StandardScaler가 학습한 Feature와 동일해야 함)
    if df.shape[1] != EXPECTED_FEATURE_COUNT:
        print(f"❌ Feature 개수 불일치! 모델이 {EXPECTED_FEATURE_COUNT}개의 Feature를 기대하지만, 입력 데이터는 {df.shape[1]}개입니다.")
        print("❌ FEATURES 리스트가 맞는지 확인하세요.")
        return None  # 🔥 Feature 개수가 맞지 않으면 예측하지 않음

    # ✅ NaN, Inf 값 처리 (변환 전에 수행)
    df = df.replace([np.inf, -np.inf], np.nan).fillna(0)  

    # ✅ StandardScaler 정규화 (훈련된 Scaler 적용)
    try:
        df.iloc[:, :] = scaler.transform(df)  # ✅ transform()으로 학습된 Scaler 적용
    except ValueError as e:
        print(f"❌ StandardScaler 변환 중 오류 발생: {e}")
        return None

    # ✅ 시퀀스 길이 맞추기 (최대 15 프레임 유지)
    seq_array = df.values
    seq_array = np.pad(seq_array, ((0, 15 - len(seq_array)), (0, 0)), mode='constant') if len(seq_array) < 15 else seq_array[:15]

    # ✅ [점검] 전처리된 데이터 확인
    print("🔥 전처리된 데이터 (입력 형태):", seq_array.shape)
    print("🔥 전처리된 데이터 (첫 번째 프레임):", seq_array[0])

    # ✅ PyTorch Tensor 변환
    input_tensor = torch.tensor([seq_array], dtype=torch.float32).to(DEVICE)
    return input_tensor
