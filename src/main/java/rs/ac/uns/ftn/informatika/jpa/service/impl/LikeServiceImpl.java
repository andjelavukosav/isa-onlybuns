package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.LikeDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Like;
import rs.ac.uns.ftn.informatika.jpa.model.Location;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.LikeRepository;
import rs.ac.uns.ftn.informatika.jpa.service.LikeService;
import rs.ac.uns.ftn.informatika.jpa.service.PostService;
import rs.ac.uns.ftn.informatika.jpa.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeRepository likeRepository;


    @Override
    public Like save(Like like) {
        return this.likeRepository.save(like);
    }

    @Override
    public Like findLikeByPostIdAndUserId(Integer postId,Integer userId){
        return this.likeRepository.findByPostIdAndUserId(postId, userId);
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


    @Override
    public long countLikesByPostId(int postId) {
        return likeRepository.countByPostId(postId);
    }


    @Override
    public List<LikeDTO> findAll(){
        List<Like> likes = this.likeRepository.findAll();
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

    @Override
    public List<Long> getLikedPostIdsByUser(int userId) {
        return likeRepository.findPostIdsByUserId(userId);
    }



}
