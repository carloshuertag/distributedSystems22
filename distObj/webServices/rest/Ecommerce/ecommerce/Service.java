/*
  Servicio.java
  Servicio web tipo REST
  Recibe parámetros utilizando JSON
  Carlos Pineda Guerrero, noviembre 2022
*/

package ecommerce;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import java.sql.*;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

// la URL del servicio web es http://localhost:8080/Servicio/rest/ws
// donde:
//	"Servicio" es el dominio del servicio web (es decir, el nombre de archivo Servicio.war)
//	"rest" se define en la etiqueta <url-pattern> de <servlet-mapping> en el archivo WEB-INF\web.xml
//	"ws" se define en la siguiente anotacin @Path de la clase Servicio

@Path("ws")
public class Service {
  static DataSource pool = null;
  static {
    try {
      Context ctx = new InitialContext();
      pool = (DataSource) ctx.lookup("java:comp/env/jdbc/datasource_Service");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static Gson j = new GsonBuilder().registerTypeAdapter(byte[].class, new AdaptadorGsonBase64())
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

  private final String ADD_ARTICLE_STATEMENT = "insert into articulos (article_name, article_description, price, quantity) values (?, ?, ?, ?);";
  private final String ADD_ARTICLE_PHOTO_STATEMENT = "insert into article_photos (photo, article_id) values (?, (select article_id from articulos where article_name=?));";
  private final String SEARCH_ARTICLES_STATEMENT = "select articulos.article_id, article_photos.photo, articulos.article_name, articulos.article_description, articulos.price from articulos inner join article_photos on articulos.article_id = article_photos.article_id where article_name like ? or article_description like ?;";
  private final String GET_ARTICLE_QUANTITY_STATEMENT = "select articulos.quantity from articulos where article_id = ?;";
  private final String UPDATE_ARTICLE_QUANTITY_ADD_STATEMENT = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity - carrito_compra.quantity where articulos.article_id = ?;";
  private final String UPDATE_ARTICLE_QUANTITY_REMOVE_STATEMENT = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity where articulos.article_id = ?;";
  private final String UPDATE_ARTCILE_QUANTITY_RESET = "update articulos inner join carrito_compra on articulos.article_id=carrito_compra.article_id set articulos.quantity = articulos.quantity + carrito_compra.quantity;";
  private final String ADD_ARTICLE_TO_CART_STATEMENT = "insert into carrito_compra (article_id, quantity) values (?, ?);";
  private final String GET_ARTICLES_IN_CART_STATEMENT = "select carrito_compra.article_id, article_photos.photo, articulos.article_name, carrito_compra.quantity, articulos.price from carrito_compra inner join article_photos on carrito_compra.article_id = article_photos.article_id inner join articulos on carrito_compra.article_id = articulos.article_id;";
  private final String REMOVE_ARTICLE_FROM_CART_STATEMENT = "delete from carrito_compra where article_id = ?;";
  private final String DELETE_CART_STATEMENT = "delete from carrito_compra;";

  @POST
  @Path("addArticle")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addArticle(String json) throws Exception {
    AddArticleDTO p = (AddArticleDTO) j.fromJson(json, AddArticleDTO.class);
    Article article = p.article;

    Connection conexion = pool.getConnection();

    if (article.name == null || article.name.equals(""))
      return Response.status(400).entity(j.toJson(new Error("Invalid name"))).build();

    if (article.description == null || article.description.equals(""))
      return Response.status(400).entity(j.toJson(new Error("Invalid description"))).build();

    if (article.price == null || article.price.compareTo(BigDecimal.ZERO) <= 0)
      return Response.status(400).entity(j.toJson(new Error("Invalid price"))).build();

    if (article.quantity <= 0)
      return Response.status(400).entity(j.toJson(new Error("Invalid quantity"))).build();

    try {
      conexion.setAutoCommit(false);

      PreparedStatement addArticleSttmnt = conexion.prepareStatement(ADD_ARTICLE_STATEMENT);

      try {
        addArticleSttmnt.setString(1, article.name);
        addArticleSttmnt.setString(2, article.description);
        addArticleSttmnt.setBigDecimal(3, article.price);
        addArticleSttmnt.setInt(4, article.quantity);
        addArticleSttmnt.executeUpdate();
      } finally {
        addArticleSttmnt.close();
      }

      if (article.photo != null) {
        PreparedStatement addArticlePhotoSttmnt = conexion.prepareStatement(ADD_ARTICLE_PHOTO_STATEMENT);
        try {
          addArticlePhotoSttmnt.setBytes(1, article.photo);
          addArticlePhotoSttmnt.setString(2, article.name);
          addArticlePhotoSttmnt.executeUpdate();
        } finally {
          addArticlePhotoSttmnt.close();
        }
      }
      conexion.commit();
    } catch (Exception e) {
      conexion.rollback();
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.setAutoCommit(true);
      conexion.close();
    }
    return Response.ok().build();
  }

  @POST
  @Path("searchArticles")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchArticles(String json) throws Exception {
    SearchArticlesDTO p = (SearchArticlesDTO) j.fromJson(json, SearchArticlesDTO.class);
    String keyword = p.keyword;

    Connection conexion = pool.getConnection();

    try {
      PreparedStatement searchArticlesSttmnt = conexion.prepareStatement(SEARCH_ARTICLES_STATEMENT);
      try {
        keyword = "%" + keyword + "%";
        searchArticlesSttmnt.setString(1, keyword);
        searchArticlesSttmnt.setString(2, keyword);

        ResultSet rs = searchArticlesSttmnt.executeQuery();
        try {
          List<Article> articles = new ArrayList<Article>();
          Article article = null;
          while (rs.next()) {
            article = new Article();
            article.id = rs.getInt("article_id");
            article.photo = rs.getBytes("photo");
            article.name = rs.getString("article_name");
            article.description = rs.getString("article_description");
            article.price = rs.getBigDecimal("price");
            articles.add(article);
          }
          return Response.ok().entity(j.toJson(articles.toArray())).build();
        } finally {
          rs.close();
        }
      } finally {
        searchArticlesSttmnt.close();
      }
    } catch (Exception e) {
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.close();
    }
  }

  @POST
  @Path("addArticleToCart")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addArticleToCart(String json) throws Exception {
    AddArticleToCartDTO p = (AddArticleToCartDTO) j.fromJson(json, AddArticleToCartDTO.class);
    int articleId = p.articleId;
    int quantity = p.quantity;
    int stock = -1;

    Connection conexion = pool.getConnection();

    try {
      PreparedStatement getArticleQuantitySttmnt = conexion.prepareStatement(GET_ARTICLE_QUANTITY_STATEMENT);
      try {
        getArticleQuantitySttmnt.setInt(1, articleId);

        ResultSet rs = getArticleQuantitySttmnt.executeQuery();
        try {
          if (rs.next()) {
            stock = rs.getInt("quantity");
          }
        } finally {
          rs.close();
        }
      } finally {
        getArticleQuantitySttmnt.close();
      }
    } catch (Exception e) {
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    }

    if (stock == -1)
      return Response.status(400).entity(j.toJson(new Error("El articulo no existe"))).build();
    if (stock < quantity)
      return Response.status(400).entity(j.toJson(new Error("No hay suficientes articulos"))).build();

    conexion.setAutoCommit(false);
    try {
      PreparedStatement addArticleToCartStatement = conexion.prepareStatement(ADD_ARTICLE_TO_CART_STATEMENT);
      try {
        addArticleToCartStatement.setInt(1, articleId);
        addArticleToCartStatement.setInt(2, quantity);
        addArticleToCartStatement.executeUpdate();
      } finally {
        addArticleToCartStatement.close();
      }

      PreparedStatement updateArticleQuantityAddSttmnt = conexion
          .prepareStatement(UPDATE_ARTICLE_QUANTITY_ADD_STATEMENT);
      try {
        updateArticleQuantityAddSttmnt.setInt(1, articleId);
        updateArticleQuantityAddSttmnt.executeUpdate();
      } finally {
        updateArticleQuantityAddSttmnt.close();
      }

      conexion.commit();
    } catch (Exception e) {
      conexion.rollback();
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.setAutoCommit(true);
      conexion.close();
    }
    return Response.ok().build();
  }

  @POST
  @Path("getCart")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCart(String json) throws Exception {
    Connection conexion = pool.getConnection();
    try {
      PreparedStatement getArticlesInCartStatement = conexion.prepareStatement(GET_ARTICLES_IN_CART_STATEMENT);
      try {
        ResultSet rs = getArticlesInCartStatement.executeQuery();
        try {
          List<Article> articles = new ArrayList<Article>();
          Article article = null;
          while (rs.next()) {
            article = new Article();
            article.id = rs.getInt("article_id");
            article.photo = rs.getBytes("photo");
            article.name = rs.getString("article_name");
            article.description = rs.getString("article_description");
            article.price = rs.getBigDecimal("price");
            articles.add(article);
          }
          return Response.ok().entity(j.toJson(articles.toArray())).build();
        } finally {
          rs.close();
        }
      } finally {
        getArticlesInCartStatement.close();
      }
    } catch (Exception e) {
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.close();
    }
  }

  @POST
  @Path("removeArticleFromCart")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response removeArticleFromCart(String json) throws Exception {
    RemoveArticleFromCartDTO p = (RemoveArticleFromCartDTO) j.fromJson(json, RemoveArticleFromCartDTO.class);
    int articleId = p.articleId;

    Connection conexion = pool.getConnection();

    try {
      conexion.setAutoCommit(false);
      PreparedStatement updateArticleQuantityRemoveSttmnt = conexion
          .prepareStatement(UPDATE_ARTICLE_QUANTITY_REMOVE_STATEMENT);
      try {
        updateArticleQuantityRemoveSttmnt.setInt(1, articleId);
        updateArticleQuantityRemoveSttmnt.executeUpdate();
      } finally {
        updateArticleQuantityRemoveSttmnt.close();
      }

      PreparedStatement removeArticleFromCartStatement = conexion
          .prepareStatement(REMOVE_ARTICLE_FROM_CART_STATEMENT);
      try {
        removeArticleFromCartStatement.setInt(1, articleId);
        removeArticleFromCartStatement.executeUpdate();
      } finally {
        removeArticleFromCartStatement.close();
      }
      conexion.commit();
    } catch (Exception e) {
      conexion.rollback();
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.setAutoCommit(true);
      conexion.close();
    }
    return Response.ok().build();
  }

  @POST
  @Path("eraseCart")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response eraseCart(String json) throws Exception {
    Connection conexion = pool.getConnection();

    try {
      conexion.setAutoCommit(false);
      PreparedStatement updateArticleQuantityResetSttmnt = conexion
          .prepareStatement(UPDATE_ARTCILE_QUANTITY_RESET);
      try {
        updateArticleQuantityResetSttmnt.executeUpdate();
      } finally {
        updateArticleQuantityResetSttmnt.close();
      }

      PreparedStatement deleteCartStatement = conexion
          .prepareStatement(DELETE_CART_STATEMENT);
      try {
        deleteCartStatement.executeUpdate();
      } finally {
        deleteCartStatement.close();
      }
      conexion.commit();
    } catch (Exception e) {
      conexion.rollback();
      return Response.status(400).entity(j.toJson(new Error(e.getMessage()))).build();
    } finally {
      conexion.setAutoCommit(true);
      conexion.close();
    }
    return Response.ok().build();
  }
}
