package htwb.ai.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import htwb.ai.controller.model.Ticket;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.FileExistsException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 17.02.21
 **/
@Service
public class QRUtils {
    private String savePath = "concerts/src/main/resources/TicketQr/";

    /**
     * Generate a qrCode containing the ticket as json
     *
     * @param ticket Ticket to be deserialized in the qrcode
     * @return path to generated qr code
     * @throws JsonProcessingException if ticket deserialization failed
     * @throws FileExistsException     if file at same path already exists
     */
    public String generateTicketQR(Ticket ticket) throws JsonProcessingException, FileExistsException {
        //Create Json from Ticket POJO
        ObjectMapper objectMapper = new ObjectMapper();
        String ticketJson = objectMapper.writeValueAsString(ticket);

        File tempFile = QRCode.from(ticketJson).to(ImageType.PNG)
                .withSize(200, 200)
                .file();

        UUID uuid = UUID.randomUUID();
        File file = new File(savePath + uuid.toString() + ".png");

        if (Files.exists(file.toPath())) {
            throw new FileExistsException("Path: " + file.toPath() + " already exists!");
        } else {
            try {
                Files.copy(tempFile.toPath(), file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file.toPath().toString();
        }
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
