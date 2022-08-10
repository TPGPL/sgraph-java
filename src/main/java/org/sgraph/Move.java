package org.sgraph;

/**
 * Klasa reprezentująca przejścia między wierzchołkami w grafie.
 */
public class Move {
    /**
     * Typ wyliczeniowy reprezentujący możliwe przejścia między wierzchołkami w grafie.
     */
    public enum MoveDirection {
        /**
         * Ruch w górę.
         */
        UP,
        /**
         * Ruch w lewo.
         */
        LEFT,
        /**
         * Ruch w prawo.
         */
        RIGHT,
        /**
         * Ruch w dół.
         */
        DOWN,
        /**
         * Brak ruchu.
         */
        NO_MOVE
    }

    /**
     * Zwraca kierunek przejścia z jednego wierzchołka do drugiego.
     *
     * @param currPosition indeks wierzchołka przed przejściem
     * @param newPosition  indeks wierzchołka po przejściu
     * @param columnCount  liczba kolumn w siatce
     * @param rowCount     liczba wierszy w siatce
     * @return kierunek przejścia w postaci elementu typu wyliczającego Move
     */
    public static MoveDirection getDirection(int currPosition, int newPosition, int columnCount, int rowCount) {
        if (currPosition - columnCount > -1 && currPosition - columnCount == newPosition)
            return MoveDirection.UP;
        else if (currPosition - 1 == newPosition && currPosition / columnCount == newPosition / columnCount)
            return MoveDirection.LEFT;
        else if (currPosition + 1 == newPosition && currPosition / columnCount == newPosition / columnCount)
            return MoveDirection.RIGHT;
        else if (currPosition + columnCount < rowCount * columnCount && currPosition + columnCount == newPosition)
            return MoveDirection.DOWN;
        else
            return MoveDirection.NO_MOVE;
    }
}
