package htwb.ai.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 14.02.21
 **/
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "ticket_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @NotNull
    @Size(max = 50)
    private String owner;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "concert_id")
    @JsonManagedReference
    private Concert concert;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TicketTransaction ticketTransaction;

    @Column(name = "ticket_qr_path")
    @JsonIgnore
    private String qrCodePath;

    public Ticket() {
    }

    public Ticket(String owner, Concert concert, TicketTransaction ticketTransaction) {
        this.owner = owner;
        this.concert = concert;
        this.ticketTransaction = ticketTransaction;
    }

    public Ticket(String owner, Concert concert, TicketTransaction ticketTransaction, String qrCodePath) {
        this.owner = owner;
        this.concert = concert;
        this.ticketTransaction = ticketTransaction;
        this.qrCodePath = qrCodePath;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public TicketTransaction getTicketTransaction() {
        return ticketTransaction;
    }

    public void setTicketTransaction(TicketTransaction ticketTransaction) {
        this.ticketTransaction = ticketTransaction;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", owner='" + owner + '\'' +
                ", concert=" + concert +
                ", ticketTransaction=" + ticketTransaction +
                ", qrCodePath='" + qrCodePath + '\'' +
                '}';
    }
}
