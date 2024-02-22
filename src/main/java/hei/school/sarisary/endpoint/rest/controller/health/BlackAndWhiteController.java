package hei.school.sarisary.endpoint.rest.controller.health;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BlackAndWhiteController {

    private Map<String, byte[]> images = new HashMap<>();
    private AmazonS3 s3Client;

    @PutMapping("/black-and-white/{id}")
    public ResponseEntity<Void> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            images.put(id, imageBytes);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/black-and-white/{id}")
    public ResponseEntity<Map<String, String>> getImageUrls(@PathVariable String id) {
        if (!images.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1 heure
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest("prod-bucket-poja-sarisary-std22026-bucket-bo4ryg8drttm", id)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);
        URL originalUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        GeneratePresignedUrlRequest generateTransformedUrlRequest =
                new GeneratePresignedUrlRequest("prod-bucket-poja-sarisary-std22026-bucket-bo4ryg8drttm", id)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);
        URL transformedUrl = s3Client.generatePresignedUrl(generateTransformedUrlRequest);


        Map<String, String> urls = new HashMap<>();
        urls.put("original_url", originalUrl.toString());
        urls.put("transformed_url", transformedUrl.toString());

        return ResponseEntity.ok(urls);
    }
}
