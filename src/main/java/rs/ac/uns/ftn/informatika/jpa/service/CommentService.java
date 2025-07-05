package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Comment;

import java.util.List;

public interface CommentService {
    Comment addComment(String text, Integer userId, Integer postId);
    public List<CommentDTO> findCommentsByPostId(Integer postId);
    public int countCommentsInLastHour(int userId);
}
