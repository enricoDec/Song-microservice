package htwb.ai.controller.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @Column(name = "concert_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    @NotNull
    @Size(max = 50)
    private String location;

    @NotNull
    @Size(max = 50)
    private String artist;

    @Column(name = "max_tickets")
    @NotNull
    private Integer maxTickets;

    public Concert(){};

    public Concert(String location, String artist, Integer maxTickets) {
        this.location = location;
        this.artist = artist;
        this.maxTickets = maxTickets;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getMaxTickets() {
        return maxTickets;
    }

    public void setMaxTickets(Integer maxTickets) {
        this.maxTickets = maxTickets;
    }

    @Override
    public String toString() {
        return "Concert{" +
                "concertId=" + concertId +
                ", location='" + location + '\'' +
                ", artist='" + artist + '\'' +
                ", maxTickets=" + maxTickets +
                '}';
    }
}
