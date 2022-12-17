package ecommerce;

import java.math.BigDecimal;

import javax.sql.DataSource;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ecommerce.dto.*;
import ecommerce.dao.*;
import ecommerce.models.*;

@Path("ws")
public class WebService {

        private final static String DATASOURCE_CONTEXT = "java:comp/env/jdbc/ecommerce_datasource";

        static DataSource pool = null;
        static {
                try {
                        javax.naming.Context cntxt = new javax.naming.InitialContext();
                        pool = (DataSource) cntxt.lookup(DATASOURCE_CONTEXT);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        static Gson gson = GsonBuilder()
                        .registerTypeAdapter(byte[].class, new ecommerce.utils.GsonBase64Adapter())
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        @POST
        @Path("addArticle")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response addArticle(String json) {
                AddArticleDTO addArticleDTO = gson.fromJson(json, AddArticleDTO.class);
                Article article = addArticleDTO.article;
                if (article == null)
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Article is null" + json))).build();
                if (article.getName() == null || article.getName().equals(""))
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid article name"))).build();
                if (article.getDescription() == null || article.getDescription().equals(""))
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid article description"))).build();
                if (article.getQuantity() < 0)
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid article quantity"))).build();
                if (article.getPrice() == null || article.getPrice().compareTo(BigDecimal.ZERO) < 0)
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid article price"))).build();
                ArticleDAO articleDAO = new ArticleDAO(pool.getConnection());
                return articleDAO.addArticle(article) ? Response.status(Response.Status.OK).build()
                                : Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .entity(gson.toJson(new MessageDTO("Article couldn't be added")))
                                                .build();
        }

        @POST
        @Path("searchArticle")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response searchArticle(String json) {
                SearchArticlesDTO searchArticlesDTO = gson.fromJson(json, SearchArticlesDTO.class);
                String keyword = searchArticlesDTO.keyword;
                if (keyword == null || keyword.equals(""))
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Empty search"))).build();
                ArticleDAO articleDAO = new ArticleDAO(pool.getConnection());
                return Response.status(Response.Status.OK)
                                .entity(gson.toJson(articleDAO.searchArticles(keyword).toArray())).build();
        }

        @POST
        @Path("addArticleToCart")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response addArticleToCart(String json) {
                AddArticleToCartDTO addArticleToCartDTO = gson.fromJson(json, AddArticleToCartDTO.class);
                int articleId = addArticleToCartDTO.articleId;
                int quantity = addArticleToCartDTO.quantity;
                if (quantity < 0)
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid quantity"))).build();
                ArticleNCartDAO articleNCartDAO = new ArticleNCartDAO(pool.getConnection());
                return articleNCartDAO.addArticleToCartTransaction(articleId, quantity)
                                ? Response.status(Response.Status.OK)
                                                .entity(gson.toJson(new MessageDTO("Article added to cart"))).build()
                                : Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .entity(gson.toJson(
                                                                new MessageDTO("Article couldn't be added to cart")))
                                                .build();
        }

        @GET
        @Path("getCart")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response getCart() {
                CartDAO cartDAO = new CartDAO(pool.getConnection());
                return Response.status(Response.Status.OK)
                                .entity(gson.toJson(cartDAO.getArticlesInCart())).build();
        }

        @POST
        @Path("removeArticleFromCart")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response removeArticleFromCart(String json) {
                RemoveArticleFromCartDTO removeArticleFromCartDTO = gson.fromJson(json, RemoveArticleFromCartDTO.class);
                int articleId = removeArticleFromCartDTO.articleId;
                if (articleId < 0)
                        return Response.status(Response.Status.BAD_REQUEST)
                                        .entity(gson.toJson(new MessageDTO("Invalid article id"))).build();
                ArticleNCartDAO articleNCartDAO = new ArticleNCartDAO(pool.getConnection());
                return articleNCartDAO.removeArticleFromCartTransaction(articleId)
                                ? Response.status(Response.Status.OK)
                                                .entity(gson.toJson(new MessageDTO("Article removed"))).build()
                                : Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .entity(gson.toJson(new MessageDTO(
                                                                "Article couldn't be removed from cart")))
                                                .build();
        }

        @DELETE
        @Path("eraseCart")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response eraseCart() {
                ArticleNCartDAO articleNCartDAO = new ArticleNCartDAO(pool.getConnection());
                return articleNCartDAO.deleteCartTransaction()
                                ? Response.status(Response.Status.OK)
                                                .entity(gson.toJson(new MessageDTO("Cart erased"))).build()
                                : Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                .entity(gson.toJson(new MessageDTO("Cart couldn't be erased"))).build();
        }

}
