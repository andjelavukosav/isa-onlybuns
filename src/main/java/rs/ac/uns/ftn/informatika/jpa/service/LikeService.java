package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.cache.annotation.Cacheable;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

import java.util.List;

public interface LikeService {
    Like save(Like like);
    Like findLikeByPostIdAndUserId(Integer postId,Integer userId);
    List<LikeDTO> findLikesByPostId(Integer postId);
    List<LikeDTO> findAll();
    long countLikesByPostId(int postId);
    List<Long> getLikedPostIdsByUser(int userId);


    }
