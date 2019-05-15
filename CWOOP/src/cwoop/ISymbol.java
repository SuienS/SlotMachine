package cwoop;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

//interface for the symbol class
public interface ISymbol {
    public void setImage(Image Image);
    public ImageView getImage();
    public void setValue(int v);
    public int getValue();
}
