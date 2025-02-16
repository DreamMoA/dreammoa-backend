import json
import logging
import cv2
import torch
import base64
import numpy as np
import mediapipe as mp
import asyncio
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from ultralytics import YOLO
from models.predict import predict_focus
import time

# ✅ Logger 설정
logger = logging.getLogger(__name__)
logging.getLogger("ultralytics").setLevel(logging.ERROR)

router = APIRouter()

# ✅ YOLOv8 모델 로드 (핸드폰 감지)
device = "cuda" if torch.cuda.is_available() else "cpu"
yolo_model = YOLO("yolov8s.pt", verbose=False).to(device)
logger.info("✅ YOLOv8s 모델 로드 완료")

# ✅ Mediapipe Pose & Face Mesh 초기화
mp_pose = mp.solutions.pose
mp_face_mesh = mp.solutions.face_mesh
pose = mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5)
face_mesh = mp_face_mesh.FaceMesh(min_detection_confidence=0.5, min_tracking_confidence=0.5)

def extract_body_landmarks(pose_landmarks):
    """상체 관절 좌표를 0~1 범위로 정규화하여 반환"""
    if pose_landmarks is None:
        return None
    
    return {
        "head_x": pose_landmarks[0].x, "head_y": pose_landmarks[0].y,
        "neck_x": pose_landmarks[1].x, "neck_y": pose_landmarks[1].y,
        "shoulder_left_x": pose_landmarks[11].x, "shoulder_left_y": pose_landmarks[11].y,
        "shoulder_right_x": pose_landmarks[12].x, "shoulder_right_y": pose_landmarks[12].y,
        "elbow_left_x": pose_landmarks[13].x, "elbow_left_y": pose_landmarks[13].y,
        "elbow_right_x": pose_landmarks[14].x, "elbow_right_y": pose_landmarks[14].y,
        "wrist_left_x": pose_landmarks[15].x, "wrist_left_y": pose_landmarks[15].y,
        "wrist_right_x": pose_landmarks[16].x, "wrist_right_y": pose_landmarks[16].y
    }

def compute_head_tilt(pose_landmarks):
    """고개 기울기 계산"""
    if pose_landmarks is None:
        return None
    return abs(pose_landmarks[0].y - pose_landmarks[1].y)

def compute_eye_direction(face_landmarks):
    """시선 방향 분석"""
    if face_landmarks is None:
        return None
    left_eye = face_landmarks[33].y
    right_eye = face_landmarks[263].y
    left_mouth = face_landmarks[61].y
    right_mouth = face_landmarks[291].y
    return abs(left_eye - left_mouth) + abs(right_eye - right_mouth)

def detect_phone(frame):
    """YOLOv8을 사용하여 핸드폰 감지"""
    results = yolo_model(frame, conf=0.3)
    for result in results:
        for box in result.boxes:
            class_id = int(box.cls)
            if class_id == 67:
                return 1  # 📱 핸드폰 감지됨
    return 0  # ❌ 핸드폰 미감지

@router.websocket("/focus")
async def focus_websocket(websocket: WebSocket):
    await websocket.accept()
    logger.info("✅ WebSocket 연결됨")

    try:
        while True:
            start_time = time.time()

            # 🔥 1초 동안의 프레임 데이터 저장 버퍼 (초기화)
            frame_data = []
            phone_detected_history = []
            head_tilt_history = []
            eye_direction_history = []

            # ✅ 프론트에서 `frames: []` 형식으로 전송됨 -> 이를 받아서 처리
            data = await websocket.receive_json()
            frames = data.get("frames", [])

            if not frames:
                logger.error("❌ WebSocket 데이터 오류: frames 배열이 비어 있음")
                continue

            for frame_index, base64_frame in enumerate(frames):
                try:
                    # ✅ Base64를 OpenCV 이미지로 변환
                    frame_data_bytes = base64.b64decode(base64_frame)
                    np_arr = np.frombuffer(frame_data_bytes, np.uint8)
                    frame = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

                    if frame is None:
                        logger.error("❌ OpenCV에서 프레임을 디코딩할 수 없습니다!")
                        continue

                    # ✅ YOLOv8 핸드폰 감지 실행 (원본 해상도 유지)
                    phone_detected = detect_phone(frame)
                    phone_detected_history.append(phone_detected)

                    # ✅ Mediapipe 포즈 분석
                    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    results_pose = pose.process(frame_rgb)
                    results_face = face_mesh.process(frame_rgb)

                    # ✅ 포즈 분석 결과 저장
                    body_landmarks = extract_body_landmarks(results_pose.pose_landmarks.landmark if results_pose.pose_landmarks else None)
                    head_tilt = compute_head_tilt(results_pose.pose_landmarks.landmark if results_pose.pose_landmarks else None)
                    eye_direction = compute_eye_direction(results_face.multi_face_landmarks[0].landmark if results_face.multi_face_landmarks else None)

                    head_tilt_history.append(head_tilt)
                    eye_direction_history.append(eye_direction)

                    frame_data.append({
                        "frame_index": frame_index,
                        **(body_landmarks if body_landmarks else {}),
                        "head_tilt": head_tilt if head_tilt is not None else 0,
                        "eye_direction": eye_direction if eye_direction is not None else 0,
                        "phone_detected": 1 if phone_detected else 0
                    })
                except Exception as e:
                    logger.error(f"❌ 프레임 처리 중 오류 발생: {e}")

            # ✅ 1초 동안의 데이터 평균 계산
            phone_detected_percentage = 1 if any(phone_detected_history) else 0
            avg_head_tilt = sum(filter(None, head_tilt_history)) / len(head_tilt_history) if head_tilt_history else 0
            avg_eye_direction = sum(filter(None, eye_direction_history)) / len(eye_direction_history) if eye_direction_history else 0

            # ✅ 1초 동안의 모든 프레임을 포함한 payload 생성
            payload = {
                "frame_data": frame_data,  # 🔥 전체 프레임 데이터 포함
                "phone_detected_percentage": phone_detected_percentage,
                "head_tilt": avg_head_tilt,
                "eye_direction": avg_eye_direction
            }

            # ✅ AI 모델 예측 실행 (🔥 집중도 분석)
            prediction, confidence = predict_focus(payload)

            # ✅ WebSocket으로 최종 결과 전송
            result = {
                "focus_prediction": prediction,
                "confidence": confidence,
                "phone_detected_percentage": phone_detected_percentage,
                "head_tilt": avg_head_tilt,
                "eye_direction": avg_eye_direction,
                "timestamp": int(time.time())
            }

            logger.info(f"📡 AI 예측 결과: {json.dumps(result, indent=2)}")
            await websocket.send_json(result)

    except WebSocketDisconnect:
        logger.info("🔴 클라이언트가 연결을 종료함")
    finally:
        await websocket.close()
