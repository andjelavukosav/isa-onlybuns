package rs.ac.uns.ftn.informatika.jpa.dto;

import org.springframework.web.multipart.MultipartFile;


public class CreatePostDTO {

    private String description = null;

    private MultipartFile imageFile = null;

    private Double latitude = null;

    private Double longitude = null;

    public CreatePostDTO() {}

    public CreatePostDTO(String description, MultipartFile imageFile, Double latitude, Double longitude) {
        this.description = description;
        this.imageFile = imageFile;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDescription() {return this.description; }

    public MultipartFile getImage() {return this.imageFile;}

    public Double getLatitude() {return this.latitude;}

    public Double getLongitude() {return this.longitude;}

    public void setDescription(String description) {this.description = description;}

    public void setImage(MultipartFile imageFile) {this.imageFile = imageFile;}

    public void setLatitude(Double latitude) {this.latitude = latitude;}

    public void setLongitude(Double longitude) {this.longitude = longitude;}


    public boolean isValid(){
        return this.description !=null && this.imageFile!=null && this.latitude != null && this.longitude != null;
    }
}
