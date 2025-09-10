package rs.ac.uns.ftn.informatika.jpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.jpa.dto.PostCommentAnalyticsDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UsersActivityPercentagesDTO;
import rs.ac.uns.ftn.informatika.jpa.service.AnalyticsService;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/posts-comments")
    public ResponseEntity<PostCommentAnalyticsDTO> getPostsCommentsAnalytics() {
        return ResponseEntity.ok(analyticsService.getPostsAndCommentsAnalytics());
    }

    @GetMapping("/user-activity")
    public ResponseEntity<UsersActivityPercentagesDTO> getUserActivityPercentages() {
        UsersActivityPercentagesDTO dto = analyticsService.getUserActivityPercentages();
        return ResponseEntity.ok(dto);
    }
}
