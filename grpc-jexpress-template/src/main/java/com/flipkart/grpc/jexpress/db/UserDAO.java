package com.flipkart.grpc.jexpress.db;

import com.flipkart.gjex.hibernate.SelectedDataSource;
import com.flipkart.gjex.hibernate.SessionFactoryContext;
import com.flipkart.gjex.hibernate.Transactional;
import com.flipkart.gjex.hibernate.core.AbstractDAO;

import javax.inject.Inject;
import javax.persistence.Query;
import java.util.List;


/**
 * @Transactional and @SelectedDataSource together can be added at both either method level OR class level OR both.
 * When added at class level, it means ALL methods inside that class will support @Transactional semantics.
 * When added at method level, it means @Transactional semantics will start at the beginning of this method.
 *
 */
@Transactional
@SelectedDataSource
public class UserDAO extends AbstractDAO<User> implements IUserDAO {

    @Inject
    public UserDAO(SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    public List<User> getAllUsers() {
        Query query = namedQuery("User.findAll");
        List<User> result = (List<User>) query.getResultList();
        System.out.println(result);
        return result;
    }

    @Override
    public void createUser(String userName) {
        User user = new User();
        user.setName(userName);
        System.out.println("Saving user in database.");
        persist(user);
    }

    @Override
    @Transactional
    @SelectedDataSource
    public User getUserByUserId(Long userId) {
        Query query = namedQuery("User.findById").setParameter("id", userId);
        try {
           return (User) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
