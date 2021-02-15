package htwb.ai.controller.repo;

import htwb.ai.controller.model.Song;
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
public interface SongRepository extends CrudRepository<Song, Integer> {
    @Query(value = "SELECT * FROM songs WHERE id = ?1", nativeQuery = true)
    Song findSongBySongId(Integer userId);

    @Query(value = "SELECT * FROM songs", nativeQuery = true)
    List<Song> getAllSongs();

    @Query(value = "SELECT * FROM songs WHERE artist = ?1", nativeQuery = true)
    List<Song> findSongByArtist(String artist);
}
