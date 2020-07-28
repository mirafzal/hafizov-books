package uz.mirafzal.hafizovbooks.models;

public class Book {
    public int id;
    public int photoId;
    public String name;
    public String author;
    public int price;
    public String path;

    public Book(int id, int photoId, String name, String author, int price, String path) {
        this.id = id;
        this.photoId = photoId;
        this.name = name;
        this.author = author;
        this.price = price;
        this.path = path;
    }
}
