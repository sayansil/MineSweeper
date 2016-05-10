package org.minesweeper.core;
import java.io.Serializable;
/**
 * Encapsulates 1 cell of a MineSweeper board
 * <br>
 * UI programmers note:
 * <br>
 * Please check the status flags, especially the {@link #hasBeenRevealed} and {@link #marked} flags,
 * before displaying/updating the game board and cells.
 */
public class Cell implements Serializable, Cloneable {
    public static final int MINE = -1, CLEAR = 0, MAX_NEIGHBOURS = 8;
    private static final int HASH_MAGIC_NUMBER = 31;
    private int state, x, y;
    private boolean hasBeenRevealed, marked;
    public Cell(int x, int y) {
        setX(x);
        setY(y);
        clear();
    }
    public int clear() {
        if (!isClear()) {
            state = CLEAR;
            setHasBeenRevealed(false);
            return 1;
        }
        return 0;
    }
    public boolean isClear() {
        return state == CLEAR;
    }
    public void setHasBeenRevealed(boolean hasBeenRevealed) {
        this.hasBeenRevealed = hasBeenRevealed;
    }
    public Cell(Cell cell) {
        this(cell, false);
    }
    public Cell(Cell cell, boolean copyStatus) {
        setX(cell.getX());
        setY(cell.getY());
        state = cell.state;
        clear();
        if (copyStatus) {
            hasBeenRevealed = cell.hasBeenRevealed();
            marked = cell.isMarked();
        }
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    /**
     * @return true if the cell has been revealed, false if it hasn't
     */
    public boolean hasBeenRevealed() {
        return hasBeenRevealed;
    }
    public boolean isMarked() {
        return marked;
    }
    public void setMarked(boolean marked) {
        if (marked) {
            mark();
        } else {
            clear();
        }
    }
    public int mark() {
        if (!isMarked()) {
            marked = true;
            return 1;
        }
        return 0;
    }
    @Override
    public int hashCode() {
        int result = state;
        result = HASH_MAGIC_NUMBER * result + getX();
        result = HASH_MAGIC_NUMBER * result + getY();
        result = HASH_MAGIC_NUMBER * result + (hasBeenRevealed() ? 1 : 0);
        result = HASH_MAGIC_NUMBER * result + (isMarked() ? 1 : 0);
        return result;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return state == cell.state && getX() == cell.getX() && getY() == cell.getY();
    }
    @Override
    public String toString() {
        return this.getClass().getName() + "{" + "state=" + state + ", x=" + x + ", y=" + y + '}';
    }
    public int getNumberOfNeighboursWithMines() {
        return state < 0 ? MINE : state;
    }
    public int setMine() {
        if (!hasMine()) {
            state = MINE;
            return 1;
        }
        return 0;
    }
    public boolean hasMine() {
        return state == MINE || state < 0;
    }
    public void setNeighbouringMineCount(int neighbouringMineCount) {
        if (neighbouringMineCount < 0) {
            throw new IllegalArgumentException("Number of neighbouring mines can't be -ve : " + neighbouringMineCount);
        } else if (neighbouringMineCount > MAX_NEIGHBOURS) {
            throw new IllegalArgumentException("Number of neighbouring mines can't be > " + MAX_NEIGHBOURS + " : " + neighbouringMineCount);
        } else {
            state = neighbouringMineCount;
            reveal();
        }
    }
    public int reveal() {
        if (!hasBeenRevealed()) {
            setHasBeenRevealed(true);
            return 1;
        }
        return 0;
    }
}