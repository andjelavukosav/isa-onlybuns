package rs.ac.uns.ftn.informatika.jpa.service.impl;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ImageCompressionServiceImpl {
    // Metod za kompresiju slike
    public void compressImage(String imagePath) throws IOException {
        File inputFile = new File(imagePath);
        File outputFile = new File(imagePath.replace(".jpg", "_compressed.jpg")); // Kreira novu kompresovanu sliku

        Thumbnails.of(inputFile)
                .size(800, 600) // Željena veličina slike
                .outputQuality(0.7) // Željeni kvalitet slike
                .toFile(outputFile);
    }

    // Zakazivanje zadatka koji se pokreće svakog dana u ponoć
    @Scheduled(cron = "0 2 9 * * ?")  // Pokreće se svakog dana u 00:00
    public void compressOldImages() {
        File directory = new File("uploads/images"); // Putanja do direktorijuma sa slikama
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jpg"));

        if (files != null) {
            for (File file : files) {
                long lastModified = file.lastModified();
                long oneMonthAgo = new Date().getTime() - (30L * 24 * 60 * 60 * 1000); // Milisekunde za mesec dana

                // Ako je slika starija od mesec dana, kompresuj je
                if (lastModified < oneMonthAgo) {
                    try {
                        compressImage(file.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
