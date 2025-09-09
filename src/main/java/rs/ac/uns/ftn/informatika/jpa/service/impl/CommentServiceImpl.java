package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Comment;
import rs.ac.uns.ftn.informatika.jpa.model.Post;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.FollowRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.CommentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    @Transactional
    public Comment addComment(String text, Integer userId, Integer postId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post does not exist."));

        User postOwner = post.getUser();

        boolean follows = followRepository.existsByFollowerAndFollowed(user, postOwner);
        if (!follows) {
            throw new IllegalStateException("You cannot comment on a post from a user you do not follow.");
        }

        int commentsInLastHour = countCommentsInLastHour(userId);
        if (commentsInLastHour >= 60) {
        //if (commentsInLastHour >= 2) {
            throw new RuntimeException("Limit of 60 comments per hour exceeded.");
        }

        Comment comment = new Comment();
        comment.setText(text);
        user.addComment(comment);
        //comment.setUser(user);
        //comment.setPost(post);
        post.addComment(comment);
        comment.setCreationDateTime(LocalDateTime.now());

        return commentRepository.save(comment);
    }


    @Override
    public List<CommentDTO> findCommentsByPostId(Integer postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreationDateTimeDesc(postId);
        List<CommentDTO> commentDTOs = new ArrayList<>();

        for (Comment comment : comments) {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setText(comment.getText());
            dto.setUserId(comment.getUser().getId());
            dto.setPostId(comment.getPost().getId());
            dto.setCreationDateTime(comment.getCreationDateTime());
            dto.setUsername(comment.getUser().getUsername());
            commentDTOs.add(dto);
        }

        return commentDTOs;
    }

    @Override
    public int countCommentsInLastHour(int userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return commentRepository.countByUserIdAndCreationDateTimeAfter(userId, oneHourAgo);
    }

}
