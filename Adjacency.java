/**
 * Created by andersonjd on 3/12/16.
 */
public class Adjacency {

    private Node node;
    private Connection connection;

    public Adjacency(Node node, Connection connection){

        this.node = node;
        this.connection = connection;
    }

    public Node getNode(){
        return this.node;
    }

    public Connection getConnection(){
        return this.connection;
    }
}
