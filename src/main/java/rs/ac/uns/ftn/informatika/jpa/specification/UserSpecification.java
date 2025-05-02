package rs.ac.uns.ftn.informatika.jpa.specification;

import org.springframework.data.jpa.domain.Specification;
import rs.ac.uns.ftn.informatika.jpa.dto.UserSearchCriteria;
import rs.ac.uns.ftn.informatika.jpa.model.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification implements Specification<User> {

    private final UserSearchCriteria criteria;

    public UserSpecification(UserSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if(!criteria.getFirstName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
        }

        if(!criteria.getLastName().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
        }

        if(!criteria.getEmail().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail() + "%"));
        }

        if(criteria.getMinPostsCount() != null){
            predicates.add(cb.greaterThanOrEqualTo(root.get("postsCount"), criteria.getMinPostsCount()));
        }

        if(criteria.getMaxPostsCount() != null){
            predicates.add(cb.lessThanOrEqualTo(root.get("postsCount"), criteria.getMaxPostsCount()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
