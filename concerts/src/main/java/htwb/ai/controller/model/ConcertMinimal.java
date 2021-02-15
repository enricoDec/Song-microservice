package htwb.ai.controller.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author : Enrico Gamil Toros
 * Project name : KBE-Beleg
 * @version : 1.0
 * @since : 15.02.21
 **/
public class ConcertMinimal {

    @NotNull
    @Size(max = 50)
    private String location;

    @NotNull
    @Size(max = 50)
    private String artist;

    @NotNull
    private Integer maxTickets;

    public ConcertMinimal() {
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
}
