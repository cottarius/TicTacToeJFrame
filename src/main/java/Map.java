import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.Scanner;

public class Map extends JPanel {
    private int gameOverType;
    private static final int STATE_DRAW = 0;
    private static final int STATE_WIN_HUMAN = 1;
    private static final int STATE_WIN_AI = 2;
    private static final int DOT_HUMAN = 1;
    private static final int DOT_AI = 2;
    private static final int DOT_EMPTY = 0;
    private static final int DOT_PADDING = 5;
    private static final String MSG_WIN_HUMAN = "Победил Игрок!";
    private static final String MSG_WIN_AI = "Победил Компьютер!";
    private static final String MSG_DRAW = "Ничья!";

    private static final int WIN_COUNT = 3;

    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    private static int[][] field;
    private static int fieldSizeX;
    private static int fieldSizeY;
    private int panelWidth;
    private int panelHeight;
    private int cellHeight;
    private int cellWidth;
    private int wLen;
    private boolean isGameOver;
    private boolean isInitialized;
    Map(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                update(e);
            }
        });
        isInitialized = false;
    }

    private void update(MouseEvent e){
        if(isGameOver || !isInitialized) return;

        int cellX = e.getX()/cellWidth;
        int cellY = e.getY()/cellWidth;

        System.out.printf("x=%d, y=%d\n", cellX, cellY);
        if (!isCellValid(cellX, cellY) || !isCellEmpty(cellX, cellY)) return;
        field[cellY][cellX] = DOT_HUMAN;
        if(gameChecks(DOT_HUMAN, WIN_COUNT, "Human win!", STATE_WIN_HUMAN)) return;

        aiTurn();
        repaint();
        if(gameChecks(DOT_AI, WIN_COUNT, "Computer win!", STATE_WIN_AI)) return;
    }

    void startNewGame(int mode, int fSzX, int fSzY, int wLen){
        fieldSizeY = fSzY;
        fieldSizeX = fSzX;
        System.out.printf("Mode: %d;\nSize: x=%d, y=%d;\nWin Length: %d\n",
                mode, fSzX, fSzY, wLen);
        this.wLen = wLen;
        initialize();
        isGameOver = false;
        isInitialized = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }

    private void render(Graphics g) {
        if (!isInitialized) return;
        panelWidth = getWidth();
        panelHeight = getHeight();
        cellHeight = panelHeight / fieldSizeY;
        cellWidth = panelWidth / fieldSizeX;

        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                System.out.printf("%d \t",field[y][x]);
            }
            System.out.println();
        }

        g.getColor();
        for(int h = 0; h < fieldSizeY; h++){
            int y = h * cellHeight;
            g.drawLine(0, y, panelWidth, y);
        }
        for (int w = 0; w < fieldSizeX; w++){
            int x = w * cellWidth;
            g.drawLine(x, 0, x, panelHeight);
        }
        for (int y = 0; y < fieldSizeY; y++){
            for (int x = 0; x < fieldSizeX; x++){
                if (field[y][x] == DOT_EMPTY) continue;

                if (field[y][x] == DOT_HUMAN){
                    g.setColor(Color.BLUE);
                    g.fillOval(x * cellWidth + DOT_PADDING,
                            y * cellHeight + DOT_PADDING,
                            cellWidth - DOT_PADDING * 2,
                            cellHeight - DOT_PADDING * 2);

                } else if(field[y][x] == DOT_AI){
                    g.setColor(Color.RED);
                    g.fillOval(x * cellWidth + DOT_PADDING,
                            y * cellHeight + DOT_PADDING,
                            cellWidth - DOT_PADDING * 2,
                            cellHeight - DOT_PADDING * 2);
                } else {
                    throw new RuntimeException("Unexpected value " + field[y][x] +
                            " in cell: x=" + x + " y=" + y);
                }
            }
        }
        if (isGameOver) showMessageGameOver(g);
    }
    private void showMessageGameOver(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 200, getWidth(), 70);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Times new roman", Font.BOLD, 48));
        switch (gameOverType) {
            case STATE_DRAW:
                g.drawString(MSG_DRAW, 180, getHeight() / 2);
                break;
            case STATE_WIN_AI:
                g.drawString(MSG_WIN_AI, 20, getHeight() / 2);
                break;
            case STATE_WIN_HUMAN:
                g.drawString(MSG_WIN_HUMAN, 70, getHeight() / 2);
                break;
            default:
                throw new RuntimeException("Unexpected gameOver state: " + gameOverType);
        }
    }

    //region Logic

    /**
     * Инициализация игрового поля
     */
    private void initialize() {
        fieldSizeY = 3;
        fieldSizeX = 3;

        field = new int[fieldSizeY][fieldSizeX];
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                field[y][x] = DOT_EMPTY;
            }
        }
    }

    /**
     * Проверка доступности ячейки игрового поля
     *
     * @param x
     * @param y
     * @return
     */
    private static boolean isCellValid(int x, int y) {
        return x >= 0 && x < fieldSizeX && y >= 0 && y < fieldSizeY;
    }

    /**
     * Проверка, является ли ячейка игрового поля пустой
     *
     * @param x
     * @param y
     * @return
     */
    private static boolean isCellEmpty(int x, int y) {
        return field[y][x] == DOT_EMPTY;
    }

    /**
     //     * Ход игрока (компьютера) без "интеллекта"
     //     */
