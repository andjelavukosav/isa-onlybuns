package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    byte[] getImage(String imagePath);
    String saveImage(MultipartFile file);
    void evictImage(String imagePath);
}
