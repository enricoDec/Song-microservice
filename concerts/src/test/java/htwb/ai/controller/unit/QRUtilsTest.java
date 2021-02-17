package htwb.ai.controller.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import htwb.ai.controller.model.Concert;
import htwb.ai.controller.model.Ticket;
import htwb.ai.controller.model.TicketTransaction;
import htwb.ai.controller.utils.QRUtils;
import org.apache.commons.io.FileExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 17.02.21
 **/
public class QRUtilsTest {
    private QRUtils qrUtils;
    private Ticket ticket;
    private String path;

    @BeforeEach
    void setup() {
        qrUtils = new QRUtils();
        //Has to be absolute path because of intellij bug with test coverage...
        path = "/Users/enrico/Desktop/KBE-Beleg/concerts/src/test/resources/sample_qr.png";
        qrUtils.setSavePath(path);
        Concert concert = new Concert("Rome", "Enrico", 100);
        TicketTransaction ticketTransaction = new TicketTransaction(false);

        ticket = new Ticket("mmuster", concert, ticketTransaction);
    }


    @Test
    @DisplayName("Good Test Generate Qr")
    void goodQrTest() {
        String location = "";
        try {
            location = qrUtils.generateTicketQR(ticket);
        } catch (JsonProcessingException | FileExistsException e) {
            Assertions.fail(e.getMessage());
        } finally {
            File file = new File(location);
            Assertions.assertTrue(Files.exists(file.toPath()));
            Assertions.assertTrue(file.delete());
        }
    }

    @Test
    @DisplayName("Wrong Path Qr Generation")
    void qrWrongPathTest() {
        String location = "";
        qrUtils.setSavePath("12321dwsdiuh321udhiuijansd");
        try {
            location = qrUtils.generateTicketQR(ticket);
        } catch (JsonProcessingException | FileExistsException e) {
            Assertions.fail(e.getMessage());
        } finally {
            File file = new File(location);
            Assertions.assertTrue(Files.exists(file.toPath()));
            Assertions.assertTrue(file.delete());
        }
    }
}
