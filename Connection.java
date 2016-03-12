import java.util.ArrayList;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Connection {

    private Node n1;
    private Node n2;
    private int saturation = 0;

    public Connection(Node n1, Node n2){
        this.n1 = n1;
        this.n2 = n2;
    }

    public Node getNode1(){
        return this.n1;
    }

    public Node getNode2(){
        return this.n2;
    }

    public void increaseSaturation(int amount){
        this.saturation += amount;
    }

    public void decreaseSaturation(int amount){
        this.saturation -= amount;
    }

    public int getSaturation(){
        return this.saturation;
    }
}
