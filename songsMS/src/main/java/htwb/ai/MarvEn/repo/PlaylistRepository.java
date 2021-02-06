package htwb.ai.MarvEn.repo;

import htwb.ai.MarvEn.model.Playlist;
import htwb.ai.MarvEn.model.Song;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Enrico de Chadarevian, Marvin Rausch
 * Project name : Marven
 * @version : 1.0
 * @since : 22-12-2020
 **/
@Repository
public interface PlaylistRepository extends CrudRepository<Playlist, Integer> {
    @Query(value = "SELECT * FROM playlists WHERE owner = ?1", nativeQuery = true)
    List<Playlist> getAllByOwnerId(String user);

    @Query(value = "SELECT * FROM playlists WHERE owner = ?1 AND is_private = FALSE", nativeQuery = true)
    List<Playlist> getAllPublicByOwnerId(String user);

    @Query(value = "SELECT * FROM playlists WHERE id = ?1", nativeQuery = true)
    Playlist getPlaylistById(Integer id);
}
