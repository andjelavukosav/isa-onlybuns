package rs.ac.uns.ftn.informatika.jpa.service;

import rs.ac.uns.ftn.informatika.jpa.dto.PostCommentAnalyticsDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UsersActivityPercentagesDTO;

public interface AnalyticsService {
    PostCommentAnalyticsDTO getPostsAndCommentsAnalytics();
    UsersActivityPercentagesDTO getUserActivityPercentages();


}
