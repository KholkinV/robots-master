package logic;

import gui.RobotsProgram;
import log.Logger;
import model.Model;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingDeque;

import static java.awt.geom.Point2D.distance;

public class Robot implements Serializable{

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;
    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;
    private ArrayDeque<Cell> path = breadthSearch(new Point((int)m_robotPositionX, (int)m_robotPositionY));
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    private volatile double velocity = Model.paused ? 0 : maxVelocity;

    public double getM_robotPositionX() {
        return m_robotPositionX;
    }
    public double getM_robotPositionY() {
        return m_robotPositionY;
    }

    public double getM_robotDirection() {
        return m_robotDirection;
    }

    public static double getMaxVelocity() {
        return maxVelocity;
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, maxVelocity);
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);
        double newX = m_robotPositionX + velocity / angularVelocity *
                (Math.sin(m_robotDirection  + angularVelocity * duration) -
                        Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
                (Math.cos(m_robotDirection  + angularVelocity * duration) -
                        Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }
        m_robotPositionX = newX;
        m_robotPositionY = newY;
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
        m_robotDirection = newDirection;
    }

    private void rotateRobot(Point target){
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                target.x, target.y);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
        {
            angularVelocity = maxAngularVelocity;
        }
        if (angleToTarget < m_robotDirection)
        {
            angularVelocity = -maxAngularVelocity;
        }
        while(Math.abs(angleToTarget - m_robotDirection) > 0.01){
            angleToTarget = angleTo(m_robotPositionX, m_robotPositionY,
                    target.x, target.y);
            moveRobot(0, angularVelocity, 10);
        }
    }

    public void getNewPath(){
        Thread thread = new Thread(() -> path = breadthSearch(new Point((int)m_robotPositionX, (int)m_robotPositionY)));
        thread.start();
    }

    public void onModelUpdateEvent()
    {

        if(!path.isEmpty()){
            Cell nextStep = path.remove();
            rotateRobot(new Point(nextStep.x, nextStep.y));
            moveRobot(velocity, 0, 10);
        }
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }

    private ArrayDeque<Cell> breadthSearch(Point start){
        ArrayDeque<Cell> path = new ArrayDeque<>();
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        HashSet<Cell> visited = new HashSet<>();
        queue.add(new Cell(start.x, start.y));

        while(!queue.isEmpty()){
            Cell p = queue.remove();
            if(visited.contains(p)) continue;
            visited.add(p);

            ArrayList<Cell> neighbours = getNeighbours(p);
            if(neighbours.size() != 0){
                for (Cell neighbour: neighbours){
                    if(neighbour.x == RobotsProgram.model.getTarget().getM_targetPositionX() &&
                            neighbour.y == RobotsProgram.model.getTarget().getM_targetPositionY()){
                        Cell temp = neighbour;
                        while(temp.x != start.x || temp.y != start.y) {
                            path.addFirst(temp);
                            temp = temp.getPrevious();
                        }
                        return path;
                    }
                    if(!visited.contains(neighbour)){
                        queue.add(neighbour);
                    }
                }
            }

        }
        return path;
    }

    private ArrayList<Cell> getNeighbours(Cell location){
        Point[] neighbour = new Point[]{
                new Point(location.x, location.y - 1),
                new Point(location.x + 1, location.y),
                new Point(location.x, location.y + 1),
                new Point(location.x - 1, location.y)
        };
        boolean flag = false;
        ArrayList<Cell> result = new ArrayList<>();
        ArrayList<Rectangle> rect = RobotsProgram.model.getRect();
        for (Point p : neighbour){
            if(p.x >= 0 && p.y >= 0 && p.x < 800 && p.y < 800){
                for (Rectangle e : rect) {
                    if (e.contains(p)) flag = true;
                }
                if(!flag) {
                    Cell cell = new Cell(p.x, p.y);
                    cell.setPrevious(location);
                    result.add(cell);
                    flag = false;
                }
            }
        }
        return result;
    }
}
