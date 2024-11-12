package rs.ac.uns.ftn.informatika.jpa.pagedResult;

import java.util.List;

public class PagedResults<T> {
    private List<T> results;
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
    public List<T> getResults() {
        return results;
    }
}