package application.db;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Image {
    private StringProperty path;
    private StringProperty name;
    private javafx.scene.image.Image image;
    private int[][] imageArray;

    public Image(String path, String name, int[][] imageArray, String url) {
        this.path = new SimpleStringProperty(path);
        this.name = new SimpleStringProperty(name);
        image = new javafx.scene.image.Image(url);
        this.imageArray = imageArray;
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public javafx.scene.image.Image getImage() {
        return image;
    }

    public void setImage(javafx.scene.image.Image image) {
        this.image = image;
    }

    public int[][] getImageArray() {
        return imageArray;
    }

    public void setImageArray(int[][] imageArray) {
        this.imageArray = imageArray;
    }

    @Override
    public String toString() {
        return this.name.get();
    }
}
