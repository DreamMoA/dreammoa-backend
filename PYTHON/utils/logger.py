import asyncio
import json
import websockets

# ✅ 테스트할 WebSocket 서버 주소
WEBSOCKET_URL = "ws://localhost:8000/focus"

# ✅ 테스트용 데이터 (프런트에서 전송할 JSON 형식)
TEST_PAYLOAD = {
    "timestamp": 1739238235,
    "frame_data": [
        {"head_x": 0.5, "head_y": 0.4, "neck_x": 0.6, "neck_y": 0.5, 
         "shoulder_left_x": 0.3, "shoulder_left_y": 0.7, "shoulder_right_x": 0.8, "shoulder_right_y": 0.9,
         "wrist_left_x": 0.2, "wrist_left_y": 0.1, "wrist_right_x": 0.9, "wrist_right_y": 0.3,
         "head_tilt": 0.1, "eye_direction": 0.5, "phone_detected_percentage": 0}
    ]
}

async def test_websocket():
    async with websockets.connect(WEBSOCKET_URL) as ws:
        print("✅ WebSocket 연결 성공")

        # ✅ 데이터 전송
        await ws.send(json.dumps(TEST_PAYLOAD))
        print("📡 데이터 전송 완료:", TEST_PAYLOAD)

        # ✅ 서버 응답 확인
        response = await ws.recv()
        result = json.loads(response)
        print("📡 서버 응답:", result)

        assert "focus_prediction" in result  # 🔥 응답 데이터 검증
        assert result["focus_prediction"] in [0, 1]  # 🔥 0 또는 1인지 확인

# ✅ 테스트 실행
if __name__ == "__main__":
    asyncio.run(test_websocket())
