package ecommerce.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import ecommerce.models.Article;

public class CartDAO {

    private final String ADD_ARTICLE_TO_CART_STATEMENT = "insert into carrito_compra (article_id, quantity) values (?, ?);";
    private final String GET_ARTICLES_IN_CART_STATEMENT = "select carrito_compra.article_id, article_photos.photo, articulos.article_name, carrito_compra.quantity, articulos.price from carrito_compra inner join article_photos on carrito_compra.article_id = article_photos.article_id inner join articulos on carrito_compra.article_id = articulos.article_id;";
    private final String REMOVE_ARTICLE_FROM_CART_STATEMENT = "delete from carrito_compra where article_id = ?;";
    private final String DELETE_CART_STATEMENT = "delete from carrito_compra;";

    private Connection connection;

    public CartDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addArticleToCart(int articleId, int quantity) {
        try {
            PreparedStatement addArticleToCartStatement = connection
                    .prepareStatement(ADD_ARTICLE_TO_CART_STATEMENT);
            try {
                addArticleToCartStatement.setInt(1, articleId);
                addArticleToCartStatement.setInt(2, quantity);
                return addArticleToCartStatement.executeUpdate() == 1;
            } finally {
                addArticleToCartStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Article> getArticlesInCart() {
        try {
            PreparedStatement getArticlesInCartStatement = connection
                    .prepareStatement(GET_ARTICLES_IN_CART_STATEMENT);
            ResultSet results = null;
            try {
                results = getArticlesInCartStatement.executeQuery();
                List<Article> articles = new ArrayList<Article>();
                while (results.next()) {
                    Article article = new Article();
                    article.setId(results.getInt("article_id"));
                    article.setPhoto(results.getBytes("photo"));
                    article.setName(results.getString("article_name"));
                    article.setQuantity(results.getInt("quantity"));
                    article.setPrice(results.getBigDecimal("price"));
                    articles.add(article);
                }
                return articles;
            } finally {
                if (results != null)
                    results.close();
                getArticlesInCartStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean removeArticleFromCart(int articleId) {
        try {
            PreparedStatement removeArticleFromCartStatement = connection
                    .prepareStatement(REMOVE_ARTICLE_FROM_CART_STATEMENT);
            try {
                removeArticleFromCartStatement.setInt(1, articleId);
                return removeArticleFromCartStatement.executeUpdate() == 1;
            } finally {
                removeArticleFromCartStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCart() {
        try {
            PreparedStatement deleteCartStatement = connection
                    .prepareStatement(DELETE_CART_STATEMENT);
            try {
                return deleteCartStatement.executeUpdate() != 0;
            } finally {
                deleteCartStatement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
