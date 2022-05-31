package com.example.sudoku;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BacktrackingAlgorithm {

    public static final int BOARD_SIZE = 9;
    private static final int SUBSECTION_SIZE = 3;
    private static final int BOARD_START_INDEX = 0;

    public static final int NO_VALUE = 0;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 9;

    private static int totalCorrectAnswers = 2;
    private static int countCorrectAnswers = 0;
    private static boolean isSolvable = true;

    private static int[][] boardInit = new int[BOARD_SIZE][BOARD_SIZE];
    private static int[][] boardSolved = new int[BOARD_SIZE][BOARD_SIZE];

    public static void generateBoardData(int numbersToWin) {
        boardRandomFill();
        totalCorrectAnswers = numbersToWin;

        boardSolved = copyInitBoard();
        boolean isSolved = solve(boardSolved);
//        System.out.println(isSolved);
        if (!isSolved) {
            boardInit = new int[BOARD_SIZE][BOARD_SIZE];
            boardSolved = new int[BOARD_SIZE][BOARD_SIZE];
            generateBoardData(totalCorrectAnswers);
        }


        boardInit = copySolvedBoard();

        boardFillByZero(totalCorrectAnswers);
    }

    public static int[][] copyInitBoard() {
        int[][] board = Arrays.stream(boardInit)
                .map(int[]::clone)
                .toArray(int[][]::new);
        return board;
    }

    public static int[][] copySolvedBoard() {
        int[][] board = Arrays.stream(boardSolved)
                .map(int[]::clone)
                .toArray(int[][]::new);
        return board;
    }

    public static boolean checkAnswer(List<Integer> collect) {
        int row = collect.get(0);
        int column = collect.get(1);
        int value = collect.get(2);

        if (isCorrectAnswer(column, row, value)) {
//            System.out.println("That's right!");
            boardInit[column][row] = value;
            countCorrectAnswers++;
            return true;
        } else {
//            System.out.println("Oh, my God...");
        }
        return false;
    }

    private static boolean isCorrectAnswer(int column, int row, int value) {
        return value == boardSolved[column][row];
    }

    public static boolean isWin() {
        return countCorrectAnswers == totalCorrectAnswers;
    }

    public static int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public static int getCountCorrectAnswers() {
        return countCorrectAnswers;
    }

    private static void boardFillByZero(int numbersToWin) {
        int distribution = (int) Math.ceil(numbersToWin / 9.0);
        int rest = numbersToWin;

        for (int i = 0; i <= distribution;) {
            for (int column = 0; column < BOARD_SIZE;) {
                int row = new Random().nextInt(0, 8);

                if (boardInit[row][column] != NO_VALUE) {
                    boardInit[row][column] = NO_VALUE;
                    column++;
                    rest--;
                }
                if (rest == 0) {
                    break;
                }
            }
            if (rest == 0) {
                break;
            }
        }
    }

   private static void boardRandomFill() {
        for (int i = BOARD_START_INDEX; i < BOARD_SIZE; i++) {
            for (int j = BOARD_START_INDEX; j < BOARD_SIZE; j++) {
                if ((new Random().nextInt(0, 100)) > 95) {
                    boardInit[i][j] = new Random().nextInt(1, 9);
                } else {
                    boardInit[i][j] = NO_VALUE;
                }
            }
        }
    }

    private static boolean solve(int[][] board) {
        for (int row = BOARD_START_INDEX; row < BOARD_SIZE; row++) {
            for (int column = BOARD_START_INDEX; column < BOARD_SIZE; column++) {
                if (board[row][column] == NO_VALUE) {
                    for (int k = MIN_VALUE; k <= MAX_VALUE; k++) {
                        board[row][column] = k;
                        if (!isSolvable) {
                            return false;
                        }
                        if (isValid(board, row, column) && solve(board)) {
                            return true;
                        }
                        board[row][column] = NO_VALUE;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValid(int[][] board, int row, int column) {
        return rowConstraint(board, row) &&
                columnConstraint(board, column) &&
                subsectionConstraint(board, row, column);
    }

    private static boolean subsectionConstraint(int[][] board, int row, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        int subsectionRowStart = (row / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionRowEnd = subsectionRowStart + SUBSECTION_SIZE;

        int subsectionColumnStart = (column / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionColumnEnd = subsectionColumnStart + SUBSECTION_SIZE;

        for (int r = subsectionRowStart; r < subsectionRowEnd; r++) {
            for (int c = subsectionColumnStart; c < subsectionColumnEnd; c++) {
                if (!checkConstraint(board, r, constraint, c)) return false;
            }
        }
        return true;
    }

    private static boolean columnConstraint(int[][] board, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(row -> checkConstraint(board, row, constraint, column));
    }

    private static boolean rowConstraint(int[][] board, int row) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(column -> checkConstraint(board, row, constraint, column));
    }

    private static boolean checkConstraint(int[][] board, int row, boolean[] constraint, int column) {
        if (board[row][column] != NO_VALUE) {
            if (!constraint[board[row][column] - 1]) {
                constraint[board[row][column] - 1] = true;
            } else {
                return false;
            }
        }
        return true;
    }
}