/**
 * Created by andersonjd on 3/12/16.
 */
public class Router extends Node{


    private boolean edgeNode = false;

    public Router(boolean edgeNode){
        this.edgeNode = edgeNode;
    }

    public boolean isEdgeNode(){
        return this.edgeNode;
    }
}
