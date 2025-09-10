package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.service.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);
    private static final String UPLOAD_DIR = "uploads/images";

    @Override
    public String saveImage(MultipartFile imageFile) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID().toString() + "-" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while saving the image", e);
        }
    }
    @Override
    @Cacheable(value= "postImages", key = "#imagePath")
    public byte[] getImage(String imagePath) {
        log.info("Getting image from disk for path: {}", imagePath); // ovo se vidi samo ako nije u kešu
        try {
            Path path = Paths.get(UPLOAD_DIR, imagePath.replace("/images/", ""));
            return Files.readAllBytes(path); // učitaj fajl sa diska
        } catch (IOException e) {
            throw new RuntimeException("Could not load image: " + imagePath, e);
        }
    }

    @CacheEvict(value = "postImages", key = "#imagePath")
    public void evictImage(String imagePath) {
        // ručno uklanjanje iz keša, anotacija se sama brine da izbrise stavku iz kesa
    }


}
