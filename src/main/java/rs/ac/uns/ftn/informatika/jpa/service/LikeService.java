package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;

import java.util.List;

public interface LikeService {
    Like save(Like like);
    LikeDTO findLikeByPostIdAndUserId(Integer postId,Integer userId);
    List<LikeDTO> findLikesByPostId(Integer postId);
}
