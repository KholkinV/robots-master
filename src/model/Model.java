package model;

import log.Logger;
import logic.Robot;

import java.awt.*;
import java.util.ArrayList;

public final class Model {

    private Model(){}

    private static ArrayList<Robot> robots = new ArrayList<>();
    private static Robot activeRobot;
    public static boolean paused;
    private static ArrayList<Rectangle> rect = new ArrayList<>();
    private static volatile int m_targetPositionX = 150;
    private static volatile int m_targetPositionY = 100;

    private static void setM_targetPositionX(int targetPositionX) {
        m_targetPositionX = targetPositionX;
    }
    private static void setM_targetPositionY(int targetPositionY) {
        m_targetPositionY = targetPositionY;
    }
    public static int getM_targetPositionX() {
        return m_targetPositionX;
    }
    public static int getM_targetPositionY() {
        return m_targetPositionY;
    }

    public static void setTargetPosition(Point p)
    {
        Model.setM_targetPositionX(p.x);
        Model.setM_targetPositionY(p.y);
        activeRobot.getNewPath();
    }

    public static ArrayList<Rectangle> getRect() {
        return rect;
    }

    public static void addRect(Rectangle rectangle) {
        rect.add(rectangle);
        Logger.debug("Рисуем прямоугольник");
    }

    public static void init(){
        activeRobot = new Robot();
        robots.add(activeRobot);
    }

    public static ArrayList<Robot> getRobots() {
        return robots;
    }

    public static void addRobot(Robot robot) {
        Model.robots.add(robot);
        Logger.debug("Добавлен робот");
    }

    public static Robot getActiveRobot() {
        return activeRobot;
    }

    public static void changeActiveRobot() {
        int index = robots.indexOf(activeRobot);
        if(index + 1 >= robots.size())
            activeRobot = robots.get(0);
        else
            activeRobot = robots.get(index + 1);
        Logger.debug("Следующий робот активен");
        activeRobot.getNewPath();
    }

    public static void pause(){
        robots.forEach(e -> {
            e.setVelocity(0);
            paused = true;
        });
    }

    public static void unpause(){
        robots.forEach(e ->{
            e.setVelocity(Robot.getMaxVelocity());
            paused = false;
        });
    }

    public static void Restart() {
        rect.clear();
        robots.clear();
        setTargetPosition(new Point(150, 100));
        activeRobot = new Robot();
        robots.add(activeRobot);
    }
}
