package logic;

import java.awt.*;
import java.io.Serializable;

public class Cell extends Point implements Serializable{

    private Cell previous;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Cell getPrevious() {
        return previous;
    }

    public void setPrevious(Cell previous) {
        this.previous = previous;
    }

}
