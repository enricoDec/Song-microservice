package htwb.ai.controller.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
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

    @Transient
    @JsonInclude
    private List<SongData> songDataList = new ArrayList<>();

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Ticket> tickets = new ArrayList<>();

    public Concert() {
    }

    public Concert(@NotNull @Size(max = 50) String location, @NotNull @Size(max = 50) String artist, @NotNull Integer maxTickets) {
        this.location = location;
        this.artist = artist;
        this.maxTickets = maxTickets;
    }

    public List<SongData> getSongList() {
        return songDataList;
    }

    public void setSongList(List<SongData> songDataList) {
        this.songDataList = songDataList;
    }

    public void addSong(SongData songData) {
        songDataList.add(songData);
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

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @Override
    public String toString() {
        return "Concert{" +
                "concertId=" + concertId +
                ", location='" + location + '\'' +
                ", artist='" + artist + '\'' +
                ", maxTickets=" + maxTickets +
                ", songList=" + songDataList +
                ", tickets=" + tickets +
                '}';
    }
}
