package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.jpa.dto.PostCommentAnalyticsDTO;
import rs.ac.uns.ftn.informatika.jpa.dto.UsersActivityPercentagesDTO;
import rs.ac.uns.ftn.informatika.jpa.model.User;
import rs.ac.uns.ftn.informatika.jpa.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.PostRepository;
import rs.ac.uns.ftn.informatika.jpa.repository.UserRepository;
import rs.ac.uns.ftn.informatika.jpa.service.AnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AnalyticsServiceImpl(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PostCommentAnalyticsDTO getPostsAndCommentsAnalytics() {
        LocalDate today = LocalDate.now();

        List<Integer> weeklyPosts = countPostsPerDay(today, 7);
        List<Integer> weeklyComments = countCommentsPerDay(today, 7);

        List<Integer> monthlyPosts = countPostsPerWeekOfMonth(today);
        List<Integer> monthlyComments = countCommentsPerWeekOfMonth(today);

        List<Integer> yearlyPosts = countPostsPerMonth(today);
        List<Integer> yearlyComments = countCommentsPerMonth(today);

        return new PostCommentAnalyticsDTO(
                weeklyPosts, weeklyComments,
                monthlyPosts, monthlyComments,
                yearlyPosts, yearlyComments
        );
    }

    @Override
    @Transactional
    public UsersActivityPercentagesDTO getUserActivityPercentages() {
        List<User> allUsers = userRepository.findAllNonAdminUsers();
        int total = allUsers.size();

        long usersWithPosts = allUsers.stream().filter(u -> u.getPostsCount() > 0).count();
        long usersWithCommentsOnly = allUsers.stream()
                .filter(u -> u.getPostsCount() == 0 && u.getCommentsCount() > 0)
                .count();
        long usersInactive = allUsers.stream()
                .filter(u -> u.getPostsCount() == 0 && u.getCommentsCount() == 0)
                .count();

        double postsPercentage = (double) usersWithPosts / total * 100;
        double commentsOnlyPercentage = (double) usersWithCommentsOnly / total * 100;
        double inactivePercentage = (double) usersInactive / total * 100;

        return new UsersActivityPercentagesDTO(postsPercentage, commentsOnlyPercentage, inactivePercentage);

    }


    private List<Integer> countPostsPerDay(LocalDate startDate, int days) {
        List<Integer> postsCount = new ArrayList<>();
        LocalDate firstDayOfWeek = startDate.minusDays(startDate.getDayOfWeek().getValue() - 1);
        for (int i = 0; i < days; i++) {
            LocalDate day = firstDayOfWeek.plusDays(i);
            postsCount.add((int) postRepository.countByCreationDateTimeBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()));
        }
        return postsCount;
    }

    private List<Integer> countCommentsPerDay(LocalDate startDate, int days) {
        List<Integer> commentsCount = new ArrayList<>();
        LocalDate firstDayOfWeek = startDate.minusDays(startDate.getDayOfWeek().getValue() - 1);
        for (int i = 0; i < days; i++) {
            LocalDate day = firstDayOfWeek.plusDays(i);
            commentsCount.add((int) commentRepository.countByCreationDateTimeBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()));
        }
        return commentsCount;
    }

    private List<Integer> countPostsPerWeekOfMonth(LocalDate date) {
        return countPerWeekOfMonth(date, true);
    }

    private List<Integer> countCommentsPerWeekOfMonth(LocalDate date) {
        return countPerWeekOfMonth(date, false);
    }

    private List<Integer> countPerWeekOfMonth(LocalDate date, boolean isPost) {
        List<Integer> counts = new ArrayList<>();
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        int daysInMonth = date.lengthOfMonth();
        for (int weekStart = 1; weekStart <= daysInMonth; weekStart += 7) {
            LocalDate startWeek = firstDayOfMonth.plusDays(weekStart - 1);
            LocalDate endWeek = startWeek.plusDays(6);
            if (endWeek.getMonthValue() != date.getMonthValue()) {
                endWeek = date.withDayOfMonth(daysInMonth);
            }
            counts.add((int) (isPost
                    ? postRepository.countByCreationDateTimeBetween(startWeek.atStartOfDay(), endWeek.plusDays(1).atStartOfDay())
                    : commentRepository.countByCreationDateTimeBetween(startWeek.atStartOfDay(), endWeek.plusDays(1).atStartOfDay())));
        }
        return counts;
    }

    private List<Integer> countPostsPerMonth(LocalDate date) {
        return countPerMonth(date, true);
    }

    private List<Integer> countCommentsPerMonth(LocalDate date) {
        return countPerMonth(date, false);
    }

    private List<Integer> countPerMonth(LocalDate date, boolean isPost) {
        List<Integer> counts = new ArrayList<>();
        LocalDate firstDayOfYear = date.withDayOfYear(1);
        for (int month = 1; month <= 12; month++) {
            LocalDate startMonth = firstDayOfYear.withMonth(month).withDayOfMonth(1);
            LocalDate endMonth = startMonth.withDayOfMonth(startMonth.lengthOfMonth());
            counts.add((int) (isPost
                    ? postRepository.countByCreationDateTimeBetween(startMonth.atStartOfDay(), endMonth.plusDays(1).atStartOfDay())
                    : commentRepository.countByCreationDateTimeBetween(startMonth.atStartOfDay(), endMonth.plusDays(1).atStartOfDay())));
        }
        return counts;
    }
}
