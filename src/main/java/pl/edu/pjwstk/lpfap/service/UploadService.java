package pl.edu.pjwstk.lpfap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pjwstk.lpfap.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    public static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif", "image/avif");
    public static final long MAX_BYTES = 2L * 1024 * 1024;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String save(MultipartFile file, String subdir, String prefix) throws IOException {
        if (file == null || file.isEmpty()) throw new BadRequestException("No file uploaded");
        if (file.getSize() > MAX_BYTES)     throw new BadRequestException("File too large (max 2 MiB)");

        String type = file.getContentType();
        if (type == null || !ALLOWED_IMAGE_TYPES.contains(type)) {
            throw new BadRequestException("Unsupported image type: " + type
                    + " (allowed: jpeg, png, webp, gif, avif)");
        }

        String ext = switch (type) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif";
            case "image/avif" -> "avif";
            default           -> "img";
        };
        String filename = prefix + "-" + UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, subdir).toAbsolutePath();
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));
        return "/uploads/" + subdir + "/" + filename;
    }
}
