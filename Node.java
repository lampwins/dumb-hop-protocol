/**
 * Created by andersonjd on 3/12/16.
 */
public abstract class Node {

    private int row;
    private int col;

    public void setId(int row, int col){
        this.row = row;
        this.col = col;
    }

    public int getRow(){
        return this.row;
    }

    public int getCol(){
        return this.col;
    }

}
