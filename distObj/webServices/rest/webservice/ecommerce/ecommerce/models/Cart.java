package ecommerce.models;

public class Cart {

    private int articleId;
    private int quantity;

    public Cart() {
    }

    public Cart(int articleId, int quantity) {
        this.articleId = articleId;
        this.quantity = quantity;
    }

    public int getArticleId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
