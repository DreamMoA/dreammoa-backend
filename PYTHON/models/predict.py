import torch
import torch.nn.functional as F
from .model import load_model
from .preprocess import preprocess_input
from .config import DEVICE

# ✅ 모델 로드 (한 번만 실행)
model = load_model()

# ✅ 모델 예측 함수
def predict_focus(data):
    """
    입력 데이터를 받아 모델을 통해 집중/비집중을 예측하는 함수
    :param data: JSON 형식의 프레임 데이터
    :return: 예측 클래스 (0=비집중, 1=집중), 예측 확률값 (softmax)
    """
    # ✅ 데이터 전처리
    input_tensor = preprocess_input(data)

    if input_tensor is None:
        return None, None  # 🔥 유효한 데이터가 없으면 예측 X

    # ✅ [점검] 입력 데이터 Shape 확인
    print(f"🔥 모델 입력 데이터 Shape: {input_tensor.shape}")  # 예: (1, 15, 19)

    # ✅ 모델 예측
    with torch.no_grad():
        output = model(input_tensor)
        probabilities = F.softmax(output, dim=1)  # 🔥 확률값 계산 (softmax)
        prediction = torch.argmax(probabilities, dim=1).item()  # 🔥 0(비집중) 또는 1(집중)
        confidence = probabilities[0, prediction].item()  # 🔥 해당 클래스의 확률값

    print(f"🔥 예측 결과: {prediction} (확률: {confidence:.4f})")

    return prediction, confidence  # ✅ 확률값 함께 반환
