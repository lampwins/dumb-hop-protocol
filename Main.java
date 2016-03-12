import java.util.*;

/**
 * Created by andersonjd on 3/12/16.
 */
public class Main {

    public static void main(String[] args){

        int collums = 10;
        int rows = 10;

        Network network = new Network();

        Node[][] routerTopology = new Node[rows][collums];
        Node[][] hostTopology = new Node[rows - 1][collums - 1];

        //provision routers
        int idCounter = 0;
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < collums; j++){
                Router r = new Router((i == 0 || i == rows - 1));
                r.setId(i, j);
                routerTopology[i][j] = r;
            }
        }

        //provision hosts
        idCounter = 0;
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < collums - 1; j++){
                Host h = new Host();
                h.setId(i, j);
                hostTopology[i][j] = h;
            }
        }

        //create router connections
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < collums; j++){

                //not last collum
                if(!(j == collums - 1)) {
                    //this router and the one next to it
                    Connection c = new Connection(routerTopology[i][j], routerTopology[i][j + 1]);
                    network.addConnection(c);
                }

                //not first row
                if(!(i == 0)){
                    //this router and the one above it
                    Connection c = new Connection(routerTopology[i][j], routerTopology[i - 1][j]);
                    network.addConnection(c);
                }

            }
        }

        //create host connections
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < collums - 1; j++){

                //this this host and surrounding routers
                Connection c1 = new Connection(hostTopology[i][j], routerTopology[i][j]);
                Connection c2 = new Connection(hostTopology[i][j], routerTopology[i + 1][j]);
                Connection c3 = new Connection(hostTopology[i][j], routerTopology[i][j + 1]);
                Connection c4 = new Connection(hostTopology[i][j], routerTopology[i + 1][j + 1]);
                network.addConnection(c1);
                network.addConnection(c2);
                network.addConnection(c3);
                network.addConnection(c4);

            }
        }


        //begin simulation
        int iterations = 10,i = 0;

        ArrayList<Transmission> transmissions = new ArrayList<>();

        while(i++ < iterations){

            Random r = new Random();

            Node source = hostTopology[r.nextInt(rows - 1)][r.nextInt(collums - 1)];
            Node destination = hostTopology[r.nextInt(rows - 1)][r.nextInt(collums - 1)];

            if(source.equals(destination)){
                //try again if the same
                i--;
                continue;
            }

            Node currentNode = source;
            Transmission transmission = new Transmission(source, destination, r.nextInt(iterations / 2), r.nextInt(100));
            transmissions.add(transmission);

            int lateralDif = destination.getCol() - source.getCol();
            int verticalDif = destination.getRow() - source.getRow();

            boolean notThereYet = true;

            while(notThereYet) {

                Collection<Adjacency> connectedNodes = network.getAdjacencies(currentNode);
                ArrayList<Adjacency> candidateAdjacencies = new ArrayList<>();

                for(Adjacency a: connectedNodes){
                    if(!transmission.hasNode(a.getNode())){
                        //remove adjacencies that would result in a routing loop
                        candidateAdjacencies.add(a);
                    }
                }

                for(Iterator<Adjacency> iterator = candidateAdjacencies.iterator(); iterator.hasNext();) {

                    Adjacency a = iterator.next();


                }


                if(!currentNode.equals(destination)) {
                    if (candidateAdjacencies.size() > 1) {
                        Collections.shuffle(candidateAdjacencies);
                    }

                    Adjacency selectedPath = candidateAdjacencies.get(0);
                    transmission.addConnection(selectedPath);
                    currentNode = selectedPath.getNode();

                    if (currentNode.equals(destination)) {
                        notThereYet = false;
                    }
                }
            }

            //iteration cleanup
            for(Transmission t: transmissions){
                t.setTtl(t.getTtl() - 1);

                if(t.getTtl() == 0){
                    for(Connection c: t.getConnections()){
                        c.decreaseSaturation(t.getSaturation());
                    }
                    System.out.println("Transmission between " +
                            t.getSource().getClass().toString().replaceAll("class ", "") +
                            " [" + t.getSource().getRow() + "," +
                            t.getSource().getCol() + "] and " +
                            t.getDestination().getClass().toString().replaceAll("class ", "") +
                            " [" + t.getDestination().getRow() + "," +
                            t.getDestination().getCol() + "] Complete");
                    transmissions.remove(t);
                }
            }
        }
    }
}
