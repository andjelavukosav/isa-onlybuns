package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.repository.LikeRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;

import java.util.ArrayList;
import java.util.List;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private LikeRepository likeRepository;

    @Override
    public Like save(Like like) {
        return this.likeRepository.save(like);
    }

    @Override
    public LikeDTO findLikeByPostIdAndUserId(Integer postId,Integer userId){
        Like like = this.likeRepository.findLikeByPostIdAndUserId(postId, userId);
        LikeDTO likeDTO = new LikeDTO();
        likeDTO.setId(like.getId());
        likeDTO.setPostId(like.getPost().getId());
        likeDTO.setUserId(like.getUser().getId());
        likeDTO.setCreationDateTime(like.getCreationDateTime());
        return likeDTO;
    }

    @Override
    public List<LikeDTO> findLikesByPostId(Integer postId) {
        List<Like> likes = this.likeRepository.findLikesByPostId(postId);
        List<LikeDTO> likeDTOs = new ArrayList<>();
        for (Like like : likes) {
            LikeDTO likeDTO = new LikeDTO();
            likeDTO.setId(like.getId());
            likeDTO.setPostId(like.getPost().getId());
            likeDTO.setUserId(like.getUser().getId());
            likeDTO.setCreationDateTime(like.getCreationDateTime());
            likeDTOs.add(likeDTO);
        }
        return likeDTOs;
    }

}
