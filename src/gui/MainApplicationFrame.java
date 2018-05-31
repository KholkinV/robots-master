package gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import log.Logger;
import logic.Robot;
import model.Model;
import sun.misc.JavaAWTAccess;
import sun.security.util.SecurityConstants;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    GameWindow gameWindow;
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(800,  800);
        gameWindow.setLocation(400, 10);
        gameWindow.setJMenuBar(gameWindowBar());
        gameWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Model.setTargetPosition(e.getPoint());
                Logger.debug("Изменена позиция цели");
            }
        });

        addWindow(gameWindow);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(MainApplicationFrame.this, "Закрыть?",
                        "", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_NO_OPTION)
                {
                    MainApplicationFrame.this.setVisible(false);
                    MainApplicationFrame.this.dispose();
                }
            }

        });



        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
    
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(lookAndFeelMenu());
        menuBar.add(testMenu());
        menuBar.add(closerButton());
        return menuBar;
    }

    private JMenuBar gameWindowBar(){
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(drawRectangleButton());
        menuBar.add(pauseButton());
        menuBar.add(addRobotButton());
        menuBar.add(changeActiveRobotButton());
        return menuBar;
    }

    private JMenu lookAndFeelMenu(){

        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        {
            JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }
        return lookAndFeelMenu;
    }

    private JMenu testMenu(){
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> {
                Logger.debug("Новая строка");
            });
            testMenu.add(addLogMessageItem);
        }
        return testMenu;
    }

    private JButton closerButton(){
        JButton closerButton = new JButton("Выход");

        closerButton.addActionListener(e ->
                processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        return closerButton;
    }

    private JButton pauseButton(){
        JButton pauseButton = new JButton("Пауза");
        pauseButton.addActionListener(e -> {
            if(!Model.paused) {
                Model.pause();
                Logger.debug("Пауза");
                pauseButton.setText("Продолжить");
            } else {
                Model.unpause();
                pauseButton.setText("Пауза");
            }
        });
        return pauseButton;
    }

    private JButton drawRectangleButton(){
        JButton drawRectangleButton = new JButton("Нарисовать прямоугольник");
        drawRectangleButton.addActionListener(l ->
        {
            gameWindow.addMouseListener(new MouseAdapter()
            {
                Point firstPoint;
                Point secondPoint;
                public void mousePressed(MouseEvent e)
                {
                    firstPoint = e.getPoint();
                }
                public void mouseReleased(MouseEvent e){
                    secondPoint = e.getPoint();
                    Rectangle rect = new Rectangle(firstPoint.x, firstPoint.y,
                            Math.abs(secondPoint.x - firstPoint.x), Math.abs(secondPoint.y - firstPoint.y));
                    Model.addRect(rect);
                }
            });
        });
        return drawRectangleButton;
    }

    private JButton addRobotButton(){
        JButton addRobotButton = new JButton("Добавить робота");
        addRobotButton.addActionListener(e ->
        {
            Model.addRobot(new Robot());
        });
        return addRobotButton;
    }

    private JButton changeActiveRobotButton(){
        JButton changeActiveRobotButton = new JButton("Следующий робот");
        changeActiveRobotButton.addActionListener(e -> {
            Model.changeActiveRobot();
        });
        return changeActiveRobotButton;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }
}
