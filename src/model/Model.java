package model;

import log.Logger;
import logic.Robot;
import logic.Target;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public final class Model implements Serializable{

    private ArrayList<Robot> robots = new ArrayList<>();
    private Robot activeRobot;
    public static boolean paused;
    private ArrayList<Rectangle> rect = new ArrayList<>();
    private Target target = new Target();

    public Target getTarget() {
        return target;
    }

    public ArrayList<Rectangle> getRect() {
        return rect;
    }

    public void addRect(Rectangle rectangle) {
        rect.add(rectangle);
    }

    public void init(){
        activeRobot = new Robot();
        robots.add(activeRobot);
    }

    public ArrayList<Robot> getRobots() {
        return robots;
    }

    public void addRobot(Robot robot) {
        robots.add(robot);
        Logger.debug("Добавлен робот");
    }

    public Robot getActiveRobot() {
        return activeRobot;
    }

    public void changeActiveRobot() {
        int index = robots.indexOf(activeRobot);
        if(index + 1 >= robots.size())
            activeRobot = robots.get(0);
        else
            activeRobot = robots.get(index + 1);
        Logger.debug("Следующий робот активен");
        activeRobot.getNewPath();
    }

    public void pause(){
        robots.forEach(e -> {
            e.setVelocity(0);
            paused = true;
        });
    }

    public void unpause(){
        robots.forEach(e ->{
            e.setVelocity(Robot.getMaxVelocity());
            paused = false;
        });
    }

    public void Restart() {
        rect.clear();
        robots.clear();
        target.setTargetPosition(new Point(150, 100));
        activeRobot = new Robot();
        robots.add(activeRobot);
    }
}
