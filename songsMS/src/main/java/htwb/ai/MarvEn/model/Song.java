package htwb.ai.MarvEn.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 31-10-2020
 **/
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String artist;
    private String label;
    private Integer released;

    @ManyToMany(mappedBy = "songList")
    List<Playlist> playlists = new ArrayList<>(0);

    public Song() {
    }

    public Song(String title, String artist, String label, Integer released) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public Song(Integer id, String title, String artist, String label, Integer released) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.released = released;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLabel() {
        return label;
    }

    public Integer getReleased() {
        return released;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setReleased(Integer releaseYear) {
        this.released = releaseYear;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", label='" + label + '\'' +
                ", released=" + released +
                ", playlists=" + playlists +
                '}';
    }
}
