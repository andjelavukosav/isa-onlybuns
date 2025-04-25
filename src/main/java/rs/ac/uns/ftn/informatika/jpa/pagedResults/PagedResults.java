package rs.ac.uns.ftn.informatika.jpa.pagedResults;

import java.util.List;

public class PagedResults<T> {
    private List<T> results;
    private int totalCount;

    public PagedResults(List<T> posts, int size) {
        this.results = posts;
        this.totalCount = size;
    }

    public PagedResults() {

    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public void setResults(List<T> results){ this.results = results; }
    public List<T> getResults() { return results; }

}
