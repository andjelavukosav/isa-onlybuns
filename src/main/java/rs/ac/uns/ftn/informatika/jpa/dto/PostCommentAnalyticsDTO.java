package rs.ac.uns.ftn.informatika.jpa.dto;

import java.util.List;

public class PostCommentAnalyticsDTO {
    private List<Integer> weeklyPosts;    // 7 dana
    private List<Integer> weeklyComments;

    private List<Integer> monthlyPosts;   // 4 ili 5 nedelja u mesecu
    private List<Integer> monthlyComments;

    private List<Integer> yearlyPosts;    // 12 meseci
    private List<Integer> yearlyComments;

    // Getteri i setteri

    public PostCommentAnalyticsDTO() {}

    public PostCommentAnalyticsDTO(List<Integer> weeklyPosts, List<Integer> weeklyComments,
                                        List<Integer> monthlyPosts, List<Integer> monthlyComments,
                                        List<Integer> yearlyPosts, List<Integer> yearlyComments) {
        this.weeklyPosts = weeklyPosts;
        this.weeklyComments = weeklyComments;
        this.monthlyPosts = monthlyPosts;
        this.monthlyComments = monthlyComments;
        this.yearlyPosts = yearlyPosts;
        this.yearlyComments = yearlyComments;
    }

    public List<Integer> getWeeklyPosts() {
        return weeklyPosts;
    }

    public void setWeeklyPosts(List<Integer> weeklyPosts) {
        this.weeklyPosts = weeklyPosts;
    }

    public List<Integer> getWeeklyComments() {
        return weeklyComments;
    }

    public void setWeeklyComments(List<Integer> weeklyComments) {
        this.weeklyComments = weeklyComments;
    }

    public List<Integer> getMonthlyPosts() {
        return monthlyPosts;
    }

    public void setMonthlyPosts(List<Integer> monthlyPosts) {
        this.monthlyPosts = monthlyPosts;
    }

    public List<Integer> getMonthlyComments() {
        return monthlyComments;
    }

    public void setMonthlyComments(List<Integer> monthlyComments) {
        this.monthlyComments = monthlyComments;
    }

    public List<Integer> getYearlyPosts() {
        return yearlyPosts;
    }

    public void setYearlyPosts(List<Integer> yearlyPosts) {
        this.yearlyPosts = yearlyPosts;
    }

    public List<Integer> getYearlyComments() {
        return yearlyComments;
    }

    public void setYearlyComments(List<Integer> yearlyComments) {
        this.yearlyComments = yearlyComments;
    }
}
