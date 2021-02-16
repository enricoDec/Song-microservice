package htwb.ai.controller.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 22-12-2020
 **/
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty
    @Size(max = 45)
    private String name;

    @Column(name = "is_private")
    @NotNull
    private Boolean isPrivate;

    @Column(name = "owner")
    @Size(max = 50)
    private String ownerId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "playlists_songs",
            joinColumns = @JoinColumn(name = "playlist"),
            inverseJoinColumns = @JoinColumn(name = "song")
    )

    @NotEmpty
    private List<Song> songList = new ArrayList<>(0);

    public Playlist() {
    }

    public Playlist(String name, Boolean isPrivate, String ownerId) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.ownerId = ownerId;
    }

    public Playlist(String name, Boolean isPrivate, String ownerId, List<Song> songList) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.ownerId = ownerId;
        this.songList = songList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean aPublic) {
        isPrivate = aPublic;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String owner) {
        this.ownerId = owner;
    }

    public void addSong(Song song) {
        songList.add(song);
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isPrivate=" + isPrivate +
                ", owner='" + ownerId + '\'' +
                ", songs=" + songList +
                '}';
    }
}
