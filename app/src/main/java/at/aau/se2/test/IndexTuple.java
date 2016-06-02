package at.aau.se2.test;

/**
 * Created by KingSeybro on 02.06.2016.
 */
public class IndexTuple {

    private int index_i;
    private int index_j;

    public IndexTuple(int i, int j) {
        index_i = i;
        index_j = j;
    }


    public int getIndex_j() {
        return index_j;
    }

    public void setIndex_j(int index_j) {
        this.index_j = index_j;
    }

    public int getIndex_i() {
        return index_i;
    }

    public void setIndex_i(int index_i) {
        this.index_i = index_i;
    }

    @Override
    public String toString() {
        return "Index i = " + index_i + "; Index j = " + index_j;
    }
}
