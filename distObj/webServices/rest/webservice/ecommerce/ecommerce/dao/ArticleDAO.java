package ecommerce.dao;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ecommerce.models.Article;

public class ArticleDAO {

    private final String ADD_ARTICLE_STATEMENT = "insert into articulos (article_name, article_description, price, quantity) values (?, ?, ?, ?);";
    private final String ADD_ARTICLE_PHOTO_STATEMENT = "insert into article_photos (photo, article_id) values (?, ?);";
    private final String SEARCH_ARTICLES_STATEMENT = "select articulos.article_id, article_photos.photo, articulos.article_name, articulos.article_description, articulos.price from articulos inner join article_photos on articulos.article_id = article_photos.article_id where article_name like ? or article_description like ?;";
    private final String GET_ARTICLE_QUANTITY_STATEMENT = "select articulos.quantity from articulos where article_id = ?;";
    private final String UPDATE_ARTICLE_QUANTITY_ADD_STATEMENT = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = ?;";
    private final String UPDATE_ARTICLE_QUANTITY_REMOVE_STATEMENT = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity where articulos.article_id = ?;";
    private final String UPDATE_ARTCILE_QUANTITY_RESET = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity;";

    private Connection connection;

    public ArticleDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addArticle(Article article) {
        if (article.getPhoto() == null || article.getPhoto().length == 0) {
            boolean articleAdded = addArticleData(article);
            return articleAdded;
        }
        try {
            connection.setAutoCommit(false);
            boolean articleAdded = addArticleData(article);
            boolean photoAdded = addArticlePhoto(article);
            if (articleAdded && photoAdded) {
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

    private boolean addArticleData(Article article) {
        try {
            PreparedStatement addArticleSttmnt = connection.prepareStatement(ADD_ARTICLE_STATEMENT);
            try {
                addArticleSttmnt.setString(1, article.getName());
                addArticleSttmnt.setString(2, article.getDescription());
                addArticleSttmnt.setBigDecimal(3, article.getPrice());
                addArticleSttmnt.setInt(4, article.getQuantity());
                return addArticleSttmnt.executeUpdate() == 1;
            } finally {
                addArticleSttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addArticlePhoto(Article article) {
        try {
            PreparedStatement addArticlePhotoSttmnt = connection.prepareStatement(ADD_ARTICLE_PHOTO_STATEMENT);
            try {
                addArticlePhotoSttmnt.setBytes(1, article.getPhoto());
                addArticlePhotoSttmnt.setInt(2, article.getId());
                return addArticlePhotoSttmnt.executeUpdate() == 1;
            } finally {
                addArticlePhotoSttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Article> searchArticles(String keyword) {
        try {
            PreparedStatement searchArticlesSttmnt = connection.prepareStatement(SEARCH_ARTICLES_STATEMENT);
            ResultSet results = null;
            try {
                keyword = "%" + keyword + "%";
                searchArticlesSttmnt.setString(1, keyword);
                searchArticlesSttmnt.setString(2, keyword);
                results = searchArticlesSttmnt.executeQuery();
                List<Article> articles = new ArrayList<>();
                Article article = null;
                while (results.next()) {
                    article = new Article();
                    article.setId(results.getInt("article_id"));
                    article.setPhoto(results.getBytes("photo"));
                    article.setName(results.getString("article_name"));
                    article.setDescription(results.getString("article_description"));
                    article.setPrice(results.getBigDecimal("price"));
                    articles.add(article);
                }
                return articles;
            } finally {
                if (results != null)
                    results.close();
                searchArticlesSttmnt.close();
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

    public int getArticleQuantity(int articleId) {
        try {
            PreparedStatement getArticleQuantitySttmnt = connection.prepareStatement(GET_ARTICLE_QUANTITY_STATEMENT);
            ResultSet results = null;
            try {
                getArticleQuantitySttmnt.setInt(1, articleId);
                results = getArticleQuantitySttmnt.executeQuery();
                return (results.next()) ? results.getInt("quantity") : -1;
            } finally {
                if (results != null)
                    results.close();
                getArticleQuantitySttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean updateArticleQuantityAdd(int articleId) {
        try {
            PreparedStatement updateArticleQuantityAddSttmnt = connection
                    .prepareStatement(UPDATE_ARTICLE_QUANTITY_ADD_STATEMENT);
            try {
                updateArticleQuantityAddSttmnt.setInt(1, articleId);
                return updateArticleQuantityAddSttmnt.executeUpdate() == 1;
            } finally {
                updateArticleQuantityAddSttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateArticleQuantityRemove(int articleId) {
        try {
            PreparedStatement updateArticleQuantityRemoveSttmnt = connection
                    .prepareStatement(UPDATE_ARTICLE_QUANTITY_REMOVE_STATEMENT);
            try {
                updateArticleQuantityRemoveSttmnt.setInt(1, articleId);
                return updateArticleQuantityRemoveSttmnt.executeUpdate() == 1;
            } finally {
                updateArticleQuantityRemoveSttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateArticleResetQuantity() {
        try {
            PreparedStatement updateArticleQuantityResetSttmnt = connection
                    .prepareStatement(UPDATE_ARTCILE_QUANTITY_RESET);
            try {
                return updateArticleQuantityResetSttmnt.executeUpdate() != 0;
            } finally {
                updateArticleQuantityResetSttmnt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
