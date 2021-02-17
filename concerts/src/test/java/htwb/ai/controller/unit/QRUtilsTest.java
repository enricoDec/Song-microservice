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

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 17.02.21
 **/
public class QRUtilsTest {
    private QRUtils qrUtils;
    private Ticket ticket;

    @BeforeEach
    void setup() {
        qrUtils = new QRUtils();
        qrUtils.setSavePath("src/test/resources/sample_qr.png");
        Concert concert = new Concert("Rome", "Enrico", 100);
        TicketTransaction ticketTransaction = new TicketTransaction(false);

        ticket = new Ticket("mmuster", concert, ticketTransaction);
    }

    @Test
    @DisplayName("")
    void goodQrTest() {
        try {
            qrUtils.generateTicketQR(ticket);
        } catch (JsonProcessingException | FileExistsException e) {
            Assertions.fail();
        }
    }
}
