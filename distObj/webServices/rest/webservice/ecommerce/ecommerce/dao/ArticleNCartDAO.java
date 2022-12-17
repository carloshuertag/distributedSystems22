package ecommerce.dao;

import java.sql.Connection;

public class ArticleNCartDAO {

    private Connection connection;

    public ArticleNCartDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addArticleToCartTransaction(int articleId, int quantity) {
        ArticleDAO articleDAO = new ArticleDAO(connection);
        int currentQuantity = articleDAO.getArticleQuantity(articleId);
        if (currentQuantity < quantity) {
            return false;
        }
        try {
            connection.setAutoCommit(false);
            CartDAO cartDAO = new CartDAO(connection);
            boolean articleAddedToCart = cartDAO.addArticleToCart(articleId, quantity);
            boolean articleUpdated = articleDAO.updateArticleQuantityAdd(articleId);
            if (articleAddedToCart && articleUpdated) {
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean removeArticleFromCartTransaction(int articleId) {
        ArticleDAO articleDAO = new ArticleDAO(connection);
        CartDAO cartDAO = new CartDAO(connection);
        try {
            connection.setAutoCommit(false);
            boolean articleUpdated = articleDAO.updateArticleQuantityRemove(articleId);
            boolean articleRemovedFromCart = cartDAO.removeArticleFromCart(articleId);
            if (articleUpdated && articleRemovedFromCart) {
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean deleteCartTransaction() {
        ArticleDAO articleDAO = new ArticleDAO(connection);
        CartDAO cartDAO = new CartDAO(connection);
        try {
            connection.setAutoCommit(false);
            boolean articlesUpdated = articleDAO.updateArticleResetQuantity();
            boolean cartDeleted = cartDAO.deleteCart();
            if (articlesUpdated && cartDeleted) {
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
