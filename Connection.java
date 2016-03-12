import java.util.ArrayList;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Connection {

    private Node n1;
    private Node n2;

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
}
