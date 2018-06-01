package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.*;
import log.Logger;
import logic.Robot;
import model.Model;


/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame implements Serializable
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

        addWindow(gameWindow);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(MainApplicationFrame.this, "Закрыть?",
                        "", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_NO_OPTION)
                {
                    try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("model.dat")))
                    {
                        oos.writeObject(RobotsProgram.model);
                        oos.flush();
                        oos.close();
                    }
                    catch(Exception ex){
                        System.out.println(ex.getMessage());
                    }
                    try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("frame.dat"))){
                        oos.writeObject(RobotsProgram.frame);
                        oos.flush();
                        oos.close();
                    }
                    catch (Exception ex){
                        System.out.println(ex.getMessage());
                    }
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

        menuBar.add(pauseButton());
        menuBar.add(addRobotButton());
        menuBar.add(changeActiveRobotButton());
        menuBar.add(restartButton());
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

        closerButton.addActionListener(e -> {
            processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        return closerButton;
    }

    private JButton pauseButton(){
        JButton pauseButton = new JButton("Пауза");
        pauseButton.addActionListener(e -> {
            if(!Model.paused) {
                RobotsProgram.model.pause();
                Logger.debug("Пауза");
                pauseButton.setText("Продолжить");
            } else {
                RobotsProgram.model.unpause();
                pauseButton.setText("Пауза");
            }
        });
        return pauseButton;
    }

    private JButton addRobotButton(){
        JButton addRobotButton = new JButton("Добавить робота");
        addRobotButton.addActionListener(e ->
        {
            RobotsProgram.model.addRobot(new Robot());
        });
        return addRobotButton;
    }

    private JButton changeActiveRobotButton(){
        JButton changeActiveRobotButton = new JButton("Следующий робот");
        changeActiveRobotButton.addActionListener(e -> {
            RobotsProgram.model.changeActiveRobot();
        });
        return changeActiveRobotButton;
    }

    private JButton restartButton(){
        JButton restartButton = new JButton("Рестарт");
        restartButton.addActionListener(e ->{
            RobotsProgram.model.Restart();
        });
        return restartButton;
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