//    static void aiTurn() {
//        int x;
//        int y;
//
//        do {
//            x = random.nextInt(fieldSizeX);
//            y = random.nextInt(fieldSizeY);
//        }
//        while (!isCellEmpty(x, y));
//
//        field[y][x] = DOT_AI;
//    }

    //region Улучшенный ход компьютера (задача 4***)

    /**
     * Ход компьютера
     */
    private void aiTurn() {

        // Побеждает ли компьютер в текущем ходе (при выигрышной комбинации WIN_COUNT)?
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (field[y][x] == DOT_EMPTY){
                    field[y][x] = DOT_AI;
                    if (checkWin(DOT_AI, WIN_COUNT))
                        return;
                    else
                        field[y][x] = DOT_EMPTY;
                }
            }
        }

        // Побеждает ли игрок на текущий момент при выигрышной комбинации WIN_COUNT - 1?
        boolean f = checkWin(DOT_HUMAN, WIN_COUNT - 1);
        // Теперь, снова пройдем по всем свободным ячейкам игрового поля, если игрок уже побеждает при
        // выигрышной комбинации WIN_COUNT - 1, компьютер попытается закрыть последнюю выигрышную ячейку.
        // Если игрок НЕ побеждает при выигрышной комбинации WIN_COUNT - 1, компьютер будет действовать
        // на опережение, попытается заранее "подпортить" человеку выигрышную комбинацию.
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (field[y][x] == DOT_EMPTY){
                    field[y][x] = DOT_HUMAN;
                    if (checkWin(DOT_HUMAN, WIN_COUNT - (f ? 0 : 1))) {
                        field[y][x] = DOT_AI;
                        return;
                    }
                    else
                        field[y][x] = DOT_EMPTY;
                }
            }
        }

        // Ни человек, ни компьютер не выигрывают, значит, компьютер ставит фишку случайным образом
        int x, y;
        do {
            x = random.nextInt(fieldSizeX);
            y = random.nextInt(fieldSizeY);
        } while (!isCellEmpty(x, y));
        field[y][x] = DOT_AI;
    }

    //endregion

    /**
     * Проверка победы игрока
     * @param dot фишка игрока (человек или компьютер)
     * @param winCount кол-во фишек для победы
     * @return
     */
    private static boolean checkWin(int dot, int winCount) {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (field[y][x] == dot)
                    if (checkXY(y, x, 1, winCount) ||
                            checkXY(y, x, -1, winCount) ||
                            checkDiagonal(y, x, -1, winCount) ||
                            checkDiagonal(y, x, 1, winCount))
                        return true;
            }
        }
        return false;
    }

    //region Универсальная проверка победы игрока (задача 3*)

    /**
     * Проверка выигрыша игрока (человек или компьютер) горизонтали + вправо/вертикали + вниз
     * @param x начальная координата фишки
     * @param y начальная координата фишки
     * @param dir направление проверки (-1 => горизонтали + вправо/ 1 => вертикали + вниз)
     * @param win выигрышная комбинация
     * @return результат проверки
     */
    static boolean checkXY(int x, int y, int dir, int win) {
        int c = field[x][y]; // получим текущую фишку (игрок или компьютер)
        // Пройдем по всем ячейкам от начальной координаты (например 2,3) по горизонтали вправо и по вертикали вниз
        // (в зависимости от значения параметра dir)
        /*  +-1-2-3-4-5-
            1|.|.|.|.|.|
            2|.|.|.|.|.|
            3|.|X|X|X|X|
            4|.|X|.|.|.|
            5|.|X|.|.|.|
            ------------
        */
        for (int i = 1; i < win; i++)
            if (dir > 0 && (!isCellValid(x + i, y) || c != field[x + i][y])) return false;
            else if (dir < 0 && (!isCellValid(x, y + i) || c != field[x][y + i])) return false;
        return true;
    }

    /**
     * Проверка выигрыша игрока (человек или компьютер) по диагонали вверх + вправо/вниз + вправо
     * @param x начальная координата фишки
     * @param y начальная координата фишки
     * @param dir направление проверки (-1 => вверх + вправо/ 1 => вниз + вправо)
     * @param win кол-во фишек для победы
     * @return результат проверки
     */
    static boolean checkDiagonal(int x, int y, int dir, int win) {
        int c = field[x][y]; // получим текущую фишку (игрок или компьютер)
        // Пройдем по всем ячейкам от начальной координаты (например 3,3) по диагонали вверх и по диагонали вниз
        // (в зависимости от значения параметра dir)
        /*  +-1-2-3-4-5-
            1|.|.|.|.|X|
            2|.|.|.|X|.|
            3|.|.|X|.|.|
            4|.|.|.|X|.|
            5|.|.|.|.|X|
            ------------
        */
        for (int i = 1; i < win; i++)
            if (!isCellValid(x + i, y + i*dir) || c != field[x + i][y + i*dir]) return false;
        return true;
    }

    /**
     * Проверка на ничью
     *
     * @return
     */
    private static boolean checkDraw() {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (isCellEmpty(x, y))
                    return false;
            }
        }
        return true;
    }

    /**
     * Метод проверки состояния игры
     * @param dot фишка игрока (человек/компьютер)
     * @param win выигрышная комбинация
     * @param s победное сообщение
     * @return результат проверки
     */
    private boolean gameChecks(int dot, int win, String s, int gameOverType) {
        if (checkWin(dot, win)) {
            System.out.println(s);
            this.gameOverType = gameOverType;
            isGameOver = true;
            repaint();
            return true;
        }
        if (checkDraw()) {
            System.out.println("draw!");
            this.gameOverType = STATE_DRAW;
            isGameOver = true;
            repaint();
            return true;
        }
        return false;
    }

    //endregion
    //endregion

}
