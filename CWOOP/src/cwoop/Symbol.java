package cwoop;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Symbol implements ISymbol{
    private int Value;
    private ImageView Img;

    //overloaded  for symbol class 
    public Symbol(int Value, Image Img) {
        this.Value = Value;
        this.Img=new ImageView(Img);
        this.Img.setPreserveRatio(true);
        

    }
    //getter and setter methods
    public int getValue() {
        return Value;
    }

    public void setValue(int Value) {
        this.Value = Value;
    }
    public ImageView getImage() {
        return this.Img;
        
    }
    public void setImage(Image Img) {
        this.Img=new ImageView(Img);
    }
    
    //method which checkes for the equality of the reel images
    public boolean checkEquals(Symbol s){
        boolean match=false;
        if(this.Value==s.getValue()){
            match=true;
        }
        return match;
    }
   

    
}
