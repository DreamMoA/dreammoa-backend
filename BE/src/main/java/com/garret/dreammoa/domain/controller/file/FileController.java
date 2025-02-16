package com.garret.dreammoa.domain.controller.file;


import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.FileEntity.RelatedType;
import com.garret.dreammoa.domain.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController (FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 저장된 이미지를 보여주는 API
     * GET /file/display?fileName=xxx.png
     */
    @GetMapping("/display")
    public ResponseEntity<byte[]> display(@RequestParam("fileName") String fileName) {
        try {
            // 로컬 저장된 경로
            Path folder = Paths.get(System.getProperty("user.dir"), "files", "image");
            Path target = folder.resolve(fileName);

            byte[] fileData = Files.readAllBytes(target);

            // 간단히 image/png 로 가정 (실제로는 파일 확장자/ContentType 체크 필요)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (NoSuchFileException e) {
            log.error("No such file: {}", e.getFile());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("display error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 유니크한 파일명 생성 예시
    private String generateUniqueFilename() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return uuid + "_" + timestamp;
    }

    // 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("relatedId") Long relatedId,
            @RequestParam("relatedType") RelatedType relatedType
    ) {
        try {
            FileEntity savedFile = fileService.saveFile(file, relatedId, relatedType);
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).build();
        }
    }

    // 특정 관련 ID 및 타입에 해당하는 파일 목록 조회
    @GetMapping("/related")
    public ResponseEntity<List<FileEntity>> getFiles(
            @RequestParam("relatedId") Long relatedId,
            @RequestParam("relatedType") RelatedType relatedType
    ) {
        List<FileEntity> files = fileService.getFiles(relatedId, relatedType);
        return ResponseEntity.ok(files);
    }

    // 파일 삭제
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }
}