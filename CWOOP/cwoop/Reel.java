package cwoop;

import java.util.ArrayList;
import java.util.Collections;
import javafx.scene.image.Image;

public class Reel{
	//adding images
    private Symbol sSeven=new Symbol(7,new Image("file:src/images/redseven.png"));
    private Symbol sBell=new Symbol(6,new Image("file:src/images/bell.png"));
    private Symbol sWatermelon=new Symbol(5,new Image("file:src/images/watermelon.png"));
    private Symbol sPlum=new Symbol(4,new Image("file:src/images/plum.png"));
    private Symbol sLemon=new Symbol(3,new Image("file:src/images/lemon.png"));
    private Symbol sCherry=new Symbol(2,new Image("file:src/images/cherry.png"));
    
    private ArrayList<Symbol> arrSym;
    
    public Reel(){
    	//creation of arraylist for a reel
        arrSym = new ArrayList<Symbol>();
        arrSym.add(sSeven);
        arrSym.add(sBell);
        arrSym.add(sWatermelon);
        arrSym.add(sPlum);
        arrSym.add(sLemon);
        arrSym.add(sCherry);
        spin();
    }
    //Shuffling the array 
    public void spin(){
        Collections.shuffle(arrSym);        
    }

    //getter method for the shuffled array
    public synchronized ArrayList<Symbol> getArrSym() {
        return arrSym;
    } 
}